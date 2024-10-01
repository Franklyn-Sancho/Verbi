package br.com.verbi.verbi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.FriendshipStatus;
import br.com.verbi.verbi.repository.FriendshipRepository;

/* @SpringBootTest
public class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipService friendshipService;

    private User sender;
    private User receiver;
    private Friendship friendship;

    @BeforeEach
    public void setUp() {
        sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setEmail("sender@test.com");

        receiver = new User();
        receiver.setId(UUID.randomUUID());
        receiver.setEmail("receiver@test.com");

        friendship = new Friendship();
        friendship.setId(UUID.randomUUID());
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
    }

    @Test
    public void testSendFriendRequest_Success() {
        when(userService.findUserById(sender.getId())).thenReturn(Optional.of(sender));
        when(userService.findUserById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(friendshipRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        Friendship result = friendshipService.sendFriendRequest(sender.getId(), receiver.getId());

        assertNotNull(result);
        assertEquals(FriendshipStatus.PENDING, result.getStatus());
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());

        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    @Test
    public void testSendFriendRequest_AlreadySent() {
        when(userService.findUserById(sender.getId())).thenReturn(Optional.of(sender));
        when(userService.findUserById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(friendshipRepository.findBySenderAndReceiver(sender, receiver)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.sendFriendRequest(sender.getId(), receiver.getId());
        });

        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    public void testAcceptFriendRequest_Success() {
        UUID friendshipId = UUID.randomUUID();

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));

        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        friendship.setStatus(FriendshipStatus.PENDING);

        Friendship result = friendshipService.acceptFriendRequest(friendshipId);

        assertNotNull(result, "Friendship result should not be null");
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus(), "Status should be ACCEPTED");

        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    public void testDeclineFriendRequest_Success() {
        UUID friendshipId = UUID.randomUUID();

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));

        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        friendship.setStatus(FriendshipStatus.PENDING);

        Friendship result = friendshipService.declineFriendRequest(friendshipId);

        assertNotNull(result, "Friendship result should not be null");
        assertEquals(FriendshipStatus.DECLINED, result.getStatus(), "Status should be DECLINED");

        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    public void testGetFriends_Success() {

        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findBySenderOrReceiverAndStatus(sender, sender, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(friendship));

        List<Friendship> friends = friendshipService.getFriends(sender);

        assertNotNull(friends);
        assertEquals(1, friends.size());
        assertEquals(FriendshipStatus.ACCEPTED, friends.get(0).getStatus());
    }

} */
