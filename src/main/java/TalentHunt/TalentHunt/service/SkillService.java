package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Skill;
import TalentHunt.TalentHunt.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {
    @Autowired
    private SkillRepository skillRepository;


    public List<Skill> getSkillsByUserId(Long userId) {
        return skillRepository.findByUser_UserId(userId);
    }

    public void saveSkill(Skill skill) {
        skillRepository.save(skill);
    }


    // Add method to get skill by ID
    public Skill getskillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("skill not found with id: " + id));
    }

    // Add method to delete skill by ID
    public void deleteskill(Long id) {
        skillRepository.deleteById(id);
    }
}
