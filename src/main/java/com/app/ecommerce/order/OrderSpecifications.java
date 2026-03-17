package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
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
}
