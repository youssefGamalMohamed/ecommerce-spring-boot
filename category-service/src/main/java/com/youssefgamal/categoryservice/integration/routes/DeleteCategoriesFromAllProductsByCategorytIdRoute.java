package com.youssefgamal.categoryservice.integration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;



@Component
public class DeleteCategoriesFromAllProductsByCategorytIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:deleteCategoriesFromProductByProductIdRoute")
			.routeId("deleteCategoriesFromProductByProductIdRoute")
			.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.DELETE))
			.toD("http://localhost:9092/ecommerce/api/v1/products?categoryId=${body}")
	    	.log("Json Response: body = ${body}, headers = ${headers}")
	    	.process(exchange -> exchange.getIn().setBody(HttpStatus.valueOf((Integer)exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE))));
	}

}
