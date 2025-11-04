package com.spring.monew.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String CLIENT_IP_MDC_KEY = "clientIp";
    private static final String REQUEST_ID_PREFIX = "req_";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = generateRequestId();
        String clientIp = extractClientIp(request);
        
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        MDC.put(CLIENT_IP_MDC_KEY, clientIp);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(CLIENT_IP_MDC_KEY);
        }
    }

    private String generateRequestId() {
        return REQUEST_ID_PREFIX + UUID.randomUUID().toString();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
