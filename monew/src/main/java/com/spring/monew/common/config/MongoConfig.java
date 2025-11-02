package com.spring.monew.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.spring.monew.activity.repository")
public class MongoConfig {
}
