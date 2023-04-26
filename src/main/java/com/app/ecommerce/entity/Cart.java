package com.app.ecommerce.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Table(name = "Cart")
@Entity(name = "Cart")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Cart {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@OneToMany(mappedBy = "cart")
	private Set<CartItem> cartItems;
	
	@OneToOne
	@JoinColumn(name = "order_id")
	private Order order;
}
