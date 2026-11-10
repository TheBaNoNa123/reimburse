package com.reimburse.expenseservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="expense_history")
public class ExpenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="expense_id", nullable = false)
    private Expense expense;

    @Enumerated(EnumType.STRING)
    @Column(name="expense_status", nullable = false, length = 20)
    private ExpenseStatus status;

    @Column(name="actor", nullable = false)
    private String actor;

    @Column(name="acted_at", nullable = false)
    private Instant actedAt;

}


