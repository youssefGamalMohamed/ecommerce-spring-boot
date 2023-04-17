package com.app.ecommerce.models.response.success;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AssignTaskToUserResponseBody {
    private boolean assigning_status;
}
