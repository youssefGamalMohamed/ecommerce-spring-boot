package com.app.ecommerce.entity;


import com.app.ecommerce.enums.Status;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Embeddable
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Delivery {
	
	@Enumerated(EnumType.STRING)
	private Status status = Status.NOT_MOVED_OUT_FROM_WAREHOUSE;
	
	private String address;
	
	private String date;
	
}
