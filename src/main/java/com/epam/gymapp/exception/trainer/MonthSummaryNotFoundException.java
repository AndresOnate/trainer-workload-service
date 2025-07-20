package com.epam.gymapp.exception.trainer;

public class MonthSummaryNotFoundException extends RuntimeException {
    public MonthSummaryNotFoundException(String username, int year, int month) {
        super("Month summary not found for trainer " + username + " in " + year + "-" + month);
    }
}