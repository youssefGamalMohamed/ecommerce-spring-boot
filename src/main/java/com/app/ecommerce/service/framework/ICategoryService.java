package com.app.ecommerce.service.framework;

import java.util.List;

import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.models.response.success.AddNewCategoryResponse;
import com.app.ecommerce.models.response.success.DeleteCategoryResponse;
import com.app.ecommerce.models.response.success.GetAllCategoriesReponse;
import com.app.ecommerce.models.response.success.GetCategoryByIdResponse;
import com.app.ecommerce.models.response.success.UpdateCategoryResponse;



public interface ICategoryService {
	
    AddNewCategoryResponse add(PostCategoryRequestBody category) throws DuplicatedUniqueColumnValueException;

    DeleteCategoryResponse deleteById(Long categoryId) throws IdNotFoundException;

    GetAllCategoriesReponse findAll();

	GetCategoryByIdResponse findById(Long categoryId);
	
	UpdateCategoryResponse updateById(Long categoryId , PutCategoryRequestBody updatedCategory);
}
