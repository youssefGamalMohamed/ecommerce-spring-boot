package com.app.ecommerce.service.framework;


import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.models.response.endpoints.AddNewCategoryResponse;
import com.app.ecommerce.models.response.endpoints.DeleteCategoryResponse;
import com.app.ecommerce.models.response.endpoints.GetAllCategoriesResponse;
import com.app.ecommerce.models.response.endpoints.GetCategoryByIdResponse;
import com.app.ecommerce.models.response.endpoints.UpdateCategoryResponse;



public interface ICategoryService {
	
    AddNewCategoryResponse add(PostCategoryRequestBody category) throws DuplicatedUniqueColumnValueException;

    DeleteCategoryResponse deleteById(Long categoryId) throws IdNotFoundException;

    GetAllCategoriesResponse findAll();

	GetCategoryByIdResponse findById(Long categoryId);
	
	UpdateCategoryResponse updateById(Long categoryId , PutCategoryRequestBody updatedCategory);
}
