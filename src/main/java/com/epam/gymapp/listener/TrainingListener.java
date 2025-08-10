package com.epam.gymapp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.service.TrainerWorkloadService;

@Component
public class TrainingListener {

    private final TrainerWorkloadService trainerWorkloadService;
    private static final Logger listenerLogger = LoggerFactory.getLogger("listenerLogger");

    public TrainingListener(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }
    
    @JmsListener(destination = "training.queue")
    public void procesarMensaje(TrainerWorkloadRequest dto) {
        listenerLogger.info("Processing workload for trainer: {}", dto.getTrainingDuration());
        try {
            if (dto.getUsername() == null || dto.getActionType() == null) {
                throw new IllegalArgumentException("Missing required fields in message");
            }
            trainerWorkloadService.updateTrainerWorkload(dto);
            listenerLogger.info("Successfully processed workload for trainer: {}", dto.getUsername());
        } catch (Exception e) {
            listenerLogger.error("Error processing workload for trainer: {}. Error: {}", dto.getUsername(), e.getMessage());
        }

    }
}
