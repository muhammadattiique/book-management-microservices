package com.bookstore.service;

import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import java.util.List;

public interface LoanService {
    LoanResponse createLoan(LoanCreateRequest request);
    List<LoanResponse> getAllLoans();
    LoanResponse getLoanById(Long id);
    List<LoanResponse> getLoansByMemberId(Long memberId);
    List<LoanResponse> getLoansForUser(String username);
    LoanResponse returnLoan(Long id);
    LoanResponse renewLoan(Long id);
    LoanResponse approveRenewal(Long id); // <-- Yeh add hona zaroori hai
    LoanResponse rejectRenewal(Long id); // <-- Yeh bhi add hona zaroori hai
}