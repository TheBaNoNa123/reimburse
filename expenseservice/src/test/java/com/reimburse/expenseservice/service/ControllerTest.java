package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.config.SecurityConfig;
import com.reimburse.expenseservice.controller.ExpenseController;
import com.reimburse.expenseservice.dto.CreateExpenseRequest;
import com.reimburse.expenseservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@Import(SecurityConfig.class)
public class ControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ExpenseService expenseService;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void createExpense_returns201_forLoggedInEmployee() throws Exception{
        User user = new User();
        user.setId(1L);
        when(userService.getOrCreateUser(any(), any())).thenReturn(user);
        when(expenseService.createExpense(any(), any())).thenReturn(1L);

//        when(expenseservice.createExpense(eq(1L), any(CreateExpenseRequest.class)))
//                .thenReturn(99L);

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

        mockMvc.perform(post("/expense")
                        .with(jwt()
                                .jwt(j -> j.subject("alice-sub").claim("username", "alice"))
                                .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }
    @Test
    void createExpense_returns401_forNonLoggedInEmployee() throws Exception{
        User user = new User();
        user.setId(1L);

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


        mockMvc.perform(post("/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());

        verify(expenseService, never()).createExpense(any(), any());
    }

    @Test
    void employee_cant_approve() throws Exception{
        User user = new User();
        user.setId(1L);

        mockMvc.perform(post("/expense/1/accepted")
                .with(jwt()
                        .jwt(j -> j.subject("alice-sub").claim("username", "alice"))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
                .andExpect(status().isForbidden());

        verify(expenseService, never()).approveExpense(any(), any());

    }
}
