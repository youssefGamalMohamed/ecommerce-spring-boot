package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class FindAllCategoriesRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindAllCategoriesRoute")
	    .doTry()
	        .routeId("FindAllCategoriesRoute")
	        .to("direct:LogRequestRoute")
            .setHeader("CamelRedis.Key", simple("CATEGORIES_CACHE::ALL"))
            .to("spring-redis://localhost:6379?command=GET")
	        .choice()
	        	.when(exchange -> exchange.getIn().getBody() == null || exchange.getIn().getBody(String.class).isBlank() ||  exchange.getIn().getBody(String.class).isEmpty())
	        		.log("Cache: No Value Present for key = ${headers.CamelRedis.Key}, body = ${body}")
	    	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	    	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	    	        .toD("{{ecommerce.category-service.url}}?bridgeEndpoint=true")
	    	        .log("After Save Category ==> ${headers.id} ==> BODY = ${body} , HEADERS = ${headers}")
	    	        .setHeader("CamelRedis.Key", simple("CATEGORIES_CACHE::ALL"))
	    	        .setHeader("CamelRedis.Value", simple("${bodyAs(java.lang.String)}"))
                    .setHeader("CamelRedis.Timeout", constant(10)) // set value with expiration time in seconds
                    .to("spring-redis://localhost:6379?command=SETEX") 
                    .log("Cache: Saved into Redis: SETEX key = ${headers.CamelRedis.Key} with value = ${headers.CamelRedis.Value} and expiration of ${headers.CamelRedis.Timeout} seconds")
                    .unmarshal().json(List.class)
                    .marshal().json(JsonLibrary.Jackson)
                    .endChoice()
	        	.otherwise()
	        		.log("Cache Hit Values Exist, body = ${body}")
	        		.unmarshal().json(List.class)
                    .marshal().json(JsonLibrary.Jackson)
                    .endChoice()
	        .endDoTry()
	    .doCatch(Exception.class)
	    	.to("direct:RestExceptionHandlingRoute")
	    .doFinally()
	    	.to("direct:LogResponseRoute")
	    .end()
	    ;
	}

}
