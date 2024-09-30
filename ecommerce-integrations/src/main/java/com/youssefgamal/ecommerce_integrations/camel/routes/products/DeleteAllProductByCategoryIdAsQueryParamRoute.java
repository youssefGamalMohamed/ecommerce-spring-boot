package com.youssefgamal.ecommerce_integrations.camel.routes.products;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class DeleteAllProductByCategoryIdAsQueryParamRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:DeleteAllProductByCategoryIdAsQueryParamRoute")
			.routeId("DeleteAllProductByCategoryIdAsQueryParamRoute")
			.to("direct:LogRequestRoute")
			.log("Http-Request: body = ${body}, headers = ${headers}")
			.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.DELETE))
			.toD("{{ecommerce.product-service.url}}?category_id=${headers.categoryIdQueryParam}&bridgeEndpoint=true")
	    	.log("Http-Response: body = ${body}, headers = ${headers}")
	    	.to("direct:LogResponseRoute")
	    ;
	}

}
