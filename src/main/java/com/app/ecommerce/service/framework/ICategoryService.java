package com.app.ecommerce.service.framework;

import java.util.List;

import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryRequestBody;
import com.app.ecommerce.models.response.success.AddNewCategoryResponse;
import com.app.ecommerce.models.response.success.DeleteCategoryResponse;
import com.app.ecommerce.models.response.success.GetAllCategoriesReponse;



public interface ICategoryService {
	
    AddNewCategoryResponse add(CategoryRequestBody category) throws DuplicatedUniqueColumnValueException;

    DeleteCategoryResponse deleteById(Long categoryId) throws IdNotFoundException;

    GetAllCategoriesReponse findAll();

}
