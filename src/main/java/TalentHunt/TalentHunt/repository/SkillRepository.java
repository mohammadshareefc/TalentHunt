package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUser_UserId(Long userId);

    // Add method to find experience by ID
    Optional<Skill> findById(Long id);

    // Add method to delete experience by ID
    void deleteById(Long id);
}
