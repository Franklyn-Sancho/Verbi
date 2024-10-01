package br.com.verbi.verbi.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.ChatRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    public Chat createChat(User user1, User user2) {
        Chat chat = new Chat(user1, user2);
        return chatRepository.save(chat);
    }

    public Chat findChatById(UUID chatId) {
        return chatRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    public Chat findChatBetweenUsers(User user1, User user2) {
        return chatRepository.findByUser1AndUser2(user1, user2)
                .orElseThrow(() -> new RuntimeException("Chat not found between these users"));
    }

    public Chat getChatById(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    }
}
