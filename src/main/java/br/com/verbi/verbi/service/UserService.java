package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.exception.EmailAlreadyExistsException;
import br.com.verbi.verbi.exception.TokenExpiredException;
import br.com.verbi.verbi.exception.TokenInvalidException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.UserRepository;

import java.time.LocalDateTime;

import java.io.IOException;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileService fileService;

    public User registerUser(String name, String email, String password, MultipartFile picture) {
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
    
        // Lógica para salvar a imagem de perfil
        if (picture != null && !picture.isEmpty()) {
            try {
                String pictureUrl = fileService.saveFile(picture, "imageProfile"); // Diretório para fotos de perfil
                newUser.setPicture(pictureUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save picture: " + e.getMessage());
            }
        }
    
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

    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findUserByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    public void updatePassword(UUID userId, String oldPassword, String newPassword) {

        authService.verifyUserAuthorization(userId, userRepository);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserPicure(UUID userId, String fileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        user.setPicture(fileName);
        userRepository.save(user);
    }

    public User requestPasswordReset(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("No user found with this email.");
        }

        User user = optionalUser.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(1)); // Token válido por 1 hora

        userRepository.save(user);

        return user;
    }

    public void resetPassword(String token, String newPassword) {

        Optional<User> optionalUser = userRepository.findByResetPasswordToken(token);

        if (!optionalUser.isPresent()) {
            throw new TokenInvalidException("Invalid reset password token.");
        }

        User user = optionalUser.get();

        // Verificar se o token ainda é válido
        if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("The reset password token has expired.");
        }

        // Atualizar a senha e remover o token de redefinição
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null); // Remover o token após o uso
        user.setResetPasswordExpires(null); // Remover a validade do token

        userRepository.save(user);
    }

    public User updateUser(UUID userId, UserDto userDto) {
        authService.verifyUserAuthorization(userId, userRepository); // Verifica se o usuário tem permissão

        return userRepository.findById(userId).map(user -> {
            // Atualiza os dados do usuário
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setDescription(userDto.getDescription());

            // Se uma nova senha for fornecida, atualiza a senha
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Atualiza a senha se fornecida
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + userId));
    }

    public void suspendUser(UUID userId) {
        authService.verifyUserAuthorization(userId, userRepository); // Verifica se o usuário tem permissão

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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

        authService.verifyUserAuthorization(userId, userRepository); // Verifica se o usuário tem permissão

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

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
