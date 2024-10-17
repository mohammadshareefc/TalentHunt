package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.JobApplication;
import TalentHunt.TalentHunt.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    public List<JobApplication> getJobApplicationsByUserId(Long userId) {
        return jobApplicationRepository.findByUser_UserId(userId);
    }

    public List<JobApplication> getJobApplicationsByJobId(Long jobId) {
        return jobApplicationRepository.findByJob_Id(jobId);
    }

    // Add this method to fetch JobApplication by its ID
    public JobApplication getJobApplicationById(Long applicationId) {
        return jobApplicationRepository.findById(applicationId).orElse(null);
    }
}
