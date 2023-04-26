package com.app.ecommerce.entity;

import java.util.Set;

import jakarta.persistence.Entity;
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


@Table(name = "Cart_Item")
@Entity(name = "Cart_Item")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItem {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "cartItem")
	private Product product;
	
	private int productQuantity;
	
	@ManyToOne
	@JoinColumn(name = "cart_id")
	private Cart cart;
}
