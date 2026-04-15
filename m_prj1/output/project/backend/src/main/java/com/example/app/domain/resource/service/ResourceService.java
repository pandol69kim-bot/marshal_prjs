package com.example.app.domain.resource.service;

import com.example.app.common.exception.BusinessException;
import com.example.app.common.exception.ErrorCode;
import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import com.example.app.domain.resource.dto.CreateResourceRequest;
import com.example.app.domain.resource.dto.ResourceDto;
import com.example.app.domain.resource.dto.UpdateResourceRequest;
import com.example.app.domain.resource.entity.Resource;
import com.example.app.domain.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ResourceDto> findAll(String status, String keyword, Pageable pageable) {
        Resource.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Resource.Status.valueOf(status);
        }
        return resourceRepository.findAllWithFilter(statusEnum, keyword, pageable)
                .map(ResourceDto::from);
    }

    @Transactional(readOnly = true)
    public ResourceDto findById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return ResourceDto.from(resource);
    }

    @Transactional
    public ResourceDto create(CreateResourceRequest request, String userId) {
        User owner = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        Resource resource = Resource.create(request.name(), request.description(), owner);
        return ResourceDto.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceDto update(Long id, UpdateResourceRequest request, String userId) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        resource.update(request.name(), request.description());
        return ResourceDto.from(resource);
    }

    @Transactional
    public void delete(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        resource.delete();
    }
}
