package com.maatricare.tracking;

import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
@Table(name = "appointment_questions")
public class AppointmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Appointment appointment;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false)
    private boolean answered;

    protected AppointmentQuestion() {
    }

    public AppointmentQuestion(Appointment appointment, String question) {
        this.appointment = appointment;
        this.question = question;
    }

    public UUID getId() { return id; }
    public UUID getAppointmentId() { return appointment.getId(); }
    public String getQuestion() { return question; }
    public boolean isAnswered() { return answered; }

    public void update(String question, boolean answered) {
        this.question = question;
        this.answered = answered;
    }
}