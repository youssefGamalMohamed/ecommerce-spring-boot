package com.youssefgamal.ecommerce_integrations.camel.apis;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.CategoryDto;
@Component
public class CategoryRESTApi extends RouteBuilder {

	
	@Autowired
	private Environment env;
	
	
	@Override
	public void configure() throws Exception {
		
        rest("/categories")
        	.description("CRUD Operation On Category")
        	.post()
        		.description("Create New Category")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(CategoryDto.class)
	        	.outType(CategoryDto.class)
	        	.responseMessage()
	        		.code(HttpStatus.CREATED.value())
	        		.message("Category Created Successfully")
	        		.endResponseMessage()
	        	.to("direct:SaveCategoryRoute")
	        .delete("/{id}")
	        	.description("Delete Category By Id")
	            .to("direct:DeleteCategorytByIdRoute")
	            .responseMessage()
	            	.code(HttpStatus.NO_CONTENT.value())
	            	.message("Category Deleted Successfully")
	            	.endResponseMessage()
	            .responseMessage()
	            	.code(HttpStatus.NOT_FOUND.value())
	            	.message("No Category Exists with This Id")
	            	.endResponseMessage()
	        .put("/{id}")
	        	.description("Update Category By Id")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(CategoryDto.class)
	        	.outType(CategoryDto.class)
	        	.to("direct:UpdateCategoryByIdRoute")
	            .responseMessage()
	            	.code(HttpStatus.OK.value())
	            	.message("Category Updated Successfully")
	            	.endResponseMessage()
	            .responseMessage()
	            	.code(HttpStatus.NOT_FOUND.value())
	            	.message("No Category Exists with This Id")
	            	.endResponseMessage()
	        .get()
	        	.description("Find All Categories")
        		.produces(MediaType.APPLICATION_JSON_VALUE)
        		.outType(CategoryDto[].class)
        		.to("direct:FindAllCategoriesRoute")
        		.responseMessage()
        			.code(HttpStatus.OK.value())
        			.message("All Categories Retreived Successfully")
        			.endResponseMessage()
        	.get("/{id}")
        		.description("Find Category By Id")
        		.produces(MediaType.APPLICATION_JSON_VALUE)
        		.outType(CategoryDto.class)
        		.to("direct:FindCategoryByIdRoute")
        		.responseMessage()
        			.code(HttpStatus.OK.value())
        			.message("Categroy Retrieved Successfully")
        			.endResponseMessage()
        		.responseMessage()
        			.code(HttpStatus.NOT_FOUND.value())
        			.message("No Category Found With This Id")
        			.endResponseMessage()
        	.head()
        		.description("Allow External Backend As Wso2 to know if there are an backend runing or not")
        		.responseMessage()
        			.code(HttpStatus.OK.value())
        			.endResponseMessage()
        		.to("direct:HeadCategoriesRoute")
        	
        	;	
		
       
	}

}
