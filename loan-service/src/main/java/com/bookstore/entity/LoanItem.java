package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Long bookId; // Microservice identifier

    @Column(nullable = false)
    private Long copyId; // Microservice identifier
}