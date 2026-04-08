package com.app.ecommerce.order;

import com.app.ecommerce.cart.Cart;
import com.app.ecommerce.shared.entity.BaseEntity;
import com.app.ecommerce.shared.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedSubgraph;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "`order`")
@Entity(name = "Order")
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DynamicUpdate
@NamedEntityGraph(
    name = "Order.withCartAndItems",
    attributeNodes = {
        @NamedAttributeNode(value = "cart", subgraph = "cart.items"),
        @NamedAttributeNode("deliveryInfo")
    },
    subgraphs = {
        @NamedSubgraph(name = "cart.items",
            attributeNodes = {
                @NamedAttributeNode(value = "cartItems", subgraph = "items.product"),
                @NamedAttributeNode("owner")
            }),
        @NamedSubgraph(name = "items.product",
            attributeNodes = @NamedAttributeNode("product"))
    }
)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Version
    private Long version;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "delivery_status")),
            @AttributeOverride(name = "address", column = @Column(name = "delivery_address")),
            @AttributeOverride(name = "date", column = @Column(name = "delivery_date"))
    })
    private DeliveryInfo deliveryInfo;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Cart cart;
}
