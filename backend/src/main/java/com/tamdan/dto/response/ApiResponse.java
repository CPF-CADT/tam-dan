package com.tamdan.dto.response;

public record ApiResponse<T>(T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(null);
    }
}
