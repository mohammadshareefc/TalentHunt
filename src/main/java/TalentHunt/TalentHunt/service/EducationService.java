package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Education;
import TalentHunt.TalentHunt.repository.EducationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EducationService {
    @Autowired
    private EducationRepository educationRepository;

    public void saveEducation(Education education) {
        educationRepository.save(education);
    }

    public List<Education> getEducationsByUserId(Long userId) {
        return educationRepository.findByUser_UserId(userId);
    }


    // Add method to get Education by ID
    public Education getEducationById(Long id) {
        return educationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education not found with id: " + id));
    }

    // Add method to delete Education by ID
    public void deleteEducation(Long id) {
        educationRepository.deleteById(id);
    }
}
