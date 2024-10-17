package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.*;
import TalentHunt.TalentHunt.service.*;
import TalentHunt.TalentHunt.utils.TimeUtils;
import jakarta.servlet.http.HttpSession;
import TalentHunt.TalentHunt.model.Event;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private FollowService followService;

    @Autowired
    private PostService postService;

    @Autowired
    private ExperienceService experienceService;

    @Autowired
    private EducationService educationService;

    @Autowired
    private SkillService skillService;
    @Autowired
    private EventService eventService;
    @Autowired
    private ArticleService articleService;

    @GetMapping
    public String viewProfile(@RequestParam(name="userId",required = false) Long userId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        if (user != null) {


            Long userid = user.getUserId();
            model.addAttribute("currentUserId", userid);
            // Fetch unread messages count
            long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
            model.addAttribute("unreadMessagesCount", unreadMessagesCount);

            // Fetch unread notifications count
            long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
            model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

            // Fetch connection count
            int connectionCount = connectionService.getConnectionCount(user.getUserId());
            model.addAttribute("connectionCount", connectionCount);

            long followersCount = followService.countFollowers(userid);;
            model.addAttribute("followersCount", followersCount);

            List<Post> userPosts = postService.getPostsByUserId(user.getUserId());
            userPosts.forEach(post -> post.setDescription(postService.getTruncatedDescription(post.getDescription(), 30)));

            // Add data to the model
            model.addAttribute("currentUser", user);
            model.addAttribute("posts", userPosts);
            model.addAttribute("timeUtils", new TimeUtils());

            // Fetch experiences
            List<Experience> experiences = experienceService.getExperiencesByUserId(user.getUserId());
            model.addAttribute("experiences", experiences);

            // Fetch educations
            List<Education> educations = educationService.getEducationsByUserId(user.getUserId());
            model.addAttribute("educations", educations);

            // Fetch skills
            List<Skill> skills = skillService.getSkillsByUserId(user.getUserId());
            model.addAttribute("skills", skills);

            return "profile";

        }
        return "redirect:/404"; // or any error page
    }

    @GetMapping({"/", "/{slug}"})
    public String viewProfileBySlug(
            @PathVariable(name = "slug", required = false) String slug,
            @RequestParam(name = "userId", required = false) Long userId,
            HttpSession session,
            Model model
    ) {
        // Check if the user is logged in
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User currentUser = userService.getUserByEmail(email);

        if (currentUser == null) {
            return "redirect:/404"; // Handle user not found
        }

        User profileUser = null;

        if (slug != null) {
            // Fetch profile user by slug
            profileUser = userService.getUserByProfileUrlSlug(slug);
        } else if (userId != null) {
            // Fetch profile user by userId
            profileUser = userService.getUserById(userId);
        } else {
            return "redirect:/404"; // Handle invalid request
        }

        if (profileUser == null) {
            return "redirect:/404"; // Handle profile user not found
        }

        boolean isOwner = currentUser.getUserId().equals(profileUser.getUserId());

        // Set profile user and current user in model
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", currentUser);

        Long profileUserId = profileUser.getUserId();
        Long currentUserId = currentUser.getUserId();

        // Fetch unread messages count for the profile user
        long unreadMessagesCount = messageService.getUnreadMessagesCount(currentUserId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch unread notifications count for the profile user
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(currentUserId);	    	    model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch connection count for the profile user
        int connectionCount = connectionService.getConnectionCount(profileUserId);
        model.addAttribute("connectionCount", connectionCount);

        // Additional profile details
        List<Post> userPosts = postService.getPostsByUserId(profileUserId);
        userPosts.forEach(post -> post.setDescription(postService.getTruncatedDescription(post.getDescription(), 30)));
        model.addAttribute("posts", userPosts);

        long followersCount = followService.countFollowers(profileUserId);;
        model.addAttribute("followersCount", followersCount);

        // Fetch experiences
        List<Experience> experiences = experienceService.getExperiencesByUserId(profileUserId);
        model.addAttribute("experiences", experiences);

        // Fetch educations
        List<Education> educations = educationService.getEducationsByUserId(profileUserId);
        model.addAttribute("educations", educations);

        // Fetch skills
        List<Skill> skills = skillService.getSkillsByUserId(profileUserId);
        model.addAttribute("skills", skills);


        model.addAttribute("isOwner", isOwner);
        model.addAttribute("timeUtils", new TimeUtils());

        return "userprofile"; // Return the view for the profile page
    }

    @PostMapping("/saveExperience")
    public String saveExperience(
            @RequestParam("title") String title,
            @RequestParam("company") String company,
            @RequestParam("employment") String employment,
            @RequestParam("location") String location,
            @RequestParam("locationtype") String locationType,
            @RequestParam("startdate") String startDate,
            @RequestParam(value = "enddate", required = false) String endDate,
            HttpSession session,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        // Convert dates from String to LocalDate
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Experience experience = new Experience();
            experience.setTitle(title);
            experience.setCompany(company);
            experience.setEmploymentType(employment);
            experience.setLocation(location);
            experience.setLocationType(locationType);
            experience.setStartDate(start);
            experience.setEndDate(end);
            experience.setUser(user);

            experienceService.saveExperience(experience);

            redirectAttributes.addFlashAttribute("message", "Experience added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        // Redirect to the previous page
        return referer != null ? "redirect:" + referer : "redirect:/profile";
    }


    @PostMapping("/addEducation")
    public String saveEducation(
            @RequestParam("degree") String degree,
            @RequestParam("school") String institution,
            @RequestParam("field") String fieldOfStudy,
            @RequestParam("percentage") String percentage,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpSession session,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        // Convert dates from String to LocalDate
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Education education = new Education();
            education.setDegree(degree);
            education.setSchool(institution);
            education.setField(fieldOfStudy);
            education.setPercentage(percentage);
            education.setDescription(description);
            education.setStartDate(start);
            education.setEndDate(end);
            education.setUser(user);

            educationService.saveEducation(education);

            redirectAttributes.addFlashAttribute("message", "Education added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        // Redirect to the previous page
        return referer != null ? "redirect:" + referer : "redirect:/profile";
    }


    @PostMapping("/addSkill")
    public String saveSkill(
            @RequestParam("skill") String skillName,
            HttpSession session,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Skill skill = new Skill();
            skill.setSkill(skillName);
            skill.setUser(user);

            skillService.saveSkill(skill);

            redirectAttributes.addFlashAttribute("message", "Skill added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        // Redirect to the previous page
        return referer != null ? "redirect:" + referer : "redirect:/profile";
    }



    @GetMapping("/details/experience")
    public String viewDetailExperience(@RequestParam(name="userId",required = false) Long userId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        if (user != null) {


            Long userid = user.getUserId();
            model.addAttribute("currentUserId", userId);
            // Fetch unread messages count
            long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
            model.addAttribute("unreadMessagesCount", unreadMessagesCount);

            // Fetch unread notifications count
            long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
            model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

            // Add data to the model
            model.addAttribute("currentUser", user);


            // Fetch experiences
            List<Experience> experiences = experienceService.getExperiencesByUserId(user.getUserId());
            model.addAttribute("experiences", experiences);


            return "experience";

        }
        return "redirect:/404"; // or any error page
    }


    @PostMapping("/updateExperience")
    public String updateExperience(
            @RequestParam("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("company") String company,
            @RequestParam("employment") String employment,
            @RequestParam("location") String location,
            @RequestParam("locationtype") String locationType,
            @RequestParam("startdate") String startDate,
            @RequestParam(value = "enddate", required = false) String endDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Convert dates from String to LocalDate
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Experience experience = experienceService.getExperienceById(id);
            if (experience != null) {
                experience.setTitle(title);
                experience.setCompany(company);
                experience.setEmploymentType(employment);
                experience.setLocation(location);
                experience.setLocationType(locationType);
                experience.setStartDate(start);
                experience.setEndDate(end);
                experience.setUser(user);

                experienceService.saveExperience(experience);

                redirectAttributes.addFlashAttribute("message", "Experience updated successfully!");
                return "redirect:/profile/details/experience";
            }

            redirectAttributes.addFlashAttribute("error", "Experience not found!");
            return "redirect:/profile/details/experience";
        }

        redirectAttributes.addFlashAttribute("error", "User not found!");
        return "redirect:/profile/details/experience";
    }

    @PostMapping("/deleteExperience")
    public String deleteExperience(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {

        experienceService.deleteExperience(id);

        redirectAttributes.addFlashAttribute("message", "Experience deleted successfully!");
        return "redirect:/profile/details/experience";
    }


    @GetMapping("/details/education")
    public String viewDetailEducation(@RequestParam(name="userId",required = false) Long userId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        if (user != null) {


            Long userid = user.getUserId();
            model.addAttribute("currentUserId", userId);
            // Fetch unread messages count
            long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
            model.addAttribute("unreadMessagesCount", unreadMessagesCount);

            // Fetch unread notifications count
            long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
            model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

            // Add data to the model
            model.addAttribute("currentUser", user);

            // Fetch educations
            List<Education> educations = educationService.getEducationsByUserId(user.getUserId());
            model.addAttribute("educations", educations);


            return "education";


        }
        return "redirect:/404"; // or any error page
    }

    @PostMapping("/updateEducation")
    public String updateEducation(
            @RequestParam("id") Long id,
            @RequestParam("degree") String degree,
            @RequestParam("school") String institution,
            @RequestParam("field") String fieldOfStudy,
            @RequestParam("percentage") String percentage,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Convert dates from String to LocalDate
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Education education = educationService.getEducationById(id);
            if (education != null) {
                education.setDegree(degree);
                education.setSchool(institution);
                education.setField(fieldOfStudy);
                education.setPercentage(percentage);
                education.setDescription(description);
                education.setStartDate(start);
                education.setEndDate(end);
                education.setUser(user);

                educationService.saveEducation(education);

                redirectAttributes.addFlashAttribute("message", "education updated successfully!");
                return "redirect:/profile/details/education";
            }

            redirectAttributes.addFlashAttribute("error", "education not found!");
            return "redirect:/profile/details/education";
        }

        redirectAttributes.addFlashAttribute("error", "User not found!");
        return "redirect:/profile/details/education";
    }

    @PostMapping("/deleteEducation")
    public String deleteEducation(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {

        educationService.deleteEducation(id);

        redirectAttributes.addFlashAttribute("message", "education deleted successfully!");
        return "redirect:/profile/details/education";
    }




    @GetMapping("/details/skill")
    public String viewDetailSkill(@RequestParam(name="userId",required = false) Long userId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        if (user != null) {


            Long userid = user.getUserId();
            model.addAttribute("currentUserId", userId);
            // Fetch unread messages count
            long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
            model.addAttribute("unreadMessagesCount", unreadMessagesCount);

            // Fetch unread notifications count
            long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
            model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

            // Add data to the model
            model.addAttribute("currentUser", user);



            List<Skill> skills = skillService.getSkillsByUserId(user.getUserId());
            model.addAttribute("skills", skills);


            return "skill";

        }
        return "redirect:/404"; // or any error page
    }

    @PostMapping("/updateSkills")
    public String updateSkills(
            @RequestParam("id") Long id,
            @RequestParam("skill") String skills,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Retrieve the current user from the session
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            Skill skill = skillService.getskillById(id);
            if (skill != null) {
                skill.setSkill(skills);
                skill.setUser(user);

                skillService.saveSkill(skill);

                redirectAttributes.addFlashAttribute("message", "skill updated successfully!");
                return "redirect:/profile/details/skill";
            }

            redirectAttributes.addFlashAttribute("error", "skill not found!");
            return "redirect:/profile/details/skill";
        }

        redirectAttributes.addFlashAttribute("error", "User not found!");
        return "redirect:/profile/details/education";
    }

    @PostMapping("/deleteSkill")
    public String deleteSkill(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {

        skillService.deleteskill(id);

        redirectAttributes.addFlashAttribute("message", "Skill deleted successfully!");
        return "redirect:/profile/details/skill";
    }

    @GetMapping("/details/posts")
    public String viewDetailPost(@RequestParam(name="userId",required = false) Long userId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }
        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        if (user != null) {
            Long userid = user.getUserId();
            model.addAttribute("currentUserId", userId);
            // Fetch unread messages count
            long unreadMessagesCount = messageService.getUnreadMessagesCount(userid);
            model.addAttribute("unreadMessagesCount", unreadMessagesCount);

            // Fetch unread notifications count
            long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userid);
            model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

            // Add data to the model
            model.addAttribute("currentUser", user);

            long followersCount = followService.countFollowers(userid);;
            model.addAttribute("followersCount", followersCount);

            List<Post> userPosts = postService.getPostsByUserId(userid);
            userPosts.forEach(post -> post.setDescription(postService.getTruncatedDescription(post.getDescription(), 30)));
            model.addAttribute("posts", userPosts);

            // Fetch events created by the user
            List<Event> userEvents = eventService.getEventsByUserId(userid);
            model.addAttribute("events", userEvents);

            // Fetch articles created by the user
            List<Article> userArticles = articleService.getArticlesByUserId(userid);
            model.addAttribute("articles", userArticles);

            model.addAttribute("timeUtils", new TimeUtils());

            return "posts";

        }
        return "redirect:/404"; // or any error page
    }

}
