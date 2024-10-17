package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Event;
import TalentHunt.TalentHunt.repository.UserRepository;
import TalentHunt.TalentHunt.service.EventService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/events")
    public String getEventPage(HttpSession session, @RequestParam(name="userId",required = false) Long userId, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }

        // Fetch all events from the service
        List<Event>events = eventService.getAllEvents();

        Long userid = currentUser.getUserId();
        model.addAttribute("currentUserId", userid);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", events);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "events";
    }

    @GetMapping("/event-details/{eventId}")
    public String getEventDetailsPage(HttpSession session, @PathVariable("eventId")  Long eventId, Model model) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin";
        }

        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return "redirect:/signin";
        }

        // Fetch all events from the service
        jdk.jfr.Event event = eventService.getEventById(eventId);
        if (event == null) {
            return "redirect:/events";
        }

        Long userid = currentUser.getUserId();
        model.addAttribute("currentUserId", userid);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);

        List<Event>events = eventService.getAllEvents();
        List<Event> limitedEvents = events.size() > 5 ? events.subList(0, 5) : events;
        model.addAttribute("events", limitedEvents);
        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        return "eventdetails";
    }

    @PostMapping("/createEvent")
    public String createEvent(
            @RequestParam("eventName") String eventName,
            @RequestParam("eventDate") LocalDate eventDate,
            @RequestParam("eventTime") LocalTime eventTime,
            @RequestParam("eventMode") String eventMode,
            @RequestParam(value = "eventLink") String eventLink,
            @RequestParam(value = "eventAddress") String eventAddress,
            @RequestParam("eventDescription") String eventDescription,
            @RequestParam("eventImage") MultipartFile eventImage,
            @RequestParam("userId") Long userId, // Assuming you pass the User object or its ID
            RedirectAttributes redirectAttributes
    ) throws IOException {

        //Retrieve the User object using the User ID
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        Event event = new Event();
        event.setEvent_name(eventName);
        event.setDate(eventDate);
        event.setTime(eventTime);
        event.setMode(eventMode);
        event.setLink(eventMode.equals("Online") ? eventLink : null);
        event.setAddress(eventMode.equals("Offline") ? eventAddress : null);
        event.setDescription(eventDescription);
        event.setImage(eventImage.getBytes());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());


        // Set the event link or address based on the mode
        if ("Online".equals(eventMode)) {
            event.setLink(eventLink);
        } else if ("Offline".equals(eventMode)) {
            event.setAddress(eventAddress);
        }

        // Handle image upload
        try {
            if (!eventImage.isEmpty()) {
                event.setImage(eventImage.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to upload image. Please try again.");
            return "redirect:/events"; // Redirect back to the form
        }

        // Save the event
        try {
            eventService.createEvent(event);

            // Notify all connections about the new event
            notificationService.notifyConnectionsOfNewEvent(event);
            redirectAttributes.addFlashAttribute("message", "Event created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create event. Please try again.");
        }

        return "redirect:/events"; // Redirect to the homepage after posting
    }
}
