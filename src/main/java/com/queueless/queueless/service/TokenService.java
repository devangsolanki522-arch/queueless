package com.queueless.queueless.service;

import com.queueless.queueless.dto.QueueTokenResponse;
import com.queueless.queueless.dto.TokenRequest;
import com.queueless.queueless.dto.TokenResponse;
import com.queueless.queueless.entity.Priority;
import com.queueless.queueless.entity.Token;
import com.queueless.queueless.entity.TokenStatus;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.ServiceRepository;
import com.queueless.queueless.repository.TokenRepository;
import com.queueless.queueless.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public TokenService(
            TokenRepository tokenRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    public TokenResponse createToken(
            TokenRequest request,
            String customerEmail
    ) {

        User customer =
                userRepository.findByEmail(customerEmail)
                        .orElseThrow();

        com.queueless.queueless.entity.Service service =
                serviceRepository.findById(request.getServiceId())
                        .orElseThrow();

        Token token = new Token();

        Token lastToken =
                tokenRepository
                        .findTopByServiceIdOrderByTokenNumberDesc(
                                service.getId()
                        )
                        .orElse(null);

        int nextTokenNumber;

        if (lastToken == null) {
            nextTokenNumber = 1;
        } else {
            nextTokenNumber =
                    lastToken.getTokenNumber() + 1;
        }

        token.setTokenNumber(nextTokenNumber);

        token.setTokenNumber(nextTokenNumber);

        // Customers always enter with NORMAL priority.
        token.setPriority(Priority.NORMAL);

        token.setStatus(TokenStatus.WAITING);

        token.setCreatedAt(
                LocalDateTime.now()
        );

        token.setCustomer(customer);

        token.setService(service);

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    public QueueTokenResponse getQueuePosition(
            Long tokenId,
            String requesterEmail
    ) {

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        User requester =
                userRepository.findByEmail(requesterEmail)
                        .orElseThrow();

        boolean isCustomer =
                token.getCustomer()
                        .getId()
                        .equals(requester.getId());

        boolean isStaff =
                token.getService()
                        .getStaff()
                        .getId()
                        .equals(requester.getId());

        if (!isCustomer && !isStaff) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view this token"
            );
        }

        List<Token> waitingTokens =
                tokenRepository
                        .findWaitingTokensByPriority(
                                token.getService().getId(),
                                TokenStatus.WAITING
                        );

        int position = 1;

        for (Token waitingToken : waitingTokens) {

            if (waitingToken.getId()
                    .equals(token.getId())) {

                break;
            }

            position++;
        }

        int estimatedWaitTime =
                (position - 1)
                        * token.getService()
                        .getEstimatedServiceTime();

        return new QueueTokenResponse(
                token.getTokenNumber(),
                token.getPriority(),
                token.getStatus(),
                position,
                estimatedWaitTime
        );
    }

    public List<TokenResponse> getWaitingTokens(
            Long serviceId,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        com.queueless.queueless.entity.Service service =
                serviceRepository.findById(serviceId)
                        .orElseThrow();

        verifyStaffOwnsService(
                service,
                staff
        );

        List<Token> waitingTokens =
                tokenRepository
                        .findWaitingTokensByPriority(
                                serviceId,
                                TokenStatus.WAITING
                        );

        return waitingTokens
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TokenResponse callNext(
            Long serviceId,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        com.queueless.queueless.entity.Service service =
                serviceRepository.findById(serviceId)
                        .orElseThrow();

        verifyStaffOwnsService(
                service,
                staff
        );

        List<Token> waitingTokens =
                tokenRepository
                        .findWaitingTokensByPriority(
                                serviceId,
                                TokenStatus.WAITING
                        );

        if (waitingTokens.isEmpty()) {

            throw new IllegalStateException(
                    "No customers are waiting"
            );
        }

        Token nextToken =
                waitingTokens.get(0);

        nextToken.setStatus(
                TokenStatus.CALLED
        );

        Token savedToken =
                tokenRepository.save(nextToken);

        return toResponse(savedToken);
    }

    // STAFF ONLY:
    // Change the priority of a waiting token.
    public TokenResponse changePriority(
            Long tokenId,
            Priority newPriority,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        verifyStaffOwnsService(
                token.getService(),
                staff
        );

        if (token.getStatus()
                != TokenStatus.WAITING) {

            throw new IllegalStateException(
                    "Priority can only be changed for a WAITING token"
            );
        }

        token.setPriority(newPriority);

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    // STAFF ONLY:
    // Mark a customer as NO_SHOW if they did not arrive
    // after their token was called.
    public TokenResponse markNoShow(
            Long tokenId,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        verifyStaffOwnsService(
                token.getService(),
                staff
        );

        if (token.getStatus()
                != TokenStatus.CALLED) {

            throw new IllegalStateException(
                    "Only a CALLED token can be marked as NO_SHOW"
            );
        }

        token.setStatus(
                TokenStatus.NO_SHOW
        );

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    public TokenResponse startServing(
            Long tokenId,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        verifyStaffOwnsService(
                token.getService(),
                staff
        );

        if (token.getStatus()
                != TokenStatus.CALLED) {

            throw new IllegalStateException(
                    "Only a CALLED token can start serving"
            );
        }

        token.setStatus(
                TokenStatus.SERVING
        );

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    public TokenResponse completeToken(
            Long tokenId,
            String staffEmail
    ) {

        User staff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow();

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        verifyStaffOwnsService(
                token.getService(),
                staff
        );

        if (token.getStatus()
                != TokenStatus.SERVING) {

            throw new IllegalStateException(
                    "Only a SERVING token can be completed"
            );
        }

        token.setStatus(
                TokenStatus.COMPLETED
        );

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    public TokenResponse cancelToken(
            Long tokenId,
            String customerEmail
    ) {

        Token token =
                tokenRepository.findById(tokenId)
                        .orElseThrow();

        User customer =
                userRepository.findByEmail(customerEmail)
                        .orElseThrow();

        if (!token.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only cancel your own token"
            );
        }

        if (token.getStatus()
                != TokenStatus.WAITING) {

            throw new IllegalStateException(
                    "Only a WAITING token can be cancelled"
            );
        }

        token.setStatus(
                TokenStatus.CANCELLED
        );

        Token savedToken =
                tokenRepository.save(token);

        return toResponse(savedToken);
    }

    private void verifyStaffOwnsService(
            com.queueless.queueless.entity.Service service,
            User staff
    ) {

        if (!service.getStaff()
                .getId()
                .equals(staff.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only manage your own service"
            );
        }
    }

    private TokenResponse toResponse(
            Token token
    ) {

        return new TokenResponse(
                token.getId(),
                token.getTokenNumber(),
                token.getPriority(),
                token.getStatus(),
                token.getService().getId(),
                token.getCustomer().getId()
        );
    }
}