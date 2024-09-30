package com.youssefgamal.ecommerce_integrations.camel.routes.exceptions;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class RestExceptionHandlingRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:RestExceptionHandlingRoute")
			.routeId("RestExceptionHandlingRoute")
			.log("Start Of RestExceptionHandlingRoute")
			.choice()
				.when(exchange -> exchange.getProperty(Exchange.EXCEPTION_CAUGHT) instanceof HttpOperationFailedException)
					.process(this::setHttpStatusCodeOfResponseFromHttpFailedException)
					.log(LoggingLevel.ERROR, "Exception Type is HttpOperationFailedException.class")
					.choice()
						.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value()))
			            	.log(LoggingLevel.ERROR, "HttpStatus Code = 500, Internal Server Error Other exception")
			            	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.INTERNAL_SERVER_ERROR.value()))
			            	.to("direct:LogResponseRoute")
			            	
			            .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(HttpStatus.NOT_FOUND.value()))
			            	.log(LoggingLevel.ERROR, "HttpStatus Code = 404, Element Not-Found")
			            	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.NOT_FOUND.value()))
			            	.to("direct:LogResponseRoute")
			            .otherwise()
			            	.log(LoggingLevel.ERROR, "HttpStatus Code = 500, Internal Server Error Other exception")
			            	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.INTERNAL_SERVER_ERROR.value()))
			            	.to("direct:LogResponseRoute")
			         .endChoice()
			            	
		            	
	            .otherwise()
	            	.log(LoggingLevel.ERROR, "Exception Details ${exception}")
	            	.log(LoggingLevel.ERROR, "HttpStatus Code = 500, Internal Server Error Other exception")
	            	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.INTERNAL_SERVER_ERROR.value()))
	            	.setBody(simple(""))
	            	.to("direct:LogResponseRoute")
	            .endChoice()
	        .end()
			;
	}
	
	
	private void setHttpStatusCodeOfResponseFromHttpFailedException(Exchange exchange) {
		HttpOperationFailedException httpOperationFailedException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, httpOperationFailedException.getStatusCode());
	}

}
