package com.youssefgamal.ecommerce_integrations.camel.routes.products;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.ProductDto;

import lombok.extern.slf4j.Slf4j;



@Component
@Slf4j
public class SaveProductRoute extends RouteBuilder {

	
	
	@Override
	public void configure() throws Exception {
		
		from("direct:SaveProductRoute")
	    .doTry()
	        .routeId("SaveProductRoute")
	        .to("direct:LogRequestRoute")
	        .unmarshal().json(ProductDto.class)
	        .to("bean-validator:validateSavedProductDto")
	        .marshal().json()
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
	        .toD("{{ecommerce.product-service.url}}?bridgeEndpoint=true")
	        .log("After Save Category ==> ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
	        .to("direct:LogResponseRoute")
	    .doCatch(Exception.class)
	        .to("direct:RestExceptionHandlingRoute")
	     .end()
	    ;
	}

}
