package com.reimburse.expenseservice.specification;

import com.reimburse.expenseservice.entity.Expense;
import com.reimburse.expenseservice.entity.ExpenseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

// Creating custom filters
public class ExpenseSpecification {

    //
    public static Specification<Expense> hasStatus(ExpenseStatus status){
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Expense> hasCategory(Long categoryId){
        // r : main table ; q :
        return (root , query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> dateFrom(LocalDate from){
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to){
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }
}
