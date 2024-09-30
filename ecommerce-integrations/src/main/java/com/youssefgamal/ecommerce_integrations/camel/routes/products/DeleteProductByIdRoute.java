package com.youssefgamal.ecommerce_integrations.camel.routes.products;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class DeleteProductByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:DeleteProductByIdRoute")
	    .doTry()
	        .routeId("DeleteProductByIdRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.DELETE))
	        .toD("{{ecommerce.product-service.url}}/${header.id}?bridgeEndpoint=true")
	        .log("After Deleting Product By Id = ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
		    .log("Product Deleted Successfully with id = ${headers.id}")
		    .to("direct:LogResponseRoute")
		  .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .end()
	    ;
	}

}
