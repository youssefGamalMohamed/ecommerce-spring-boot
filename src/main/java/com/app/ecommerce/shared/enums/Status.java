package com.app.ecommerce.shared.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum Status {
    NOT_MOVED_OUT_FROM_WAREHOUSE,
    ON_THE_WAY_TO_CUSTOMER,
    DELIVERED,
    CANCELED;

    private static final Map<Status, Set<Status>> TRANSITIONS;

    static {
        Map<Status, Set<Status>> map = new EnumMap<>(Status.class);
        map.put(NOT_MOVED_OUT_FROM_WAREHOUSE, Set.of(ON_THE_WAY_TO_CUSTOMER, CANCELED));
        map.put(ON_THE_WAY_TO_CUSTOMER, Set.of(DELIVERED, CANCELED));
        map.put(DELIVERED, Collections.emptySet());
        map.put(CANCELED, Collections.emptySet());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public Set<Status> getAllowedTransitions() {
        return TRANSITIONS.get(this);
    }

    public boolean canTransitionTo(Status target) {
        return TRANSITIONS.get(this).contains(target);
    }
}
