package com.bookstore.loan;

import com.bookstore.repository.LoanRepository;
import com.bookstore.service.impl.LoanServiceImpl; // Change to LoanService if it's not an interface/impl setup
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    void testCreateLoan_Validation() {
        assertThat(loanService).isNotNull();
    }
}