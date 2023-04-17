package com.app.ecommerce.models.response.success;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class DeleteTaskResponseBody {
    private boolean deletion_status;
}
