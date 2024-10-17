package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.Job;
import TalentHunt.TalentHunt.model.JobApplication;
import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.service.*;
import TalentHunt.TalentHunt.utils.TimeUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobs")
public class JobController {
    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JobApplicationService jobApplicationService;

    @GetMapping
    public String listJobs(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            // Log error and redirect if user not found
            System.out.println("User not found with email: " + email);
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);

        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);


        // Fetch all jobs
        List<Job> jobs = jobService.getAllJobs();

        // Check if the current user has applied for each job and if the job was posted by the current user
        for (Job job : jobs) {
            boolean hasApplied = jobService.hasUserAppliedForJob(job.getId(), userId);
            boolean isPostedByCurrentUser = jobService.isJobPostedByUser(job.getId(), userId);
            job.setHasApplied(hasApplied);
            job.setCanApply(!isPostedByCurrentUser); // Add this method to your Job model if it does not exist
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("timeUtils", new TimeUtils());

        return "jobs";
    }

    // Step 1: Show form to collect basic job information
    @GetMapping("/post-job/step1")
    public String showStep1Form(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);
        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        model.addAttribute("job", new BatchProperties.Job());
        return "create-job1";
    }

    // Step 1: Handle submission of basic job info
    @PostMapping("/post-job/step1")
    public String submitStep1(@ModelAttribute Job job, HttpSession session) {
        session.setAttribute("tempJob", job);
        return "redirect:/jobs/post-job/step2";
    }

    // Step 2: Show form to collect additional job details
    @GetMapping("/post-job/step2")
    public String showStep2Form(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            System.out.println("User not found with email: " + email);
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);

        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        // Fetch unread messages count
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        Job tempJob = (Job) session.getAttribute("tempJob");
        if (tempJob == null) {
            return "redirect:/jobs/post-job/step1";
        }

        model.addAttribute("job", tempJob);
        return "create-job2";
    }

    // Step 2: Handle submission of full job details and save to the database
    @PostMapping("/post-job/step2")
    public String submitStep2(@ModelAttribute Job job, HttpSession session) {
        Job tempJob = (Job) session.getAttribute("tempJob");
        if (tempJob == null) {
            return "redirect:/jobs/post-job/step1";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);
        // Merge form data from step 2 with session-stored job data from step 1
        tempJob.setUser(user);
        tempJob.setWorkplace(job.getWorkplace());
        tempJob.setLocation(job.getLocation());
        tempJob.setJobType(job.getJobType());
        tempJob.setDescription(job.getDescription());
        tempJob.setSkills(job.getSkills());

        jobService.save(tempJob);
        session.removeAttribute("tempJob");

        return "redirect:/jobs";
    }

    @GetMapping("/details/{id}")
    public String getJobDetails(@PathVariable("id") Long jobId, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);
        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications and messages count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch job details
        Job job = jobService.findById(jobId);
        if (job != null) {
            model.addAttribute("job", job);

            // Check if the user has already applied
            boolean hasApplied = jobService.hasUserAppliedForJob(jobId, userId);
            model.addAttribute("hasApplied", hasApplied);

            List<Job> jobs = jobService.findTop6ByOrderByPostedDateDesc();
            model.addAttribute("jobs", jobs);
            model.addAttribute("timeUtils", new TimeUtils());

            return "job-details";  // Return the template name
        }


        return "redirect:/jobs";
    }


    @GetMapping("/my-items")
    public String getMyJobs(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return "redirect:/signin";
        }

        model.addAttribute("currentUser", user);
        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications and messages count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        // Fetch the list of jobs posted by the user
        List<Job> postedJobs = jobService.getJobsByUserId(userId);
        model.addAttribute("postedJobs", postedJobs);

        // Fetch job applications for each job posted by the user
        Map<Long, List<JobApplication>> jobApplicationsMap = new HashMap<>();
        for (Job job : postedJobs) {
            List<JobApplication> applications = jobApplicationService.getJobApplicationsByJobId(job.getId());
            jobApplicationsMap.put(job.getId(), applications);
        }
        model.addAttribute("jobApplicationsMap", jobApplicationsMap);

        // Fetch the count of jobs posted by the user
        int postedJobsCount = postedJobs.size();
        model.addAttribute("postedJobsCount", postedJobsCount);

        // Fetch job applications made by the current user
        List<JobApplication> jobApplications = jobApplicationService.getJobApplicationsByUserId(user.getUserId());
        model.addAttribute("jobApplications", jobApplications);

        long appliedJobsCount = jobService.countAppliedJobsForUser(userId);
        model.addAttribute("appliedJobsCount", appliedJobsCount);

        model.addAttribute("timeUtils", new TimeUtils());

        return "myjobs";
    }



    // Show the form to edit a job
    @GetMapping("/edit-job/{id}")
    public String showEditJobForm(@PathVariable("id") Long jobId, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return "redirect:/signin";
        }

        Job job = jobService.findById(jobId);
        if (job == null || !job.getUser().equals(user)) {
            return "redirect:/jobs";
        }

        model.addAttribute("currentUser", user);
        Long userId = user.getUserId();
        model.addAttribute("currentUserId", userId);

        // Fetch unread notifications and messages count
        long unreadNotificationsCount = notificationService.getUnreadNotificationsCount(userId);
        long unreadMessagesCount = messageService.getUnreadMessagesCount(userId);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);

        model.addAttribute("job", job);
        return "edit-job";
    }

    // Handle submission of the edited job
    @PostMapping("/edit-job/{id}")
    public String updateJob(@PathVariable("id") Long jobId, @ModelAttribute Job updatedJob, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/signin";
        }

        String email = (String) session.getAttribute("user");
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return "redirect:/signin";
        }

        Job existingJob = jobService.findById(jobId);
        if (existingJob == null || !existingJob.getUser().equals(user)) {
            return "redirect:/jobs";
        }

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setCompany(updatedJob.getCompany());
        existingJob.setWorkplace(updatedJob.getWorkplace());
        existingJob.setLocation(updatedJob.getLocation());
        existingJob.setJobType(updatedJob.getJobType());
        existingJob.setDescription(updatedJob.getDescription());
        existingJob.setSkills(updatedJob.getSkills());

        jobService.save(existingJob);

        return "redirect:/jobs"; // Redirect to the list of jobs or a confirmation page
    }


    @PostMapping("/delete/{id}")
    public String deleteJob(@PathVariable("id") Long jobId) {
        jobService.deleteJob(jobId);
        return "redirect:/jobs"; // Redirect to job list or any other appropriate page
    }

    @PostMapping("/apply")
    public String applyForJob(
            @RequestParam("jobId") Long jobId,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("resume") MultipartFile resumeFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            jobService.applyForJob(jobId, fullName, email, phone, resumeFile);
            redirectAttributes.addFlashAttribute("successMessage", "Your application has been submitted successfully!");
            return "redirect:/jobs";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error submitting your application.");
            return "redirect:/jobs";
        }
    }
}
