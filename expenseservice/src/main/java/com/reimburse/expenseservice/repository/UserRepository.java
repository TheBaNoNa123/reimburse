package com.reimburse.expenseservice.repository;

import com.reimburse.expenseservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
