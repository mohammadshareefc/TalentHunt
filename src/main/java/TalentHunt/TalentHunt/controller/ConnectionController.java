package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.service.ConnectionService;
import TalentHunt.TalentHunt.service.NotificationService;
import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    @Controller
    @RequestMapping("/connect")
    public class ConnectionController {

        @Autowired
        private UserService userService;

        @Autowired
        private ConnectionService connectionService;

        @Autowired
        private NotificationService notificationService;

        @RequestMapping("/{userId}")
        public String connect(@PathVariable("userId") Long userId, HttpSession session, RedirectAttributes redirectAttributes, @RequestHeader(value = "referer", required = false) String referer) {
            String email = (String) session.getAttribute("user");
            if (email == null) {
                return "redirect:/signin";
            }

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return "redirect:/signin";
            }

            User targetUser = userService.getUserById(userId);
            if (targetUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return referer != null ? "redirect:" + referer : "redirect:/networks";
            }

            String result = connectionService.connect(currentUser.getUserId(), userId);
            if ("Request sent".equals(result)) {
                notificationService.notifyUserOfConnectionRequest(currentUser, targetUser);
                redirectAttributes.addFlashAttribute("successMessage", "Connection request sent successfully!");
            } else if ("Pending".equals(result)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Connection request is already pending.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to send connection request.");
            }

            return referer != null ? "redirect:" + referer : "redirect:/networks";
        }


        @PostMapping("/withdraw")
        public String withdraw(@RequestParam("userId") Long userId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(value = "referer", required = false) String referer) {
            String email = (String) session.getAttribute("user");
            if (email == null) {
                return "redirect:/signin";
            }

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return "redirect:/signin";
            }

            String result = connectionService.withdraw(currentUser.getUserId(), userId);
            if ("Withdrawn".equals(result)) {
                redirectAttributes.addFlashAttribute("successMessage", "Connection request withdrawn successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to withdraw connection request.");
            }

            return referer != null ? "redirect:" + referer : "redirect:/networks";
        }


        @PostMapping("/accept/{requestId}")
        public String acceptRequest(@PathVariable("requestId") Long requestId, HttpSession session, RedirectAttributes redirectAttributes) {
            String email = (String) session.getAttribute("user");
            if (email == null) {
                return "redirect:/signin";
            }

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return "redirect:/signin";
            }

            String result = connectionService.acceptRequest(requestId);
            if ("Accepted".equals(result)) {
                User requester = connectionService.getRequesterByRequestId(requestId);
                notificationService.notifyUserOfRequestAcceptance(currentUser, requester);
                redirectAttributes.addFlashAttribute("successMessage", "Connection request accepted.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to accept connection request.");
            }

            return "redirect:/networks/connections";
        }

        @PostMapping("/decline/{requestId}")
        public String declineRequest(@PathVariable("requestId")  Long requestId, HttpSession session, RedirectAttributes redirectAttributes) {
            String email = (String) session.getAttribute("user");
            if (email == null) {
                return "redirect:/signin";
            }

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return "redirect:/signin";
            }

            String result = connectionService.declineRequest(requestId);
            if ("Declined".equals(result)) {
                User requester = connectionService.getRequesterByRequestId(requestId);
                notificationService.notifyUserOfRequestDecline(currentUser, requester);
                redirectAttributes.addFlashAttribute("successMessage", "Connection request declined.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to decline connection request.");
            }
            return "redirect:/networks";
        }



        @PostMapping("/remove")
        public String removeConnection(@RequestParam("connectionId") Long connectionId, HttpSession session, RedirectAttributes redirectAttributes) {
            String email = (String) session.getAttribute("user");
            if (email == null) {
                return "redirect:/signin";
            }

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return "redirect:/signin";
            }

            String result = connectionService.removeConnection(currentUser.getUserId(), connectionId);
            if ("Removed".equals(result)) {
                redirectAttributes.addFlashAttribute("successMessage", "Connection removed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove connection.");
            }
            return "redirect:/networks/connections";
        }


    }


