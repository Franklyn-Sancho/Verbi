package br.com.verbi.verbi.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import br.com.verbi.verbi.dto.FriendshipDto;
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

    /**
     * Sends a friend request from the authenticated user to another user.
     *
     * @param receiverId The UUID of the receiver.
     * @param token      The JWT token containing the sender's credentials.
     * @return A response indicating the success of the friend request.
     */
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable UUID receiverId,
                                                    @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Extract email from JWT

        User sender = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        friendshipService.sendFriendRequest(sender.getId(), receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent successfully");
    }

    /**
     * Accepts a friend request.
     *
     * @param friendshipId The UUID of the friendship to be accepted.
     * @return A response indicating the success of the friend request acceptance.
     */
    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable UUID friendshipId) {
        friendshipService.acceptFriendRequest(friendshipId);
        return ResponseEntity.ok("Friend request accepted successfully");
    }

    /**
     * Declines a friend request.
     *
     * @param friendshipId The UUID of the friendship to be declined.
     * @return A response indicating the success of the friend request decline.
     */
    @PostMapping("/decline/{friendshipId}")
    public ResponseEntity<String> declineFriendRequest(@PathVariable UUID friendshipId) {
        friendshipService.declineFriendRequest(friendshipId);
        return ResponseEntity.ok("Friend request declined successfully");
    }

    /**
     * Retrieves the list of friends for the authenticated user.
     *
     * @param token The JWT token containing the user's credentials.
     * @return A list of friends as a response.
     */
    @GetMapping("/friends")
    public ResponseEntity<List<FriendshipDto>> getFriends(@RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Extract email from JWT

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<FriendshipDto> friends = friendshipService.getFriends(user).stream()
                .map(FriendshipDto::fromEntity) // Convert each Friendship to FriendshipResponseDTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(friends);
    }

}
