package com.reimburse.expenseservice.controller;


import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.dto.ExpenseResponse;
import com.reimburse.expenseservice.dto.UpdateExpense;
import com.reimburse.expenseservice.entity.ExpenseStatus;
import com.reimburse.expenseservice.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Long> createExpense(@PathVariable Long userId, @Valid @RequestBody CreateExpenseRequest request){

        Long id = expenseService.createExpense(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(@PathVariable Long expenseId){

        return ResponseEntity.ok(expenseService.getExpense(expenseId));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable){

        return ResponseEntity.ok(expenseService.getExpenses(status, categoryId, from, to, pageable));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<Long> updateExpense(@PathVariable Long expenseId, @Valid @RequestBody UpdateExpense request){

        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId){
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{expenseId}/accepted")
    public ResponseEntity<ExpenseResponse> approveExpense(@PathVariable Long expenseId, @RequestParam String reviewer){
        return ResponseEntity.ok(expenseService.approveExpense(expenseId, reviewer));
    }

    @PostMapping("/{expenseId}/rejected")
    public ResponseEntity<ExpenseResponse> rejectExpense(@PathVariable Long expenseId, @RequestParam String reviewer){
        return ResponseEntity.ok(expenseService.rejectExpense(expenseId, reviewer));
    }
}
