package com.epam.gymapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "yearly_summaries")
public class YearlySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key for YearlySummary

    private int year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_username", nullable = false)
    private TrainerSummary trainerSummary;

    @OneToMany(mappedBy = "yearlySummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MonthlySummary> months = new ArrayList<>(); // Renamed to MonthlySummary

    public YearlySummary() {}

    public YearlySummary(int year) {
        this.year = year;
        // Initialize all 12 months with 0 duration when a new YearlySummary is created
        for (int i = 1; i <= 12; i++) {
            MonthlySummary newMonth = new MonthlySummary(i, 0);
            newMonth.setYearlySummary(this); // Set parent reference
            this.months.add(newMonth);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public TrainerSummary getTrainerSummary() {
        return trainerSummary;
    }

    public void setTrainerSummary(TrainerSummary trainerSummary) {
        this.trainerSummary = trainerSummary;
    }

    public List<MonthlySummary> getMonths() {
        return months;
    }

    public void setMonths(List<MonthlySummary> months) {
        this.months = months;
    }

    public MonthlySummary getMonthSummary(int month) {
        return months.stream()
                .filter(ms -> ms.getMonth() == month)
                .findFirst()
                .orElse(null); // Should always find if initialized correctly
    }
}