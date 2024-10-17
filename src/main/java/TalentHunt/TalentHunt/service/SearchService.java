package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Article;
import TalentHunt.TalentHunt.model.Event;
import TalentHunt.TalentHunt.model.Post;
import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.repository.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    @Autowired
    private SearchRepository searchRepository;

    public List<Post> searchPosts(String query) {
        return searchRepository.searchPosts(query);
    }

    public List<User> searchPeople(String query) {
        return searchRepository.searchPeople(query);
    }

    public List<Article> searchArticles(String query) {
        return searchRepository.searchArticles(query);
    }

    public List<Event> searchEvents(String query) {
        return searchRepository.searchEvents(query);
    }
}
