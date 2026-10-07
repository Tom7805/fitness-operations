package com.fitnessops.modules.auth.service.impl;

import com.fitnessops.common.constant.AppConstants;
import com.fitnessops.common.constant.SecurityConstants;
import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.exception.BusinessException;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.config.properties.AuthProperties;
import com.fitnessops.modules.auth.dto.request.LoginRequest;
import com.fitnessops.modules.auth.dto.response.AuthResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.entity.UserSession;
import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.auth.exception.AccountLockedException;
import com.fitnessops.modules.auth.exception.InvalidCredentialsException;
import com.fitnessops.modules.auth.mapper.SessionResponseMapper;
import com.fitnessops.modules.auth.service.AuthAuditEvent;
import com.fitnessops.modules.auth.service.AuthAuditService;
import com.fitnessops.modules.auth.service.AuthService;
import com.fitnessops.modules.auth.service.CounterDeviceService;
import com.fitnessops.modules.auth.service.SessionService;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.modules.branch.service.BranchService;
import com.fitnessops.modules.user.entity.User;
import com.fitnessops.modules.user.service.UserService;
import com.fitnessops.security.jwt.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Đăng nhập theo luồng 5.1 của tài liệu phân tích NCL-01-CN-001.
 *
 * <p>Toàn bộ một lần xử lý chạy trong <b>một</b> giao dịch đã khóa dòng tài khoản: bộ đếm sai, thời điểm hết khóa,
 * phiên và nhật ký được ghi cùng nhau. Giao dịch luôn được xác nhận (kể cả khi đăng nhập thất bại) rồi lỗi mới
 * được ném ra ngoài, nên một lần nhập sai không bị hoàn tác.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** BCrypt chỉ dùng 72 byte đầu; mật khẩu dài hơn không thể khớp với bản băm đã lưu một cách an toàn. */
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserService userService;
    private final SessionService sessionService;
    private final CounterDeviceService counterDeviceService;
    private final BranchService branchService;
    private final AuthAuditService auditService;
    private final SessionResponseMapper sessionResponseMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;
    /** Bản băm giả để tên đăng nhập không tồn tại tốn cùng thời gian xử lý như sai mật khẩu. */
    private final String dummyPasswordHash;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public AuthServiceImpl(UserService userService, SessionService sessionService,
                           CounterDeviceService counterDeviceService, BranchService branchService,
                           AuthAuditService auditService, SessionResponseMapper sessionResponseMapper,
                           JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
                           AuthProperties authProperties, Clock clock,
                           PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.counterDeviceService = counterDeviceService;
        this.branchService = branchService;
        this.auditService = auditService;
        this.sessionResponseMapper = sessionResponseMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-equalization");
    }

    @Override
    public AuthResponse login(LoginRequest request, ClientRequestInfo client) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        LoginOutcome outcome = transactionTemplate.execute(status -> attempt(username, request, client));
        if (outcome == null) {
            throw new IllegalStateException("Login transaction returned no outcome");
        }
        if (outcome.error() != null) {
            throw outcome.error();
        }
        return outcome.response();
    }

    private LoginOutcome attempt(String username, LoginRequest request, ClientRequestInfo client) {
        Instant now = Instant.now(clock);
        Optional<CounterDevice> device = counterDeviceService.findActiveByToken(client.deviceToken());
        ClientType clientType = resolveClientType(request.clientType(), device.isPresent());
        AttemptContext ctx = new AttemptContext(username, clientType, device.orElse(null), client);

        Optional<User> found = userService.findByUsernameForUpdate(username);
        if (found.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            audit(ctx, null, AuthEventType.LOGIN_FAILED, AuthEventReason.UNKNOWN_USERNAME,
                    "Đăng nhập thất bại: tên đăng nhập không tồn tại");
            return LoginOutcome.failed(new InvalidCredentialsException());
        }
        User user = found.get();

        if (user.isTemporarilyLocked(now)) {
            audit(ctx, user, AuthEventType.LOGIN_REJECTED, AuthEventReason.ACCOUNT_LOCKED,
                    "Từ chối đăng nhập: tài khoản đang tạm khóa đến " + display(user.getLockedUntil()));
            return LoginOutcome.failed(locked(user, now));
        }
        user.clearExpiredLock(now);

        if (!passwordMatches(request.password(), user.getPasswordHash())) {
            return handleWrongPassword(ctx, user, now);
        }
        user.resetFailedLogins(now);

        if (!user.isActive()) {
            audit(ctx, user, AuthEventType.LOGIN_REJECTED, AuthEventReason.ACCOUNT_DISABLED,
                    "Từ chối đăng nhập: tài khoản đã bị khóa");
            return LoginOutcome.failed(new BusinessException(ErrorCode.ACCOUNT_DISABLED));
        }
        if (ctx.device() != null && !user.canWorkAtBranch(ctx.device().getBranchId())) {
            String branchName = branchName(ctx.device().getBranchId());
            audit(ctx, user, AuthEventType.LOGIN_REJECTED, AuthEventReason.DEVICE_BRANCH_NOT_ASSIGNED,
                    "Từ chối đăng nhập: tài khoản không được giao " + branchName + " của máy quầy \""
                            + ctx.device().getName() + "\"");
            return LoginOutcome.failed(new BusinessException(ErrorCode.DEVICE_BRANCH_NOT_ASSIGNED,
                    ErrorCode.DEVICE_BRANCH_NOT_ASSIGNED.defaultMessage(), Map.of("branchName", branchName)));
        }
        if (ctx.device() == null && user.requiresCounterDevice()) {
            audit(ctx, user, AuthEventType.LOGIN_REJECTED, AuthEventReason.DEVICE_NOT_REGISTERED,
                    "Từ chối đăng nhập: vai trò chỉ được đăng nhập trên máy quầy đã đăng ký, máy đang dùng "
                            + "chưa được đăng ký");
            return LoginOutcome.failed(new BusinessException(ErrorCode.DEVICE_NOT_REGISTERED));
        }

        return LoginOutcome.succeeded(openSession(ctx, user, now));
    }

    private LoginOutcome handleWrongPassword(AttemptContext ctx, User user, Instant now) {
        int max = authProperties.maxFailedAttempts();
        boolean lockedNow = user.registerFailedLogin(max, authProperties.lockDuration(), now);
        audit(ctx, user, AuthEventType.LOGIN_FAILED, AuthEventReason.INVALID_PASSWORD,
                "Đăng nhập thất bại: sai mật khẩu (lần " + user.getFailedLoginAttempts() + "/" + max
                        + " liên tiếp)");
        if (!lockedNow) {
            return LoginOutcome.failed(new InvalidCredentialsException());
        }
        audit(ctx, user, AuthEventType.ACCOUNT_TEMPORARILY_LOCKED, AuthEventReason.TOO_MANY_FAILED_ATTEMPTS,
                "Tạm khóa đăng nhập " + authProperties.lockDuration().toMinutes() + " phút đến "
                        + display(user.getLockedUntil()) + " sau " + max + " lần nhập sai mật khẩu liên tiếp");
        log.warn("Account {} temporarily locked until {} after {} consecutive failed logins",
                user.getId(), user.getLockedUntil(), max);
        return LoginOutcome.failed(locked(user, now));
    }

    private AuthResponse openSession(AttemptContext ctx, User user, Instant now) {
        Long activeBranchId = resolveActiveBranch(user, ctx.device());
        UserSession session = sessionService.open(user, ctx.clientType(), ctx.device(), activeBranchId,
                ctx.client(), now);
        user.recordSuccessfulLogin(now);
        if (ctx.device() != null) {
            ctx.device().markSeen(now);
        }

        String where = ctx.device() == null
                ? ctx.clientType().label()
                : "máy quầy \"" + ctx.device().getName() + "\" (" + branchName(ctx.device().getBranchId()) + ")";
        auditService.record(AuthAuditEvent.builder()
                .type(AuthEventType.LOGIN_SUCCEEDED)
                .userId(user.getId())
                .username(ctx.username())
                .sessionId(session.getId())
                .clientType(ctx.clientType())
                .deviceId(ctx.device() == null ? null : ctx.device().getId())
                .branchId(activeBranchId)
                .client(ctx.client())
                .detail("Đăng nhập thành công trên " + where)
                .build());

        String token = jwtTokenProvider.issue(session.getId(), user.getId(), now, session.getExpiresAt());
        return new AuthResponse(token, SecurityConstants.TOKEN_TYPE, session.getExpiresAt(),
                sessionResponseMapper.toResponse(session, user, ctx.device()));
    }

    /**
     * Máy quầy: câu lạc bộ của máy. Ngoài ra: câu lạc bộ duy nhất được giao, nếu có; nhiều câu lạc bộ hoặc toàn
     * chuỗi thì để trống.
     */
    private static Long resolveActiveBranch(User user, CounterDevice device) {
        if (device != null) {
            return device.getBranchId();
        }
        if (!user.isAllBranches() && user.getBranchIds().size() == 1) {
            return user.getBranchIds().iterator().next();
        }
        return null;
    }

    private static ClientType resolveClientType(ClientType requested, boolean hasCounterDevice) {
        if (hasCounterDevice) {
            return ClientType.COUNTER;
        }
        return requested == ClientType.MOBILE ? ClientType.MOBILE : ClientType.OFFICE;
    }

    private boolean passwordMatches(String rawPassword, String passwordHash) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            passwordEncoder.matches("x", dummyPasswordHash);
            return false;
        }
        return passwordEncoder.matches(rawPassword, passwordHash);
    }

    private AccountLockedException locked(User user, Instant now) {
        return new AccountLockedException(user.getLockedUntil(), now, authProperties.maxFailedAttempts());
    }

    private String branchName(Long branchId) {
        return branchService.findById(branchId).map(BranchResponse::name).orElse("câu lạc bộ #" + branchId);
    }

    private static String display(Instant instant) {
        return AppConstants.DISPLAY_DATE_TIME.format(instant);
    }

    private void audit(AttemptContext ctx, User user, AuthEventType type, AuthEventReason reason, String detail) {
        auditService.record(AuthAuditEvent.builder()
                .type(type)
                .reason(reason)
                .userId(user == null ? null : user.getId())
                .username(ctx.username())
                .clientType(ctx.clientType())
                .deviceId(ctx.device() == null ? null : ctx.device().getId())
                .branchId(ctx.device() == null ? null : ctx.device().getBranchId())
                .client(ctx.client())
                .detail(detail)
                .build());
    }

    private record AttemptContext(String username, ClientType clientType, CounterDevice device,
                                  ClientRequestInfo client) {
    }

    private record LoginOutcome(AuthResponse response, BusinessException error) {

        static LoginOutcome succeeded(AuthResponse response) {
            return new LoginOutcome(response, null);
        }

        static LoginOutcome failed(BusinessException error) {
            return new LoginOutcome(null, error);
        }
    }
}
