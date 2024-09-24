package com.youssefgamal.categoryservice.integration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.youssefgamal.categoryservice.dtos.ProductDto;



@Component
public class FindAllProductsByCategoryNameRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:findAllProductByCategoryName")
			.routeId("findAllProductByCategoryName")
			.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
			.toD("http://localhost:9092/ecommerce/api/v1/products?${body}")
	    	.log("Json Response: body = ${body}, headers = ${headers}")
	    	.unmarshal().json(ProductDto[].class)
	    	;
	}

}
