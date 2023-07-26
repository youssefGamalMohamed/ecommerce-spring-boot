package com.app.ecommerce.service.framework;

import com.app.ecommerce.enums.Status;
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderStatusByIdResponse;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface IOrderService {

    CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody) throws JsonProcessingException;
    
    GetOrderByIdResponse findById(Long orderId);
    
    GetOrderStatusByIdResponse findOrderStatusById(Long orderId);

    void updateOrderStatus(Long orderId, Status orderStatus);
}
