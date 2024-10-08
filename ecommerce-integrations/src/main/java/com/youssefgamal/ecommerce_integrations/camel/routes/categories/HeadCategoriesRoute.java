package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class HeadCategoriesRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:HeadCategoriesRoute")
			.routeId("HeadCategoriesRoute")
	    	.setHeader(HttpHeaders.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
	    	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.OK.value()))
	    	;
	}

}
