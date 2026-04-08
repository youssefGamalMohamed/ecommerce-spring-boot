package com.app.ecommerce.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(value = "Order.withCartAndItems", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Order> findById(UUID id);

    @EntityGraph(value = "Order.withCartAndItems", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Order> findAll(org.springframework.data.jpa.domain.Specification<Order> spec, Pageable pageable);

}
