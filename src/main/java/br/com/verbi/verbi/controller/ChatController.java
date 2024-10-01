package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import br.com.verbi.verbi.dto.ChatDto;
import br.com.verbi.verbi.dto.CreateChatRequest;
import br.com.verbi.verbi.entity.Chat;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.service.ChatService;
import br.com.verbi.verbi.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ChatDto createChat(@RequestBody CreateChatRequest request) {
        User user1 = userService.findUserById(request.getUser1Id())
                .orElseThrow(() -> new UsernameNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(request.getUser2Id())
                .orElseThrow(() -> new UsernameNotFoundException("User 2 not found"));
        Chat chat = chatService.createChat(user1, user2);
        return new ChatDto(chat.getId(), user1.getId(), user2.getId());
    }

    @GetMapping("/between/{user1Id}/{user2Id}")
    public ChatDto getChatBetweenUsers(@PathVariable UUID user1Id, @PathVariable UUID user2Id) {
        User user1 = userService.findUserById(user1Id)
                .orElseThrow(() -> new UsernameNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(user2Id)
                .orElseThrow(() -> new UsernameNotFoundException("User 2 not found"));

        Chat chat = chatService.findChatBetweenUsers(user1, user2);
        return new ChatDto(chat.getId(), user1.getId(), user2.getId());
    }

    @GetMapping("/{chatId}")
    public ChatDto getChatById(@PathVariable UUID chatId) {
        Chat chat = chatService.getChatById(chatId);
        return new ChatDto(chat.getId(), chat.getUser1().getId(), chat.getUser2().getId());
    }
}
