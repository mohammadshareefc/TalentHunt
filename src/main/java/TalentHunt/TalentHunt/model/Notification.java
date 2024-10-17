package TalentHunt.TalentHunt.model;

import jakarta.persistence.*;
import TalentHunt.TalentHunt.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", columnDefinition = "LONGTEXT")
    private String message;

    private boolean isHtmlContent;

    private String link;
    private LocalDateTime timestamp;
    private boolean isRead;
    @Lob
    @Column(name = "profileImage", columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    private String formattedTime;

    // Getters and Setters

    public byte[] getProfileImage() {
        return profileImage;
    }
    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHtmlContent() {
        return isHtmlContent;
    }
    public void setHtmlContent(boolean isHtmlContent) {
        this.isHtmlContent = isHtmlContent;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
}
