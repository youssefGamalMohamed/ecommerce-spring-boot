package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderStatusById;


public interface IOrderService {

    CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody);
    
    GetOrderByIdResponse findById(Long orderId);
    
    GetOrderStatusById findOrderStatusById(Long orderId);
}
