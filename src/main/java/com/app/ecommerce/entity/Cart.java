package com.app.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Table(name = "Cart")
@Entity(name = "Cart")
@SuperBuilder
@NoArgsConstructor
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
	@ToString.Exclude
	private Order order;
}
