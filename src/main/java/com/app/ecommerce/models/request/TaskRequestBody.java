package com.app.ecommerce.models.request;

import com.app.ecommerce.enums.Status;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TaskRequestBody {

    @NotBlank(message = "Subject Should Not Be Null or Empty")
    private String subject;

    @NotBlank(message = "Description Should Not Be Null or Empty")
    private String description;

    @Nullable
    private Status status;
}
