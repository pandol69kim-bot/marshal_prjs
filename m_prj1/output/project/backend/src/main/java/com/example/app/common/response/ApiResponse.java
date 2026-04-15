package com.example.app.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;
    private final PageMeta meta;

    private ApiResponse(boolean success, T data, ErrorDetail error, PageMeta meta) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> error(ErrorDetail error) {
        return new ApiResponse<>(false, null, error, null);
    }

    @Getter
    public static class PageMeta {
        private final long totalElements;
        private final int totalPages;
        private final int page;
        private final int size;

        public PageMeta(org.springframework.data.domain.Page<?> page) {
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.page = page.getNumber();
            this.size = page.getSize();
        }
    }

    @Getter
    public static class ErrorDetail {
        private final String type;
        private final String title;
        private final int status;
        private final String detail;
        private final String instance;

        public ErrorDetail(String type, String title, int status, String detail, String instance) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = instance;
        }
    }
}
