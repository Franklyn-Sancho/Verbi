package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.com.verbi.verbi.dto.CreateMessageRequest;
import br.com.verbi.verbi.dto.MessageDto;
import br.com.verbi.verbi.entity.Message;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.UserRepository;
import br.com.verbi.verbi.service.AuthorizationService;
import br.com.verbi.verbi.service.MessageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/send/{chatId}")
    public void sendMessage(@AuthenticationPrincipal User sender,
            @PathVariable UUID chatId,
            @RequestBody CreateMessageRequest request) {

        // Verifique se o senderDetails não é nulo
        if (sender == null || sender.getName() == null) {
            throw new IllegalArgumentException("Sender cannot be null");
        }

        Message message = messageService.sendMessage(chatId, sender, request.getContent());

        // Envia a mensagem para o tópico onde os usuários estão escutando
        messagingTemplate.convertAndSend("/topic/messages/" + chatId, message);
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
