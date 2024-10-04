package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import br.com.verbi.verbi.dto.ChatDto;
import br.com.verbi.verbi.dto.CreateChatRequest;
import br.com.verbi.verbi.dto.CreateMessageRequest;
import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.Message;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.ResourceNotFoundException;
import br.com.verbi.verbi.service.ChatService;
import br.com.verbi.verbi.service.MessageService;
import br.com.verbi.verbi.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * Cria um novo chat entre dois usuários.
     * 
     * @param request Objeto contendo os IDs dos usuários que vão participar do chat.
     * @return DTO do chat criado.
     */
    @PostMapping
    public ChatDto createChat(@RequestBody CreateChatRequest request) {
        User user1 = userService.findUserById(request.getUser1Id())
                .orElseThrow(() -> new ResourceNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(request.getUser2Id())
                .orElseThrow(() -> new ResourceNotFoundException("User 2 not found"));
        
        Chat chat = chatService.createChat(user1, user2);
        
        return new ChatDto(chat.getId(), user1.getId(), user2.getId());
    }

    /**
     * Busca um chat existente entre dois usuários.
     * 
     * @param user1Id ID do primeiro usuário.
     * @param user2Id ID do segundo usuário.
     * @return DTO do chat encontrado.
     */
    @GetMapping("/between/{user1Id}/{user2Id}")
    public ChatDto getChatBetweenUsers(@PathVariable UUID user1Id, @PathVariable UUID user2Id) {
        User user1 = userService.findUserById(user1Id)
                .orElseThrow(() -> new ResourceNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(user2Id)
                .orElseThrow(() -> new ResourceNotFoundException("User 2 not found"));

        Chat chat = chatService.findChatBetweenUsers(user1, user2);
        return new ChatDto(chat.getId(), user1.getId(), user2.getId());
    }

    /**
     * Retorna um chat por seu ID.
     * 
     * @param chatId ID do chat.
     * @return DTO do chat.
     */
    @GetMapping("/{chatId}")
    public ChatDto getChatById(@PathVariable UUID chatId) {
        Chat chat = chatService.getChatById(chatId);
        return new ChatDto(chat.getId(), chat.getUser1().getId(), chat.getUser2().getId());
    }
}

