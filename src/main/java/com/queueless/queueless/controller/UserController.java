package com.queueless.queueless.controller;

import com.queueless.queueless.dto.LoginRequest;
import com.queueless.queueless.dto.LoginResponse;
import com.queueless.queueless.dto.RegisterRequest;
import com.queueless.queueless.dto.UserResponse;
import com.queueless.queueless.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public UserResponse createUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        return userService.createUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public String getCurrentUser(
            Authentication authentication
    ) {
        return "Authenticated as: " + authentication.getName();
    }
}