package com.epam.gymapp.exception.trainer;

public class TrainerNotFoundException extends RuntimeException {
    public TrainerNotFoundException(String username) {
        super("Trainer summary not found for username: " + username);
    }
}