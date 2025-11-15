package com.paymentchain.businessdomain.notification.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event received when a new transaction is created.
 * Mirrors the event from transaction-service.
 *
 * @param id Transaction unique identifier
 * @param iban Account IBAN number
 * @param amount Transaction amount
 * @param timestamp Event creation timestamp
 *
 * @author benas
 */
public record TransactionCreatedEvent(
        Long id,
        String iban,
        Double amount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) implements Serializable {
}
