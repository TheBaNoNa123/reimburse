package com.reimburse.expenseservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cognito_sub", unique = true, nullable = false, updatable = false)
    private String cognitoSub;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "display_name", length = 20, nullable = false)
    private String displayName;

}
