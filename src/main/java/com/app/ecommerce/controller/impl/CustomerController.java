package com.app.ecommerce.controller.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.controller.framework.ICustomerController;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.mappers.OrderMapper;
import com.app.ecommerce.mappers.UserMapper;
import com.app.ecommerce.service.impl.CustomerService;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class CustomerController implements ICustomerController {

    @Autowired
    private CustomerService customerService;


    @RolesAllowed({"USER"})
    @GetMapping("/customers/{id}/orders")
    @Override
    public ResponseEntity<?> finAllOrdersForCustomerById(@PathVariable("id") Long customerId) {
    	List<Order> orders =  customerService.findOrdersForCustomer(customerId);
        return ResponseEntity.ok(OrderMapper.INSTANCE.mapToDtos(orders));
    }


    @RolesAllowed({"ADMIN"})
    @GetMapping("/customers")
    @Override
    public ResponseEntity<?> finAllCustomers() {
    	List<User> customers = customerService.findAllCustomers();
        return ResponseEntity.ok(UserMapper.INSTANCE.mapToDtos(customers));
    }
}
