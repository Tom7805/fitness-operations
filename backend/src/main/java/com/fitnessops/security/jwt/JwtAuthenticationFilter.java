package com.fitnessops.security.jwt;

import com.fitnessops.common.constant.SecurityConstants;
import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.service.SessionAuthentication;
import com.fitnessops.modules.auth.service.SessionService;
import com.fitnessops.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Xác thực yêu cầu bằng mã truy cập: kiểm chữ ký, rồi kiểm phiên ở máy chủ (R5). Phiên không hợp lệ thì yêu cầu
 * đi tiếp như chưa đăng nhập, kèm lý do để {@link com.fitnessops.security.handler.JwtAuthenticationEntryPoint}
 * trả đúng mã lỗi nếu chức năng cần đăng nhập. Nhờ vậy một mã cũ gửi nhầm tới API công khai không làm hỏng yêu cầu.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Thuộc tính yêu cầu chứa {@link AuthenticationFailure} khi mã truy cập bị từ chối. */
    public static final String AUTH_FAILURE_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".FAILURE";

    private final JwtTokenProvider tokenProvider;
    private final SessionService sessionService;
    private final SecurityContextHolderStrategy contextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final WebAuthenticationDetailsSource detailsSource = new WebAuthenticationDetailsSource();

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, SessionService sessionService) {
        this.tokenProvider = tokenProvider;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        JwtTokenProvider.TokenParseResult parsed = tokenProvider.parse(token);
        switch (parsed.status()) {
            case EXPIRED -> reject(request, ErrorCode.SESSION_EXPIRED,
                    "Phiên làm việc đã hết thời hạn. Vui lòng đăng nhập lại.");
            case INVALID -> reject(request, ErrorCode.SESSION_INVALID, ErrorCode.SESSION_INVALID.defaultMessage());
            case VALID -> authenticate(request, parsed.claims());
        }
        chain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, JwtClaims claims) {
        SessionAuthentication result = sessionService.authenticate(claims, ClientRequestInfo.from(request));
        if (!result.isAuthenticated()) {
            reject(request, result.errorCode(), result.message());
            return;
        }
        CustomUserDetails principal = result.principal();
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        authentication.setDetails(detailsSource.buildDetails(request));
        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
    }

    private static void reject(HttpServletRequest request, ErrorCode code, String message) {
        request.setAttribute(AUTH_FAILURE_ATTRIBUTE, new AuthenticationFailure(code, message));
    }

    /** Lý do mã truy cập bị từ chối. */
    public record AuthenticationFailure(ErrorCode code, String message) {
    }
}
