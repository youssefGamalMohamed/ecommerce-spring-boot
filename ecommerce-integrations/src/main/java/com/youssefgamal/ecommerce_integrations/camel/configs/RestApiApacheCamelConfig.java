package com.youssefgamal.ecommerce_integrations.camel.configs;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestApiApacheCamelConfig extends RouteBuilder {

	@Override
	public void configure() throws Exception {
        restConfiguration()
        	.apiContextRouteId("RestApiApacheCamelConfig")
	    	.contextPath("{{camel.servlet.mapping.context-path}}") // from the application.yml
	        .apiContextPath("/api-doc")
	        .apiProperty("api.title", "Ecommerce API")
	        .apiProperty("api.version", "1.0")
	        .enableCORS(false)
	        .apiContextRouteId("doc-api")
	        .port("{{server.port}}");   							// from the application.yml
		
	}

}
