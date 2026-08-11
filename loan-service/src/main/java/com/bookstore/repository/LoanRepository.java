package com.bookstore.repository;

import com.bookstore.entity.Loan;
import com.bookstore.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByMemberId(Long memberId);
    List<Loan> findByStatus(LoanStatus status);
}