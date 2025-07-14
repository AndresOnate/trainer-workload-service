package com.epam.gymapp.model;


import jakarta.persistence.*;

@Entity
@Table(name = "monthly_summaries")
public class MonthlySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key for MonthlySummary

    private int month;
    private Integer trainingSummaryDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yearly_summary_id", nullable = false)
    private YearlySummary yearlySummary;

    public MonthlySummary() {}

    public MonthlySummary(int month, Integer trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Integer getTrainingSummaryDuration() {
        return trainingSummaryDuration;
    }

    public void setTrainingSummaryDuration(Integer trainingSummaryDuration) {
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    public YearlySummary getYearlySummary() {
        return yearlySummary;
    }

    public void setYearlySummary(YearlySummary yearlySummary) {
        this.yearlySummary = yearlySummary;
    }
}