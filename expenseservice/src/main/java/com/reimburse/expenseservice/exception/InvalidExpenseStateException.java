package com.reimburse.expenseservice.exception;

public class InvalidExpenseStateException extends RuntimeException {
    public InvalidExpenseStateException(String message) {
        super(message);
    }
}
