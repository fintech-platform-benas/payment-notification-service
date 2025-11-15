package com.paymentchain.businessdomain.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service - Consumes transaction events from Kafka
 * and sends notifications via email/SMS.
 *
 * @author benas
 */
@SpringBootApplication
@EnableKafka
public class NotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationApplication.class, args);
	}

}
