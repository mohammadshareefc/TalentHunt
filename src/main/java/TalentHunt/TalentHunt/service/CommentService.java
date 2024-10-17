package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Comment;
import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.repository.CommentRepository;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public void saveComment(Comment comment) {
        comment.setTimestamp(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public List<TalentHunt.TalentHunt.model.Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPost_PostIdOrderByTimestampAsc(postId);
    }

    public Comment getCommentById(Long commentId) {
        return (Comment) commentRepository.findById(commentId).orElse(null);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public void likeComment(Long commentId, User user) {
        Comment comment = getCommentById(commentId);
        if (comment != null) {
            Set<User> likes = comment.getLikes();
            if (likes == null) {
                likes = new HashSet<>();
            }
            if (likes.contains(user)) {
                likes.remove(user); // Unlike
                comment.setLikeCount(comment.getLikeCount() - 1);
            } else {
                likes.add(user); // Like
                comment.setLikeCount(comment.getLikeCount() + 1);
            }
            comment.setLikes(likes);
            commentRepository.save(comment);
        }
    }

    public Long getReplyCount(Comment comment) {
        return commentRepository.countByParentComment(comment);
    }

    public TalentHunt.TalentHunt.model.Comment getCommentWithReplies(Long commentId) {
        return commentRepository.findById(commentId)
                .map(comment -> {
                    // Load replies for this comment
                    comment.getReplies().size();
                    return comment;
                })
                .orElseThrow();
    }

    //For Notification
    // Method to get comments by post
    public List<TalentHunt.TalentHunt.model.Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPost(post);
    }

}
