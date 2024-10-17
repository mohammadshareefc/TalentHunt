package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/update")
    public String updateUser(
            @RequestParam("userId") Long userId,
            @RequestParam("profileImage") MultipartFile profileImage,
            @RequestParam("backgroundImage") MultipartFile backgroundImage,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "pronouns", required = false) String pronouns,
            @RequestParam(value = "headline", required = false) String headline,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "birthday", required = false) String birthday,
            @RequestParam(value = "buttonName", required = false) String buttonName,
            @RequestParam(value = "url", required = false) String url,
            RedirectAttributes redirectAttributes) {

        try {
            // Retrieve the current user from the database
            TalentHunt.TalentHunt.model.User existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/profile";
            }

            // Update user details
            existingUser.setFirstName(firstName);
            existingUser.setLastName(lastName);
            existingUser.setPronouns(pronouns);
            existingUser.setHeadline(headline);
            existingUser.setCountry(country);
            existingUser.setCity(city);
            existingUser.setEmail(email);
            existingUser.setPhoneNumber(phoneNumber);
            existingUser.setAddress(address);
            existingUser.setBirthday(birthday);
            existingUser.setButtonName(buttonName);
            existingUser.setUrl(url);

            // Handle profile image upload
            if (!profileImage.isEmpty()) {
                existingUser.setProfileImage(profileImage.getBytes());
            }

            // Handle background image upload
            if (!backgroundImage.isEmpty()) {
                existingUser.setBackgroundImage(backgroundImage.getBytes());
            }

            // Encrypt password if changed
            // Note: Handle password separately if you have a password change mechanism

            // Save the updated user
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile.");
        }

        return "redirect:/profile"; // Redirect to the profile page after updating
    }

    @GetMapping("/suggestedConnections")
    public String showSuggestedConnections(Model model, HttpSession session) {
        String email = (String) session.getAttribute("user");
        if (email == null) {
            return "redirect:/signin"; // Redirect if not logged in
        }

        User currentUser = userService.getUserByEmail(email);
        List<User> suggestedUsers = userService.getAllUsersExceptCurrentUser(currentUser.getUserId());

        model.addAttribute("suggestedUsers", suggestedUsers);

        return "suggestedConnections"; // Name of the view
    }


    @PostMapping("/update-about")
    public String updateAbout(@RequestParam("userId") Long userId,
                              @RequestParam("about") String about) {
        User user = userService.getUserById(userId);
        user.setAbout(about);
        userService.saveUser(user);
        return "redirect:/profile";  // Redirect back to the profile page
    }

}
