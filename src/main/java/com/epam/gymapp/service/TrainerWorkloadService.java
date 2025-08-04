package com.epam.gymapp.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        TrainerSummary trainer = trainerSummaryRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    operationLogger.info("[{}] Creating new summary entry for trainer: {}", transactionId, request.getUsername());
                    TrainerSummary t = new TrainerSummary();
                    t.setUsername(request.getUsername());
                    t.setFirstName(request.getFirstName());
                    t.setLastName(request.getLastName());
                    t.setTrainerStatus(request.getIsActive());
                    return t;
                });

        trainer.setTrainerStatus(request.getIsActive());

        YearlySummary yearly = trainer.getYears().stream()
                .filter(y -> y.getYear() == request.getTrainingDate().getYear())
                .findFirst()
                .orElseGet(() -> {
                    YearlySummary y = new YearlySummary();
                    y.setYear(request.getTrainingDate().getYear());
                    trainer.getYears().add(y);
                    return y;
                });

        MonthlySummary monthly = yearly.getMonths().stream()
                .filter(m -> m.getMonth() == request.getTrainingDate().getMonthValue())
                .findFirst()
                .orElseGet(() -> {
                    MonthlySummary m = new MonthlySummary();
                    m.setMonth(request.getTrainingDate().getMonthValue());
                    m.setTrainingSummaryDuration(0);
                    yearly.getMonths().add(m);
                    return m;
                });

        updateMonthlyDuration(transactionId, monthly, request);

        trainerSummaryRepository.save(trainer);
        operationLogger.info("[{}] Successfully updated workload for trainer: {}", transactionId, request.getUsername());
    }

    private void updateMonthlyDuration(String transactionId, MonthlySummary monthly, TrainerWorkloadRequest request) {
        int current = monthly.getTrainingSummaryDuration() != null ? monthly.getTrainingSummaryDuration() : 0;
        int change = request.getTrainingDuration();
        int updated;

        switch (request.getActionType()) {
            case ADD:
                updated = current + change;
                break;
            case DELETE:
                updated = Math.max(0, current - change);
                break;
            default:
                throw new InvalidActionTypeException(request.getActionType().name());
        }

        monthly.setTrainingSummaryDuration(updated);
        operationLogger.info("[{}] Updated trainer {} for {}-{}: new total duration = {}", transactionId,
                request.getUsername(), request.getTrainingDate().getYear(), request.getTrainingDate().getMonthValue(), updated);
    }

    public TrainerMonthlySummary getTrainerSummary(String username, Integer year, Integer month) {
        String transactionId = TransactionContext.getTransactionId();
        operationLogger.info("[{}] Retrieving summary for trainer: {}, year: {}, month: {}", transactionId, username, year, month);

        TrainerSummary trainer = trainerSummaryRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));

        TrainerMonthlySummary response = new TrainerMonthlySummary();
        response.setUsername(trainer.getUsername());
        response.setFirstName(trainer.getFirstName());
        response.setLastName(trainer.getLastName());
        response.setStatus(trainer.getTrainerStatus());

        List<YearSummary> dtoYears = new ArrayList<>();

        for (YearlySummary ys : trainer.getYears()) {
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
