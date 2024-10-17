package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.service.MessageService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.SearchService;
import TalentHunt.TalentHunt.service.UserService;
import TalentHunt.TalentHunt.utils.TimeUtils;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, HttpSession session, Model model) {

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

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        model.addAttribute("timeUtils", new TimeUtils());

        var posts = searchService.searchPosts(query);
        var people = searchService.searchPeople(query);
        var articles = searchService.searchArticles(query);
        var events = searchService.searchEvents(query);

        // Pass search results and their counts to the model
        model.addAttribute("posts", posts);
        model.addAttribute("postCount", posts.size());

        model.addAttribute("people", people);
        model.addAttribute("peopleCount", people.size());

        model.addAttribute("articles", articles);
        model.addAttribute("articleCount", articles.size());

        model.addAttribute("events", events);
        model.addAttribute("eventCount", events.size());

        return "search";
    }
}
