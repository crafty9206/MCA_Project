package com.maatricare.tracking;

import java.util.UUID;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_details")
public class TaskDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String title;

    @Column(nullable = false)
    private boolean shared;

    @Column(nullable = false, length = 20)
    private String recurrence = "NONE";

    @Column(name = "recurrence_start_date")
    private LocalDate recurrenceStartDate;

    protected TaskDetail() {
    }

    public TaskDetail(String title) {
        this(title, false);
    }

    public TaskDetail(String title, boolean shared) {
        this.title = title;
        this.shared = shared;
    }

    public TaskDetail(String title, boolean shared, String recurrence, LocalDate recurrenceStartDate) {
        this.title = title;
        this.shared = shared;
        this.recurrence = recurrence;
        this.recurrenceStartDate = recurrenceStartDate;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public boolean isShared() { return shared; }
    public String getRecurrence() { return recurrence; }
    public LocalDate getRecurrenceStartDate() { return recurrenceStartDate; }

    public void setTitle(String title) { this.title = title; }
}