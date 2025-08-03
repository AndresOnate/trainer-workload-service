package com.epam.gymapp.model;


public class MonthlySummary {

    private int month;
    private Integer trainingSummaryDuration;

    public MonthlySummary() {}

    public MonthlySummary(int month, Integer trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
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

}