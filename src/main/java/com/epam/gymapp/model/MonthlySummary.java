package com.epam.gymapp.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MonthlySummary {

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private int month;

    @NotNull(message = "Training summary duration is required")
    @PositiveOrZero(message = "Training summary duration cannot be negative")
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