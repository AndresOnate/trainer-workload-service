package com.epam.gymapp.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.epam.gymapp.dto.ActionType;
import com.epam.gymapp.dto.MonthSummary;
import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.dto.YearSummary;
import com.epam.gymapp.exception.common.InvalidActionTypeException;
import com.epam.gymapp.exception.trainer.MonthSummaryNotFoundException;
import com.epam.gymapp.exception.trainer.TrainerNotFoundException;
import com.epam.gymapp.exception.trainer.YearSummaryNotFoundException;
import com.epam.gymapp.model.MonthlySummary;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.model.YearlySummary;
import com.epam.gymapp.repository.TrainerSummaryRepository;
import com.epam.gymapp.util.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TrainerWorkloadService {

    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    private final TrainerSummaryRepository trainerSummaryRepository;

    public TrainerWorkloadService(TrainerSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Starting update for trainer: {} with action: {}", transactionId, request.getUsername(), request.getActionType());

        String username = request.getUsername();
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int change = request.getTrainingDuration();
        ActionType actionType = request.getActionType();

        trainerSummaryRepository.findByUsername(username)
            .ifPresentOrElse(
                trainer -> handleExistingTrainer(trainer, actionType, year, month, change, transactionId),
                () -> handleNewTrainer(request, actionType, transactionId)
            );
    }

    private void handleExistingTrainer(TrainerSummary trainer, ActionType actionType, int year, int month, int change, String transactionId) {
        String username = trainer.getUsername();
        YearlySummary yearSummary = trainer.getYearSummary(year);
        MonthlySummary monthSummary = yearSummary.getMonthSummary(month);

        if (monthSummary != null) {
            handleExistingMonth(username, year, month, change, actionType, transactionId, monthSummary);
        } else {
            handleMissingMonth(trainer, yearSummary, month, change, actionType, transactionId);
        }
    }

    private void handleExistingMonth(String username, int year, int month, int change, ActionType actionType, String transactionId, MonthlySummary monthSummary) {
        int current = monthSummary.getTrainingSummaryDuration() != null ? monthSummary.getTrainingSummaryDuration() : 0;
        int updated;
        switch (actionType) {
            case ADD:
                updated = current + change;
                trainerSummaryRepository.setMonthlyDuration(username, year, month, change);
                break;
            case DELETE:
                if(current - change < 0){
                    updated = 0;
                    trainerSummaryRepository.setMonthlyDuration(username, year, month, -current);
                }else{
                    updated = current - change;
                    trainerSummaryRepository.setMonthlyDuration(username, year, month, -change);

                }
                
                break;
            default:
                throw new InvalidActionTypeException(actionType.name());
        }

        operationLogger.info("[{}] Updated trainer {} for {}-{}: new total duration = {}", transactionId, username, year, month, updated);
    }

    private void handleMissingMonth(TrainerSummary trainer, YearlySummary yearSummary, int month, int change, ActionType actionType, String transactionId) {
        String username = trainer.getUsername();
        int year = yearSummary.getYear();
        if (actionType == ActionType.ADD) {
            yearSummary.getMonths().add(new MonthlySummary(month, change));
            trainerSummaryRepository.save(trainer);
            operationLogger.info("[{}] Added new month summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);
        } else {
            operationLogger.warn("[{}] Cannot DELETE: No existing summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);
            throw new MonthSummaryNotFoundException(username, year, month);
        }
    }

    private void handleNewTrainer(TrainerWorkloadRequest request, ActionType actionType, String transactionId) {
        if (actionType == ActionType.ADD) {
            TrainerSummary newTrainer = creatTrainerSummary(request);
            trainerSummaryRepository.save(newTrainer);
            operationLogger.info("[{}] Created new trainer and added summary: {}", transactionId, request.getUsername());
        } else {
            operationLogger.warn("[{}] Cannot DELETE: Trainer not found: {}", transactionId, request.getUsername());
            throw new TrainerNotFoundException(request.getUsername());
        }
    }

    private TrainerSummary creatTrainerSummary(TrainerWorkloadRequest request) {
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername(request.getUsername());
        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setTrainerStatus(request.getIsActive());

        YearlySummary yearly = new YearlySummary(request.getTrainingDate().getYear());
        MonthlySummary monthly = new MonthlySummary(request.getTrainingDate().getMonthValue(), request.getTrainingDuration());
        yearly.getMonths().add(monthly);
        trainer.getYears().add(yearly);
        return trainer;
    }

    public TrainerMonthlySummary getTrainerSummary(String username, Integer year, Integer month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);

        TrainerSummary trainer = trainerSummaryRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));

        List<YearSummary> filteredYears = trainer.getYears().stream()
                .filter(ys -> year == null || ys.getYear() == year)
                .map(ys -> {
                    List<MonthSummary> filteredMonths = ys.getMonths().stream()
                            .filter(ms -> month == null || ms.getMonth() == month)
                            .map(ms -> new MonthSummary(ms.getMonth(), ms.getTrainingSummaryDuration()))
                            .collect(Collectors.toList());

                    if (filteredMonths.isEmpty()) return null;

                    YearSummary dtoYear = new YearSummary(ys.getYear());
                    dtoYear.setMonths(filteredMonths);
                    return dtoYear;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (filteredYears.isEmpty()) {
            if (year != null && month != null) {
                throw new MonthSummaryNotFoundException(username, year, month);
            } else if (year != null) {
                throw new YearSummaryNotFoundException(username, year);
            }
        }

        TrainerMonthlySummary response = new TrainerMonthlySummary();
        response.setUsername(trainer.getUsername());
        response.setFirstName(trainer.getFirstName());
        response.setLastName(trainer.getLastName());
        response.setStatus(trainer.getTrainerStatus());
        response.setYears(filteredYears);

        operationLogger.info("[{}] Successfully retrieved summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);

        return response;
    }

}
