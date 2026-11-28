package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.dto.ExpenseResponse;
import com.reimburse.expenseservice.dto.UpdateExpense;
import com.reimburse.expenseservice.entity.*;
import com.reimburse.expenseservice.exception.ForbiddenOperationException;
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
    public ExpenseResponse getExpense(Long expenseId, CurrentUser caller){
        Expense expense = findExpense(expenseId);

        if(!caller.manager()){
            requireOwner(expense, caller);
        }

        return toResponse(expense);
    }

    public void requireOwner(Expense expense, CurrentUser caller){
        if(!expense.getUser().getId().equals(caller.id())){
            throw new ResourceNotFoundException(
                    "Expense not found with id " + expense.getId()
            );
        }
    }

    @Transactional
    public Long updateExpense(Long expenseId, UpdateExpense request, CurrentUser caller){
        Expense expense = findExpense(expenseId);

        requireOwner(expense, caller);

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be updated. Current status: " + expense.getStatus()
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
    public void deleteExpense(Long expenseId, CurrentUser caller){
        Expense expense = findExpense(expenseId);

        requireOwner(expense, caller);

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be deleted. Current status: " + expense.getStatus()
            );
        }

        expenseRepository.deleteById(expenseId);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpenses(ExpenseStatus status, Long categoryId,
                                             LocalDate from, LocalDate to,Pageable pageable, CurrentUser caller){

        Specification<Expense> spec = (root, query, cb) -> cb.conjunction();

        if(!caller.manager()){
            spec = spec.and(ExpenseSpecification.belongsTo(caller.id()));
        }

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
    public ExpenseResponse approveExpense(Long expenseId, CurrentUser caller){

        return reviewExpense(expenseId, caller, ExpenseStatus.APPROVED);

    }

    @Transactional
    public ExpenseResponse rejectExpense(Long expenseId, CurrentUser caller){

        return reviewExpense(expenseId, caller, ExpenseStatus.REJECTED);
    }

    @Transactional
    public ExpenseResponse cancelExpense(Long expenseId, CurrentUser caller){
        Expense expense = findExpense(expenseId);
        ExpenseHistory expenseHistory = new ExpenseHistory();
        requireOwner(expense, caller);

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be CANCELLED, Current status: " + expense.getStatus()
            );
        }


        // Set status to CANCELLED and save
        expense.setStatus(ExpenseStatus.CANCELLED);
        expenseRepository.save(expense);

        // Save to history
        expenseHistory.setExpense(expense);
        expenseHistory.setStatus(ExpenseStatus.CANCELLED);
        expenseHistory.setActor(caller.sub());
        expenseHistory.setActedAt(Instant.now());
        expenseHistoryRepository.save(expenseHistory);

        return toResponse(expense);

    }

    private ExpenseResponse reviewExpense(Long expenseId, CurrentUser caller, ExpenseStatus newStatus){

        Expense expense = findExpense(expenseId);

        if(!caller.manager()){
            throw new ForbiddenOperationException(
                    "Only managers can review expenses"
            );
        }

        if(expense.getUser().getId().equals(caller.id())){
            throw new ForbiddenOperationException(
                    "Managers cannot review their own expense"
            );
        }

        if(expense.getStatus() != ExpenseStatus.PENDING){
            throw new InvalidExpenseStateException(
                    "Only PENDING expenses can be reviewed. Current status: " + expense.getStatus()
            );
        }

        Instant now = Instant.now();

        // Review expense
        expense.setReviewedBy(caller.sub());
        expense.setReviewedAt(now);
        expense.setStatus(newStatus);

        ExpenseHistory expenseHistory = new ExpenseHistory();

        // Record expense review in history
        expenseHistory.setExpense(expense);
        expenseHistory.setStatus(newStatus);
        expenseHistory.setActor(caller.sub());
        expenseHistory.setActedAt(now);

        expenseHistoryRepository.save(expenseHistory);
        expenseRepository.save(expense);

        return toResponse(expense);

    }
}
