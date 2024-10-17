package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Comment;
import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.service.CommentService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.PostService;
import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDateTime;
@Controller
public class CommentController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/addComment")
    public String addComment(@RequestParam("postId") Long postId,
                             @RequestParam("content") String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer", required = false) String referer) {

        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Post post = postService.getPostById(postId);

        if (post != null) {
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUser(user);
            comment.setCommentText(content);
            commentService.saveComment(comment);

            // Notify users who commented on the same post when a new post is made
            notificationService.notifyCommentersOfNewPost(post);
            // Notify the post creator of the new comment
            notificationService.notifyPostCreatorOfComment(post, user, content);

            redirectAttributes.addFlashAttribute("message", "Comment added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Post not found.");
        }

        return "redirect:" + (referer != null ? referer : "/home");
    }


    @PostMapping("/editComment")
    public String editComment(@RequestParam("commentId") Long commentId,
                              @RequestParam("content") String content,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              @RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Comment comment = (Comment) commentService.getCommentById(commentId);

        if (comment != null && comment.getUser().equals(user)) {
            comment.setCommentText(content);
            commentService.saveComment(comment);
            redirectAttributes.addFlashAttribute("message", "Comment updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Comment not found or you don't have permission to edit it.");
        }

        return "redirect:" + (referer != null ? referer : "/home");
    }

    @PostMapping("/deleteComment")
    public String deleteComment(@RequestParam("commentId") Long commentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                @RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Comment comment = (Comment) commentService.getCommentById(commentId);

        if (comment != null && comment.getUser().equals(user)) {
            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("message", "Comment deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Comment not found or you don't have permission to delete it.");
        }

        return "redirect:" + (referer != null ? referer : "/home");
    }


    @PostMapping("/likeComment")
    public String likeComment(@RequestParam("commentId") Long commentId, HttpSession session, RedirectAttributes redirectAttributes,@RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Comment comment = (Comment) commentService.getCommentById(commentId);

        if (comment != null) {
            boolean isLiked = comment.getLikes() != null && comment.getLikes().contains(user);

            if (isLiked) {
                // Unlike the comment
                commentService.likeComment(commentId, user); // This will handle removing the like
                redirectAttributes.addFlashAttribute("message", "Comment unliked successfully!");
            } else {
                // Like the comment
                commentService.likeComment(commentId, user); // This will handle adding the like
                redirectAttributes.addFlashAttribute("message", "Comment liked successfully!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Comment not found.");
        }

        return "redirect:" + (referer != null ? referer : "/home");
    }

    @PostMapping("/replyToComment")
    public String replyToComment(@RequestParam("commentId") Long commentId,
                                 @RequestParam("replyContent") String replyContent,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Comment parentComment = (Comment) commentService.getCommentById(commentId);

        if (parentComment != null) {
            Comment reply = new Comment();
            reply.setPost(parentComment.getPost()); // Assuming replies are tied to the same post
            reply.setUser(user);
            reply.setCommentText(replyContent);
            reply.setTimestamp(LocalDateTime.now()); // Set the timestamp for the reply
            reply.setParentComment(parentComment);

            commentService.saveComment(reply);
            redirectAttributes.addFlashAttribute("message", "Reply added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Comment not found.");
        }

        return "redirect:" + (referer != null ? referer : "/home");
    }
}
