package com.epam.gymapp.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gymapp.dto.MonthSummary;
import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.dto.YearSummary;
import com.epam.gymapp.exception.InvalidActionTypeException;
import com.epam.gymapp.exception.MonthSummaryNotFoundException;
import com.epam.gymapp.exception.TrainerNotFoundException;
import com.epam.gymapp.exception.YearSummaryNotFoundException;
import com.epam.gymapp.model.ActionType;
import com.epam.gymapp.model.MonthlySummary;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.model.YearlySummary;
import com.epam.gymapp.repository.TrainerSummaryRepository;
import com.epam.gymapp.util.TransactionContext;

import java.time.LocalDate;
import java.util.Collections;



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

        TrainerSummary trainerSummary = trainerSummaryRepository.findByTrainerUsername(username)
                .orElseGet(() -> {
                    operationLogger.info("[{}] Creating new summary entry for trainer: {}", transactionId, username);
                    TrainerSummary ts = new TrainerSummary();
                    ts.setTrainerUsername(username);
                    ts.setTrainerFirstName(request.getTrainerFirstName());
                    ts.setTrainerLastName(request.getTrainerLastName());
                    return ts;
                });

        trainerSummary.setTrainerStatus(request.getIsActive());

        YearlySummary yearlySummary = trainerSummary.getYearSummary(year);
        if (yearlySummary.getTrainerSummary() == null) {
            yearlySummary.setTrainerSummary(trainerSummary);
            if (!trainerSummary.getYears().contains(yearlySummary)) {
                trainerSummary.getYears().add(yearlySummary);
            }
        }

        MonthlySummary monthlySummary = yearlySummary.getMonthSummary(month);
        if (monthlySummary == null) {
            operationLogger.error("[{}] Monthly summary not found for trainer {} in {}-{}", transactionId, username, year, month);
            throw new MonthSummaryNotFoundException(username, year, month);
        }

        int currentDuration = monthlySummary.getTrainingSummaryDuration();
        int newDuration;

        switch (actionType) {
            case ADD:
                newDuration = currentDuration + duration;
                break;
            case DELETE:
                newDuration = Math.max(0, currentDuration - duration);
                break;
            default:
                throw new InvalidActionTypeException(actionType.name());
        }

        operationLogger.info("[{}] Updating trainer {} for {}-{} with new total: {}", transactionId, username, year, month, newDuration);

        monthlySummary.setTrainingSummaryDuration(newDuration);

        if (monthlySummary.getYearlySummary() == null) {
            monthlySummary.setYearlySummary(yearlySummary);
            if (!yearlySummary.getMonths().contains(monthlySummary)) {
                yearlySummary.getMonths().add(monthlySummary);
            }
        }

        trainerSummaryRepository.save(trainerSummary);
        operationLogger.info("[{}] Successfully updated workload for trainer: {}", transactionId, username);
    }

    @Transactional(readOnly = true)
    public TrainerMonthlySummary getTrainerMonthlySummary(String username, int year, int month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving monthly summary for trainer: {} for {}-{}", transactionId, username, year, month);

        TrainerSummary trainerSummary = trainerSummaryRepository.findByTrainerUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));

        YearlySummary yearlySummary = trainerSummary.getYears().stream()
                .filter(ys -> ys.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new YearSummaryNotFoundException(username, year));

        MonthlySummary monthlySummary = yearlySummary.getMonths().stream()
                .filter(ms -> ms.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new MonthSummaryNotFoundException(username, year, month));

        TrainerMonthlySummary responseSummary = new TrainerMonthlySummary();
        responseSummary.setTrainerUsername(trainerSummary.getTrainerUsername());
        responseSummary.setTrainerFirstName(trainerSummary.getTrainerFirstName());
        responseSummary.setTrainerLastName(trainerSummary.getTrainerLastName());
        responseSummary.setTrainerStatus(trainerSummary.getTrainerStatus());

        YearSummary dtoYearSummary = new YearSummary(year);
        MonthSummary dtoMonthSummary = new MonthSummary(month, monthlySummary.getTrainingSummaryDuration());

        dtoYearSummary.setMonths(Collections.singletonList(dtoMonthSummary));
        responseSummary.setYears(Collections.singletonList(dtoYearSummary));

        operationLogger.info("[{}] Successfully retrieved monthly summary for trainer {} for {}-{}", transactionId, username, year, month);

        return responseSummary;
    }
}
