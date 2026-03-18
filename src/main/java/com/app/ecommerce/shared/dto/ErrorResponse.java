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
public class ErrorResponse {

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

    public static ErrorResponse build(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ErrorResponse build(int status, String error, String message, String details, String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ErrorResponse notFound(String message, String path) {
        return build(404, "NOT_FOUND", message, path);
    }

    public static ErrorResponse badRequest(String message, String path) {
        return build(400, "BAD_REQUEST", message, path);
    }

    public static ErrorResponse badRequest(String message, String details, String path) {
        return build(400, "BAD_REQUEST", message, details, path);
    }

    public static ErrorResponse conflict(String message, String path) {
        return build(409, "CONFLICT", message, path);
    }

    public static ErrorResponse internalError(String message, String path) {
        return build(500, "INTERNAL_SERVER_ERROR", message, path);
    }

    public static ErrorResponse serviceUnavailable(String message, String path) {
        return build(503, "SERVICE_UNAVAILABLE", message, path);
    }

    public static ErrorResponse forbidden(String message, String path) {
        return build(403, "FORBIDDEN", message, path);
    }

    public static ErrorResponse unauthorized(String message, String path) {
        return build(401, "UNAUTHORIZED", message, path);
    }
}
