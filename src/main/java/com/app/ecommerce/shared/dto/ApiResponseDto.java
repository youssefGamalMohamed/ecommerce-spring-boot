package com.app.ecommerce.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Standard API response wrapper")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {

    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "The actual response data")
    private T data;

    @Schema(description = "Timestamp of the response")
    private long timestamp;

    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(200)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponseDto<T> created(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(201)
                .message("Resource created successfully")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponseDto<T> noContent() {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(204)
                .message("Operation completed successfully")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
