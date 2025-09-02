package com.epam.gymapp.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.epam.gymapp.dto.ActionType;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.repository.TrainerSummaryRepository;


@SpringBootTest
@ActiveProfiles("test")
class TrainingListenerIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerSummaryRepository trainerSummaryRepository;

    @BeforeEach
    void setUp() {
        trainerSummaryRepository.deleteAll();
    }

    @Test
    void shouldReceiveMessageAndUpdateDatabase() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setUsername("trainer1");
        request.setActionType(ActionType.ADD);
        request.setTrainingDate(LocalDate.of(2025, 8, 1));
        request.setTrainingDuration(90);
        request.setFirstName("Mike");
        request.setLastName("Trainer");
        request.setIsActive(true);

        jmsTemplate.convertAndSend("training.queue", request);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerSummary> trainer = trainerSummaryRepository.findByUsername("trainer1");
            assertTrue(trainer.isPresent());
            assertEquals(90, (int) trainer.get().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
        });

    }
}
