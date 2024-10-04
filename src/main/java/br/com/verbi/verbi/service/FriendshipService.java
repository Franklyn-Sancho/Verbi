package br.com.verbi.verbi.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.FriendshipStatus;
import br.com.verbi.verbi.repository.FriendshipRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    /**
     * Sends a friend request from one user to another.
     *
     * @param senderId   The UUID of the sender.
     * @param receiverId The UUID of the receiver.
     * @return The created Friendship entity.
     * @throws UsernameNotFoundException If the sender or receiver does not exist.
     * @throws IllegalArgumentException  If a friend request has already been sent.
     */
    public Friendship sendFriendRequest(UUID senderId, UUID receiverId) {
        User sender = userService.findUserById(senderId)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        User receiver = userService.findUserById(receiverId)
                .orElseThrow(() -> new UsernameNotFoundException("Receiver not found"));

        // Check if the friendship already exists
        if (friendshipRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new IllegalArgumentException("Friend request already sent");
        }

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);
    }

    /**
     * Accepts a pending friend request.
     *
     * @param friendshipId The UUID of the friendship to accept.
     * @return The updated Friendship entity.
     * @throws EntityNotFoundException If the friendship does not exist.
     */
    public Friendship acceptFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Automatically create a chat between the two friends
        chatService.createChat(savedFriendship.getSender(), savedFriendship.getReceiver());

        return savedFriendship;
    }

    /**
     * Declines a pending friend request.
     *
     * @param friendshipId The UUID of the friendship to decline.
     * @return The updated Friendship entity with declined status.
     * @throws EntityNotFoundException If the friendship does not exist.
     */
    public Friendship declineFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendshipRepository.save(friendship);
    }

    /**
     * Retrieves all friends of a given user.
     *
     * @param user The user whose friends are to be retrieved.
     * @return A list of accepted friendships.
     */
    public List<Friendship> getFriends(User user) {
        return friendshipRepository.findBySenderOrReceiverAndStatus(user, user, FriendshipStatus.ACCEPTED);
    }

    /**
     * Checks if two users are friends.
     *
     * @param userId1 The UUID of the first user.
     * @param userId2 The UUID of the second user.
     * @return True if they are friends, false otherwise.
     * @throws UsernameNotFoundException If either user does not exist.
     */
    public boolean areFriends(UUID userId1, UUID userId2) {
        User user1 = userService.findUserById(userId1)
                .orElseThrow(() -> new UsernameNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(userId2)
                .orElseThrow(() -> new UsernameNotFoundException("User 2 not found"));

        // Check both directions of friendship
        return friendshipRepository.findBySenderAndReceiver(user1, user2)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .isPresent()
                || friendshipRepository.findBySenderAndReceiver(user2, user1)
                        .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                        .isPresent();
    }
}
