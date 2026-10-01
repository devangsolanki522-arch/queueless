package com.queueless.queueless.service;

import com.queueless.queueless.dto.ServiceRequest;
import com.queueless.queueless.dto.ServiceResponse;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ServiceResponse createService(
            ServiceRequest request,
            User staff
    ) {

        // Deactivate the staff's previous services.
        List<com.queueless.queueless.entity.Service> oldServices =
                serviceRepository.findByStaffIdAndActiveTrue(
                        staff.getId()
                );

        for (com.queueless.queueless.entity.Service oldService
                : oldServices) {

            oldService.setActive(false);

            serviceRepository.save(oldService);
        }

        // Create the new active service.
        com.queueless.queueless.entity.Service service =
                new com.queueless.queueless.entity.Service();

        service.setName(request.getName());
        service.setEstimatedServiceTime(
                request.getEstimatedServiceTime()
        );
        service.setStaff(staff);
        service.setActive(true);

        com.queueless.queueless.entity.Service savedService =
                serviceRepository.save(service);

        return new ServiceResponse(
                savedService.getId(),
                savedService.getName(),
                savedService.getEstimatedServiceTime(),
                savedService.getStaff().getId()
        );
    }

    // Customers should only see active services.
    public List<ServiceResponse> getAllServices() {

        return serviceRepository.findByActiveTrue()
                .stream()
                .map(service -> new ServiceResponse(
                        service.getId(),
                        service.getName(),
                        service.getEstimatedServiceTime(),
                        service.getStaff().getId()
                ))
                .toList();
    }

    // Staff sees their active service.
    public List<ServiceResponse> getMyServices(User staff) {

        return serviceRepository
                .findByStaffIdAndActiveTrue(staff.getId())
                .stream()
                .map(service -> new ServiceResponse(
                        service.getId(),
                        service.getName(),
                        service.getEstimatedServiceTime(),
                        service.getStaff().getId()
                ))
                .toList();
    }
}