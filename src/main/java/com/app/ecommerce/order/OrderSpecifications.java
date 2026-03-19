package com.app.ecommerce.order;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecifications {

    public static Specification<Order> hasStatus(Status status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("deliveryInfo").get("status"), status);
        };
    }

    public static Specification<Order> hasPaymentType(PaymentType paymentType) {
        return (root, query, cb) -> {
            if (paymentType == null) {
                return null;
            }
            return cb.equal(root.get("paymentType"), paymentType);
        };
    }

    public static Specification<Order> createdAfter(Instant after) {
        return (root, query, cb) -> {
            if (after == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), after);
        };
    }

    public static Specification<Order> createdBefore(Instant before) {
        return (root, query, cb) -> {
            if (before == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), before);
        };
    }

    public static Specification<Order> hasOwner(User user) {
        return (root, query, cb) -> {
            if (user == null) {
                return null;
            }
            Join<Object, Object> cart = root.join("cart");
            Join<Object, Object> owner = cart.join("owner");
            return cb.equal(owner.get("id"), user.getId());
        };
    }
}
