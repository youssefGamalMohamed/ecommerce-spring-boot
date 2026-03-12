package com.app.ecommerce.entity;


import com.app.ecommerce.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(name = "`order`")
@Entity(name = "`order`")
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	@Column
	private double totalPrice;
	
	@Embedded
	@AttributeOverrides({
		  @AttributeOverride( name = "status", column = @Column(name = "delivery_status")),
		  @AttributeOverride( name = "address", column = @Column(name = "delivery_address")),
		  @AttributeOverride( name = "date", column = @Column(name = "delivery_date"))
	})
	private DeliveryInfo deliveryInfo;
	
	@OneToOne(mappedBy = "order" , cascade = CascadeType.ALL)
	private Cart cart;
}
