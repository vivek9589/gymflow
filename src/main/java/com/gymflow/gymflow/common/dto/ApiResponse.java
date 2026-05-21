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
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // =====================================================
    // SUCCESS WITH DATA + MESSAGE
    // =====================================================

    public static <T> ApiResponse<T> success(
            T data,
            String message
    ) {

        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // =====================================================
    // SUCCESS WITH ONLY DATA
    // =====================================================

    public static <T> ApiResponse<T> success(T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    // =====================================================
    // ERROR RESPONSE
    // =====================================================

    public static <T> ApiResponse<T> error(String message) {

        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}