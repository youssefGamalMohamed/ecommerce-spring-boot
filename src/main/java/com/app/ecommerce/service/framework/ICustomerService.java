package com.app.ecommerce.service.framework;

import java.util.List;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.entity.User;


public interface ICustomerService {

    List<Order> findOrdersForCustomer(Long customerId);

    List<User> findAllCustomers();
}
