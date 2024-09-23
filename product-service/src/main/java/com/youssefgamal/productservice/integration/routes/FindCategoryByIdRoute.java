package com.youssefgamal.productservice.integration.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.youssefgamal.productservice.dtos.CategoryDto;


@Component
public class FindCategoryByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:findCategoryById")
		 	.routeId("findCategoryById")
			.doTry()
	        	.setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	        	.toD("http://localhost:9091/ecommerce/api/v1/categories/${body}")
	        	.log("Json Response: ${body}")
	        	.unmarshal().json(CategoryDto.class)
	        	.log("Java Object: ${body}")
	        .doCatch(Exception.class)
	            .choice()
	            	.when(exchange -> {
	            		 HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
	                     return exception != null && exception.getStatusCode() == 404;
	            	})
	                	.log(LoggingLevel.INFO, "Category not found, returning null")
	                	.setBody(constant(CategoryDto.builder().build()))  // Set body to null for 404
	                .otherwise()
	                	.log(LoggingLevel.INFO, "Other exception, returning empty CategoryDto")
	                	.setBody(constant(CategoryDto.builder().build()))  // Handle other HTTP errors
	               .endChoice()
	             .endDoCatch()
	        .end();
	        	
	}

}
