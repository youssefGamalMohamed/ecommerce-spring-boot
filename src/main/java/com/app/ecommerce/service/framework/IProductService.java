package com.app.ecommerce.service.framework;

import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.models.response.endpoints.AddNewProductResponse;
import com.app.ecommerce.models.response.endpoints.DeleteProductByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetAllProductsByCategoryNameResponse;
import com.app.ecommerce.models.response.endpoints.UpdateProductResponse;


public interface IProductService {

    AddNewProductResponse addNewProduct(PostProductRequestBody productDTO);

    GetAllProductsByCategoryNameResponse findProductsByCategoryName(String categoryName);
    
    UpdateProductResponse updateProductById(Long productId , PutProductRequestBody updatedProductRequestBody);

    DeleteProductByIdResponse deleteProductById(Long productId);

}
