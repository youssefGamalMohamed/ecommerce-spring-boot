package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.entity.User;

import java.util.List;


public interface ICustomerService {

    List<Order> findOrdersForCustomer(Long customerId);

    List<User> findAllCustomers();
}
