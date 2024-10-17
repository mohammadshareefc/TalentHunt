package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository <Event, Long >{
    @Query("SELECT COUNT(e) FROM Event e")
    long countEvents();
    List<Event> findByUser(User user);

}
