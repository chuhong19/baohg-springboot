package com.example.baohg.services;

import com.example.baohg.exception.LogicException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            final String authHeader = request.getHeader("Authorization");
            System.out.println('1');
            final String jwt;
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                throw new LogicException("LogoutService: Please login first");
            }
            SecurityContextHolder.clearContext();
        } catch (LogicException ex) {
            throw new LogicException(ex.getMessage());
        }
    }
}
