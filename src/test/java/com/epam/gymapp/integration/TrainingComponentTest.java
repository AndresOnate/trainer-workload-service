package com.epam.gymapp.integration;

import org.junit.runner.RunWith;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.epam.gymapp.integration"},
    plugin = {"pretty"}
)
public class TrainingComponentTest {
}
