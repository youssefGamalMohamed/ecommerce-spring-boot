package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.models.response.success.AddNewProductResponse;
import com.app.ecommerce.models.response.success.CreateNewOrderResponse;
import com.app.ecommerce.models.response.success.GetAllProductsByCategoryNameResponse;
import com.app.ecommerce.models.response.success.GetOrderByIdResponse;
import com.app.ecommerce.models.response.success.GetOrderStatusById;
import com.app.ecommerce.models.response.success.UpdateProductResponse;


public interface IOrderService {

    CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody);
    
    GetOrderByIdResponse findById(Long orderId);
    
    GetOrderStatusById findOrderStatusById(Long orderId);
}
