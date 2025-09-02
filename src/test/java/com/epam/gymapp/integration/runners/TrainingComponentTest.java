package com.epam.gymapp.integration.runners;

import org.junit.runner.RunWith;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/integration",
    glue = {"com.epam.gymapp.integration.steps"},
    plugin = {"pretty"}
)
public class TrainingComponentTest {
}
