package com.bookstore.service;

import com.bookstore.dto.LoanCreateRequest;
import com.bookstore.dto.LoanResponse;
import java.util.List;

public interface LoanService {
    LoanResponse createLoan(LoanCreateRequest request);
    LoanResponse getLoanById(Long id);
    List<LoanResponse> getLoansByMemberId(Long memberId);
    LoanResponse returnLoan(Long id);
}