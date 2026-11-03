package com.reimburse.expenseservice.repository;

import com.reimburse.expenseservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
