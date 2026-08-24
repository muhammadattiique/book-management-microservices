package com.bookstore.kafka;

import com.bookstore.entity.Notification;
import com.bookstore.event.BookReturnedEvent;
import com.bookstore.event.LoanCreatedEvent;
import com.bookstore.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "loan-created-topic", groupId = "notification-group")
    public void handleLoanCreated(String messagePayload) {
        try {
            LoanCreatedEvent event = objectMapper.readValue(messagePayload, LoanCreatedEvent.class);
            log.info("Received LoanCreatedEvent for loan ID: {}", event.getLoanId());

            Notification notification = Notification.builder()
                    .memberId(event.getMemberId())
                    .eventType("LOAN_CREATED")
                    .message("Loan created successfully for loan ID: " + event.getLoanId())
                    .build();

            notificationRepository.save(notification);
            log.info("Saved notification for LOAN_CREATED event (Loan ID: {}).", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to parse LoanCreatedEvent message: {}", messagePayload, e);
        }
    }

    @KafkaListener(topics = "book-returned-topic", groupId = "notification-group")
    public void handleBookReturned(String messagePayload) {
        try {
            BookReturnedEvent event = objectMapper.readValue(messagePayload, BookReturnedEvent.class);
            log.info("Received BookReturnedEvent for loan ID: {}", event.getLoanId());

            Notification notification = Notification.builder()
                    .memberId(101L) // Default member mapping
                    .eventType("BOOK_RETURNED")
                    .message("Book returned successfully for loan ID: " + event.getLoanId())
                    .build();

            notificationRepository.save(notification);
            log.info("Saved notification for BOOK_RETURNED event (Loan ID: {}).", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to parse BookReturnedEvent message: {}", messagePayload, e);
        }
    }
}