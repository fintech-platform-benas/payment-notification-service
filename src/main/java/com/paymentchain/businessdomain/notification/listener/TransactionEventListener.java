package com.paymentchain.businessdomain.notification.listener;

import com.paymentchain.businessdomain.notification.events.TransactionCreatedEvent;
import com.paymentchain.businessdomain.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for transaction created events.
 * Consumes messages from transaction.created topic and processes notifications.
 *
 * @author benas
 */
@Component
public class TransactionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);

    private final NotificationService notificationService;

    public TransactionEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Listens to transaction.created topic and processes events.
     * Uses manual acknowledgment for at-least-once delivery guarantee.
     *
     * @param record Kafka consumer record
     * @param acknowledgment Manual acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topic.transaction-created:transaction.created}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransactionCreated(ConsumerRecord<String, TransactionCreatedEvent> record,
                                     Acknowledgment acknowledgment) {
        try {
            TransactionCreatedEvent event = record.value();

            logger.info("Received transaction event from topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            // Process notification
            notificationService.processTransactionNotification(event);

            // Manual acknowledgment (at-least-once delivery)
            acknowledgment.acknowledge();

            logger.info("Successfully processed and acknowledged transaction event: id={}",
                    event.id());

        } catch (Exception e) {
            logger.error("Error processing transaction event at offset {}: {}",
                    record.offset(), e.getMessage(), e);
            // In production: send to DLQ (Dead Letter Queue)
            // For now: acknowledge to avoid blocking the consumer
            acknowledgment.acknowledge();
        }
    }
}
