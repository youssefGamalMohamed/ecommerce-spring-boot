package com.app.ecommerce.dtos;



import java.util.HashSet;
import java.util.Set;

import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.DeliveryInfo;
import com.app.ecommerce.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDto {
    private Long id;
    private Set<CartItemDto> cartItems = new HashSet<>();
}
