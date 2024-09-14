package br.com.verbi.verbi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
   Optional<User> findByEmail(String email);

   Optional<User> findById(Long id);

   List<User> findByNameContaining(String name);
}
