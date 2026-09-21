package com.maatricare.tracking;

import java.util.UUID;

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

    protected TaskDetail() {
    }

    public TaskDetail(String title) {
        this.title = title;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
}