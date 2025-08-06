package com.epam.gymapp.repository;

public interface TrainerWorkloadCustomRepository {
    void setMonthlyDuration(String username, int year, int month, int durationChange);
}