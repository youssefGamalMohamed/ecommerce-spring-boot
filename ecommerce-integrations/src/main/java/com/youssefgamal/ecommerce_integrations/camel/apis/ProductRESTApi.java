package com.youssefgamal.ecommerce_integrations.camel.apis;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.ProductDto;

@Component
public class ProductRESTApi extends RouteBuilder {

	@Override
	public void configure() throws Exception {
        rest("/products")
        	.description("CRUD Operation On Product")
        	.post()
        		.description("Create New Product")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(ProductDto.class)
	        	.outType(ProductDto.class)
	        	.to("direct:SaveProductRoute")
	        	.responseMessage()
	        		.code(HttpStatus.CREATED.value())
	        		.message("Product Created Successfully")
	        		.endResponseMessage()
	        .get("/{id}")
	        	.description("Find Product By Id")
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.outType(ProductDto.class)
	        	.to("direct:FindProductByIdRoute")
        		.responseMessage()
	    			.code(HttpStatus.OK.value())
	    			.message("Product Retrieved Successfully")
	    			.endResponseMessage()
	    		.responseMessage()
	    			.code(HttpStatus.NOT_FOUND.value())
	    			.message("No Product Found With This Id")
	    			.endResponseMessage()
	        .get()
	        	.description("Find All Products")
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.outType(ProductDto[].class)
	        	.to("direct:FindAllProductsRoute")
	        	.responseMessage()
	        		.code(HttpStatus.OK.value())
	        		.message("Products Retrieved Sucessfully")
	        		.endResponseMessage()
	        .delete("/{id}")
	        	.description("Delete Product By Id")
	        	.to("direct:DeleteProductByIdRoute")
	        	.responseMessage()
	        		.code(HttpStatus.NO_CONTENT.value())
	        		.message("Product Delete Successfully")
	        		.endResponseMessage()
		    	.responseMessage()
	    			.code(HttpStatus.NOT_FOUND.value())
	    			.message("No Product Found With This Id")
	    			.endResponseMessage()	
	        .put("/{id}")
	        	.description("Update Product By Id")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(ProductDto.class)
	        	.outType(ProductDto.class)
	        	.to("direct:UpdateProductRoute")
	        	.responseMessage()
	        		.code(HttpStatus.OK.value())
	        		.message("Product Updated Successfully")
	        		.endResponseMessage()
		    	.responseMessage()
	    			.code(HttpStatus.NOT_FOUND.value())
	    			.message("No Product Found With This Id")
	    			.endResponseMessage()	
	        .head()
	        	.description("Allow External Backend As Wso2 to know if there are an backend runing or not")
	        	.to("direct:HeadProductsRoute")
        		.responseMessage()
	    			.code(HttpStatus.OK.value())
	    			.endResponseMessage()
        	;	
		
       
	}

}
