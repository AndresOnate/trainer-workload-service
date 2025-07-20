package com.epam.gymapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.epam.gymapp.model.TrainerSummary;

import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends JpaRepository<TrainerSummary, String> {
    Optional<TrainerSummary> findByUsername(String trainerUsername);
}