package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.CategoryDto;



@Component
public class SaveCategoryRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:SaveCategoryRoute")
	    .doTry()
	        .routeId("SaveCategoryRoute")
	        .unmarshal().json(CategoryDto.class)
	        .to("bean-validator:validateSavedCategoryDto")
	        .marshal().json()
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
	        .toD("{{ecommerce.category-service.url}}?bridgeEndpoint=true")
	        .log("After Save Category ==> ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
	    .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .end()
	    ;
	}

}
