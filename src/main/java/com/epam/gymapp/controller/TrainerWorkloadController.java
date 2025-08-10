package com.epam.gymapp.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.service.TrainerWorkloadService;
import com.epam.gymapp.util.TransactionContext;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/trainer-workload")
public class TrainerWorkloadController {

    private static final Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        String transactionId = UUID.randomUUID().toString();
        TransactionContext.setTransactionId(transactionId);

        transactionLogger.info("[{}] Endpoint: POST /api/workload/update, Request: {}", transactionId, request.toString());
        operationLogger.info("[{}] Processing trainer workload update for trainer: {}", transactionId, request.getUsername());

        try {
            trainerWorkloadService.updateTrainerWorkload(request);
            transactionLogger.info("[{}] Response: 200 OK, Message: Trainer workload updated successfully.", transactionId);
            return ResponseEntity.ok("Trainer workload updated successfully.");
        } finally {
            TransactionContext.clearTransactionId();
        }
    }

    @GetMapping("/summary/{username}")
    public ResponseEntity<?> getTrainerSummary(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        String transactionId = UUID.randomUUID().toString();
        TransactionContext.setTransactionId(transactionId);

        transactionLogger.info("[{}] Endpoint: GET /api/workload/summary/{}, Request Params: username={}, year={}, month={}",
                transactionId, username, username, year, month);

        try {
            TrainerMonthlySummary summary = trainerWorkloadService.getTrainerSummary(username, year, month);
            transactionLogger.info("[{}] Response: 200 OK, Message: Successfully retrieved summary.", transactionId);
            return ResponseEntity.ok(summary);
        } finally {
            TransactionContext.clearTransactionId();
        }
    }
}