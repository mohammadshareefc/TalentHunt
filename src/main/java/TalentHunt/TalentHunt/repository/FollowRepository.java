package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Follow;
import TalentHunt.TalentHunt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FollowRepository extends JpaRepository<Follow , Long> {
    Follow findByFollowerAndFollowed(User follower, User followed);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowed(User followed);

    @Query("SELECT f.followed FROM Follow f WHERE f.follower.id = :followerId AND f.status = 'FOLLOWING'")
    List<User> findFollowingByUserId(@Param("followerId") Long followerId);

    @Query("SELECT f.follower FROM Follow f WHERE f.followed.id = :followedId AND f.status = 'FOLLOWING'")
    List<User> findFollowersByUserId(@Param("followedId") Long followedId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :followerId AND f.status = 'FOLLOWING'")
    long countFollowing(@Param("followerId") Long followerId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :followedId AND f.status = 'FOLLOWING'")
    long countFollowers(@Param("followedId") Long followedId);
}
