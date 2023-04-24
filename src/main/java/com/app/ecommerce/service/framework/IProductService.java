package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.ProductRequestBody;
import com.app.ecommerce.models.response.success.AddNewProductResponse;
import com.app.ecommerce.models.response.success.GetAllProductsByCategoryIdResponse;


public interface IProductService {

    AddNewProductResponse addNewProduct(ProductRequestBody productDTO);

    GetAllProductsByCategoryIdResponse findProductsByCategoryId(Long categoryId);
}
