package com.epam.gymapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.epam.gymapp.model.TrainerSummary;

public class TrainerWorkloadCustomRepositoryImpl implements TrainerWorkloadCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public TrainerWorkloadCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setMonthlyDuration(String username, int year, int month, int durationChange) {
        Query query = new Query(
            Criteria.where("_id").is(username)
                    .and("years.year").is(year)
                    .and("years.months.month").is(month)
        );

        Update update = new Update()
            .inc("years.$[year].months.$[month].trainingSummaryDuration", durationChange);

        update.filterArray("year.year", year);
        update.filterArray("month.month", month);

        mongoTemplate.updateFirst(query, update, TrainerSummary.class);
    }

}
