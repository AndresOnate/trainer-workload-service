package com.epam.gymapp.integration;

import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class WorkloadSpyListener {

    @Autowired
    private ObjectMapper objectMapper;

    private static TrainerWorkloadRequest lastReceived;

    @JmsListener(destination = "training.queue", concurrency = "1-1")
    public void intercept(@Payload TrainerWorkloadRequest request, Message message) throws Exception {
        lastReceived = request;
        System.out.println("Received workload request: " + objectMapper.writeValueAsString(request));
    }

    public static void clear() {
        lastReceived = null;
    }

    public static TrainerWorkloadRequest getLastReceived() {
        return lastReceived;
    }
}


