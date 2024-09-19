package br.com.verbi.verbi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.verbi.verbi.repository.TokenBlacklistRepository;
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

@Component
public class TokenBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    public TokenBlacklistFilter(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenBlacklistRepository.existsByToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token está na blacklist");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
