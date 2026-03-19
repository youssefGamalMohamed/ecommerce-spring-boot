package com.app.ecommerce.shared.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import static org.springframework.http.HttpStatus.*;

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

    @Schema(description = "Error type", example = "Not Found")
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
        return build(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase(), message, path);
    }

    public static ErrorResponse badRequest(String message, String path) {
        return build(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase(), message, path);
    }

    public static ErrorResponse badRequest(String message, String details, String path) {
        return build(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase(), message, details, path);
    }

    public static ErrorResponse conflict(String message, String path) {
        return build(CONFLICT.value(), CONFLICT.getReasonPhrase(), message, path);
    }

    public static ErrorResponse internalError(String message, String path) {
        return build(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.getReasonPhrase(), message, path);
    }

    public static ErrorResponse serviceUnavailable(String message, String path) {
        return build(SERVICE_UNAVAILABLE.value(), SERVICE_UNAVAILABLE.getReasonPhrase(), message, path);
    }

    public static ErrorResponse forbidden(String message, String path) {
        return build(FORBIDDEN.value(), FORBIDDEN.getReasonPhrase(), message, path);
    }

    public static ErrorResponse unauthorized(String message, String path) {
        return build(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), message, path);
    }
}
