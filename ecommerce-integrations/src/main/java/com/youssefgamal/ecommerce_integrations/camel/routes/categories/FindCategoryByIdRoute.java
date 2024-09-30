package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class FindCategoryByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindCategoryByIdRoute")
	    .doTry()
	        .routeId("FindCategoryByIdRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	        .toD("{{ecommerce.category-service.url}}/${header.id}?bridgeEndpoint=true")
		    .log("Category GET /categories/${headers.id} Successfully with id = ${headers.id}")
	    .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .end()
	    ;
	}

}
