package br.com.verbi.verbi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desativa CSRF
            .authorizeHttpRequests(authorizeRequests -> 
                authorizeRequests
                    .requestMatchers("/api/users/register").permitAll() // Permite acesso ao registro sem autenticação
                    .requestMatchers("/api/auth/login").permitAll() // Permite acesso à página de login sem autenticação
                    .requestMatchers("/api/mural/write").permitAll()
                    .anyRequest().authenticated() // Requer autenticação para outras requisições
            )
            .formLogin(formLogin -> 
                formLogin
                    .loginPage("/login") // Página de login personalizada
                    .permitAll() // Permite acesso à página de login sem autenticação
            );

        return http.build();
    }
}