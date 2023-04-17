package com.app.ecommerce.entity.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.*;

@Schema(hidden = true)
@Embeddable
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Name {
    private String firstName;
    private String lastName;
}
