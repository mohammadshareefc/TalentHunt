package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Connection;
import TalentHunt.TalentHunt.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Connection findByUser1AndUser2(User user1, User user2);

    List<Connection> findByUser2AndStatus(User user, String status);

    List<Connection> findByUser1AndStatus(User user, String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Connection c WHERE c.user1.userId = :userId AND c.connectionId = :connectionId")
    int removeConnection(@Param("userId") Long userId, @Param("connectionId") Long connectionId);

    @Query("SELECT c FROM Connection c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Connection> findConnectionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Connection c WHERE c.user1 = :user1 AND c.user2 = :user2")
    void deleteConnection(@Param("user1") User user1, @Param("user2") User user2);

}
