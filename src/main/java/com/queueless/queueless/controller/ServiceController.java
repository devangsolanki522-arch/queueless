package com.queueless.queueless.controller;

import com.queueless.queueless.dto.ServiceRequest;
import com.queueless.queueless.dto.ServiceResponse;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.UserRepository;
import com.queueless.queueless.service.ServiceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserRepository userRepository;

    public ServiceController(
            ServiceService serviceService,
            UserRepository userRepository
    ) {
        this.serviceService = serviceService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(
            @Valid @RequestBody ServiceRequest request,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User staff =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return serviceService.createService(
                request,
                staff
        );
    }

    @GetMapping
    public List<ServiceResponse> getServices() {

        return serviceService.getAllServices();
    }

    @GetMapping("/my")
    public List<ServiceResponse> getMyServices(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User staff =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return serviceService.getMyServices(staff);
    }
}