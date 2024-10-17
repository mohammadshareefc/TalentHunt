package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Notification;
import TalentHunt.TalentHunt.service.MessageService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    // Get all notifications for the current user
    @GetMapping
    public String getNotifications(HttpSession session, Model model) {
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
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUser", user); // Assumes method exists to get current logged-in user
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        // Add formatted time for each notification
        notifications.forEach(notification ->
                notification.setFormattedTime(notificationService.getFormattedNotificationTime(notification.getTimestamp()))
        );
        model.addAttribute("notifications", notifications);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "notification"; // Thymeleaf template
    }

    @PostMapping("/mark-as-read/{id}")
    public String markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications"; // Adjust the redirect URL as needed
    }

    @PostMapping("/mark-as-unread/{id}")
    public String markAsUnread(@PathVariable("id") Long id) {
        notificationService.markAsUnread(id);
        return "redirect:/notifications"; // Adjust the redirect URL as needed
    }

    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable("id") Long id) {
        notificationService.deleteById(id);
        return "redirect:/notifications"; // Adjust the redirect URL as needed
    }
}
