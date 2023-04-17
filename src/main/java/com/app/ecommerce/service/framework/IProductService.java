package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Product;
import com.app.ecommerce.models.request.ProductDTO;

import java.util.List;

import org.springframework.stereotype.Service;

public interface IProductService {

    boolean addProduct(ProductDTO productDTO);

	List<ProductDTO> getProductsByCategoryId(Long categoryId);
}
