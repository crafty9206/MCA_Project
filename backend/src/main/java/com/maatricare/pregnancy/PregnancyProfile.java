package com.maatricare.pregnancy;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

import com.maatricare.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pregnancy_profiles")
public class PregnancyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_menstrual_period", nullable = false)
    private LocalDate lastMenstrualPeriod;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "age_years")
    private Integer ageYears;

    @Column(name = "height_cm")
    private BigDecimal heightCm;

    @Column(name = "pre_pregnancy_weight_kg")
    private BigDecimal prePregnancyWeightKg;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "blood_group", length = 3)
    private String bloodGroup;

    protected PregnancyProfile() {
    }

    public PregnancyProfile(User user, LocalDate lastMenstrualPeriod, LocalDate dueDate, Integer ageYears,
            BigDecimal heightCm, BigDecimal prePregnancyWeightKg, String bloodPressure, String bloodGroup) {
        this.user = user;
        this.lastMenstrualPeriod = lastMenstrualPeriod;
        this.dueDate = dueDate;
        this.ageYears = ageYears;
        this.heightCm = heightCm;
        this.prePregnancyWeightKg = prePregnancyWeightKg;
        this.bloodPressure = bloodPressure;
        this.bloodGroup = bloodGroup;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getLastMenstrualPeriod() {
        return lastMenstrualPeriod;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getAgeYears() { return ageYears; }
    public BigDecimal getHeightCm() { return heightCm; }
    public BigDecimal getPrePregnancyWeightKg() { return prePregnancyWeightKg; }
    public String getBloodPressure() { return bloodPressure; }
    public String getBloodGroup() { return bloodGroup; }
}
