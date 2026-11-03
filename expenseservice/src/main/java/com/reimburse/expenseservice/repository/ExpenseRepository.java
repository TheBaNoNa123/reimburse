package com.reimburse.expenseservice.repository;

import com.reimburse.expenseservice.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
