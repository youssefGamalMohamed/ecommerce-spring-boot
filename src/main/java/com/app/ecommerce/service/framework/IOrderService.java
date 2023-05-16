package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.success.CreateNewOrderResponse;
import com.app.ecommerce.models.response.success.GetOrderByIdResponse;
import com.app.ecommerce.models.response.success.GetOrderStatusById;


public interface IOrderService {

    CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody);
    
    GetOrderByIdResponse findById(Long orderId);
    
    GetOrderStatusById findOrderStatusById(Long orderId);
}
