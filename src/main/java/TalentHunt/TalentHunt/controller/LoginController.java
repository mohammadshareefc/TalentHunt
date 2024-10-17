package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.service.UserService;
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

@Controller
@RequestMapping("/signin")
public class LoginController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String showSignIn(HttpSession session, Model model) {
        // Redirect to homepage if user is already logged in
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        return "signin";
    }

    @PostMapping
    public String loginUser(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(email);

            // Check if user exists
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/signin";
            }

            // Check if the email is verified
            if (!user.isEnabled()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email is not verified. Please verify your email to login.");
                return "redirect:/signin";
            }

            // Authenticate the user (password check)
            if (userService.authenticate(email, password)) {
                session.setAttribute("user", email); // Store user info in session
                return "redirect:/home"; // Redirect to the homepage on successful login
            } else {
                // If authentication fails, redirect to sign-in page with error message
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid Email or Password.");
                return "redirect:/signin";
            }
        } catch (Exception e) {
            // Handle exception and redirect to sign-in page with error message
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred. Please try again!");
            return "redirect:/signin";
        }
    }
}
