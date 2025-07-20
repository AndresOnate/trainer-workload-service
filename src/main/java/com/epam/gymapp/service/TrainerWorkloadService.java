package com.epam.gymapp.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        operationLogger.info("[{}] Starting update for trainer: {} with action: {}", transactionId, request.getUsername(), request.getActionType());

        TrainerSummary trainerSummary = getOrCreateTrainerSummary(request);
        trainerSummary.setTrainerStatus(request.getIsActive());

        YearlySummary yearlySummary = associateYearlySummary(trainerSummary, request.getTrainingDate().getYear());
        MonthlySummary monthlySummary = getOrCreateMonthlySummary(yearlySummary, request.getTrainingDate().getMonthValue());

        updateMonthlyDuration(transactionId, monthlySummary, request);

        trainerSummaryRepository.save(trainerSummary);
        operationLogger.info("[{}] Successfully updated workload for trainer: {}", transactionId, request.getUsername());
    }

    private TrainerSummary getOrCreateTrainerSummary(TrainerWorkloadRequest request) {
    return trainerSummaryRepository.findByUsername(request.getUsername())
            .orElseGet(() -> {
                operationLogger.info("[{}] Creating new summary entry for trainer: {}", TransactionContext.getTransactionId(), request.getUsername());
                TrainerSummary ts = new TrainerSummary();
                ts.setUsername(request.getUsername());
                ts.setFirstName(request.getFirstName());
                ts.setLastName(request.getLastName());
                return ts;
            });
    }

    private YearlySummary associateYearlySummary(TrainerSummary trainerSummary, int year) {
        YearlySummary yearlySummary = trainerSummary.getYearSummary(year);

        if (yearlySummary.getTrainerSummary() == null) {
            yearlySummary.setTrainerSummary(trainerSummary);
            if (!trainerSummary.getYears().contains(yearlySummary)) {
                trainerSummary.getYears().add(yearlySummary);
            }
        }

        return yearlySummary;
    }

    private MonthlySummary getOrCreateMonthlySummary(YearlySummary yearlySummary, int month) {
        String transactionId = TransactionContext.getTransactionId();
        MonthlySummary monthlySummary = yearlySummary.getMonthSummary(month);

        if (monthlySummary == null) {
            String username = yearlySummary.getTrainerSummary().getUsername();
            operationLogger.info("[{}] Monthly summary not found, creating for trainer {} in {}-{}", transactionId, username, yearlySummary.getYear(), month);

            monthlySummary = new MonthlySummary(month, 0);
            monthlySummary.setYearlySummary(yearlySummary);
            yearlySummary.getMonths().add(monthlySummary);
        }

        return monthlySummary;
    }

    private void updateMonthlyDuration(String transactionId, MonthlySummary monthlySummary, TrainerWorkloadRequest request) {
        int currentDuration = monthlySummary.getTrainingSummaryDuration();
        int durationChange = request.getTrainingDuration();
        int newDuration;

        switch (request.getActionType()) {
            case ADD:
                newDuration = currentDuration + durationChange;
                break;
            case DELETE:
                newDuration = Math.max(0, currentDuration - durationChange);
                break;
            default:
                throw new InvalidActionTypeException(request.getActionType().name());
        }

        monthlySummary.setTrainingSummaryDuration(newDuration);
        operationLogger.info("[{}] Updated trainer {} for {}-{}: new total duration = {}", transactionId,
                request.getUsername(), request.getTrainingDate().getYear(), request.getTrainingDate().getMonthValue(), newDuration);
    }



    @Transactional(readOnly = true)
    public TrainerMonthlySummary getTrainerMonthlySummary(String username, int year, int month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving monthly summary for trainer: {} for {}-{}", transactionId, username, year, month);

        TrainerSummary trainerSummary = trainerSummaryRepository.findByUsername(username)
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
        responseSummary.setTrainerUsername(trainerSummary.getUsername());
        responseSummary.setTrainerFirstName(trainerSummary.getFirstName());
        responseSummary.setTrainerLastName(trainerSummary.getLastName());
        responseSummary.setTrainerStatus(trainerSummary.getTrainerStatus());

        YearSummary dtoYearSummary = new YearSummary(year);
        MonthSummary dtoMonthSummary = new MonthSummary(month, monthlySummary.getTrainingSummaryDuration());

        dtoYearSummary.setMonths(Collections.singletonList(dtoMonthSummary));
        responseSummary.setYears(Collections.singletonList(dtoYearSummary));

        operationLogger.info("[{}] Successfully retrieved monthly summary for trainer {} for {}-{}", transactionId, username, year, month);

        return responseSummary;
    }
}
