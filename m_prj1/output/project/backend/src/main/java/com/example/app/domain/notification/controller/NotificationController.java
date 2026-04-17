package com.example.app.domain.notification.controller;

import com.example.app.common.response.ApiResponse;
import com.example.app.domain.notification.dto.NotificationDto;
import com.example.app.domain.notification.dto.SendNotificationRequest;
import com.example.app.domain.notification.entity.NotificationHistory;
import com.example.app.domain.notification.repository.NotificationHistoryRepository;
import com.example.app.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "알림 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationHistoryRepository historyRepository;

    @Operation(summary = "알림 발송 이력 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getList(
            @RequestParam(value = "sort", required = false) List<String> sortParams) {
        List<NotificationHistory> histories = historyRepository.findAll(resolveSort(sortParams));
        Page<NotificationHistory> page = new PageImpl<>(histories);
        return ResponseEntity.ok(
                ApiResponse.success(
                        page.getContent().stream().map(NotificationDto::from).toList(),
                        new ApiResponse.PageMeta(page)));
    }

    private Sort resolveSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return defaultSort();
        }

        String sortValue = sortParams.get(0);
        if (sortValue == null || sortValue.isBlank()) {
            return defaultSort();
        }

        if ("asc".equalsIgnoreCase(sortValue) || "desc".equalsIgnoreCase(sortValue)) {
            return Sort.by(parseDirection(sortValue), "createdAt");
        }

        String[] parts = sortValue.split(",", 2);
        if (parts.length == 2) {
            return Sort.by(parseDirection(parts[1]), parts[0]);
        }

        return Sort.by(Sort.Direction.DESC, sortValue);
    }

    private Sort defaultSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private Sort.Direction parseDirection(String value) {
        return "asc".equalsIgnoreCase(value) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    @Operation(summary = "알림 발송")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(
            @Valid @RequestBody SendNotificationRequest request) {
        if ("SMS".equalsIgnoreCase(request.channel())) {
            notificationService.sendSms(request.userId(), request.phone(), request.content());
        } else {
            notificationService.sendEmail(request.userId(), request.title(), request.content());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
