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

    public Friendship sendFriendRequest(UUID senderId, UUID receiverId) {
        User sender = userService.findUserById(senderId)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        User receiver = userService.findUserById(receiverId)
                .orElseThrow(() -> new UsernameNotFoundException("receiver not found"));

        if (friendshipRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new IllegalArgumentException("Friend request already sent");
        }

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);

    }

    public Friendship acceptFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Criar o chat automaticamente
        chatService.createChat(savedFriendship.getSender(), savedFriendship.getReceiver());

        return savedFriendship; // Retorna a amizade atualizada
    }

    public Friendship declineFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendshipRepository.save(friendship);
    }

    public List<Friendship> getFriends(User user) {
        return friendshipRepository.findBySenderOrReceiverAndStatus(user, user, FriendshipStatus.ACCEPTED);
    }

    public boolean areFriends(UUID userId1, UUID userId2) {
        User user1 = userService.findUserById(userId1)
                .orElseThrow(() -> new UsernameNotFoundException("User 1 not found"));
        User user2 = userService.findUserById(userId2)
                .orElseThrow(() -> new UsernameNotFoundException("User 2 not found"));

        boolean areFriends = friendshipRepository.findBySenderAndReceiver(user1, user2)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .isPresent()
                || friendshipRepository.findBySenderAndReceiver(user2, user1)
                        .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                        .isPresent();

        return areFriends;
    }

}
