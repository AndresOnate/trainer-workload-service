package com.epam.gymapp.dto;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrainerMonthlySummary {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean status;
    private List<YearSummary> years;

    public TrainerMonthlySummary() {
        this.years = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
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
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
