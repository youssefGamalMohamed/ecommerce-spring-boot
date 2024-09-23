package com.youssefgamal.productservice.integration.services;

import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.dtos.CategoryDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CamelCategoryIntegrationServiceImpl implements CategoryIntegrationServiceIfc {

	
	@Autowired
	private ProducerTemplate producerTemplate;
	
	@Override
	public CategoryDto findById(Long id) {
		log.info("findById({})",id);
		return producerTemplate.requestBody("direct:findCategoryById", String.valueOf(id), CategoryDto.class);
	}

}
