package com.app.ecommerce.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Standard error response")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    @Schema(description = "Indicates if the request was successful", example = "false")
    private boolean success;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error type", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Error message", example = "Resource not found")
    private String message;

    @Schema(description = "Detailed error description")
    private String details;

    @Schema(description = "Path where the error occurred", example = "/api/products/123")
    private String path;

    @Schema(description = "Timestamp of the error")
    private long timestamp;

    public static ErrorResponseDto build(int status, String error, String message, String path) {
        return ErrorResponseDto.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ErrorResponseDto build(int status, String error, String message, String details, String path) {
        return ErrorResponseDto.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ErrorResponseDto notFound(String message, String path) {
        return build(404, "NOT_FOUND", message, path);
    }

    public static ErrorResponseDto badRequest(String message, String path) {
        return build(400, "BAD_REQUEST", message, path);
    }

    public static ErrorResponseDto badRequest(String message, String details, String path) {
        return build(400, "BAD_REQUEST", message, details, path);
    }

    public static ErrorResponseDto conflict(String message, String path) {
        return build(409, "CONFLICT", message, path);
    }

    public static ErrorResponseDto internalError(String message, String path) {
        return build(500, "INTERNAL_SERVER_ERROR", message, path);
    }

    public static ErrorResponseDto serviceUnavailable(String message, String path) {
        return build(503, "SERVICE_UNAVAILABLE", message, path);
    }

    public static ErrorResponseDto forbidden(String message, String path) {
        return build(403, "FORBIDDEN", message, path);
    }

    public static ErrorResponseDto unauthorized(String message, String path) {
        return build(401, "UNAUTHORIZED", message, path);
    }
}
