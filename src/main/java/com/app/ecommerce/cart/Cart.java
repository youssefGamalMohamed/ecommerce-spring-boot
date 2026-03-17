package com.app.ecommerce.cart;

import com.app.ecommerce.order.Order;
import com.app.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;
import java.util.UUID;

@Table(name = "Cart")
@Entity(name = "Cart")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DynamicUpdate
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private Set<CartItem> cartItems;

    @OneToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private Order order;
}
