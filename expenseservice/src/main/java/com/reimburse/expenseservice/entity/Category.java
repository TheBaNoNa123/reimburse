package com.reimburse.expenseservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;
}
