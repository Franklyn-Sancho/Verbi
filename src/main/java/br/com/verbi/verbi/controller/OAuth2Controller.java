package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.UserService;

import java.security.Principal;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class OAuth2Controller {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @GetMapping("/success")
    public ResponseEntity<?> getLoginSuccess(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String googleId = oauth2User.getAttribute("sub");

            User user = userService.createUserFromOAuth2(name, email, googleId);

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", user.getName());
            userDetails.put("email", user.getEmail());
            userDetails.put("picture", oauth2User.getAttribute("picture"));

            String token = jwtGenerator.generateToken(user.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer " + token);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @GetMapping("/login/failure")
    public ResponseEntity<?> handleOAuth2Failure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth2 login failed");
    }
}
