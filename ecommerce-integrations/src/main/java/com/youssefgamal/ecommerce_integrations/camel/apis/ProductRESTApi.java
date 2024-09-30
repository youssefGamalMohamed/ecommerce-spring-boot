package com.youssefgamal.ecommerce_integrations.camel.apis;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.ProductDto;

@Component
public class ProductRESTApi extends RouteBuilder {

	@Override
	public void configure() throws Exception {
        rest("/ecommerce")
        	.post("/api/v1/products")
	        	.consumes(MediaType.APPLICATION_JSON_VALUE)
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.type(ProductDto.class)
	        	.outType(ProductDto.class)
	        	.to("direct:SaveProductRoute")
	        .get("/api/v1/products/{id}")
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.outType(ProductDto.class)
	        	.to("direct:FindProductByIdRoute")
	        .get("/api/v1/products")
	        	.produces(MediaType.APPLICATION_JSON_VALUE)
	        	.outType(ProductDto[].class)
	        	.to("direct:FindAllProductsRoute")
	        .delete("/api/v1/products/{id}")
	        	.to("direct:DeleteProductByIdRoute")
        	;	
		
       
	}

}
