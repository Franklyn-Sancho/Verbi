package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;

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

    public User registerUser(String name, String email, String password) {
    User newUser = new User();
    newUser.setName(name);
    newUser.setEmail(email);
    newUser.setPassword(passwordEncoder.encode(password));

    // Gera o token de confirmação de e-mail
    String emailConfirmationToken = UUID.randomUUID().toString();
    newUser.setEmailConfirmationToken(emailConfirmationToken);

    // Define o tempo de expiração do token (ex: 1 hora a partir de agora)
    LocalDateTime tokenExpiryDate = LocalDateTime.now().plusHours(1);
    newUser.setEmailConfirmationExpires(tokenExpiryDate);

    // Salva o usuário no banco de dados
    userRepository.save(newUser);

    return newUser;
}

    public boolean authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findUserByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    public User updateUser(Long id, UserDto userDto) {
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

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
