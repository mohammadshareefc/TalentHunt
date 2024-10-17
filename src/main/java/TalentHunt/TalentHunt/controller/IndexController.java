package TalentHunt.TalentHunt.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class IndexController {
    @GetMapping("/")
    public String home(HttpSession session) {
        // Redirect to homepage if user is already logged in
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        return "index";
    }

    @GetMapping("/about")
    public String about(HttpSession session) {
        // Redirect to about if user is already logged in
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        return "about";
    }

    @GetMapping("/contact")
    public String contact(HttpSession session) {
        // Redirect to contact if user is already logged in
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        return "contact";
    }

    @GetMapping("/signup")
    public String signup() {
        return "redirect:/register/step1";
    }

}
