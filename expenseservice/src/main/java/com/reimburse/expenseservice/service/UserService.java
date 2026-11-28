package com.reimburse.expenseservice.service;

import com.reimburse.expenseservice.entity.User;
import com.reimburse.expenseservice.exception.ResourceNotFoundException;
import com.reimburse.expenseservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(String sub, String username){
        User user = userRepository.findByCognitoSub(sub)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setCognitoSub(sub);
                    newUser.setDisplayName(username != null ? username: sub);
                    userRepository.save(newUser);
                    return newUser;
                });
        return user;
    }

}
