package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.Message;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.MessageRepository;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatService chatService;

    public Message sendMessage(Chat chat, User sender, String content) {
        Message message = new Message(chat, sender, content);
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByChat(UUID chatId) {
        Chat chat = chatService.findChatById(chatId);
        return messageRepository.findByChat(chat);
    }
}
