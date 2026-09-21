package com.maatricare.tracking;

import java.time.LocalDate;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "care_tasks", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "task_date", "task_detail_id" }))
public class CareTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_detail_id", nullable = false)
    private TaskDetail taskDetail;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private boolean completed;

    protected CareTask() {
    }

    public CareTask(User user, TaskDetail taskDetail, LocalDate taskDate) {
        this.user = user;
        this.taskDetail = taskDetail;
        this.taskDate = taskDate;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public TaskDetail getTaskDetail() { return taskDetail; }
    public LocalDate getTaskDate() { return taskDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}