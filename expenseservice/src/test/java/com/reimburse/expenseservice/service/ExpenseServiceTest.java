package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.dto.ExpenseResponse;
import com.reimburse.expenseservice.dto.UpdateExpense;
import com.reimburse.expenseservice.entity.*;
import com.reimburse.expenseservice.exception.InvalidExpenseStateException;
import com.reimburse.expenseservice.exception.ResourceNotFoundException;
import com.reimburse.expenseservice.repository.CategoryRepository;
import com.reimburse.expenseservice.repository.ExpenseHistoryRepository;
import com.reimburse.expenseservice.repository.ExpenseRepository;
import com.reimburse.expenseservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ExpenseHistoryRepository expenseHistoryRepository;

    @InjectMocks private ExpenseService expenseService;

    @Test
    void createExpense_savesExpense_andReturnsId(){
        // Create user and category
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(1L);


        // Create fake expense
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setTitle("Vacation");
        request.setAmount(new BigDecimal(400));
        request.setExpenseDate(LocalDate.of(2026, 11, 10));
        request.setCategoryId(category.getId());

        // Looks up fake data
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // The mock save() does nothing by default, so the expense would keep a null id.
        // A real database assigns the id when saving, so we fake that here,
        // when the service calls save(any expense), grab that expense, give it id 99, and return it.
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        // Call method
        Long id = expenseService.createExpense(1L, request);

        assertEquals(99L, id);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_throwsNotFound_whenCategoryMissing(){
        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(1L);

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setTitle("Vacation");
        request.setAmount(new BigDecimal(400));
        request.setExpenseDate(LocalDate.of(2026, 11, 10));
        request.setCategoryId(category.getId());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.createExpense(1L, request);
        });

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void updateExpense_throwsInvalidExpense_ifUpdateNonPending(){
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.APPROVED);

        UpdateExpense request = new UpdateExpense();
        request.setTitle("Vacation");
        request.setAmount(new BigDecimal(400));
        request.setExpenseDate(LocalDate.of(2026, 11, 10));
        request.setCategoryId(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        assertThrows(InvalidExpenseStateException.class, () -> {
            expenseService.updateExpense(1L, request);
        });
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void approveExpense_writesHistory(){
        Category category = new Category();
        category.setId(1L);

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setCategory(category);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        ExpenseResponse result = expenseService.approveExpense(1L, "Sam");
        assertEquals(ExpenseStatus.APPROVED, result.getStatus());
        assertEquals("Sam", result.getReviewedBy());

        verify(expenseHistoryRepository).save(any(ExpenseHistory.class));
    }
}
