package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Job;
import TalentHunt.TalentHunt.model.JobApplication;
import TalentHunt.TalentHunt.repository.JobApplicationRepository;
import TalentHunt.TalentHunt.repository.JobRepository;
import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    public Job save(Job job) {
        job.setPostedDate(LocalDateTime.now());
        return jobRepository.save(job);
    }

    // Fetch all job postings from the repository
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job findById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    public List<Job> findTop6ByOrderByPostedDateDesc() {
        return jobRepository.findTop6ByOrderByPostedDateDesc();
    }

    public List<Job> getJobsByUserId(Long userId) {
        return jobRepository.findByUser_UserId(userId);
    }

    public void deleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    public void applyForJob(Long jobId, String fullName, String email, String phone, MultipartFile resumeFile) throws IOException {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found"));

        JobApplication jobApplication = new JobApplication();

        User currentUser = userService.getUserByEmail(email);

        jobApplication.setUser(currentUser);
        jobApplication.setJob(job);
        jobApplication.setFullName(fullName);
        jobApplication.setEmail(email);
        jobApplication.setPhone(phone);
        jobApplication.setApplicationDate(LocalDateTime.now());

        // Store the resume as a byte array
        jobApplication.setResume(resumeFile.getBytes());

        // Save the job application to the database
        jobApplicationRepository.save(jobApplication);
    }


    public boolean hasUserAppliedForJob(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return false;  // Job not found
        }

        return jobApplicationRepository.existsByJobAndUser(job, userService.getUserById(userId));
    }


    public long countAppliedJobsForUser(Long userId) {
        return jobApplicationRepository.countJobsAppliedByUserId(userId);
    }

    public boolean isJobPostedByUser(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return false; // Job not found
        }
        return job.getUser().getUserId().equals(userId);
    }
}
