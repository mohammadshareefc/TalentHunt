package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Article;
import TalentHunt.TalentHunt.model.Event;
import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.description LIKE %:query%")
    List<Post> searchPosts(@Param("query") String query);

    @Query("SELECT u FROM User u WHERE CONCAT(u.firstName, ' ', u.lastName) LIKE %:query%")
    List<User> searchPeople(@Param("query") String query);

    @Query("SELECT a FROM Article a WHERE a.title LIKE %:query% OR a.content LIKE %:query%")
    List<Article> searchArticles(@Param("query") String query);

    @Query("SELECT e FROM Event e WHERE e.event_name LIKE %:query% OR e.description LIKE %:query%")
    List<Event> searchEvents(@Param("query") String query);
}
