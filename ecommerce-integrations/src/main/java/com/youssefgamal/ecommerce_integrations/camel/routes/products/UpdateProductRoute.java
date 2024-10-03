package com.youssefgamal.ecommerce_integrations.camel.routes.products;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:UpdateProductRoute")
			.routeId("UpdateProductRoute")
			.to("direct:LogRequestRoute")
			.doTry()
				.log("Before Calling PUT /product/${headers.id} with body = ${body}")
				.toD("http://localhost:9092/ecommerce/api/v1/products/${headers.id}?bridgeEndpoint=true")
				.log("After Calling PUT /product/${headers.id} with body = ${body}")
				.to("direct:LogResponseRoute")
			.doCatch(Exception.class)
				.to("direct:RestExceptionHandlingRoute")
		;
	}

}
