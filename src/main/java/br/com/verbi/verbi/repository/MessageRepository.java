package br.com.verbi.verbi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByChat(Chat chat);
}

