package TalentHunt.TalentHunt.model;

import jakarta.persistence.*;
import TalentHunt.TalentHunt.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @Transient // Use @Transient if this field should not be persisted in the database
    private String formattedTimestamp;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;


    @ManyToMany
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Transient // Use @Transient for fields that are not persisted
    private Long replyCount; // New field for reply count

    // Getters and Setters

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Set<Comment> getReplies() {
        return replies;
    }

    public void setReplies(Set<Comment> replies) {
        this.replies = replies;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    /**
     * @return the likes
     */
    public Set<User> getLikes() {
        return likes;
    }

    /**
     * @param likes the likes to set
     */
    public void setLikes(Set<User> likes) {
        this.likes = likes;
    }

    /**
     * @return the replyCount
     */
    public Long getReplyCount() {
        return replyCount;
    }

    /**
     * @param replyCount the replyCount to set
     */
    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }

}
