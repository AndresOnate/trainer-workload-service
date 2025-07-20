package com.epam.gymapp.dto;


public class MonthSummary {
    private int month;
    private Integer trainingSummaryDuration; 

    public MonthSummary(int month, Integer trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    // Getters and Setters
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
}