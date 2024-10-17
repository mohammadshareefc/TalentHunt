package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Notification;
import TalentHunt.TalentHunt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<TalentHunt.TalentHunt.model.Notification> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    int countUnreadNotificationsByUserId(@Param("userId") Long userId);
}
