package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Message;
import TalentHunt.TalentHunt.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository  extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :user1Id AND m.receiver.userId = :user2Id) " +
            "OR (m.sender.userId = :user2Id AND m.receiver.userId = :user1Id) ORDER BY m.timestamp ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT m FROM Message m WHERE m.receiver.userId = :userId AND m.isRead = false")
    List<Message> findUnreadMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :user1Id AND m.receiver.userId = :user2Id) " +
            "OR (m.sender.userId = :user2Id AND m.receiver.userId = :user1Id) ORDER BY m.timestamp DESC")
    List<Message> findRecentMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp DESC")
    List<Message> findMessagesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    default Message findTopBySenderAndReceiverOrderByTimestampDesc(User sender, User receiver) {
        List<Message> messages = findMessagesBetweenUsers(sender, receiver);
        return messages.isEmpty() ? null : messages.get(0);
    }

    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.message_id = :messageId")
    void markMessageAsRead(@Param("messageId") Long messageId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Message m WHERE (m.sender.userId = :userId OR m.receiver.userId = :targetUserId) OR (m.sender.userId = :targetUserId AND m.receiver.userId = :userId)")
    void deleteMessagesBySenderOrReceiver(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = false WHERE m.message_id = :messageId")
    void markMessageAsUnread(@Param("messageId") Long messageId);

    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = false WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)")
    void markAllMessagesAsUnread(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    int countUnreadMessagesByUserId(@Param("userId") Long userId);
}
