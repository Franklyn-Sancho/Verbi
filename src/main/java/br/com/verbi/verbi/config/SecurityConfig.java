package br.com.verbi.verbi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.verbi.verbi.controller.OAuth2LoginSuccessHandlerController;
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
                                // Desabilita CSRF, pois usamos JWT
                                .csrf(csrf -> csrf.disable())

                                // Configura as regras de autorização das rotas
                                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                                // Rotas públicas
                                                .requestMatchers("/api/user/register", "/api/user/login", "/oauth/**",
                                                                "/index.html")
                                                .permitAll()
                                                .requestMatchers("/login/oauth2/code/google", "/oauth2/**").permitAll()
                                                .requestMatchers("/ws/**").permitAll() // Permite WebSocket

                                                // Rotas protegidas
                                                .requestMatchers("/api/user/logout", "/api/mural/**",
                                                                "/api/messages/**", "/api/chat/**",
                                                                "/api/friendship/**")
                                                .authenticated()

                                                // Qualquer outra rota precisa de autenticação
                                                .anyRequest().authenticated())

                                // Configura login com OAuth2
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .successHandler(oAuth2LoginSuccessHandler()) // Handler de sucesso para
                                                                                             // OAuth2
                                                .failureUrl("/login?error=true")
                                                .permitAll())

                                // Configura login tradicional com formulário
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .permitAll())

                                // Configura logout
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())

                                // Adiciona filtros de validação de JWT e blacklist de token
                                .addFilterBefore(new JwtFilter(jwtGenerator, userService),
                                                UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
                return new OAuth2LoginSuccessHandlerController(jwtGenerator, userService);
        }
}
