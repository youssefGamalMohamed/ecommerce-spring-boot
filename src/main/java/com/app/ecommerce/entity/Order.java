package com.app.ecommerce.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.app.ecommerce.enums.PaymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Table(name = "`Order`")
@Entity(name = "`Order`")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	@Column
	private double totalPrice;
	
	@Column
	private String deliveryAddress;
	
	@Column
	private String deliveryDate;
	
	@OneToOne(mappedBy = "order")
	private Cart cart;
	
	@Column
	@CreationTimestamp
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
}
