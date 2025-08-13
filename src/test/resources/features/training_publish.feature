Feature: TrainerSummary model validation

  Scenario: Empty username
    Given a trainer with username ""
    When I validate the trainer
    Then it should have an error with message "Username is required"

  Scenario: Month out of range
    Given a valid trainer
    And I set the month to 15
    When I validate the trainer
    Then it should have an error with message "Month must be between 1 and 12"

  Scenario: Negative duration
    Given a valid trainer
    And I set the duration to -10
    When I validate the trainer
    Then it should have an error with message "Training summary duration cannot be negative"

  Scenario: Duration is null
    Given a valid trainer
    And I set the duration to null
    When I validate the trainer
    Then it should have an error with message "Training summary duration is required"

  Scenario: Negative month
    Given a valid trainer
    And I set the month to -5
    When I validate the trainer
    Then it should have an error with message "Month must be between 1 and 12"

  Scenario: Username too long
    Given a trainer with username "averyveryverylongusernamethatexceedslimit4546"
    When I validate the trainer
    Then it should have an error with message "Username cannot exceed 30 characters"

