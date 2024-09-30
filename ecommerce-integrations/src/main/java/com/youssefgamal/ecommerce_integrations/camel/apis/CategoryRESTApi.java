package com.youssefgamal.ecommerce_integrations.camel.apis;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.CategoryDto;

@Component
public class CategoryRESTApi extends RouteBuilder {

	@Override
	public void configure() throws Exception {
        rest("/ecommerce")
        	.post("/api/v1/categories")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(CategoryDto.class)
	        	.outType(CategoryDto.class)
	        	.to("direct:SaveCategoryRoute")
	        .delete("/api/v1/categories/{id}")
	            .to("direct:DeleteCategorytByIdRoute")
	        .put("/api/v1/categories/{id}")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(CategoryDto.class)
	        	.outType(CategoryDto.class)
	        	.to("direct:UpdateCategoryByIdRoute")
	        .get("/api/v1/categories")
        		.produces(MediaType.APPLICATION_JSON_VALUE)
        		.outType(CategoryDto[].class)
        		.to("direct:FindAllCategoriesRoute")
        	.get("/api/v1/categories/{id}")
        		.produces(MediaType.APPLICATION_JSON_VALUE)
        		.outType(CategoryDto.class)
        		.to("direct:FindCategoryByIdRoute")
        	
        	;	
		
	}

}
