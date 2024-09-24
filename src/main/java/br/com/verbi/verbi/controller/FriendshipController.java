package br.com.verbi.verbi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.FriendshipService;
import br.com.verbi.verbi.service.UserService;
import br.com.verbi.verbi.entity.User;

@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Friendship> sendFriendRequest(@PathVariable UUID receiverId,
            @RequestHeader("Authorization") String token) {

        String actualToken = token.substring(7);
        String email = jwtGenerator.getUsername(actualToken);
        User sender = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Friendship friendship = friendshipService.sendFriendRequest(sender.getId(), receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(friendship);
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<Friendship> acceptFriendRequest(@PathVariable UUID friendshipId) {
        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId);
        return ResponseEntity.ok(friendship);
    }

    @PostMapping("/decline/{friendshipId}")
    public ResponseEntity<Friendship> declineFriendRequest(@PathVariable UUID friendshipId) {
        Friendship friendship = friendshipService.declineFriendRequest(friendshipId);
        return ResponseEntity.ok(friendship);
    }

    // Listar amizades de um usuário
    @GetMapping("/friends")
    public ResponseEntity<List<Friendship>> getFriends(@RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7);
        String email = jwtGenerator.getUsername(actualToken);
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Friendship> friends = friendshipService.getFriends(user);
        return ResponseEntity.ok(friends);
    }

}
