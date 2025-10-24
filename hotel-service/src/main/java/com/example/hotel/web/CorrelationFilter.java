package com.example.hotel.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(CorrelationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String cid = request.getHeader("X-Correlation-Id");
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        response.setHeader("X-Correlation-Id", cid);
        request.setAttribute("correlationId", cid);
        log.debug("[{}] {} {}", cid, request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}


