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
                        // Rotas sem autenticação
                        .requestMatchers("/api/user/register", "/api/user/login", "/oauth/**").permitAll()
                        // Rotas protegidas
                        .requestMatchers("/api/user/logout", "/api/mural/**").authenticated()
                        .anyRequest().authenticated())
                // Configura login com OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/oauth/login/success")
                        .failureUrl("/oauth/login/failure")
                        .permitAll())
                // Configura login JWT
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())
                // Filtro para validação de tokens JWT
                .addFilterBefore(new JwtFilter(jwtGenerator, userService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
