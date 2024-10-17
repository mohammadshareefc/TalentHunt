package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByProfileUrlSlug(String profileUrlSlug);

    List<User> findAllByUserIdNot(Long userId);

    long countConnectionsByUserId(@Param("userId") Long userId);

    List<User> findConnectionsByUserId(Long userId);

    //Article
    Optional<User> findById(Long userId);
}
