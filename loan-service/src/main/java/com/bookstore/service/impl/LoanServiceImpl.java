package com.bookstore.service.impl;

import com.bookstore.client.BookClient;
import com.bookstore.client.InventoryClient;
import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanItem;
import com.bookstore.enums.LoanStatus;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.LoanRepository;
import com.bookstore.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final InventoryClient inventoryClient;
    private final BookClient bookClient;

    private static final double DAILY_FINE_RATE = 1.0;

    @Override
    public LoanResponse createLoan(LoanCreateRequest request) {
        log.info("Creating new loan for member ID: {} and Name: {}", request.getMemberId(), request.getMemberName());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Loan must contain at least one item.");
        }

        // Prevent duplicate active loans for the same book by the same user
        List<Loan> userLoans = loanRepository.findByMemberId(request.getMemberId());
        for (Loan existingLoan : userLoans) {
            if (existingLoan.getStatus() == LoanStatus.ACTIVE ||
                    existingLoan.getStatus() == LoanStatus.OVERDUE ||
                    existingLoan.getStatus() == LoanStatus.RENEWAL_PENDING) {
                for (LoanItem existingItem : existingLoan.getItems()) {
                    for (var requestedItem : request.getItems()) {
                        if (existingItem.getBookId().equals(requestedItem.getBookId())) {
                            log.warn("User ID {} already has an active loan for book ID {}", request.getMemberId(), requestedItem.getBookId());
                            throw new IllegalStateException("You already have an active loan for this book.");
                        }
                    }
                }
            }
        }

        for (var itemDto : request.getItems()) {
            if (itemDto.getCopyId() == null) {
                itemDto.setCopyId(1L);
            }

            boolean isAvailable = inventoryClient.checkAvailability(itemDto.getBookId(), itemDto.getCopyId());
            if (!isAvailable) {
                log.warn("Book copy ID {} is not available for loan.", itemDto.getCopyId());
                throw new IllegalStateException("Book copy is not available.");
            }
        }

        Loan loan = Loan.builder()
                .memberId(request.getMemberId())
                .memberName(request.getMemberName() != null ? request.getMemberName() : "Attique")
                .loanDate(LocalDate.now())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(14))
                .fineAmount(0.0)
                .renewed(false)
                .status(LoanStatus.ACTIVE)
                .build();

        List<LoanItem> items = request.getItems().stream()
                .map(itemDto -> LoanItem.builder()
                        .loan(loan)
                        .bookId(itemDto.getBookId())
                        .copyId(itemDto.getCopyId() != null ? itemDto.getCopyId() : 1L)
                        .build())
                .collect(Collectors.toList());

        loan.setItems(items);
        Loan savedLoan = loanRepository.save(loan);
        log.info("Successfully created loan with ID: {}", savedLoan.getId());

        for (var item : savedLoan.getItems()) {
            try {
                inventoryClient.borrowBook(item.getBookId());
                log.info("Successfully decremented inventory for Book ID: {}", item.getBookId());
            } catch (Exception e) {
                log.error("Failed to decrement inventory for Book ID: {}", item.getBookId(), e);
            }
        }

        return mapToResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        log.info("Fetching all loans in the system");
        return loanRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansForUser(String username) {
        log.info("Fetching loans for user identifier from token: {}", username);
        if (username == null || username.trim().isEmpty()) {
            return List.of();
        }
        Long memberId;
        try {
            memberId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            memberId = Math.abs((long) username.hashCode());
        }
        return getLoansByMemberId(memberId);
    }

    @Override
    public LoanResponse returnLoan(Long id) {
        log.info("Processing return for loan ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));

        LocalDate returnDate = LocalDate.now();
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(returnDate);

        if (loan.getDueDate() != null && returnDate.isAfter(loan.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), returnDate);
            double fine = overdueDays * DAILY_FINE_RATE;
            loan.setFineAmount(fine);
            log.info("Loan ID {} is {} days overdue. Calculated fine: ${}", id, overdueDays, fine);
        } else {
            loan.setFineAmount(0.0);
        }

        Loan updatedLoan = loanRepository.save(loan);
        log.info("Successfully returned loan ID: {}", id);

        for (var item : updatedLoan.getItems()) {
            try {
                inventoryClient.returnBook(item.getBookId());
                log.info("Successfully incremented inventory for Book ID: {}", item.getBookId());
            } catch (Exception e) {
                log.error("Failed to increment inventory for Book ID: {}", item.getBookId(), e);
            }
        }

        return mapToResponse(updatedLoan);
    }

    @Override
    public LoanResponse renewLoan(Long id) {
        log.info("User requested renewal for loan ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Only active or overdue loans can request renewal.");
        }

        if (loan.isRenewed()) {
            throw new IllegalStateException("This book has already been renewed once and cannot be renewed again.");
        }

        loan.setStatus(LoanStatus.RENEWAL_PENDING);
        Loan updatedLoan = loanRepository.save(loan);
        log.info("Loan ID {} marked as RENEWAL_PENDING, waiting for admin approval.", id);

        return mapToResponse(updatedLoan);
    }

    @Override
    public LoanResponse approveRenewal(Long id) {
        log.info("Admin approving renewal for loan ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));

        if (loan.getStatus() != LoanStatus.RENEWAL_PENDING) {
            throw new IllegalStateException("This loan does not have a pending renewal request.");
        }

        LocalDate newDueDate = loan.getDueDate().plusDays(7);
        loan.setDueDate(newDueDate);
        loan.setRenewed(true);
        loan.setStatus(LoanStatus.ACTIVE);

        Loan updatedLoan = loanRepository.save(loan);
        log.info("Renewal approved successfully for loan ID: {}. New due date: {}", id, newDueDate);

        return mapToResponse(updatedLoan);
    }

    @Override
    public LoanResponse rejectRenewal(Long id) {
        log.info("Admin rejecting renewal for loan ID: {}", id);
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));

        if (loan.getStatus() == LoanStatus.RENEWAL_PENDING) {
            loan.setStatus(LoanStatus.ACTIVE);
            loan = loanRepository.save(loan);
        }
        return mapToResponse(loan);
    }

    private LoanResponse mapToResponse(Loan loan) {
        List<LoanResponse.LoanItemResponse> itemResponses = loan.getItems().stream().map(item -> {
            String bookTitle = "Unknown Book";
            try {
                var bookDto = bookClient.getBookById(item.getBookId());
                if (bookDto != null && bookDto.getTitle() != null) {
                    bookTitle = bookDto.getTitle();
                }
            } catch (Exception e) {
                log.warn("Could not fetch book title for book ID: {}", item.getBookId());
            }

            return LoanResponse.LoanItemResponse.builder()
                    .id(item.getId())
                    .bookId(item.getBookId())
                    .copyId(item.getCopyId())
                    .bookTitle(bookTitle)
                    .build();
        }).collect(Collectors.toList());

        return LoanResponse.builder()
                .id(loan.getId())
                .memberId(loan.getMemberId())
                .memberName(loan.getMemberName()) // <-- Mapped successfully
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .fineAmount(loan.getFineAmount() != null ? loan.getFineAmount() : 0.0)
                .status(loan.getStatus())
                .items(itemResponses)
                .createdAt(loan.getCreatedAt())
                .build();
    }
}