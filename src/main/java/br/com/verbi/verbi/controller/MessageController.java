package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import br.com.verbi.verbi.dto.CreateMessageRequest;
import br.com.verbi.verbi.dto.MessageDto;
import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.Message;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.service.ChatService;
import br.com.verbi.verbi.service.MessageService;
import br.com.verbi.verbi.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    
    @PostMapping
    public MessageDto sendMessage(@RequestBody CreateMessageRequest request) {
        Chat chat = chatService.findChatById(request.getChatId());
        User sender = userService.findUserById(request.getSenderId())
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        Message message = messageService.sendMessage(chat, sender, request.getContent());
        return new MessageDto(message.getId(), message.getChat().getId(), message.getSender().getId(),
                message.getContent(), message.getTimestamp());
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageDto> getMessagesByChat(@PathVariable UUID chatId) {
        List<Message> messages = messageService.getMessagesByChat(chatId);
        return messages.stream()
                .map(message -> new MessageDto(
                        message.getId(),
                        message.getChat().getId(),
                        message.getSender().getId(),
                        message.getContent(),
                        message.getTimestamp()))
                .toList();
    }
}
