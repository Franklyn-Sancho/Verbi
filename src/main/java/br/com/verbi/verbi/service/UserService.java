package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.exception.EmailAlreadyExistsException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.UserRepository;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int GRACE_PERIOD_DAYS = 30;

    public User registerUser(String name, String email, String password) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            System.out.println("Email already exists: " + email);
            throw new EmailAlreadyExistsException("An error occurred, please check your data.");
        }

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));

        String emailConfirmationToken = UUID.randomUUID().toString();
        newUser.setEmailConfirmationToken(emailConfirmationToken);

        LocalDateTime tokenExpiryDate = LocalDateTime.now().plusHours(1);
        newUser.setEmailConfirmationExpires(tokenExpiryDate);

        userRepository.save(newUser);

        System.out.println("User saved successfully: " + newUser.getEmail());
        return newUser;
    }

    public User createUserFromOAuth2(String name, String email, String googleId) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setGoogleId(googleId);
            return userRepository.save(newUser);
        });

        // Atualiza o googleId se necessário
        if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        return user;
    }

    public boolean authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findUserByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    public User updateUser(UUID id, UserDto userDto) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setDescription(userDto.getDescription());

            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Atualiza a senha se fornecida
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public void suspendUser(UUID userId) {
        // Obtém o usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName(); // Pega o email do usuário autenticado

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        // Verifica se o email do usuário autenticado é o mesmo do usuário que está
        // sendo suspenso
        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new AccessDeniedException("You are not allowed to suspend this account");
        }

        user.setSuspended(true);
        user.setSuspensionDate(LocalDateTime.now());
        userRepository.save(user);
    }

    public void reactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        user.setSuspended(false);
        user.setSuspensionDate(null);
        userRepository.save(user);
    }

    public boolean isUserSuspended(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        return user.isSuspended();
    }

    public void markForDeletion(UUID userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName(); // Pega o email do usuário autenticado

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new AccessDeniedException("You are not allowed to delete this account");
        }

        user.setDeleteMarkedDate(LocalDateTime.now());
        userRepository.save(user);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Executa diariamente à meia-noite
    public void deleteMarkedAccounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffDate = now.minusDays(30); // Excluir após 30 dias

        List<User> usersToDelete = userRepository.findByDeleteMarkedDateBeforeAndDeletionDateIsNull(cutoffDate);

        for (User user : usersToDelete) {
            deleteUser(user.getId()); // Excluir todas as contas
        }
    }

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        // Excluir usuário e todos os seus dados relacionados
        userRepository.delete(user);
    }
}
