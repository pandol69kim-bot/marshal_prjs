package com.example.app.domain.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResourceRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 200, message = "이름은 200자 이하여야 합니다")
        String name,

        String description
) {}
