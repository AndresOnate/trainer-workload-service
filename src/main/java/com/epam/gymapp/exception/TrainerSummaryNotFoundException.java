package com.epam.gymapp.exception;

public class TrainerSummaryNotFoundException extends RuntimeException {
    public TrainerSummaryNotFoundException(String username, int year, int month) {
        super("Summary not found for trainer " + username + " in " + year + "-" + month);
    }
}
