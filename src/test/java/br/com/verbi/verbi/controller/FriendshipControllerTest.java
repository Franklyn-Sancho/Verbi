package br.com.verbi.verbi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exceptionhandler.GlobalExceptionHandler;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.FriendshipService;
import br.com.verbi.verbi.service.UserService;


public class FriendshipControllerTest {

    @InjectMocks
    private FriendshipController friendshipController;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private UUID receiverId;
    private UUID friendshipId;
    private User sender;
    private Friendship friendship;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(friendshipController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Simulação de um usuário remetente
        sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setEmail("sender@example.com");

        // Simulação de uma amizade
        friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setSender(sender);
        friendship.setReceiver(new User());
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);

        receiverId = UUID.randomUUID();
        friendshipId = UUID.randomUUID();
    }

    @Test
    public void testSendFriendRequest_Success() throws Exception {
        String token = "Bearer validToken";
        when(jwtGenerator.getUsername(anyString())).thenReturn("sender@example.com");
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.of(sender));
        when(friendshipService.sendFriendRequest(any(UUID.class), any(UUID.class))).thenReturn(friendship);

        mockMvc.perform(post("/api/friendship/send/{receiverId}", receiverId)
                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(content().string("Friend request sent successfully"));

        verify(friendshipService, times(1)).sendFriendRequest(sender.getId(), receiverId);
    }

    @Test
    public void testAcceptFriendRequest_Success() throws Exception {
        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        when(friendshipService.acceptFriendRequest(any(UUID.class))).thenReturn(friendship);

        mockMvc.perform(post("/api/friendship/accept/{friendshipId}", friendshipId))
                .andExpect(status().isOk())
                .andExpect(content().string("Friend request accepted successfully"));

        verify(friendshipService, times(1)).acceptFriendRequest(friendshipId);
    }

    @Test
    public void testDeclineFriendRequest_Success() throws Exception {
        friendship.setStatus(Friendship.FriendshipStatus.DECLINED);
        when(friendshipService.declineFriendRequest(any(UUID.class))).thenReturn(friendship);

        mockMvc.perform(post("/api/friendship/decline/{friendshipId}", friendshipId))
                .andExpect(status().isOk())
                .andExpect(content().string("Friend request declined successfully"));

        verify(friendshipService, times(1)).declineFriendRequest(friendshipId);
    }

    @Test
    public void testGetFriends_Success() throws Exception {
        String token = "Bearer validToken";
        when(jwtGenerator.getUsername(anyString())).thenReturn("sender@example.com");
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.of(sender));

        // Simulação de uma lista de amigos
        List<Friendship> friends = new ArrayList<>();
        friends.add(friendship);
        when(friendshipService.getFriends(any(User.class))).thenReturn(friends);

        mockMvc.perform(get("/api/friendship/friends")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(Friendship.FriendshipStatus.PENDING.toString()));

        verify(friendshipService, times(1)).getFriends(sender);
    }
}