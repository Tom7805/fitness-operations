package com.fitnessops.modules.auth.service.impl;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.config.properties.AuthProperties;
import com.fitnessops.modules.auth.dto.response.SessionResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.entity.UserSession;
import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.auth.enums.SessionEndReason;
import com.fitnessops.modules.auth.mapper.SessionResponseMapper;
import com.fitnessops.modules.auth.repository.CounterDeviceRepository;
import com.fitnessops.modules.auth.repository.UserSessionRepository;
import com.fitnessops.modules.auth.service.AuthAuditEvent;
import com.fitnessops.modules.auth.service.AuthAuditService;
import com.fitnessops.modules.auth.service.CounterDeviceService;
import com.fitnessops.modules.auth.service.SessionAuthentication;
import com.fitnessops.modules.auth.service.SessionService;
import com.fitnessops.modules.user.entity.Role;
import com.fitnessops.modules.user.entity.User;
import com.fitnessops.modules.user.service.UserService;
import com.fitnessops.security.jwt.JwtClaims;
import com.fitnessops.security.userdetails.CustomUserDetails;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;
    private final CounterDeviceRepository deviceRepository;
    private final CounterDeviceService counterDeviceService;
    private final UserService userService;
    private final AuthAuditService auditService;
    private final SessionResponseMapper sessionResponseMapper;
    private final AuthProperties authProperties;
    private final Clock clock;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UserSession open(User user, ClientType clientType, CounterDevice device, Long activeBranchId,
                            ClientRequestInfo client, Instant now) {
        UserSession session = UserSession.open(user.getId(), clientType, device == null ? null : device.getId(),
                activeBranchId, client.ipAddress(), client.userAgent(), now, authProperties.absoluteTimeout());
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public SessionAuthentication authenticate(JwtClaims claims, ClientRequestInfo client) {
        Instant now = Instant.now(clock);
        UserSession session = sessionRepository.findById(claims.sessionId()).orElse(null);
        if (session == null || !session.getUserId().equals(claims.userId()) || session.isEnded()) {
            return invalidSession();
        }
        if (session.isAbsoluteExpired(now)) {
            session.end(SessionEndReason.ABSOLUTE_TIMEOUT, now);
            return SessionAuthentication.rejected(ErrorCode.SESSION_EXPIRED,
                    "Phiên làm việc đã hết thời hạn " + authProperties.absoluteTimeout().toHours()
                            + " giờ. Vui lòng đăng nhập lại.");
        }
        if (session.isIdleExpired(now, authProperties.idleTimeout())) {
            session.end(SessionEndReason.IDLE_TIMEOUT, now);
            String detail = "Phiên hết hạn do không thao tác trong " + idleMinutes() + " phút";
            auditService.record(AuthAuditEvent.builder()
                    .type(AuthEventType.SESSION_EXPIRED)
                    .reason(AuthEventReason.IDLE_TIMEOUT)
                    .userId(session.getUserId())
                    .sessionId(session.getId())
                    .clientType(session.getClientType())
                    .deviceId(session.getDeviceId())
                    .branchId(session.getBranchId())
                    .client(client)
                    .detail(detail)
                    .build());
            return SessionAuthentication.rejected(ErrorCode.SESSION_EXPIRED,
                    "Phiên làm việc đã hết hạn do không thao tác trong " + idleMinutes()
                            + " phút. Vui lòng đăng nhập lại.");
        }
        if (session.getDeviceId() != null && !isSameCounterDevice(session, client)) {
            // Mã truy cập của phiên máy quầy bị mang sang máy khác hoặc máy đã bị thu hồi.
            return invalidSession();
        }
        Optional<User> user = userService.findWithAccessById(session.getUserId());
        if (user.isEmpty() || !user.get().isActive()) {
            session.end(SessionEndReason.REVOKED, now);
            return invalidSession();
        }
        session.touch(now);
        return SessionAuthentication.authenticated(toPrincipal(user.get(), session));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse describe(CustomUserDetails principal) {
        UserSession session = sessionRepository.findById(principal.sessionId()).orElseThrow();
        User user = userService.findWithAccessById(principal.userId()).orElseThrow();
        CounterDevice device = session.getDeviceId() == null
                ? null
                : deviceRepository.findById(session.getDeviceId()).orElse(null);
        return sessionResponseMapper.toResponse(session, user, device);
    }

    @Override
    @Transactional
    public void logout(CustomUserDetails principal, ClientRequestInfo client) {
        Instant now = Instant.now(clock);
        sessionRepository.findById(principal.sessionId()).ifPresent(session -> {
            session.end(SessionEndReason.LOGOUT, now);
            auditService.record(AuthAuditEvent.builder()
                    .type(AuthEventType.LOGOUT)
                    .userId(principal.userId())
                    .username(principal.username())
                    .sessionId(session.getId())
                    .clientType(session.getClientType())
                    .deviceId(session.getDeviceId())
                    .branchId(session.getBranchId())
                    .client(client)
                    .detail("Đăng xuất khỏi " + session.getClientType().label())
                    .build());
        });
    }

    private boolean isSameCounterDevice(UserSession session, ClientRequestInfo client) {
        return counterDeviceService.findActiveByToken(client.deviceToken())
                .map(device -> device.getId().equals(session.getDeviceId()))
                .orElse(false);
    }

    private long idleMinutes() {
        return authProperties.idleTimeout().toMinutes();
    }

    private static SessionAuthentication invalidSession() {
        return SessionAuthentication.rejected(ErrorCode.SESSION_INVALID, ErrorCode.SESSION_INVALID.defaultMessage());
    }

    private static CustomUserDetails toPrincipal(User user, UserSession session) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()),
                user.isAllBranches(),
                user.getBranchIds(),
                session.getId(),
                session.getClientType(),
                session.getDeviceId(),
                session.getBranchId());
    }
}
