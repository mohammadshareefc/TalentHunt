package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Contact;
import TalentHunt.TalentHunt.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class ContactController {
    @Autowired
    private ContactService contactMessageService;

    @PostMapping("/contact")
    public String submitContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {

        Contact contactMessage = new Contact();
        contactMessage.setName(name);
        contactMessage.setEmail(email);
        contactMessage.setSubject(subject);
        contactMessage.setMessage(message);

        contactMessageService.saveContactMessage(contactMessage);
        redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully!");
        return "redirect:/contact"; // Redirect to the contact page or any other page
    }
}
