package com.quickeats.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long TIME_WINDOW_MS = 60_000L; // 1 minute

    private final Map<String, RequestTracker> requestMap = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Apply rate limiting ONLY to POST login/register endpoints
        if (!"POST".equalsIgnoreCase(method)) {
            return true;
        }

        return !path.startsWith("/api/auth/login") &&
               !path.startsWith("/api/auth/register") &&
               !path.startsWith("/api/users/login") &&
               !path.startsWith("/api/users/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIP(request);
        long now = System.currentTimeMillis();

        RequestTracker tracker = requestMap.compute(clientIp, (ip, existing) -> {
            if (existing == null || (now - existing.firstRequestTime) > TIME_WINDOW_MS) {
                return new RequestTracker(now, 1);
            } else {
                existing.count++;
                return existing;
            }
        });

        if (tracker.count > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Too many login/registration attempts. Please wait a minute and try again.\"}");
            return;
        }

        // Cleanup stale records periodically
        if (requestMap.size() > 1000) {
            requestMap.entrySet().removeIf(entry -> (now - entry.getValue().firstRequestTime) > TIME_WINDOW_MS);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestTracker {
        final long firstRequestTime;
        int count;

        RequestTracker(long firstRequestTime, int count) {
            this.firstRequestTime = firstRequestTime;
            this.count = count;
        }
    }
}
