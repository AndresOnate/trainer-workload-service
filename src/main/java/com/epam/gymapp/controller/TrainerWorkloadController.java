package com.epam.gymapp.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.service.TrainerWorkloadService;
import com.epam.gymapp.util.TransactionContext;

import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
public class TrainerWorkloadController {

    private static final Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateTrainerWorkload(@RequestBody TrainerWorkloadRequest request) {
        String transactionId = UUID.randomUUID().toString();
        TransactionContext.setTransactionId(transactionId); // Set for this thread

        transactionLogger.info("[{}] Endpoint: POST /api/workload/update, Request: {}", transactionId, request.toString());
        operationLogger.info("[{}] Processing trainer workload update for trainer: {}", transactionId, request.getTrainerUsername());

        try {
            trainerWorkloadService.updateTrainerWorkload(request);
            transactionLogger.info("[{}] Response: 200 OK, Message: Trainer workload updated successfully.", transactionId);
            return ResponseEntity.ok("Trainer workload updated successfully.");
        } catch (Exception e) {
            transactionLogger.error("[{}] Response: 500 Internal Server Error, Message: {}", transactionId, e.getMessage(), e);
            operationLogger.error("[{}] Error updating trainer workload for trainer {}: {}", transactionId, request.getTrainerUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating trainer workload: " + e.getMessage());
        } finally {
            TransactionContext.clearTransactionId(); // Clear after request completion
        }
    }

    @GetMapping("/summary/{username}/{year}/{month}")
    public ResponseEntity<?> getTrainerMonthlySummary(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {
        String transactionId = UUID.randomUUID().toString();
        TransactionContext.setTransactionId(transactionId);

        transactionLogger.info("[{}] Endpoint: GET /api/workload/summary/{}/{}/{}, Request Params: username={}, year={}, month={}",
                transactionId, username, year, month, username, year, month);
        operationLogger.info("[{}] Fetching monthly summary for trainer: {} for {}-{}", transactionId, username, year, month);

        try {
            TrainerMonthlySummary summary = trainerWorkloadService.getTrainerMonthlySummary(username, year, month);
            if (summary != null) {
                transactionLogger.info("[{}] Response: 200 OK, Message: Successfully retrieved summary.", transactionId);
                return ResponseEntity.ok(summary);
            } else {
                transactionLogger.warn("[{}] Response: 404 Not Found, Message: Summary not found for trainer {} in {}-{}",
                        transactionId, username, year, month);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Summary not found for trainer " + username + " in " + year + "-" + month);
            }
        } catch (Exception e) {
            transactionLogger.error("[{}] Response: 500 Internal Server Error, Message: {}", transactionId, e.getMessage(), e);
            operationLogger.error("[{}] Error fetching monthly summary for trainer {}: {}", transactionId, username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving summary: " + e.getMessage());
        } finally {
            TransactionContext.clearTransactionId();
        }
    }
}