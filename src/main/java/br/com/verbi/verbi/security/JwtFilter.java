package br.com.verbi.verbi.security;

import org.hibernate.annotations.DialectOverride.OverridesAnnotation;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.service.UserService;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Optional;

public class JwtFilter extends GenericFilterBean {

    private final JWTGenerator jwtGenerator;
    private final UserService userService;

    public JwtFilter(JWTGenerator jwtGenerator, UserService userService) {
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String header = httpRequest.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtGenerator.validateToken(token)) {
                String email = jwtGenerator.getUsername(token);
                
                // Buscar o usuário pelo email
                Optional<User> optionalUser = userService.findUserByEmail(email);

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    
                    // Cria o AuthenticationToken
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                    
                    // Define o contexto de segurança do Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        chain.doFilter(request, response);
    }
}


