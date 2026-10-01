package com.queueless.queueless.repository;

import com.queueless.queueless.entity.Token;
import com.queueless.queueless.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            SELECT t
            FROM Token t
            WHERE t.service.id = :serviceId
            AND t.status = :status
            ORDER BY
                CASE
                    WHEN t.priority = com.queueless.queueless.entity.Priority.EMERGENCY THEN 3
                    WHEN t.priority = com.queueless.queueless.entity.Priority.HIGH THEN 2
                    WHEN t.priority = com.queueless.queueless.entity.Priority.NORMAL THEN 1
                END DESC,
                t.createdAt ASC
            """)
    List<Token> findWaitingTokensByPriority(
            @Param("serviceId") Long serviceId,
            @Param("status") TokenStatus status
    );

    Optional<Token> findTopByServiceIdOrderByTokenNumberDesc(
            Long serviceId
    );
}