package com.app.ecommerce.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
