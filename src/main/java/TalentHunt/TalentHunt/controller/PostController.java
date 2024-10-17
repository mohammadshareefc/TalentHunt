package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Post;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class PostController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/createPost")
    public String createPost(@RequestParam("description") String description,
                             @RequestParam("media") MultipartFile media,
                             @RequestParam("document") MultipartFile document,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer", required = false) String referer) {

        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        // Retrieve the current user
        User user = userService.getUserByEmail(email);

        // Create a new post
        Post post = new Post();
        post.setUser(user);
        post.setDescription(description);
        post.setTimestamp(LocalDateTime.now());

        try {
            if (!media.isEmpty()) {
                post.setImage(media.getBytes());
            }
            if (!document.isEmpty()) {
                post.setDocument(document.getBytes());
            }

            postService.savePost(post);

            // Notify users of the new post
            notificationService.notifyConnectionsOfNewPost(post);

            redirectAttributes.addFlashAttribute("message", "Post uploaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to upload post. Please try again.");
        }

        // Redirect to the referer (previous page) or default to "/home" if referer is null
        return "redirect:" + (referer != null ? referer : "/home");
    }

    @PostMapping("/likePost")
    public String likePost(@RequestParam("postId") Long postId,
                           HttpSession session,
                           @RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Long userId = user.getUserId();

        postService.likePost(postId, userId);

        // Notify the post creator of the like
        Post post = postService.getPostById(postId);
        notificationService.notifyPostCreatorOfLike(post, user);

        // Redirect back to the previous page or "/home" if no referer is available
        return "redirect:" + (referer != null ? referer : "/home");
    }


    @PostMapping("/unlikePost")
    public String unlikePost(@RequestParam("postId") Long postId,
                             HttpSession session,
                             @RequestHeader(value = "Referer", required = false) String referer) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User user = userService.getUserByEmail(email);
        Long userId = user.getUserId();

        postService.unlikePost(postId, userId);

        // Notify the post creator of the unlike
        Post post = postService.getPostById(postId);
        notificationService.notifyPostCreatorOfUnlike(post, user);

        // Redirect back to the previous page or "/home" if no referer is available
        return "redirect:" + (referer != null ? referer : "/home");
    }

}
