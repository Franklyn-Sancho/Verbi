package br.com.verbi.verbi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.entity.Friendship.FriendshipStatus;
import br.com.verbi.verbi.entity.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    List<Friendship> findByUser1OrUser2AndStatus(User sender, User receiver, FriendshipStatus status);

    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

}
