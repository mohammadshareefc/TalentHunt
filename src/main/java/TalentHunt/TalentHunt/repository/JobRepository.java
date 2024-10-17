package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository  extends JpaRepository<Job, Long > {
    List<Job> findTop6ByOrderByPostedDateDesc();

    List<Job> findByUser_UserId(Long userId);
}