package com.fitnessops.security.handler;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.exception.ErrorResponseFactory;
import com.fitnessops.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Trả 401 dạng JSON khi chức năng cần đăng nhập; nêu rõ phiên hết hạn hay không hợp lệ nếu biết. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseFactory errorResponseFactory;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object failure = request.getAttribute(JwtAuthenticationFilter.AUTH_FAILURE_ATTRIBUTE);
        if (failure instanceof JwtAuthenticationFilter.AuthenticationFailure reason) {
            errorResponseFactory.write(request, response, reason.code(), reason.message());
            return;
        }
        errorResponseFactory.write(request, response, ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
    }
}
