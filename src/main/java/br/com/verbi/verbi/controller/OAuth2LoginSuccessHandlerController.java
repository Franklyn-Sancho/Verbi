package br.com.verbi.verbi.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import br.com.verbi.verbi.entity.User;

import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2LoginSuccessHandlerController implements AuthenticationSuccessHandler {

    private JWTGenerator jwtGenerator;
    private UserService userService;

    public OAuth2LoginSuccessHandlerController(JWTGenerator jwtGenerator, UserService userService) {
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();

        // Extrair as informações do usuário do Google
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub"); // 'sub' é o ID do Google
        String picture = oauth2User.getAttribute("picture");

        // Verificar se o usuário já existe no banco de dados
        Optional<User> existingUser = userService.findUserByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Criar um novo usuário se não existir
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setPicture(picture);
            userService.save(user); // Salvar o novo usuário
        }

        // Gerar um token JWT para o usuário
        String token = jwtGenerator.generateToken(user.getEmail());

        // Criar o objeto JSON contendo o token e as informações do usuário
        String jsonResponse = String.format(
                "{\"token\": \"Bearer %s\", \"user\": {\"name\": \"%s\", \"email\": \"%s\", \"picture\": \"%s\"}}",
                token, user.getName(), user.getEmail(), user.getPicture());

        // Enviar a resposta com o token e informações do usuário
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

}
