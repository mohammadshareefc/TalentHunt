package TalentHunt.TalentHunt.service;


import TalentHunt.TalentHunt.model.*;
import TalentHunt.TalentHunt.repository.NotificationRepository;
import TalentHunt.TalentHunt.repository.UserRepository;
import TalentHunt.TalentHunt.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    private final ConnectionService connectionService;

    public NotificationService(@Lazy ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public void sendNotification(Long userId, String message, String link,  byte[] profileImage, boolean isHtmlContent) {
        User user = (User) userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Notification notification = new Notification();
        notification.setUser(user);
        if (isHtmlContent) {
            message = truncateMessage(message, 20); // Truncate to 20 words
        }
        notification.setMessage(message);
        notification.setLink(link);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setProfileImage(profileImage);
        notification.setHtmlContent(isHtmlContent);

        notificationRepository.save(notification);
    }

    private String truncateMessage(String message, int maxWords) {
        String[] words = message.split("\\s+");
        if (words.length > maxWords) {
            return String.join(" ", Arrays.copyOfRange(words, 0, maxWords)) + "...";
        } else {
            return message;
        }
    }


    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }


    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    // Notify users when a connection request is sent
    public void notifyUserOfConnectionRequest(User sender, User receiver) {
        if (!sender.equals(receiver)) { // Skip notification if sender and receiver are the same
            String message = sender.getFirstName() + " " + sender.getLastName() + " has sent you a connection request.";
            byte[] profileImage = sender.getProfileImage();
            String link = "/networks";
            sendNotification(receiver.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify users when a connection request is accepted
    public void notifyUserOfRequestAcceptance(User accepter, User requester) {
        if (!accepter.equals(requester)) { // Skip notification if accepter and requester are the same
            String message = requester.getFirstName() + " " + requester.getLastName() + " has accepted your connection request.";
            byte[] profileImage = requester.getProfileImage();
            String link = "/networks/connections";
            sendNotification(requester.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify users when a connection request is declined
    public void notifyUserOfRequestDecline(User decliner, User requester) {
        if (!decliner.equals(requester)) { // Skip notification if decliner and requester are the same
            String message = decliner.getFirstName() + " " + decliner.getLastName() + " has declined your connection request.";
            byte[] profileImage = decliner.getProfileImage();
            String link = "/networks";
            sendNotification(requester.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify users when a new post is created
    public void notifyConnectionsOfNewPost(Post post) {
        User poster = post.getUser(); // Get the user who posted
        List<User> connections = connectionService.getConnectionsOfUser(poster);
        String posterName = poster.getFirstName() + " " + poster.getLastName();
        byte[] profileImage = poster.getProfileImage();
        String trimmedDescription = trimDescriptionTo100Words(post.getDescription());

        for (User connection : connections) {
            if (!connection.equals(poster)) {
                String message = posterName + " posted: " + trimmedDescription;
                String link = "/home";
                sendNotification(connection.getUserId(), message, link, profileImage, false);
            }
        }
    }
    // Notify the post creator when someone likes their post
    public void notifyPostCreatorOfLike(Post post, User likingUser) {
        User postCreator = post.getUser();
        if (!postCreator.equals(likingUser)) { // Ensure the post creator isn't liking their own post
            String message = likingUser.getFirstName() + " " + likingUser.getLastName() + " liked your post.";
            byte[] profileImage = likingUser.getProfileImage();
            String link = "/home"; // Assuming each post has a unique URL
            sendNotification(postCreator.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify the post creator when someone unlikes their post
    public void notifyPostCreatorOfUnlike(Post post, User unlikingUser) {
        User postCreator = post.getUser();
        if (!postCreator.equals(unlikingUser)) { // Ensure the post creator isn't unliking their own post
            String message = unlikingUser.getFirstName() + " " + unlikingUser.getLastName() + " unliked your post.";
            byte[] profileImage = unlikingUser.getProfileImage();
            String link = "/home";
            sendNotification(postCreator.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify the post creator when someone comments on their post
    public void notifyPostCreatorOfComment(Post post, User commentingUser, String commentText) {
        User postCreator = post.getUser();
        if (!postCreator.equals(commentingUser)) { // Ensure the post creator isn't commenting on their own post
            String message = commentingUser.getFirstName() + " " + commentingUser.getLastName() + " commented on your post: " + commentText;
            byte[] profileImage = commentingUser.getProfileImage();
            String link = "/home";
            sendNotification(postCreator.getUserId(), message, link, profileImage,false);
        }
    }

    // Notify users when a new post is added on a post they've commented on
    public void notifyCommentersOfNewPost(Post post) {
        List<TalentHunt.TalentHunt.model.Comment> comments = commentService.getCommentsByPost(post);
        Set<User> commenters = new HashSet<>();

        for (Comment comment : comments) {
            commenters.add(comment.getUser());
        }

        User poster = post.getUser(); // Get the user who posted
        String posterName = poster.getFirstName() + " " + poster.getLastName(); // Full name of the poster
        byte[] profileImage = poster.getProfileImage();
        String trimmedDescription = trimDescriptionTo100Words(post.getDescription());

        for (User commenter : commenters) {
            if (!commenter.equals(poster)) { // Skip notification if commenter is the poster
                String message = posterName + " posted on the same post you commented on: " + trimmedDescription;
                String link = "/home";
                sendNotification(commenter.getUserId(), message, link, profileImage,false);
            }
        }
    }

    // Notify users when a new article is created
    public void notifyConnectionsOfNewArticle(Article article) {
        User author = article.getAuthor(); // Get the user who posted the article
        List<User> connections = connectionService.getConnectionsOfUser(author); // Fetch connections of the author
        String authorName = author.getFirstName() + " " + author.getLastName();
        byte[] profileImage = author.getProfileImage();
        String trimmedDescription = trimDescriptionTo100Words(article.getContent());

        for (User connection : connections) {
            if (!connection.equals(author)) { // Skip notification if user is the author
                String message = authorName + " created a new article: " + trimmedDescription;
                String link = "/articles/article/" + article.getId();
                sendNotification(connection.getUserId(), message, link, profileImage, true);
            }
        }
    }

    // Notify user's connections about a new event
    public void notifyConnectionsOfNewEvent(Event event) {
        User organizer = event.getUser(); // Get the user who organized the event
        List<User> connections = connectionService.getConnectionsOfUser(organizer); // Fetch connections of the organizer
        String organizerName = organizer.getFirstName() + " " + organizer.getLastName();
        byte[] profileImage = organizer.getProfileImage();
        String trimmedDescription = trimDescriptionTo100Words(event.getDescription());

        for (User connection : connections) {
            if (!connection.equals(organizer)) { // Skip notification if user is the organizer
                String message = organizerName + " created a new event: " + trimmedDescription;
                String link = "/event-details/" + event.getEventId();
                sendNotification(connection.getUserId(), message, link, profileImage, false);
            }
        }
    }


    private String trimDescriptionTo100Words(String description) {
        String[] words = description.split("\\s+");
        if (words.length > 20) {
            return String.join(" ", Arrays.copyOfRange(words, 0, 20)) + "...";
        } else {
            return description;
        }
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid notification ID"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAsUnread(Long id) {
       Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid notification ID"));
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public String getFormattedNotificationTime(LocalDateTime timestamp) {
        return TimeUtils.formatRelativeTime(timestamp);
    }

    public int getUnreadNotificationsCount(Long userId) {
        // Implement logic to count unread notifications for the user
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }
}
