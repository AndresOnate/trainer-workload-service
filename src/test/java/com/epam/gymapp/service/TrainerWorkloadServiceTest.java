package com.epam.gymapp.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gymapp.dto.ActionType;
import com.epam.gymapp.dto.TrainerMonthlySummary;
import com.epam.gymapp.dto.TrainerWorkloadRequest;
import com.epam.gymapp.exception.trainer.MonthSummaryNotFoundException;
import com.epam.gymapp.exception.trainer.TrainerNotFoundException;
import com.epam.gymapp.exception.trainer.YearSummaryNotFoundException;
import com.epam.gymapp.model.MonthlySummary;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.model.YearlySummary;
import com.epam.gymapp.repository.TrainerSummaryRepository;
import com.epam.gymapp.util.TransactionContext;

@ExtendWith(MockitoExtension.class)
public class TrainerWorkloadServiceTest {

    @Mock
    private TrainerSummaryRepository trainerSummaryRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @BeforeEach
    void setUp() {
        TransactionContext.setTransactionId("test-tx-id");
    }

    @Test
    void testUpdateTrainerWorkload_AddAction_NewTrainer() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "user1", "John", "Doe", true,
                LocalDate.of(2024, 5, 1), 60, ActionType.ADD
        );

        when(trainerSummaryRepository.findByUsername("user1")).thenReturn(Optional.empty());

        trainerWorkloadService.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerSummary> captor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(trainerSummaryRepository).save(captor.capture());

        TrainerSummary saved = captor.getValue();
        assertEquals("user1", saved.getUsername());
        assertEquals(true, saved.getTrainerStatus());
        assertEquals(60, saved.getYearSummary(2024).getMonthSummary(5).getTrainingSummaryDuration());
    }

    @Test
    void testUpdateTrainerWorkload_DeleteAction_ReduceToZero() {
        TrainerSummary summary = new TrainerSummary();
        summary.setUsername("user2");
        summary.setFirstName("Jane");
        summary.setLastName("Smith");
        summary.setTrainerStatus(true);

        YearlySummary year = new YearlySummary(2023);
        MonthlySummary month = new MonthlySummary(3, 30);
        month.setYearlySummary(year);
        year.getMonths().add(month);
        year.setTrainerSummary(summary);
        summary.getYears().add(year);

        when(trainerSummaryRepository.findByUsername("user2")).thenReturn(Optional.of(summary));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "user2", "Jane", "Smith", true,
                LocalDate.of(2023, 3, 15), 30, ActionType.DELETE
        );

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(0, summary.getYearSummary(2023).getMonthSummary(3).getTrainingSummaryDuration());
    }

        @Test
    void testGetTrainerSummary_AllData() {
        TrainerSummary summary = new TrainerSummary();
        summary.setUsername("user3");
        summary.setFirstName("Paul");
        summary.setLastName("Walker");
        summary.setTrainerStatus(true);

        YearlySummary year = new YearlySummary(2024);
        year.setTrainerSummary(summary);
        year.getMonths().add(new MonthlySummary(1, 120));
        summary.getYears().add(year);

        when(trainerSummaryRepository.findByUsername("user3")).thenReturn(Optional.of(summary));

        TrainerMonthlySummary result = trainerWorkloadService.getTrainerSummary("user3", null, null);

        assertEquals("user3", result.getUsername());
        assertEquals(1, result.getYears().size());
        assertEquals(120, result.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
    }

        @Test
    void testGetTrainerSummary_FilterByYear() {
        TrainerSummary summary = new TrainerSummary();
        summary.setUsername("user4");

        YearlySummary y2023 = new YearlySummary(2023);
        y2023.getMonths().add(new MonthlySummary(4, 50));
        y2023.setTrainerSummary(summary);

        YearlySummary y2024 = new YearlySummary(2024);
        y2024.getMonths().add(new MonthlySummary(5, 70));
        y2024.setTrainerSummary(summary);

        summary.getYears().addAll(List.of(y2023, y2024));

        when(trainerSummaryRepository.findByUsername("user4")).thenReturn(Optional.of(summary));

        TrainerMonthlySummary result = trainerWorkloadService.getTrainerSummary("user4", 2023, null);

        assertEquals(1, result.getYears().size());
        assertEquals(4, result.getYears().get(0).getMonths().get(0).getMonth());
    }

    @Test
    void testGetTrainerSummary_NotFound_Trainer() {
        when(trainerSummaryRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(TrainerNotFoundException.class, () ->
                trainerWorkloadService.getTrainerSummary("missing", null, null));
    }

    @Test
    void testGetTrainerSummary_NotFound_Year() {
        TrainerSummary summary = new TrainerSummary();
        summary.setUsername("user5");
        summary.getYears().add(new YearlySummary(2023));

        when(trainerSummaryRepository.findByUsername("user5")).thenReturn(Optional.of(summary));

        assertThrows(YearSummaryNotFoundException.class, () ->
                trainerWorkloadService.getTrainerSummary("user5", 2024, null));
    }

    @Test
    void testGetTrainerSummary_NotFound_Month() {
        TrainerSummary summary = new TrainerSummary();
        summary.setUsername("user6");
        YearlySummary year = new YearlySummary(2023);
        year.setTrainerSummary(summary);
        summary.getYears().add(year);

        when(trainerSummaryRepository.findByUsername("user6")).thenReturn(Optional.of(summary));

        assertThrows(MonthSummaryNotFoundException.class, () ->
                trainerWorkloadService.getTrainerSummary("user6", 2023, 5));
    }
}
