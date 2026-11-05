package com.reimburse.expenseservice.dto;

import com.reimburse.expenseservice.entity.ExpenseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;

    private ExpenseStatus status;

    private String title;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private Long categoryId;

    private String categoryName;

}
