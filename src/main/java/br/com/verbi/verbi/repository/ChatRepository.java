package br.com.verbi.verbi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    Optional<Chat> findByUser1AndUser2(User user1, User user2);
    Optional<Chat> findByUser2AndUser1(User user1, User user2); // Para encontrar a conversa de forma reversa
}

