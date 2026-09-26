package com.maatricare.tracking;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.maatricare.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "daily_wellbeing", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "entry_date" }))
public class DailyWellbeing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "water_glasses", nullable = false)
    private int waterGlasses;

    @Column(name = "prenatal_vitamin_taken", nullable = false)
    private boolean prenatalVitaminTaken;

    @Column(name = "activity_minutes", nullable = false)
    private int activityMinutes;

    @Column(length = 40)
    private String mood;

    @Column(name = "sleep_hours", precision = 3, scale = 1, nullable = false)
    private BigDecimal sleepHours = BigDecimal.ZERO;

    @Column(name = "energy_level", nullable = false, length = 16)
    private String energyLevel = "STEADY";

    @Column(name = "focus_task_count", nullable = false)
    private int focusTaskCount = 3;

    @ElementCollection
    @CollectionTable(name = "daily_wellbeing_focus_tasks", joinColumns = @JoinColumn(name = "daily_wellbeing_id"))
    @Column(name = "task_id", nullable = false)
    @OrderColumn(name = "sort_order")
    private List<UUID> focusTaskIds = new ArrayList<>();

    protected DailyWellbeing() {
    }

    public DailyWellbeing(User user, LocalDate entryDate) {
        this.user = user;
        this.entryDate = entryDate;
    }

    public UUID getId() { return id; }
    public LocalDate getEntryDate() { return entryDate; }
    public int getWaterGlasses() { return waterGlasses; }
    public void setWaterGlasses(int waterGlasses) { this.waterGlasses = waterGlasses; }
    public boolean isPrenatalVitaminTaken() { return prenatalVitaminTaken; }
    public void setPrenatalVitaminTaken(boolean prenatalVitaminTaken) { this.prenatalVitaminTaken = prenatalVitaminTaken; }
    public int getActivityMinutes() { return activityMinutes; }
    public void setActivityMinutes(int activityMinutes) { this.activityMinutes = activityMinutes; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public BigDecimal getSleepHours() { return sleepHours; }
    public void setSleepHours(BigDecimal sleepHours) { this.sleepHours = sleepHours; }
    public String getEnergyLevel() { return energyLevel; }
    public int getFocusTaskCount() { return focusTaskCount; }
    public List<UUID> getFocusTaskIds() { return List.copyOf(focusTaskIds); }
    public void updateDayPlan(String energyLevel, int focusTaskCount, List<UUID> focusTaskIds) {
        this.energyLevel = energyLevel;
        this.focusTaskCount = focusTaskCount;
        this.focusTaskIds.clear();
        this.focusTaskIds.addAll(focusTaskIds);
    }
}