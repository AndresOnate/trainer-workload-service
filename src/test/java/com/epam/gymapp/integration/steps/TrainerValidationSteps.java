package com.epam.gymapp.integration.steps;



import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import java.util.Set;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import com.epam.gymapp.model.MonthlySummary;
import com.epam.gymapp.model.TrainerSummary;
import com.epam.gymapp.model.YearlySummary;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TrainerValidationSteps {

    private TrainerSummary trainer;
    private Set<ConstraintViolation<TrainerSummary>> violations;
    private final Validator validator;

    public TrainerValidationSteps() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Given("a trainer with username {string}")
    public void a_trainer_with_username(String username) {
        trainer = new TrainerSummary();
        trainer.setUsername(username);
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setTrainerStatus(true);

        YearlySummary yearly = new YearlySummary();
        yearly.setYear(Year.now().getValue());

        MonthlySummary monthly = new MonthlySummary();
        monthly.setMonth(8);
        monthly.setTrainingSummaryDuration(60);

        yearly.getMonths().add(monthly);
        trainer.getYears().add(yearly);
    }

    @Given("a valid trainer")
    public void a_valid_trainer() {
        a_trainer_with_username("trainer1");
    }

    @And("I set the month to {int}")
    public void i_set_the_month_to(Integer month) {
        trainer.getYears().get(0).getMonths().get(0).setMonth(month);
    }


    @And("I set the duration to {int}")
    public void i_set_the_duration_to(Integer duration) {
        trainer.getYears().get(0).getMonths().get(0).setTrainingSummaryDuration(duration);
    }

    @And("I set the duration to null")
    public void i_set_the_duration_to_null() {
        trainer.getYears().get(0).getMonths().get(0).setTrainingSummaryDuration(null);
    }

    @When("I validate the trainer")
    public void i_validate_the_trainer() {
        violations = validator.validate(trainer);
    }

    @Then("it should have an error with message {string}")
    public void it_should_have_an_error_with_message(String message) {
        assertTrue(
            violations.stream().anyMatch(v -> v.getMessage().equals(message)),
            "Expected message not found: " + message
        );
    }
}