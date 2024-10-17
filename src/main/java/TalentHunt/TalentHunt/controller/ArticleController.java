package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Article;
import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.service.ArticleService;
import TalentHunt.TalentHunt.service.MessageService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.UserService;
import TalentHunt.TalentHunt.utils.TimeUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageService messageService;

    // Display all articles
    @GetMapping
    public String getAllArticles(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
         User user = userService.getUserByEmail(email);

        if (user == null) {
            // Log error and redirect if user not found
            System.out.println("User not found with email: " + email);
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);

        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);


        List<Article> articles = articleService.getAllArticles();

        // Truncate content to 300 words
        for (Article article : articles) {
            article.setContent(truncateTo100Words(article.getContent()));
        }

        model.addAttribute("articles", articles);
        model.addAttribute("timeUtils", new TimeUtils());

        return "article";  // Thymeleaf template for displaying articles
    }

    private String truncateTo100Words(String content) {
        String[] words = content.split("\\s+");
        if (words.length > 100) {
            return String.join(" ", Arrays.copyOf(words, 100)) + "...";
        }
        return content;
    }

    @GetMapping("/new")
    public String getCreateArticle(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            // Log error and redirect if user not found
            System.out.println("User not found with email: " + email);
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);

        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);


        return "article-create";  // Thymeleaf template for displaying articles
    }

    // Create an article
    @PostMapping("/create")
    public String createArticle(@RequestParam("userId") Long userId,
                                @RequestParam("title")  String title,
                                @RequestParam("content")  String content,
                                @RequestParam("image") MultipartFile image,
                                Model model) {
        Optional<User> userOpt = userService.getUserById1(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Article article = new Article();
            article.setAuthor(user);
            article.setTitle(title);
            article.setContent(content);
            article.setPublicationDate(LocalDateTime.now());

            if (!image.isEmpty()) {
                try {
                    article.setImageUrl(image.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "article-create";
                }
            }else {
                System.out.println("No image uploaded.");
            }

            articleService.createArticle(article);

            // Notify all connections about the new article
            notificationService.notifyConnectionsOfNewArticle(article);
            return "redirect:/articles";
        } else {
            model.addAttribute("error", "User not found");
            return "article-create";  // Error message in article form
        }
    }


    // Show article details by ID
    @GetMapping("/article/{id}")
    public String getArticleDetails(HttpSession session, @PathVariable("id") Long articleId, Model model) {

        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            // Log error and redirect if user not found
            System.out.println("User not found with email: " + email);
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);

        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);
        model.addAttribute("timeUtils", new TimeUtils());

        Article article = articleService.getArticleById(articleId);
        List<Article> articles = articleService. getAllArticles();
        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);
        model.addAttribute("article", article);
        model.addAttribute("articles", articles);

        return "articledetails";
    }

}
