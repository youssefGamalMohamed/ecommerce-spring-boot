package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class DeleteCategorytByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:DeleteCategorytByIdRoute")
	    .doTry()
	        .routeId("DeleteCategorytByIdRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.DELETE))
	        .toD("{{ecommerce.category-service.url}}/${header.id}?bridgeEndpoint=true")
	        .log("After Deleting Category By Id = ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
		    .log("Category Deleted Successfully with id = ${headers.id}")
		    .log("We Will Call Product Service to Delete All Products Associated With Category Id = ${headers.id}")
		    .process(exchange -> exchange.getIn().setHeader("categoryIdQueryParam", exchange.getIn().getHeader("id")))
		    .doTry()
		    	.to("direct:DeleteAllProductByCategoryIdAsQueryParamRoute")
		    	.to("direct:LogResponseRoute")
		    .doCatch(Exception.class)
		    	.to("direct:RestExceptionHandlingRoute")
	    .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .end()
	    ;
	}

}
