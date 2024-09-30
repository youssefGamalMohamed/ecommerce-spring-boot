package com.app.ecommerce.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {


	@JsonIgnore
	@OneToMany(mappedBy = "customer" , cascade = CascadeType.ALL , fetch = FetchType.EAGER)
	private List<Order> orders;
	
}
