package com.epam.gymapp.exception;

public class YearSummaryNotFoundException extends RuntimeException {
    public YearSummaryNotFoundException(String username, int year) {
        super("Year summary not found for trainer " + username + " in year: " + year);
    }
}
