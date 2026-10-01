package com.queueless.queueless.repository;

import com.queueless.queueless.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByStaffId(Long staffId);

    List<Service> findByStaffIdAndActiveTrue(Long staffId);

    List<Service> findByActiveTrue();
}