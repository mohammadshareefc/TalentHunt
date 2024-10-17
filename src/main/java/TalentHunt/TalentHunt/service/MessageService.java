package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Message;
import TalentHunt.TalentHunt.repository.MessageRepository;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getMessagesBetweenUsers(User user1, User user2) {
        return messageRepository.findMessagesBetweenUsers(user1.getUserId(), user2.getUserId());
    }

    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    public Message getLastMessageBetweenUsers(User user1, User user2) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1.getUserId(), user2.getUserId());
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1); // Get the last message in the list
    }

    public Message getLatestMessageBetween(User user1, User user2) {
        return messageRepository.findTopBySenderAndReceiverOrderByTimestampDesc(user1, user2);
    }

    public List<Message> getUnreadMessagesForUser(User user) {
        return messageRepository.findUnreadMessagesForUser(user.getUserId());
    }

    public void markMessageAsRead(Long messageId) {
        messageRepository.markMessageAsRead(messageId);
    }


    @Transactional
    public void deleteMessagesForUser(User currentUser, User targetUser) {
        messageRepository.deleteMessagesBySenderOrReceiver(currentUser.getUserId(), targetUser.getUserId());
    }

    @Transactional
    public void markMessageAsUnread(Long messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setRead(false);
            messageRepository.save(message);
        }
    }

    @Transactional
    public void markAllMessagesAsUnread(User user1, User user2) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1, user2);
        for (Message message : messages) {
            message.setRead(false);
        }
        messageRepository.saveAll(messages);
    }


    public int getUnreadMessagesCount(Long userId) {
        // Implement logic to count unread messages for the user
        return messageRepository.countUnreadMessagesByUserId(userId);
    }
}
