package com.epam.gymapp.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gymapp.dto.MonthSummary;
import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.dto.YearSummary;
import com.epam.gymapp.model.ActionType;
import com.epam.gymapp.model.MonthlySummary;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.model.YearlySummary;
import com.epam.gymapp.repository.TrainerSummaryRepository;
import com.epam.gymapp.util.TransactionContext;

import java.time.LocalDate;
import java.util.Optional;


@Service
public class TrainerWorkloadService {

    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    private final TrainerSummaryRepository trainerSummaryRepository;

    public TrainerWorkloadService(TrainerSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    @Transactional
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Starting update for trainer: {} with action: {}", transactionId, request.getTrainerUsername(), request.getActionType());

        String username = request.getTrainerUsername();
        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int duration = request.getTrainingDuration();
        ActionType actionType = request.getActionType();

        Optional<TrainerSummary> existingTrainerSummaryOpt = trainerSummaryRepository.findByTrainerUsername(username);
        TrainerSummary trainerSummary;

        if (existingTrainerSummaryOpt.isPresent()) {
            trainerSummary = existingTrainerSummaryOpt.get();
            operationLogger.info("[{}] Found existing summary entry for trainer: {}", transactionId, username);
        } else {
            operationLogger.info("[{}] Creating new summary entry for trainer: {}", transactionId, username);
            trainerSummary = new TrainerSummary();
            trainerSummary.setTrainerUsername(username);
            trainerSummary.setTrainerFirstName(request.getTrainerFirstName());
            trainerSummary.setTrainerLastName(request.getTrainerLastName());
        }

        trainerSummary.setTrainerStatus(request.getIsActive()); // Update trainer status

        YearlySummary yearlySummary = trainerSummary.getYearSummary(year);
        // Ensure the yearlySummary is associated with the trainerSummary if it's new
        if (yearlySummary.getTrainerSummary() == null) {
            yearlySummary.setTrainerSummary(trainerSummary);
            if (!trainerSummary.getYears().contains(yearlySummary)) {
                trainerSummary.getYears().add(yearlySummary);
            }
        }


        MonthlySummary monthlySummary = yearlySummary.getMonthSummary(month);

        if (monthlySummary != null) {
            int currentDuration = monthlySummary.getTrainingSummaryDuration();
            int newDuration;
            if (actionType == ActionType.ADD) {
                newDuration = currentDuration + duration;
                operationLogger.info("[{}] Adding {} duration to trainer {} for {}-{}. New total: {}", transactionId, duration, username, year, month, newDuration);
            } else if (actionType == ActionType.DELETE) {
                newDuration = currentDuration - duration;
                // Ensure duration doesn't go below zero
                newDuration = Math.max(0, newDuration);
                operationLogger.info("[{}] Deleting {} duration from trainer {} for {}-{}. New total: {}", transactionId, duration, username, year, month, newDuration);
            } else {
                operationLogger.warn("[{}] Unknown action type: {} for trainer {}", transactionId, actionType, username);
                throw new IllegalArgumentException("Unknown action type: " + actionType);
            }
            monthlySummary.setTrainingSummaryDuration(newDuration);
            // Ensure the monthlySummary is associated with the yearlySummary if it's new
            if (monthlySummary.getYearlySummary() == null) {
                 monthlySummary.setYearlySummary(yearlySummary);
                 if (!yearlySummary.getMonths().contains(monthlySummary)) {
                     yearlySummary.getMonths().add(monthlySummary);
                 }
            }
        } else {
            operationLogger.error("[{}] Monthly summary not found for trainer {} in {}-{}. This indicates an initialization error.", transactionId, username, year, month);
        }

        trainerSummaryRepository.save(trainerSummary); // Save the updated entity
        operationLogger.info("[{}] Successfully updated workload for trainer: {}", transactionId, username);
    }

    @Transactional(readOnly = true) // Read-only transaction for better performance
    public TrainerMonthlySummary getTrainerMonthlySummary(String username, int year, int month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving monthly summary for trainer: {} for {}-{}", transactionId, username, year, month);

        Optional<TrainerSummary> trainerSummaryOpt = trainerSummaryRepository.findByTrainerUsername(username);

        if (trainerSummaryOpt.isEmpty()) {
            operationLogger.warn("[{}] Trainer summary not found for username: {}", transactionId, username);
            return null;
        }

        TrainerSummary trainerSummary = trainerSummaryOpt.get();

        YearlySummary yearlySummary = trainerSummary.getYears().stream()
                .filter(ys -> ys.getYear() == year)
                .findFirst()
                .orElse(null);

        if (yearlySummary == null) {
            operationLogger.warn("[{}] Year summary not found for trainer {} in year: {}", transactionId, username, year);
            return null;
        }

        MonthlySummary monthlySummary = yearlySummary.getMonths().stream()
                .filter(ms -> ms.getMonth() == month)
                .findFirst()
                .orElse(null);

        if (monthlySummary == null) {
            operationLogger.warn("[{}] Month summary not found for trainer {} in month: {}", transactionId, username, month);
            return null;
        }

        TrainerMonthlySummary responseSummary = new TrainerMonthlySummary();
        responseSummary.setTrainerUsername(trainerSummary.getTrainerUsername());
        responseSummary.setTrainerFirstName(trainerSummary.getTrainerFirstName());
        responseSummary.setTrainerLastName(trainerSummary.getTrainerLastName());
        responseSummary.setTrainerStatus(trainerSummary.getTrainerStatus());

        // Create DTO YearSummary and MonthSummary to match the original response contract
        YearSummary dtoYearSummary = new YearSummary(yearlySummary.getYear());
        MonthSummary dtoMonthSummary = new MonthSummary(monthlySummary.getMonth(), monthlySummary.getTrainingSummaryDuration());

        dtoYearSummary.setMonths(java.util.Collections.singletonList(dtoMonthSummary));
        responseSummary.setYears(java.util.Collections.singletonList(dtoYearSummary));

        operationLogger.info("[{}] Successfully retrieved monthly summary for trainer {} for {}-{}: {}", transactionId, username, year, month, monthlySummary.getTrainingSummaryDuration());
        return responseSummary;
    }
}