package com.example.app.domain.resource.dto;

import com.example.app.domain.resource.entity.Resource;

import java.time.LocalDateTime;

public record ResourceDto(
        Long id,
        String name,
        String description,
        String status,
        String ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResourceDto from(Resource resource) {
        return new ResourceDto(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getStatus().name(),
                resource.getOwner().getId().toString(),
                resource.getOwner().getName(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
