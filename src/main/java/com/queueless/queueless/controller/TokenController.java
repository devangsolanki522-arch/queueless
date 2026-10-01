package com.queueless.queueless.controller;

import com.queueless.queueless.dto.PriorityRequest;
import com.queueless.queueless.dto.QueueTokenResponse;
import com.queueless.queueless.dto.TokenRequest;
import com.queueless.queueless.dto.TokenResponse;
import com.queueless.queueless.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse createToken(
            @Valid @RequestBody TokenRequest request,
            Authentication authentication
    ) {

        String customerEmail =
                authentication.getName();

        return tokenService.createToken(
                request,
                customerEmail
        );
    }

    @GetMapping("/{tokenId}/queue")
    public QueueTokenResponse getQueuePosition(
            @PathVariable Long tokenId,
            Authentication authentication
    ) {

        return tokenService.getQueuePosition(
                tokenId,
                authentication.getName()
        );
    }

    // STAFF ONLY:
    // Get all WAITING tokens for the staff's service.
    @GetMapping("/service/{serviceId}/waiting")
    public List<TokenResponse> getWaitingTokens(
            @PathVariable Long serviceId,
            Authentication authentication
    ) {

        return tokenService.getWaitingTokens(
                serviceId,
                authentication.getName()
        );
    }

    @PostMapping("/call-next/{serviceId}")
    public TokenResponse callNext(
            @PathVariable Long serviceId,
            Authentication authentication
    ) {

        String staffEmail =
                authentication.getName();

        return tokenService.callNext(
                serviceId,
                staffEmail
        );
    }

    @PutMapping("/{tokenId}/priority")
    public TokenResponse changePriority(
            @PathVariable Long tokenId,
            @Valid @RequestBody PriorityRequest request,
            Authentication authentication
    ) {

        return tokenService.changePriority(
                tokenId,
                request.getPriority(),
                authentication.getName()
        );
    }

    @PostMapping("/{tokenId}/no-show")
    public TokenResponse markNoShow(
            @PathVariable Long tokenId,
            Authentication authentication
    ) {

        return tokenService.markNoShow(
                tokenId,
                authentication.getName()
        );
    }

    @PostMapping("/{tokenId}/start")
    public TokenResponse startServing(
            @PathVariable Long tokenId,
            Authentication authentication
    ) {

        return tokenService.startServing(
                tokenId,
                authentication.getName()
        );
    }

    @PostMapping("/{tokenId}/complete")
    public TokenResponse completeToken(
            @PathVariable Long tokenId,
            Authentication authentication
    ) {

        return tokenService.completeToken(
                tokenId,
                authentication.getName()
        );
    }

    @PostMapping("/{tokenId}/cancel")
    public TokenResponse cancelToken(
            @PathVariable Long tokenId,
            Authentication authentication
    ) {

        return tokenService.cancelToken(
                tokenId,
                authentication.getName()
        );
    }
}