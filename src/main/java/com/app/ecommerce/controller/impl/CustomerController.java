package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.ICustomerController;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.models.response.endpoints.GetCustomerOrdersResponseBody;
import com.app.ecommerce.service.impl.CustomerService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController implements ICustomerController {

    @Autowired
    private CustomerService customerService;


    @RolesAllowed({"USER"})
    @GetMapping("/customers/{id}/orders")
    @Override
    public ResponseEntity<?> finAllOrdersForCustomerById(@PathVariable("id") Long customerId) {
        return ResponseEntity.ok(
                customerService.findOrdersForCustomer(customerId)
        );
    }


    @RolesAllowed({"ADMIN"})
    @GetMapping("/customers")
    @Override
    public ResponseEntity<?> finAllCustomers() {
        return ResponseEntity.ok(
                customerService.findAllCustomers()
        );
    }
}
