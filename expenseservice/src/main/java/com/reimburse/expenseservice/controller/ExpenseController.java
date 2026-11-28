package com.reimburse.expenseservice.controller;


import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.dto.ExpenseResponse;
import com.reimburse.expenseservice.dto.UpdateExpense;
import com.reimburse.expenseservice.entity.CurrentUser;
import com.reimburse.expenseservice.entity.ExpenseStatus;
import com.reimburse.expenseservice.entity.User;
import com.reimburse.expenseservice.service.ExpenseService;
import com.reimburse.expenseservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    private CurrentUser currentUser(Authentication auth){
        Jwt jwt = (Jwt) auth.getPrincipal();
        User user = userService.getOrCreateUser(jwt.getSubject(), jwt.getClaimAsString("username"));
        boolean manager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        return new CurrentUser(user.getId(), jwt.getSubject(), manager);
    }

    @PostMapping
    public ResponseEntity<Long> createExpense(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateExpenseRequest request){

        User user = userService.getOrCreateUser(jwt.getSubject(), jwt.getClaimAsString("username"));
        Long id = expenseService.createExpense(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense( Authentication auth, @PathVariable Long expenseId){
        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.getExpense(expenseId, caller));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            Authentication auth,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable){

        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.getExpenses(status, categoryId, from, to, pageable, caller));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<Long> updateExpense(@PathVariable Long expenseId, @Valid @RequestBody UpdateExpense request, Authentication auth){
        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request, caller));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId, Authentication auth){
        CurrentUser caller = currentUser(auth);
        expenseService.deleteExpense(expenseId, caller);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{expenseId}/accepted")
    public ResponseEntity<ExpenseResponse> approveExpense(@PathVariable Long expenseId, Authentication auth){
        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.approveExpense(expenseId, caller));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{expenseId}/rejected")
    public ResponseEntity<ExpenseResponse> rejectExpense(@PathVariable Long expenseId, Authentication auth){
        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.rejectExpense(expenseId, caller));
    }

    @PutMapping("/{expenseId}/cancelled")
    public ResponseEntity<ExpenseResponse> cancelExpense(@PathVariable Long expenseId, Authentication auth){
        CurrentUser caller = currentUser(auth);
        return ResponseEntity.ok(expenseService.cancelExpense(expenseId, caller));
    }
}
