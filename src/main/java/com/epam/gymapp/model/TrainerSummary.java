package com.epam.gymapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_summaries")
public class TrainerSummary { 
    @Id
    private String username; 
    private String firstName;
    private String lastName;
    private Boolean trainerStatus; // Active status of the trainer

    @OneToMany(mappedBy = "trainerSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<YearlySummary> years = new ArrayList<>(); 

    public TrainerSummary() {}

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String trainerUsername) {
        this.username = trainerUsername;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String trainerFirstName) {
        this.firstName = trainerFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String trainerLastName) {
        this.lastName = trainerLastName;
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