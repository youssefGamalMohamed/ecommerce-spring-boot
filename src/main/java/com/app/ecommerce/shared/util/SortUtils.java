package com.app.ecommerce.shared.util;

import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortUtils {
    private SortUtils() {
    }

    public static Sort sanitize(Sort sort, Set<String> allowedFields, Sort.Order defaultOrder) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(defaultOrder);
        }
        Sort.Order[] orders = sort.get()
                .map(order -> allowedFields.contains(order.getProperty()) ? order : defaultOrder)
                .toArray(Sort.Order[]::new);
        return Sort.by(orders);
    }
}
