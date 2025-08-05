package com.epam.gymapp.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;


public class YearlySummary {

    @Min(value = 1900, message = "Year must be a valid year")
    private int year;
    
    private List<MonthlySummary> months = new ArrayList<>(); 

    public YearlySummary() {}

    public YearlySummary(int year) {
        this.year = year;
    }
    
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
                .orElse(null); 
    }
}