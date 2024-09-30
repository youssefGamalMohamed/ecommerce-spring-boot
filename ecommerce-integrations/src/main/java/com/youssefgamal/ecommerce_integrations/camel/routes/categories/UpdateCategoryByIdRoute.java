package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class UpdateCategoryByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:UpdateCategoryByIdRoute")	
			.id("UpdateCategoryByIdRoute")
			.log("Request body=${body}, headers=${headers}")
			.to("direct:LogRequestRoute")
			.log("Befoer Update Category By Id = ${headers.id}")
			.doTry()
				.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.PUT))
				.toD("{{ecommerce.category-service.url}}/${headers.id}?bridgeEndpoint=true")
				.log("After Update Category By Id = ${headers.id}")
			.doCatch(Exception.class)
				.to("direct:RestExceptionHandlingRoute")
			.end()
	    ;
	}

}
