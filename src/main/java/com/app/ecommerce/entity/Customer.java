package com.app.ecommerce.entity;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Table(name = "Customer")
@Entity(name = "Customer")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Customer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	private String name;
	
	
	@OneToMany(mappedBy = "customer")
	private List<Order> orders;
	
}
