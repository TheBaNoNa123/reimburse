package com.reimburse.expenseservice.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;

    private String cognitoSub;

    private String email;

    private String displayName;
}
