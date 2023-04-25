package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.models.response.success.AddNewProductResponse;
import com.app.ecommerce.models.response.success.GetAllProductsByCategoryNameResponse;
import com.app.ecommerce.models.response.success.UpdateProductResponse;


public interface IProductService {

    AddNewProductResponse addNewProduct(PostProductRequestBody productDTO);

    GetAllProductsByCategoryNameResponse findProductsByCategoryName(String categoryName);
    
    UpdateProductResponse updateProductById(Long productId , PutProductRequestBody updatedProductRequstBody);
}
