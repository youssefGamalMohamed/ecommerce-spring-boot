package com.youssefgamal.categoryservice.integration.services;


import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.youssefgamal.categoryservice.dtos.ProductDto;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class CamelProductIntegrationServiceImpl implements CamelProductIntegrationServiceIfc {

	
	@Autowired
	private ProducerTemplate producerTemplate;
	
	@Override
	public HttpStatus deleteById(Long id) {
		log.info("deleteById({})",id);
		return producerTemplate.requestBody("direct:deleteProductByIdRoute", String.valueOf(id), HttpStatus.class);
	}

	@Override
	public ProductDto[] findAll() {
		log.info("findAll()");
		return producerTemplate.requestBody("direct:findAllProductRoute", null, ProductDto[].class);
	}

	@Override
	public ProductDto[] findAllByCategoryName(String categoryName) {
		log.info("findAllByCategoryName({})", categoryName);
		return producerTemplate.requestBody("direct:findAllProductByCategoryName", categoryName, ProductDto[].class);
	}

	@Override
	public HttpStatus deleteCategoryFromProduct(Long categoryId) {
		log.info("deleteCategoryFromProduct({})", categoryId);
		return producerTemplate.requestBody("direct:deleteCategoriesFromProductByProductIdRoute", categoryId, HttpStatus.class);
	}

}
