package com.youssefgamal.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idAutoIncrement;
	

	private Long id;
	
    @ManyToOne
    @JoinColumn(name = "order_fk")
    @ToString.Exclude
    private Order order;

}
