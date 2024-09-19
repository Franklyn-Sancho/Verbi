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
    
   Optional<User> findByEmail(String email);

   Optional<User> findById(UUID id);

   List<User> findByNameContaining(String name);

   List<User> findByDeleteMarkedDateBeforeAndDeletionDateIsNull(LocalDateTime cutoffDate);
}
