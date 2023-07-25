package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.enums.Status;
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.*;

import java.util.List;


public interface ICustomerService {

    GetCustomerOrdersResponseBody findOrdersForCustomer(Long customerId);

    GetAllCustomersResponse findAllCustomers();
}
