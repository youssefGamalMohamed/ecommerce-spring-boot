package com.youssefgamal.ecommerce_integrations.camel.routes.products;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class HeadProductsRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:HeadProductsRoute")
			.routeId("HeadProductsRoute")
			.setHeader(HttpHeaders.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
			.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.OK.value()))
		;
		
	}
}