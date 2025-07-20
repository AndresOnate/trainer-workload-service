package com.epam.gymapp.dto;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrainerMonthlySummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus;
    private List<YearSummary> years;

    public TrainerMonthlySummary() {
        this.years = new ArrayList<>();
    }

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

    public List<YearSummary> getYears() {
        return years;
    }

    public void setYears(List<YearSummary> years) {
        this.years = years;
    }

    public YearSummary getYearSummary(int year) {
        return years.stream()
                .filter(ys -> ys.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYearSummary = new YearSummary(year);
                    years.add(newYearSummary);
                    return newYearSummary;
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainerMonthlySummary that = (TrainerMonthlySummary) o;
        return Objects.equals(trainerUsername, that.trainerUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainerUsername);
    }
}