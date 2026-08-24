package com.bookstore.scheduler;

import com.bookstore.entity.Loan;
import com.bookstore.enums.LoanStatus;
import com.bookstore.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverdueLoanScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueLoanScheduler.class);
    private final LoanRepository loanRepository;

    public OverdueLoanScheduler(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    // Runs every night at midnight: "0 0 0 * * ?"
    // Use "0 * * * * ?" to run every 1 minute during testing
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void detectAndProcessOverdueLoans() {
        log.info("Starting scheduled job: Checking for overdue loans...");

        LocalDate today = LocalDate.now();
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.ACTIVE, today);

        if (overdueLoans.isEmpty()) {
            log.info("No overdue loans found.");
            return;
        }

        for (Loan loan : overdueLoans) {
            loan.setStatus(LoanStatus.OVERDUE);
            log.info("Loan ID {} for member ID {} marked as OVERDUE.", loan.getId(), loan.getMemberId());
        }

        loanRepository.saveAll(overdueLoans);
        log.info("Successfully updated {} loan(s) to OVERDUE status.", overdueLoans.size());
    }
}