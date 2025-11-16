package com.paymentchain.notification.listener;

import com.paymentchain.notification.events.TransactionCreatedEvent;
import com.paymentchain.notification.service.NotificationService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration test for TransactionEventListener.
 * Uses @EmbeddedKafka to test actual Kafka consumption.
 *
 * @author benas
 */
@Disabled("Requires Kafka broker running - test manually or in integration environment")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"transaction.created"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
        }
)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class TransactionEventListenerTest {

    private static final String TOPIC = "transaction.created";

    @Autowired
    private TransactionEventListener listener;

    @SpyBean
    private NotificationService notificationService;

    @Test
    void testOnTransactionCreated_ProcessesEventSuccessfully() throws Exception {
        // Given
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L,
                "ES1234567890",
                500.0,
                LocalDateTime.now()
        );

        KafkaTemplate<String, TransactionCreatedEvent> template = createKafkaTemplate();

        // When
        template.send(TOPIC, event.iban(), event).get();

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(notificationService, times(1))
                        .processTransactionNotification(argThat(e ->
                                e.id().equals(1L) &&
                                e.iban().equals("ES1234567890") &&
                                e.amount().equals(500.0)
                        ))
        );
    }

    @Test
    void testOnTransactionCreated_ProcessesHighValueTransaction() throws Exception {
        // Given - Amount > 1000 (should send EMAIL + SMS)
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                2L,
                "ES9876543210",
                1500.0,
                LocalDateTime.now()
        );

        KafkaTemplate<String, TransactionCreatedEvent> template = createKafkaTemplate();

        // When
        template.send(TOPIC, event.iban(), event).get();

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(notificationService, times(1))
                        .processTransactionNotification(argThat(e ->
                                e.amount() > 1000.0
                        ))
        );
    }

    @Test
    void testOnTransactionCreated_ProcessesMultipleEvents() throws Exception {
        // Given
        TransactionCreatedEvent event1 = new TransactionCreatedEvent(3L, "ES111", 100.0, LocalDateTime.now());
        TransactionCreatedEvent event2 = new TransactionCreatedEvent(4L, "ES222", 200.0, LocalDateTime.now());

        KafkaTemplate<String, TransactionCreatedEvent> template = createKafkaTemplate();

        // When
        template.send(TOPIC, event1.iban(), event1).get();
        template.send(TOPIC, event2.iban(), event2).get();

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(notificationService, times(2))
                        .processTransactionNotification(argThat(e ->
                                e.id().equals(3L) || e.id().equals(4L)
                        ))
        );
    }

    /**
     * Helper method to create KafkaTemplate for testing.
     */
    private KafkaTemplate<String, TransactionCreatedEvent> createKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, TransactionCreatedEvent> pf =
                new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(pf);
    }
}
