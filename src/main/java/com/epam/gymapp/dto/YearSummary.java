package com.epam.gymapp.dto;

import java.util.ArrayList;
import java.util.List;

public class YearSummary {
    private int year;
    private List<MonthSummary> months;

    public YearSummary(int year) {
        this.year = year;
        this.months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            this.months.add(new MonthSummary(i, 0));
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<MonthSummary> getMonths() {
        return months;
    }

    public void setMonths(List<MonthSummary> months) {
        this.months = months;
    }

    public MonthSummary getMonthSummary(int month) {
        return months.stream()
                .filter(ms -> ms.getMonth() == month)
                .findFirst()
                .orElse(null); 
    }
}