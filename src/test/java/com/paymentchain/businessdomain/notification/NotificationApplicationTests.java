package com.paymentchain.businessdomain.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
	}
)
@ActiveProfiles("test")
@EmbeddedKafka(
	partitions = 1,
	topics = {"transaction.created"},
	brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"}
)
class NotificationApplicationTests {

	@Test
	void contextLoads() {
		// Test passes if Spring context loads successfully with embedded Kafka
	}

}
