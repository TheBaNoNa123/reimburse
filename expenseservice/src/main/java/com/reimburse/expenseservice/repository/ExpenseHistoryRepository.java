package com.reimburse.expenseservice.repository;

import com.reimburse.expenseservice.entity.ExpenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseHistoryRepository extends JpaRepository<ExpenseHistory, Long> {
}
