package com.bookstore.repository;

import com.bookstore.entity.Loan;
import com.bookstore.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByStatus(LoanStatus status);

    // Added for Day 5 Task 1: Finds active loans where the due date is before today
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
}