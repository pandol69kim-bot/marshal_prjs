package com.example.app.domain.resource.controller;

import com.example.app.common.response.ApiResponse;
import com.example.app.domain.resource.dto.*;
import com.example.app.domain.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Resources", description = "리소스 CRUD API")
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "리소스 목록 조회 (페이지네이션/필터링)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResourceDto>>> getList(
            @Parameter(description = "상태 필터") @RequestParam(required = false) String status,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ResourceDto> page = resourceService.findAll(status, keyword, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(page.getContent(), new ApiResponse.PageMeta(page)));
    }

    @Operation(summary = "리소스 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceDto>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.findById(id)));
    }

    @Operation(summary = "리소스 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ResourceDto>> create(
            @Valid @RequestBody CreateResourceRequest request,
            @AuthenticationPrincipal String userId) {
        ResourceDto created = resourceService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @Operation(summary = "리소스 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.update(id, request, userId)));
    }

    @Operation(summary = "리소스 삭제 (관리자 전용)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
