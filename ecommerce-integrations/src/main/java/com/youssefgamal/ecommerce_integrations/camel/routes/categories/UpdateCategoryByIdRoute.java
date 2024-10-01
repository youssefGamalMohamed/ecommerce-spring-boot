package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.youssefgamal.ecommerce_integrations.camel.dtos.CategoryDto;



@Component
public class UpdateCategoryByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:UpdateCategoryByIdRoute")
			.id("UpdateCategoryByIdRoute")
			.doTry()
				.unmarshal().json(CategoryDto.class)
		        .to("bean-validator:validateUpdatedCategoryDto")
		        .marshal().json()
		        .log("Befoer Update Category By Id = ${headers.id}")
		        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.PUT))
				.toD("{{ecommerce.category-service.url}}/${headers.id}?bridgeEndpoint=true")
				.log("After Update Category By Id = ${headers.id}")
			.doCatch(Exception.class)
				.to("direct:RestExceptionHandlingRoute")
			.end()
	    ;
	}

}
