package com.app.ecommerce.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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

public class Cart extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@OneToMany(mappedBy = "cart" , cascade = CascadeType.ALL)
	private Set<CartItem> cartItems;
	
	@OneToOne
	@JoinColumn(name = "order_id")
	@JsonIgnore
	private Order order;
}
