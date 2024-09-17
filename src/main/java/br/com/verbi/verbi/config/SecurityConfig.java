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
                .csrf(csrf -> csrf.disable()) // Desativa CSRF para simplificação, considere reabilitar em produção
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/users/register").permitAll() // Permite acesso ao registro sem
                                                                            // autenticação
                        .requestMatchers("/api/auth/login").permitAll() // Permite acesso à página de login sem
                                                                        // autenticação
                        .requestMatchers("/api/mural/write").permitAll() // Permite acesso ao mural sem autenticação
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated() // Requer autenticação para outras requisições
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .defaultSuccessUrl("/api/auth/success", true) // URL para redirecionamento após login bem-sucedido
                        .failureUrl("/login?error=true")
                        .permitAll())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // Página de login personalizada para autenticação padrão
                        .permitAll() // Permite acesso à página de login sem autenticação
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL de logout
                        .logoutSuccessUrl("/login?logout=true") // Redireciona após logout
                        .permitAll());

        return http.build();
    }
}
