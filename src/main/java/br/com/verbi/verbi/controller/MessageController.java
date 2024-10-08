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
import br.com.verbi.verbi.exception.InvalidDataException;
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

    /**
     * Envia uma mensagem em um chat específico via WebSocket.
     * 
     * @param sender  Usuário que está enviando a mensagem.
     * @param chatId  ID do chat.
     * @param request Requisição contendo o conteúdo da mensagem.
     */
    @MessageMapping("/sendMessage/{chatId}")
    public void sendMessage(@AuthenticationPrincipal User sender,
            @PathVariable UUID chatId,
            @RequestBody CreateMessageRequest request) {
        try {
            // Verifique se o senderDetails não é nulo
            if (sender == null || sender.getName() == null) {
                throw new IllegalArgumentException("Sender cannot be null");
            }

            Message message = messageService.sendMessage(chatId, sender, request.getContent());
            messagingTemplate.convertAndSend("/topic/messages/" + chatId, message);
        } catch (Exception e) {
            // Log da exceção
            System.out.println("Error sending message: " + e.getMessage());
            throw e; // Rethrow ou trate a exceção conforme necessário
        }
    }

    /**
     * Retorna todas as mensagens de um chat específico.
     * 
     * @param chatId ID do chat.
     * @return Lista de DTOs das mensagens.
     */
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
