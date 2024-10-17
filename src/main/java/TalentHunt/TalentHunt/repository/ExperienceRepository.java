package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience , Long> {
    List<Experience> findByUser_UserId(Long userId);

    // Add method to find experience by ID
    Optional<Experience> findById(Long id);

    // Add method to delete experience by ID
    void deleteById(Long id);
}
