package com.youssefgamal.ecommerce_integrations.camel.routes.commons.logging;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Service;


@Service
public class LogRequestRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:LogRequestRoute")
			.routeId("LogRequestRoute")
			.log(LoggingLevel.INFO, "Incoming Request: Path: ${header.CamelHttpMethod} ${header.CamelHttpUri}, Headers: ${headers}, Body: ${body}")
			;
		
	}

}
