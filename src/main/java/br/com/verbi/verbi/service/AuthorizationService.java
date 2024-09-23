package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.repository.UserRepository;

import java.util.UUID;

@Service
public class AuthorizationService {
    
    public void verifyUserAuthorization(UUID userId, UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName(); // Pega o email do usuário autenticado

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }
    }
}
