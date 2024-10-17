package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.repository.CommentRepository;
import TalentHunt.TalentHunt.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public void savePost(Post post) {
        postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByTimestampDesc();
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    public void likePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        if (post != null) {
            post.getLikedByUsers().add(userId);
            savePost(post);
        }
    }

    public void unlikePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        if (post != null) {
            post.getLikedByUsers().remove(userId);
            savePost(post);
        }
    }

    public boolean isPostLikedByUser(Long postId, Long userId) {
        Post post = getPostById(postId);
        return post != null && post.getLikedByUsers().contains(userId);
    }

    public int getLikeCount(Long postId) {
        Post post = getPostById(postId);
        return post != null ? post.getLikedByUsers().size() : 0;
    }

    public int getCommentCount(Long postId) {
        return commentRepository.countByPost_PostId(postId);
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUser_UserId(userId);
    }

    public String getTruncatedDescription(String description, int wordLimit) {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }

        String[] words = description.split("\\s+");
        if (words.length <= wordLimit) {
            return description;
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, wordLimit)) + "...";
    }


}
