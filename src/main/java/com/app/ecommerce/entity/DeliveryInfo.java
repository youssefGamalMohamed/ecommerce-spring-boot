package com.app.ecommerce.entity;


import com.app.ecommerce.enums.Status;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeliveryInfo {
	
	@Enumerated(EnumType.STRING)
	private Status status = Status.NOT_MOVED_OUT_FROM_WAREHOUSE;
	
	private String address;
	
	private String date;
	
}
