package com.spring.monew.activity.config;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@TestConfiguration
@Profile("activity-mongo")
@ImportAutoConfiguration({
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@EnableMongoRepositories(
    basePackages = "com.spring.monew.activity.repository",
    considerNestedRepositories = true
)
@AutoConfigurationPackage
public class ActivityMongoConfig { }