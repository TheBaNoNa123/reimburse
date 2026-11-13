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
import com.reimburse.expenseservice.specification.ExpenseSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseHistoryRepository expenseHistoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, CategoryRepository categoryRepository, ExpenseHistoryRepository expenseHistoryRepository){
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseHistoryRepository = expenseHistoryRepository;
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

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be updated. Current status: " + expense.getId()
            );
        }

        Category category = findCategory(request.getCategoryId());

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);

        expenseRepository.save(expense);

        return expenseId;
    }

    @Transactional
    public void deleteExpense(Long expenseId){
        Expense expense = findExpense(expenseId);

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be deleted. Current status: " + expense.getStatus()
            );
        }

        expenseRepository.deleteById(expenseId);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpenses(ExpenseStatus status, Long categoryId,
                                             LocalDate from, LocalDate to,Pageable pageable){
        Specification<Expense> spec = (root, query, cb) -> cb.conjunction();

        if(status != null){
            spec = spec.and(ExpenseSpecification.hasStatus(status));
        }
        if(categoryId != null){
            spec = spec.and(ExpenseSpecification.hasCategory(categoryId));
        }
        if(from != null){
            spec = spec.and(ExpenseSpecification.dateFrom(from));
        }
        if(to != null){
            spec = spec.and(ExpenseSpecification.dateTo(to));
        }

        return expenseRepository.findAll(spec, pageable).map(this::toResponse);

    }

    // Helper Methods
    private ExpenseResponse toResponse(Expense expense){
        return new ExpenseResponse(
                expense.getId(),expense.getStatus(), expense.getTitle(), expense.getDescription(),
                expense.getAmount(), expense.getExpenseDate(), expense.getCategory().getId(), expense.getCategory().getName(),
                expense.getReviewedBy(), expense.getReviewedAt()
        );
    }

    private User findUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private Expense findExpense(Long expenseId){
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + expenseId));
    }

    private Category findCategory(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    }

    @Transactional
    public ExpenseResponse approveExpense(Long expenseId, String reviewer){
        return reviewExpense(expenseId, reviewer, ExpenseStatus.APPROVED);
    }

    @Transactional
    public ExpenseResponse rejectExpense(Long expenseId, String reviewer){
        return reviewExpense(expenseId, reviewer, ExpenseStatus.REJECTED);
    }

    private ExpenseResponse reviewExpense(Long expenseId, String reviewer, ExpenseStatus newStatus){

        Expense expense = findExpense(expenseId);

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be reviewed. Current status: " + expense.getId()
            );
        }

        // Review expense
        expense.setReviewedBy(reviewer);
        expense.setReviewedAt(Instant.now());
        expense.setStatus(newStatus);

        ExpenseHistory expenseHistory = new ExpenseHistory();

        // Record expense review in history
        expenseHistory.setExpense(expense);
        expenseHistory.setStatus(newStatus);
        expenseHistory.setActor(reviewer);
        expenseHistory.setActedAt(Instant.now());

        expenseHistoryRepository.save(expenseHistory);
        expenseRepository.save(expense);

        return toResponse(expense);

    }
}
