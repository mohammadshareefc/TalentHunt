package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    @Autowired
    private UserService userService;

    @GetMapping("/step1")
    public String showStep1(Model model, HttpSession session) {
        // Redirect to homepage if user is already logged in
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        // Initialize and add the new user to the session
        User user = new User();
        session.setAttribute("user", user);
        model.addAttribute("user", user);
        return "signup/step1";
    }

    @PostMapping("/step1")
    public String processStep1(@ModelAttribute User user, HttpSession session, Model model) {
        // Check if the email already exists
        if (userService.isEmailExists(user.getEmail())) {
            model.addAttribute("errorMessage", "Email already exists. Please use a different email.");
            return "signup/step1";
        }

        // Check if the password length is at least 6 characters
        if (user.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "signup/step1";
        }

        // Retrieve existing user data
        User existingUser = (User) session.getAttribute("user");
        if (existingUser == null) {
            existingUser = new User();
        }

        // Update existing user data with new fields
        existingUser.setEmail(user.getEmail()); // Make sure to set the email
        existingUser.setPassword(user.getPassword());

        // Save updated user back to session
        session.setAttribute("user", existingUser);
        return "signup/step2"; // Return the next step's HTML
    }

    @PostMapping("/step2")
    public String processStep2(@ModelAttribute User user, HttpSession session) {
        // Retrieve existing user data
        User existingUser = (User) session.getAttribute("user");
        if (existingUser == null) {
            existingUser = new User();
        }


        // Update existing user data with new fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setGender(user.getGender());
        existingUser.setHeadline(user.getHeadline());
        // Save updated user back to session
        session.setAttribute("user", existingUser);
        return "signup/step3"; // Return the next step's HTML
    }

    @PostMapping("/step3")
    public String processStep3(@ModelAttribute User user, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Retrieve existing user data
        User existingUser = (User) session.getAttribute("user");
        if (existingUser == null) {
            existingUser = new User();
        }
        // Update with remaining fields
        existingUser.setCountry(user.getCountry());
        existingUser.setCity(user.getCity());
        existingUser.setAddress(user.getAddress());

        // Generate and set profile URL slug
        existingUser.setProfileUrlSlug(userService.generateProfileUrlSlug(existingUser.getFirstName(), existingUser.getLastName()));

        try {
            userService.saveUser(existingUser);
            session.removeAttribute("user");
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/signin";
        }  catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed. Please try again.");
            e.printStackTrace(); // Log the stack trace for debugging
            return "signup/step3";
        }
    }


    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean isVerified = userService.verifyToken(token);

        if (isVerified) {
            redirectAttributes.addFlashAttribute("successMessage", "Email verified successfully! You can now log in.");
            return "redirect:/signin";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired token.");
            return "redirect:/register";
        }
    }

}
