package com.epam.gymapp.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Document(collection = "trainer_summaries")
@CompoundIndex(name = "first_last_name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerSummary { 
    
    @Id
    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username cannot exceed 30 characters")
    private String username; 

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Trainer status is required")
    private Boolean trainerStatus;

    @NotNull(message = "Years list cannot be null")
    @Valid
    private List<YearlySummary> years = new ArrayList<>(); 

    public TrainerSummary() {}

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
                    years.add(newYearSummary);
                    return newYearSummary;
                });
    }
}