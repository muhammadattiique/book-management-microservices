package com.bookstore.service.impl;

import com.bookstore.client.InventoryClient;
import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanItem;
import com.bookstore.enums.LoanStatus;
import com.bookstore.event.BookReturnedEvent;
import com.bookstore.event.ItemDto;
import com.bookstore.event.LoanCreatedEvent;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.LoanRepository;
import com.bookstore.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String LOAN_CREATED_TOPIC = "loan-created-topic";
    private static final String BOOK_RETURNED_TOPIC = "book-returned-topic";

    @Override
    public LoanResponse createLoan(LoanCreateRequest request) {
        log.info("Creating new loan for member ID: {}", request.getMemberId());

        // 1. Task 5 Requirement: Validate availability from inventory/book service via Feign
        for (var itemDto : request.getItems()) {
            boolean isAvailable = inventoryClient.checkAvailability(itemDto.getBookId(), itemDto.getCopyId());
            if (!isAvailable) {
                log.warn("Book copy ID {} is not available for loan.", itemDto.getCopyId());
                throw new IllegalStateException("Book copy with ID " + itemDto.getCopyId() + " is not available.");
            }
        }

        Loan loan = Loan.builder()
                .memberId(request.getMemberId())
                .loanDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .status(LoanStatus.ACTIVE)
                .build();

        List<LoanItem> items = request.getItems().stream()
                .map(itemDto -> LoanItem.builder()
                        .loan(loan)
                        .bookId(itemDto.getBookId())
                        .copyId(itemDto.getCopyId())
                        .build())
                .collect(Collectors.toList());

        loan.setItems(items);
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully created loan with ID: {}", savedLoan.getId());

        // 2. Map LoanItems to ItemDto list for the event
        List<ItemDto> itemDtos = savedLoan.getItems().stream()
                .map(item -> new ItemDto(item.getBookId(), item.getCopyId()))
                .collect(Collectors.toList());

        // 3. Task 5 Requirement: Publish LoanCreatedEvent to Kafka with items populated
        LoanCreatedEvent event = LoanCreatedEvent.builder()
                .loanId(savedLoan.getId())
                .memberId(savedLoan.getMemberId())
                .items(itemDtos)
                .build();

        kafkaTemplate.send(LOAN_CREATED_TOPIC, event);
        log.info("Published LoanCreatedEvent for loan ID: {}", savedLoan.getId());

        return mapToResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        log.info("Fetching loan with ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));
        return mapToResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByMemberId(Long memberId) {
        log.info("Fetching loans for member ID: {}", memberId);
        return loanRepository.findByMemberId(memberId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LoanResponse returnLoan(Long id) {
        log.info("Processing return for loan ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        Loan updatedLoan = loanRepository.save(loan);

        log.info("Successfully returned loan ID: {}", id);

        // 4. Task 5 Requirement: Publish BookReturnedEvent to Kafka
        BookReturnedEvent event = BookReturnedEvent.builder()
                .loanId(updatedLoan.getId())
                .copyIds(updatedLoan.getItems().stream().map(LoanItem::getCopyId).collect(Collectors.toList()))
                .returnDate(updatedLoan.getReturnDate())
                .build();

        kafkaTemplate.send(BOOK_RETURNED_TOPIC, event);
        log.info("Published BookReturnedEvent for loan ID: {}", updatedLoan.getId());

        return mapToResponse(updatedLoan);
    }

    private LoanResponse mapToResponse(Loan loan) {
        List<LoanResponse.LoanItemResponse> itemResponses = loan.getItems().stream()
                .map(item -> LoanResponse.LoanItemResponse.builder()
                        .id(item.getId())
                        .bookId(item.getBookId())
                        .copyId(item.getCopyId())
                        .build())
                .collect(Collectors.toList());

        return LoanResponse.builder()
                .id(loan.getId())
                .memberId(loan.getMemberId())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .items(itemResponses)
                .createdAt(loan.getCreatedAt())
                .build();
    }
}