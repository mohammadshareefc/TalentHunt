package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Message;
import TalentHunt.TalentHunt.service.MessageService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/markAsRead")
    public String markAsRead(@RequestParam("messageId") Long messageId, @RequestParam("userId") Long userId, RedirectAttributes redirectAttributes) {
        messageService.markMessageAsRead(messageId);
        return "redirect:/messages?userId=" + userId;
    }

    @GetMapping
    public String getMessagePage(HttpSession session, @RequestParam(name = "userId", required=false) Long userId, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        Long userid = currentUser.getUserId();
        model.addAttribute("currentUserId", userid);

        List<User> connections = userService.getConnectionsForUser(currentUser.getUserId());
        connections.remove(currentUser);

        Map<User, String> lastMessages = new HashMap<>();
        Map<User, String> lastMessageFormattedTimestamps = new HashMap<>();

        for (User connection : connections) {
            Message lastMessage = messageService.getLastMessageBetweenUsers(currentUser, connection);
            String displayMessage;
            String formattedTimestamp = "";
            if (lastMessage != null) {
                formattedTimestamp = lastMessage.getFormattedTimestamp();
                if (lastMessage.getSender().getUserId().equals(currentUser.getUserId())) {
                    displayMessage = "You: " + lastMessage.getContent();
                } else {
                    displayMessage = connection.getFirstName() + ": " + lastMessage.getContent();
                }
            } else {
                displayMessage = "No messages yet";
            }
            lastMessages.put(connection, displayMessage);
            lastMessageFormattedTimestamps.put(connection, formattedTimestamp);
        }

        Map<User, Message> recentUnreadMessages = new HashMap<>();
        List<Message> unreadMessages = messageService.getUnreadMessagesForUser(currentUser);
        for (Message message : unreadMessages) {
            User sender = message.getSender();
            if (!recentUnreadMessages.containsKey(sender) || message.getTimestamp().isAfter(recentUnreadMessages.get(sender).getTimestamp())) {
                recentUnreadMessages.put(sender, message);
            }
        }

        int unreadCount = unreadMessages.size();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("connections", connections);
        model.addAttribute("lastMessages", lastMessages);
        model.addAttribute("lastMessageFormattedTimestamps", lastMessageFormattedTimestamps);
        model.addAttribute("unreadMessages", recentUnreadMessages.values());
        model.addAttribute("unreadCount", unreadCount);

        if (userId != null) {
            User selectedUser = userService.getUserById(userId);
            if (selectedUser != null) {
                Message lastMessage = messageService.getLastMessageBetweenUsers(currentUser, selectedUser);
                model.addAttribute("selectedUser", selectedUser);
                model.addAttribute("lastMessage", lastMessage);
                List<Message> messages;
                messages = messageService.getMessagesBetweenUsers(currentUser, selectedUser);
                model.addAttribute("messages", messages);
            }
        } else {
            model.addAttribute("selectedUser", null);
        }

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "message";
    }

    @GetMapping("/{userId}")
    public String getChatPage(HttpSession session, @RequestParam(name = "userId", required=false) Long userId, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        Long userid = currentUser.getUserId();
        model.addAttribute("currentUserId", userid);

        List<User> connections = userService.getConnectionsForUser(currentUser.getUserId());
        connections.remove(currentUser);

        Map<User, String> lastMessages = new HashMap<>();
        Map<User, String> lastMessageFormattedTimestamps = new HashMap<>();

        for (User connection : connections) {
            Message lastMessage = messageService.getLastMessageBetweenUsers(currentUser, connection);
            String displayMessage;
            String formattedTimestamp = "";
            if (lastMessage != null) {
                formattedTimestamp = lastMessage.getFormattedTimestamp();
                if (lastMessage.getSender().getUserId().equals(currentUser.getUserId())) {
                    displayMessage = "You: " + lastMessage.getContent();
                } else {
                    displayMessage = connection.getFirstName() + ": " + lastMessage.getContent();
                }
            } else {
                displayMessage = "No messages yet";
            }
            lastMessages.put(connection, displayMessage);
            lastMessageFormattedTimestamps.put(connection, formattedTimestamp);
        }

        Map<User, Message> recentUnreadMessages = new HashMap<>();
        List<Message> unreadMessages = messageService.getUnreadMessagesForUser(currentUser);
        for (Message message : unreadMessages) {
            User sender = message.getSender();
            if (!recentUnreadMessages.containsKey(sender) || message.getTimestamp().isAfter(recentUnreadMessages.get(sender).getTimestamp())) {
                recentUnreadMessages.put(sender, message);
            }
        }

        int unreadCount = unreadMessages.size();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("connections", connections);
        model.addAttribute("lastMessages", lastMessages);
        model.addAttribute("lastMessageFormattedTimestamps", lastMessageFormattedTimestamps);
        model.addAttribute("unreadMessages", recentUnreadMessages.values());
        model.addAttribute("unreadCount", unreadCount);

        if (userId != null) {
            User selectedUser = userService.getUserById(userId);
            if (selectedUser != null) {
                Message lastMessage = messageService.getLastMessageBetweenUsers(currentUser, selectedUser);
                model.addAttribute("selectedUser", selectedUser);
                model.addAttribute("lastMessage", lastMessage);
                List<Message> messages = messageService.getMessagesBetweenUsers(currentUser, selectedUser);
                model.addAttribute("messages", messages);
            }
        } else {
            model.addAttribute("selectedUser", null);
        }

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "message";
    }

    @PostMapping("/send")
    public String sendMessage(HttpSession session, HttpServletRequest request, @RequestParam("receiverId") Long receiverId, @RequestParam("messageContent") String messageContent, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("user");
        User sender = userService.getUserByEmail(email);
        User receiver = userService.getUserById(receiverId);

        if (sender == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to send messages.");
            return "redirect:/signin";
        }

        if (receiver == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid recipient user.");
            return "redirect:/messages";
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageContent);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        messageService.saveMessage(message);

        // Capture the current URL and append the receiverId
        String currentUrl = request.getRequestURL().toString() + "?userId=" + receiverId;
        return "redirect:" + currentUrl;
    }

    @PostMapping("/deleteAll")
    public String deleteAllMessages(HttpSession session, @RequestParam("targetUserId") Long targetUserId, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("user");
        User currentUser = userService.getUserByEmail(email);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform this action.");
            return "redirect:/signin";
        }

        User targetUser = userService.getUserById(targetUserId);
        if (targetUser == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/messages";
        }

        // Assuming the current user has the right to delete messages for the target user
        messageService.deleteMessagesForUser(currentUser, targetUser);
        redirectAttributes.addFlashAttribute("message", "All messages with this user have been deleted for you.");
        return "redirect:/messages";
    }


    @GetMapping("/markAsUnread")
    public String markAsUnread(@RequestParam("messageId") Long messageId, @RequestParam("userId") Long userId, RedirectAttributes redirectAttributes) {
        messageService.markMessageAsUnread(messageId);
        redirectAttributes.addFlashAttribute("message", "Message marked as unread.");
        return "redirect:/messages?userId=" + userId;
    }

    @PostMapping("/markAllAsUnread")
    public String markAllMessagesAsUnread(HttpSession session, @RequestParam("targetUserId") Long targetUserId, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("user");
        User currentUser = userService.getUserByEmail(email);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform this action.");
            return "redirect:/signin";
        }

        User targetUser = userService.getUserById(targetUserId);
        if (targetUser == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/messages";
        }

        messageService.markAllMessagesAsUnread(currentUser, targetUser);
        redirectAttributes.addFlashAttribute("message", "All messages with this user have been marked as unread.");
        return "redirect:/messages?userId=" + targetUserId;
    }

}
