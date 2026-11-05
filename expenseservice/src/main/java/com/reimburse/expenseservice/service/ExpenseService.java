package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.dto.ExpenseResponse;
import com.reimburse.expenseservice.dto.UpdateExpense;
import com.reimburse.expenseservice.entity.Category;
import com.reimburse.expenseservice.entity.Expense;
import com.reimburse.expenseservice.entity.User;
import com.reimburse.expenseservice.repository.CategoryRepository;
import com.reimburse.expenseservice.repository.ExpenseRepository;
import com.reimburse.expenseservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, CategoryRepository categoryRepository){
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Long createExpense(Long userId, CreateExpenseRequest request){
        User user = findUser(userId);

        Category category = findCategory(request.getCategoryId());

        Expense expense = new Expense();

        // Define expense row
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);
        expense.setUser(user);

        expenseRepository.save(expense);

        return expense.getId();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long expenseId){

        Expense expense = findExpense(expenseId);

        return toResponse(expense);
    }

    @Transactional
    public Long updateExpense(Long expenseId, UpdateExpense request){

        Expense expense = findExpense(expenseId);

        Category category = findCategory(request.getCategoryId());

        // Define expense row
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);

        return expense.getId();
    }

    @Transactional
    public void deleteExpense(Long expenseId){
        expenseRepository.deleteById(expenseId);
    }

    // Helper Methods
    private ExpenseResponse toResponse(Expense expense){
        return new ExpenseResponse(
                expense.getId(),expense.getStatus(), expense.getTitle(), expense.getDescription(),
                expense.getAmount(), expense.getExpenseDate(), expense.getCategory().getId(),
                expense.getCategory().getName()
        );
    }

    private User findUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Expense findExpense(Long expenseId){
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    private Category findCategory(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
