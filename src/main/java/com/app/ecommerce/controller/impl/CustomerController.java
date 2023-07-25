package com.app.ecommerce.controller.impl;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.service.impl.CustomerService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @RolesAllowed({"USER"})
    @GetMapping("/customers/{id}/orders")
    public List<Order> finAllOrdersForCustomerById(@PathVariable("id") Long customerId) {
        return customerService.findOrdersForCustomer(customerId);
    }
}
