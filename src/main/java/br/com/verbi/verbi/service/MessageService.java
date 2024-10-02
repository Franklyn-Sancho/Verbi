package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.Message;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.MessageRepository;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ChatService chatService;

    public Message sendMessage(UUID chatId, User sender, String content) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null");
        }
    
        UUID receiverId = getReceiverIdFromChat(chatId, sender);
        if (receiverId == null) {
            throw new IllegalArgumentException("Receiver ID cannot be null");
        }
    
        // Verifica se o sender é amigo do receptor
        if (!friendshipService.areFriends(sender.getId(), receiverId)) {
            throw new IllegalArgumentException("Users are not friends");
        }
    
        Chat chat = chatService.findChatById(chatId);
        Message message = new Message(chat, sender, content);
        return messageRepository.save(message);
    }
    

    private UUID getReceiverIdFromChat(UUID chatId, User sender) {
        Chat chat = chatService.findChatById(chatId);
        // Retorna o ID do usuário que não é o remetente
        return chat.getUser1().getId().equals(sender.getId()) ? chat.getUser2().getId() : chat.getUser1().getId();
    }

    public List<Message> getMessagesByChat(UUID chatId) {
        Chat chat = chatService.findChatById(chatId);
        return messageRepository.findByChat(chat);
    }
}

