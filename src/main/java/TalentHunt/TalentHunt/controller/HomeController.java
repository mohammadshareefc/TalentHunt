package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Article;
import TalentHunt.TalentHunt.model.Comment;
import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.service.*;
import TalentHunt.TalentHunt.utils.TimeUtils;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventService eventService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public String showHomePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }
        // Fetch user details
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return "redirect:/signin";
        }

        Long userId = user.getUserId();
        model.addAttribute("currentUser", user);
        model.addAttribute("currentUserId", userId);

        // Fetch all posts to display on the homepage
        List<Post> posts = postService.getAllPosts();

        // Fetch all events to display on the homepage
        List<Event> events = eventService.getAllEvents();

        // Fetch following user IDs
        List<Long> followingUserIds = connectionService.getFollowingUserIds(userId);
        model.addAttribute("followingUserIds", followingUserIds);


        // Preprocess the posts to highlight hashtags
        posts.forEach(post -> {

            String description = post.getDescription();
            if (description != null) {
                post.setDescription(description.replaceAll("#([A-Za-z0-9_]+)", "<span class='hashtag'>#$1</span>"));
            }

            // Ensure that timestamp is not null and format it
            if (post.getTimestamp() != null) {
                post.setFormattedTimestamp(TimeUtils.formatRelativeTime(post.getTimestamp()));
            } else {
                post.setFormattedTimestamp("Unknown time"); // or any default value
            }

            List<Comment> comments = commentService.getCommentsByPostId(post.getPostId());
            comments.forEach(comment -> {
                System.out.println("Comment User: " + comment.getUser());
                if (comment.getTimestamp() != null) {
                    comment.setFormattedTimestamp(TimeUtils.formatRelativeTime(comment.getTimestamp()));
                } else {
                    comment.setFormattedTimestamp("Unknown time");
                }
                // Add reply count for each comment
                comment.setReplyCount(commentService.getReplyCount( comment));
            });


            post.setComments(comments);

        });

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);


        model.addAttribute("posts", posts);
        model.addAttribute("currentUserId", user.getUserId());

        model.addAttribute("events", events);

        // Add like counts and like statuses
        model.addAttribute("likeCounts", posts.stream()
                .collect(Collectors.toMap(Post::getPostId, post -> postService.getLikeCount(post.getPostId()))));
        model.addAttribute("likedPosts", posts.stream()
                .collect(Collectors.toMap(Post::getPostId, post -> postService.isPostLikedByUser(post.getPostId(), userId))));

        // Fetch all articles
        List<Article> articles = articleService.getAllArticles();

        // Limit to 4 articles and truncate title to 10 words
        List<Article> limitedArticles = articles.stream()
                .limit(4)
                .peek(article -> article.setTitle(truncateTo5Words(article.getTitle())))
                .collect(Collectors.toList());

        model.addAttribute("articles", limitedArticles);
        model.addAttribute("timeUtils", new TimeUtils());

        return "home"; // Replace with your actual homepage view
    }

    private String truncateTo5Words(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        String[] words = title.split("\\s+");
        if (words.length > 5) {
            return String.join(" ", Arrays.copyOf(words, 5)) + "...";
        }

        return title;
    }

}
