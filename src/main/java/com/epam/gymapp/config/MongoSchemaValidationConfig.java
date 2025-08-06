package com.epam.gymapp.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationOptions;

import java.util.List;

import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import jakarta.annotation.PostConstruct;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MongoSchemaValidationConfig {

    private final MongoTemplate mongoTemplate;
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");
    private static final String COLLECTION_NAME = "trainerSummary";

    public MongoSchemaValidationConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void setupSchemaValidation() {
        MongoJsonSchema trainerSummarySchema = buildTrainerSummarySchema();

        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            mongoTemplate.createCollection(COLLECTION_NAME, CollectionOptions.empty().schema(trainerSummarySchema));
            operationLogger.info("Collection '{}' created with schema validation.", COLLECTION_NAME);
        } else {
            Document command = new Document("collMod", COLLECTION_NAME)
                .append("validator",  trainerSummarySchema.toDocument())
                .append("validationLevel", "strict")
                .append("validationAction", "error");

            mongoTemplate.getDb().runCommand(command);
            operationLogger.info("Schema validation for collection '{}' updated.", COLLECTION_NAME);
        }
    }

    private MongoJsonSchema buildTrainerSummarySchema() {
        return MongoJsonSchema.builder()
            .required("username", "firstName", "lastName", "trainerStatus")
            .properties(
                JsonSchemaProperty.string("username").description("The trainer's unique username."),
                JsonSchemaProperty.string("firstName").description("The trainer's first name."),
                JsonSchemaProperty.string("lastName").description("The trainer's last name."),
                JsonSchemaProperty.bool("trainerStatus").description("The active status of the trainer."),
                JsonSchemaProperty.array("years")
                    .items(buildYearlySummarySchema())
                    .description("A list of yearly summaries.")
            )
            .build();
    }

    private TypedJsonSchemaObject buildYearlySummarySchema() {
        return JsonSchemaObject.object()
            .required("year", "months")
            .properties(
                JsonSchemaProperty.int32("year").description("The year of the summary."),
                JsonSchemaProperty.array("months")
                    .items(buildMonthlySummarySchema())
                    .description("A list of monthly summaries.")
            );
    }

    private TypedJsonSchemaObject buildMonthlySummarySchema() {
        return JsonSchemaObject.object()
            .required("month", "trainingSummaryDuration")
            .properties(
                JsonSchemaProperty.int32("month").description("The month of the summary."),
                JsonSchemaProperty.int32("trainingSummaryDuration").description("The total training duration for the month.")
            );
    }
}