package com.app.ecommerce.shared.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

@Schema(description = "Standard API response wrapper")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

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

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Operation completed successfully")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Resource created successfully")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> noContent() {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.NO_CONTENT.value())
                .message("Operation completed successfully")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
