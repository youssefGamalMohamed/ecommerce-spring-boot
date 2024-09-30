package com.youssefgamal.ecommerce_integrations.camel.routes.commons.logging;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class LogResponseRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:LogResponseRoute")
			.routeId("LogResponseRoute")
     		.log(LoggingLevel.INFO, "Outcoming Response: Path: ${header.CamelHttpMethod} ${header.CamelHttpUri}, Headers: ${headers}, Body: ${body}")
     		;
     	
	}

	
}
