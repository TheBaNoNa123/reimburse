package com.reimburse.expenseservice.controller;


import com.reimburse.expenseservice.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("backend")
public class Controller {

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto user){
        return ResponseEntity.ok("Good");
    }
}
