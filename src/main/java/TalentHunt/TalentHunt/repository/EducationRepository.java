package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUser_UserId(Long userId);

    // Add method to find experience by ID
    Optional<Education> findById(Long id);

    // Add method to delete experience by ID
    void deleteById(Long id);
}
