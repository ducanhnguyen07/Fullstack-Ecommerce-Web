package com.unejsi.springbootecommerce.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        
        try {
            // Continue with the request
            filterChain.doFilter(request, response);
        } finally {
            // Log after response is ready
            long duration = System.currentTimeMillis() - startTime;
            
            String timestamp = LocalDateTime.now().format(formatter);
            String method = request.getMethod();
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            int responseCode = response.getStatus();
            String remoteAddr = getClientIpAddress(request);
            
            // Full path including query string
            String fullPath = path;
            if (queryString != null && !queryString.isEmpty()) {
                fullPath = path + "?" + queryString;
            }
            
            // Log format that Fluentd can parse
            logger.info("{} INFO --- [{}] REQUEST_LOG : {} {} {} duration={}ms client_ip={}", 
                       timestamp, 
                       Thread.currentThread().getName(),
                       method, 
                       fullPath, 
                       responseCode, 
                       duration,
                       remoteAddr);
                       
            // Additional detailed log for debugging
            if (logger.isDebugEnabled()) {
                logger.debug("HTTP Request Details - Method: {}, Path: {}, Response: {}, Duration: {}ms, User-Agent: {}", 
                           method, fullPath, responseCode, duration, request.getHeader("User-Agent"));
            }
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get first IP from X-Forwarded-For header
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip logging for actuator endpoints (metrics, health checks)
        return path.startsWith("/actuator/");
    }
}