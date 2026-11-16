package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.controller.ExpenseController;
import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
public class ControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ExpenseService expenseservice;

    @Test
    void createExpense_returns201_whenRequestIsValid() throws Exception{
        when(expenseservice.createExpense(eq(1L), any(CreateExpenseRequest.class)))
                .thenReturn(99L);

        String json =
                """
                {
                    "title": "Flight to Chicago",
                    "description": "Client visit",
                    "amount": 400.00,
                    "expenseDate": "2026-10-20",
                    "categoryId": 1
                }
                """;


        mockMvc.perform(post("/expense/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("99"));
    }
}
