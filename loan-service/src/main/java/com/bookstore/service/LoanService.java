package com.bookstore.service;

import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import java.util.List;

public interface LoanService {
    LoanResponse createLoan(LoanCreateRequest request);
    LoanResponse getLoanById(Long id);
    List<LoanResponse> getLoansByMemberId(Long memberId);
    List<LoanResponse> getLoansForUser(String username);
    LoanResponse returnLoan(Long id);
    LoanResponse renewLoan(Long id); // <--- Added renew method signature
}