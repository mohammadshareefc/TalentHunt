package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Experience;
import TalentHunt.TalentHunt.repository.ExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperienceService {
    @Autowired
    private ExperienceRepository experienceRepository;

    public void saveExperience(Experience experience) {
        experienceRepository.save(experience);
    }

    public List<Experience> getExperiencesByUserId(Long userId) {
        return experienceRepository.findByUser_UserId(userId);
    }

    // Add method to get experience by ID
    public Experience getExperienceById(Long id) {
        return experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Experience not found with id: " + id));
    }

    // Add method to delete experience by ID
    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }
}
