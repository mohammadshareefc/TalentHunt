package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Connection;
import TalentHunt.TalentHunt.service.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/networks")
public class NetworkController {
    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowService followService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public String viewNetwork(HttpSession session, Model model) {

        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        // Fetch the current user
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }

        Long userId = currentUser.getUserId();
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUser", currentUser);

        // Fetch connection count
        int connectionCount = connectionService.getConnectionCount(currentUser.getUserId());
        model.addAttribute("connectionCount", connectionCount);

        // Fetch all users except the current user
        List<User> allUsers = userService.getAllUsers();
        List<User> otherUsers = allUsers.stream()
                .filter(user -> !user.getEmail().equals(currentUser.getEmail()))
                .collect(Collectors.toList());

        // Create a map of userId to Connection
        Map<Long, Connection> userConnectionMap = new HashMap<>();
        for (User user : otherUsers) {
            Connection connection = connectionService.getConnectionStatus(currentUser.getUserId(), user.getUserId());
            if (connection != null) {
                userConnectionMap.put(user.getUserId(), connection);
            }
        }
        model.addAttribute("userConnectionMap", userConnectionMap);

        // Fetch connection requests for the current user
        List<Connection> incomingRequests = connectionService.getIncomingRequests(currentUser.getUserId());
        List<Connection> outgoingRequests = connectionService.getOutgoingRequests(currentUser.getUserId());

        // Limit incoming requests to 4
        List<Connection> limitedIncomingRequests = incomingRequests.stream()
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("incomingRequests", limitedIncomingRequests);
        model.addAttribute("outgoingRequests", outgoingRequests);

        // Exclude users with incoming requests or accepted connections
        Set<Long> excludedUserIds = incomingRequests.stream()
                .map(connection -> connection.getUser1().getUserId())
                .collect(Collectors.toSet());

        excludedUserIds.addAll(userConnectionMap.entrySet().stream()
                .filter(entry -> "accepted".equals(entry.getValue().getStatus()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));

        // Exclude users where the current user has sent a request and it's in the received section for the other user
        excludedUserIds.addAll(outgoingRequests.stream()
                .filter(connection -> "received".equals(connection.getStatus()))
                .map(connection -> connection.getUser2().getUserId())
                .collect(Collectors.toSet()));

        // Exclude users who have an accepted connection with the current user
        List<Connection> acceptedConnections = connectionService.getAcceptedConnections(currentUser.getUserId());
        excludedUserIds.addAll(acceptedConnections.stream()
                .map(connection -> connection.getUser1().getUserId().equals(currentUser.getUserId()) ? connection.getUser2().getUserId() : connection.getUser1().getUserId())
                .collect(Collectors.toSet()));

        List<User> filteredOtherUsers = otherUsers.stream()
                .filter(user -> !excludedUserIds.contains(user.getUserId()))
                .collect(Collectors.toList());

        model.addAttribute("filteredOtherUsers", filteredOtherUsers);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        long eventCount = eventService.getEventCount();
        model.addAttribute("eventCount", eventCount);

        long articleCount = articleService.countArticles();
        model.addAttribute("articleCount", articleCount);

        return "networks";
    }



    @GetMapping("/invitation-manager")
    public String showInvitaion(HttpSession session, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        // Fetch the current user
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        Long userId = currentUser.getUserId();
        model.addAttribute("currentUser", currentUser);

        // Fetch connection requests for the current user
        List<Connection> incomingRequests = connectionService.getIncomingRequests(currentUser.getUserId());
        List<Connection> outgoingRequests = connectionService.getOutgoingRequests(currentUser.getUserId());

        model.addAttribute("incomingRequests", incomingRequests);
        model.addAttribute("outgoingRequests", outgoingRequests);

        model.addAttribute("incomingRequestCount", incomingRequests.size());
        model.addAttribute("outgoingRequestCount", outgoingRequests.size());

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);


        return "invitation-manage";
    }

    @GetMapping("/connections")
    public String viewConnections(HttpSession session, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        // Fetch the current user
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        Long userId = currentUser.getUserId();
        model.addAttribute("currentUser", currentUser);

        // Fetch connection count
        int connectionCount = connectionService.getConnectionCount(currentUser.getUserId());
        model.addAttribute("connectionCount", connectionCount);

        // Fetch accepted connections where the current user is either user1 or user2
        List<Connection> connections = connectionService.getAcceptedConnections(currentUser.getUserId());

        model.addAttribute("connections", connections);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "connections";
    }

    @PostMapping("/follow")
    public String followUser(@RequestParam("targetUserId") Long targetUserId, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }

        String result = followService.followUser(currentUser.getUserId(), targetUserId);
        if ("Followed".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "User followed successfully!");
        } else if ("Unfollowed".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "User unfollowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to follow/unfollow user.");
        }
        return "redirect:/home";
    }

    @GetMapping("/people-follow/following")
    public String viewFollowers(HttpSession session, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        // Fetch the current user
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }
        Long userId = currentUser.getUserId();
        model.addAttribute("currentUser", currentUser);

        List<User> following = followService.getFollowing(userId);
        List<User> followers = followService.getFollowers(userId);
        long followingCount = followService.countFollowing(userId);
        long followersCount = followService.countFollowers(userId);

        model.addAttribute("following", following);
        model.addAttribute("followers", followers);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("followersCount", followersCount);


        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "followers";
    }
}
