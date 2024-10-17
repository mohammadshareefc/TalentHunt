package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Comment;
import TalentHunt.TalentHunt.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdOrderByTimestampAsc(Long postId);
    Long countByParentComment(Comment parentComment);

    //For Notification
    List<Comment> findByPost(Post post);

    int countByPost_PostId(Long postId);
}
