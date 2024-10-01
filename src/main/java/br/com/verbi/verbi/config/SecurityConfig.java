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
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                                // Rotas sem autenticação
                                                .requestMatchers("/api/user/register", "/api/user/login", "/oauth/**")
                                                .permitAll()
                                                .requestMatchers("/login/oauth2/code/google", "/oauth2/**").permitAll()
                                                .requestMatchers("/ws/**").permitAll() // Permita o WebSocket aqui
                                                // Rotas protegidas
                                                .requestMatchers("/api/user/logout", "/api/mural/**",
                                                                "/api/friendship/**")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                // Configura login com OAuth2
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .successHandler(oAuth2LoginSuccessHandler()) // Manipulador de sucesso
                                                                                             // para OAuth2
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())
                                // Filtro para validação de tokens JWT
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
