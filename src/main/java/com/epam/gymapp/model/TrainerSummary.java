package com.epam.gymapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_summaries")
public class TrainerSummary { // Renamed from TrainerMonthlySummary to avoid confusion with the request object
    @Id
    private String trainerUsername; // Trainer username as primary key
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus; // IsActive

    @OneToMany(mappedBy = "trainerSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<YearlySummary> years = new ArrayList<>(); // Renamed to YearlySummary

    public TrainerSummary() {}

    // Getters and Setters
    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public Boolean getTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(Boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public List<YearlySummary> getYears() {
        return years;
    }

    public void setYears(List<YearlySummary> years) {
        this.years = years;
    }

    // Helper method to get or create YearSummary
    public YearlySummary getYearSummary(int year) {
        return years.stream()
                .filter(ys -> ys.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearlySummary newYearSummary = new YearlySummary(year);
                    newYearSummary.setTrainerSummary(this); // Set parent reference
                    years.add(newYearSummary);
                    return newYearSummary;
                });
    }
}