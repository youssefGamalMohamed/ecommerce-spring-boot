package com.app.ecommerce.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Table(name = "Cart_Item")
@Entity(name = "Cart_Item")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItem extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
	private Product product;
	
	private int productQuantity;
	
	@ManyToOne
	@JoinColumn(name = "cart_id")
	@JsonIgnore
	@ToString.Exclude
	private Cart cart;
}
