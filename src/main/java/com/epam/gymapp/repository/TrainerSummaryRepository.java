package com.epam.gymapp.repository;


import com.epam.gymapp.model.TrainerSummary;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerSummaryRepository extends MongoRepository<TrainerSummary, String> {

    Optional<TrainerSummary> findByUsername(String username);
    List<TrainerSummary> findByFirstNameAndLastName(String firstName, String lastName);
    List<TrainerSummary> findByTrainerStatus(Boolean trainerStatus);
    List<TrainerSummary> findByFirstNameContainingIgnoreCase(String partialName);


}