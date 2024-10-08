package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.Role;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.repository.OrderRepo;
import com.app.ecommerce.repository.UserRepo;
import com.app.ecommerce.service.framework.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService implements ICustomerService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public List<Order> findOrdersForCustomer(Long customerId) {
        Customer customer = (Customer) userRepo.findById(customerId)
                .orElseThrow(() -> new IdNotFoundException("Can Not Retrieve Orders for Customer , Id Not Found"));

        return customer.getOrders();          
    }

    @Override
    public List<User> findAllCustomers() {
         return userRepo.findAllByRole(Role.ROLE_USER)
        		 .stream()
        		 .map(user -> (Customer) user)
        		 .collect(Collectors.toList());
    }
}
