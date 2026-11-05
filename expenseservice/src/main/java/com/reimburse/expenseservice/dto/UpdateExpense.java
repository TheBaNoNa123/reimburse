package com.reimburse.expenseservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpense {

    private String title;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private Long categoryId;

}
