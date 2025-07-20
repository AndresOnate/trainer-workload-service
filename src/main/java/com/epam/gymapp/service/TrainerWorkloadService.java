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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



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
    public TrainerMonthlySummary getTrainerSummary(String username, Integer year, Integer month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);

        TrainerSummary trainerSummary = trainerSummaryRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));

        TrainerMonthlySummary response = new TrainerMonthlySummary();
        response.setUsername(trainerSummary.getUsername());
        response.setFirstName(trainerSummary.getFirstName());
        response.setLastName(trainerSummary.getLastName());
        response.setStatus(trainerSummary.getTrainerStatus());

        List<YearSummary> dtoYears = new ArrayList<>();

        for (YearlySummary ys : trainerSummary.getYears()) {
            if (year != null && ys.getYear() != year) continue;

            List<MonthSummary> dtoMonths = new ArrayList<>();
            for (MonthlySummary ms : ys.getMonths()) {
                if (month != null && ms.getMonth() != month) continue;
                dtoMonths.add(new MonthSummary(ms.getMonth(), ms.getTrainingSummaryDuration()));
            }

            if (!dtoMonths.isEmpty()) {
                YearSummary dtoYear = new YearSummary(ys.getYear());
                dtoYear.setMonths(dtoMonths);
                dtoYears.add(dtoYear);
            }
        }

        if (dtoYears.isEmpty()) {
            if (year != null && month != null)
                throw new MonthSummaryNotFoundException(username, year, month);
            if (year != null)
                throw new YearSummaryNotFoundException(username, year);
        }

        response.setYears(dtoYears);
        operationLogger.info("[{}] Successfully retrieved summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);

        return response;
    }
}
