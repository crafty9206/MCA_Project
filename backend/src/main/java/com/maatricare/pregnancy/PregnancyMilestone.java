package com.maatricare.pregnancy;

import java.util.UUID;

import com.maatricare.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pregnancy_milestones")
public class PregnancyMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(nullable = false, length = 16)
    private String type = "STANDARD";

    @Column(nullable = false)
    private boolean completed = false;

    protected PregnancyMilestone() {
    }

    public PregnancyMilestone(User user, String title, String description, int weekNumber, String type) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.weekNumber = weekNumber;
        this.type = type == null || type.isBlank() ? "STANDARD" : type.toUpperCase();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getWeekNumber() { return weekNumber; }
    public String getType() { return type; }
    public boolean isCompleted() { return completed; }

    public void update(String title, String description, int weekNumber, String type) {
        this.title = title;
        this.description = description;
        this.weekNumber = weekNumber;
        this.type = type == null || type.isBlank() ? "STANDARD" : type.toUpperCase();
    }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
