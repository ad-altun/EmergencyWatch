package de.denizaltun.analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "de.denizaltun.analyticsservice.repository.jpa")
@EnableMongoRepositories(basePackages = "de.denizaltun.analyticsservice.repository.mongo")
public class AnalyticsServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
	}
}
