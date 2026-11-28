package com.reimburse.expenseservice.entity;

public record CurrentUser(Long id, String sub, boolean manager) {
}
