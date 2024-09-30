package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class FindAllCategoriesRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindAllCategoriesRoute")
	    .doTry()
	        .routeId("FindAllCategoriesRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	        .toD("{{ecommerce.category-service.url}}?bridgeEndpoint=true")
	        .log("After Save Category ==> ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
	        .to("direct:LogResponseRoute")
	    .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .end()
	    ;
	}

}
