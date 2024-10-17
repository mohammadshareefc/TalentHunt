package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByTimestampDesc();
    Optional<Post> findById(Long postId);

    List<Post> findByUser_UserId(Long userId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    long countPostsByUserId(@Param("userId") Long userId);
}
