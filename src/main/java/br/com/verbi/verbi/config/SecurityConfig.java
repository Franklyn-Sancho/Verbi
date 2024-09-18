package br.com.verbi.verbi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.security.JwtFilter;
import br.com.verbi.verbi.service.UserService;

@Configuration
public class SecurityConfig {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desativa CSRF para simplificação, considere reabilitar em produção
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/users/register").permitAll() // Permite acesso ao registro sem autenticação
                        .requestMatchers("/api/auth/login").permitAll() // Permite acesso à página de login sem autenticação
                        .requestMatchers("/api/mural/write").authenticated()
                        .requestMatchers("/api/mural/update/**").authenticated()
                        .requestMatchers("/api/mural/delete/**").authenticated()
                        .requestMatchers("/api/mural/user/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll() // Permite acesso às rotas OAuth2 sem autenticação adicional
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
                        .permitAll())
                .addFilterBefore(new JwtFilter(jwtGenerator, userService), UsernamePasswordAuthenticationFilter.class); // Registra o filtro JWT

        return http.build();
    }
}


