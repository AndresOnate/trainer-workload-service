package com.epam.gymapp.dto;


import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TrainerWorkloadRequest implements Serializable {
    
    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username cannot exceed 30 characters")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Trainer active status is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1 minute")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public TrainerWorkloadRequest(String username, String firstName, String lastName,
                                  Boolean isActive, LocalDate trainingDate,
                                  Integer trainingDuration, ActionType actionType) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.actionType = actionType;
    }

    public TrainerWorkloadRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public String toString() {
        return "TrainerWorkloadRequest{" +
               "username='" + username + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", isActive=" + isActive +
               ", trainingDate=" + trainingDate +
               ", trainingDuration=" + trainingDuration +
               ", actionType=" + actionType +
               '}';
    }
}
