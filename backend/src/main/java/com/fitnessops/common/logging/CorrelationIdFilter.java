package com.fitnessops.common.logging;

import com.fitnessops.common.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gắn mã theo dõi cho mỗi yêu cầu: dùng lại {@code X-Request-Id} hợp lệ từ phía gọi hoặc sinh mới,
 * đưa vào MDC để mọi dòng log và phản hồi lỗi cùng mang mã này.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(SecurityConstants.REQUEST_ID_HEADER);
        String traceId = incoming != null && SAFE_REQUEST_ID.matcher(incoming).matches() ? incoming : newTraceId();
        MDC.put(SecurityConstants.TRACE_ID_MDC_KEY, traceId);
        response.setHeader(SecurityConstants.REQUEST_ID_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(SecurityConstants.TRACE_ID_MDC_KEY);
        }
    }

    private static String newTraceId() {
        byte[] bytes = new byte[8];
        ThreadLocalRandom.current().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
