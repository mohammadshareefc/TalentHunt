package TalentHunt.TalentHunt.model;

import jakarta.persistence.*;
import TalentHunt.TalentHunt.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile")
public class ProfileView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private User viewer;

    private LocalDateTime viewDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public LocalDateTime getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDateTime viewDate) {
        this.viewDate = viewDate;
    }

}
