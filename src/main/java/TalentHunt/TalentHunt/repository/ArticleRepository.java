package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Article;
import TalentHunt.TalentHunt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByAuthor(User author);

    long count();
    List<Article> findByAuthor_UserId(Long authorId);
}

