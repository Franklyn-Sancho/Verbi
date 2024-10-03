package br.com.verbi.verbi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.User;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Finds a User by their email
    Optional<User> findByEmail(String email);

    // Finds a User by their UUID
    Optional<User> findById(UUID id);

    // Finds Users whose names contain the given string
    List<User> findByNameContaining(String name);

    // Finds Users marked for deletion before a certain date, where the account is not deleted yet
    List<User> findByDeleteMarkedDateBeforeAndDeletionDateIsNull(LocalDateTime cutoffDate);

    // Finds a User by their password reset token
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    // Checks if a User with the specified email already exists
    boolean existsByEmail(String email);
}

