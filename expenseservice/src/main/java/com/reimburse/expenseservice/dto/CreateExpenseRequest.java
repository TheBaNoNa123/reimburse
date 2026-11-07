package com.reimburse.expenseservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 250)
    private String description;

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    private BigDecimal amount;

    @NotNull
    @PastOrPresent
    private LocalDate expenseDate;

    @NotNull
    private Long categoryId;

}
