package com.paymentchain.notification.service;

import com.paymentchain.notification.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending notifications based on transaction events.
 * Business logic for different notification types based on transaction amount.
 *
 * @author benas
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final double HIGH_AMOUNT_THRESHOLD = 1000.0;

    /**
     * Processes a transaction event and sends appropriate notifications.
     *
     * Business rules:
     * - Amount > 1000€: Send EMAIL + SMS
     * - Amount ≤ 1000€: Send EMAIL only
     *
     * @param event The transaction created event
     */
    public void processTransactionNotification(TransactionCreatedEvent event) {
        logger.info("Processing notification for transaction: id={}, iban={}, amount={}",
                event.id(), event.iban(), event.amount());

        if (event.amount() > HIGH_AMOUNT_THRESHOLD) {
            sendEmail(event);
            sendSMS(event);
            logger.info("📧 EMAIL + 📱 SMS sent for high-value transaction: id={}, amount={}",
                    event.id(), event.amount());
        } else {
            sendEmail(event);
            logger.info("📧 EMAIL sent for transaction: id={}, amount={}",
                    event.id(), event.amount());
        }
    }

    /**
     * Simulates sending an email notification.
     *
     * @param event The transaction event
     */
    private void sendEmail(TransactionCreatedEvent event) {
        // In production: integrate with email service (SendGrid, AWS SES, etc.)
        logger.debug("Email notification sent to account {}: Transaction {} for amount {}",
                event.iban(), event.id(), event.amount());
    }

    /**
     * Simulates sending an SMS notification.
     *
     * @param event The transaction event
     */
    private void sendSMS(TransactionCreatedEvent event) {
        // In production: integrate with SMS service (Twilio, AWS SNS, etc.)
        logger.debug("SMS notification sent to account {}: High-value transaction {} for amount {}",
                event.iban(), event.id(), event.amount());
    }
}
