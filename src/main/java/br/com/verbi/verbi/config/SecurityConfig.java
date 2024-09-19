package br.com.verbi.verbi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.security.JwtFilter;
import br.com.verbi.verbi.security.TokenBlacklistFilter;
import br.com.verbi.verbi.service.UserService;

@Configuration
public class SecurityConfig {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistFilter tokenBlacklistFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/mural/write").authenticated()
                .requestMatchers("/api/mural/update/**").authenticated()
                .requestMatchers("/api/mural/delete/**").authenticated()
                .requestMatchers("/api/mural/user/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2Login -> oauth2Login
                .defaultSuccessUrl("/api/auth/success", true)
                .failureUrl("/login?error=true")
                .permitAll())
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .permitAll())
            .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll())
            .addFilterBefore(new JwtFilter(jwtGenerator, userService), 
                UsernamePasswordAuthenticationFilter.class); // Registra o filtro JWT

        return http.build();
    }
}

