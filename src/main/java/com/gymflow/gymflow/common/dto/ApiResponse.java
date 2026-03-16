package com.gymflow.gymflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all endpoints.
 * Ensures consistent response format across the application.
 *
 * @param <T> The type of data returned in the response
 */

@Data
@AllArgsConstructor
@NoArgsConstructor // Added for JSON deserialization
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @Builder.Default // Ensures the builder uses the current time if not provided
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}