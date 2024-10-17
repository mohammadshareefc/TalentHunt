package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Event;
import TalentHunt.TalentHunt.repository.EventRepository;
import TalentHunt.TalentHunt.repository.UserRepository;

import TalentHunt.TalentHunt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public Event createEvent(Event event) {
        Event createdEvent = eventRepository.save(event);
        return createdEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElse(null);
    }

    public long getEventCount() {
        return eventRepository.countEvents();
    }

    // Fetch events created by a specific user
    public List<Event> getEventsByUserId(Long userId) {
        User user = (User) userRepository.findById(userId)
                .orElseThrow();
        return eventRepository.findByUser((TalentHunt.TalentHunt.model.User) user);
    }
}
