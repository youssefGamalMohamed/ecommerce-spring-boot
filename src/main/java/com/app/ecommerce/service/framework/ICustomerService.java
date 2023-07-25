package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.enums.Status;
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderStatusByIdResponse;

import java.util.List;


public interface ICustomerService {

    List<Order> findOrdersForCustomer(Long customerId);
}
