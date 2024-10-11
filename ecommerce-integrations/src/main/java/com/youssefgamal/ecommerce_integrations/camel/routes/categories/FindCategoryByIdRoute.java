package com.youssefgamal.ecommerce_integrations.camel.routes.categories;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;



@Component
public class FindCategoryByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindCategoryByIdRoute")
        .doTry()
            .routeId("FindCategoryByIdRoute")
            .to("direct:LogRequestRoute")
            .setHeader("CamelRedis.Key", simple("CATEGORIES_CACHE::${headers.id}"))
            .to("spring-redis://localhost:6379?command=GET")
            .choice()
                .when(body().isEqualTo(""))
                    .log("REDIS GET returned null for key: ${headers.CamelRedis.Key}")
                    .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
                    .toD("{{ecommerce.category-service.url}}/${header.id}?bridgeEndpoint=true")
                    .log("Category GET /categories/${headers.id} Successfully with id = ${headers.id}")
                    .setHeader("CamelRedis.Value", simple("${bodyAs(java.lang.String)}"))
                    .setHeader("CamelRedis.Timeout", constant(10)) // set value with expiration time in seconds
                    .to("spring-redis://localhost:6379?command=SETEX") 
                    .log("REDIS SETEX key = ${headers.CamelRedis.Key} with value = ${headers.CamelRedis.Value} and expiration of 3600 seconds")
                    .unmarshal().json(Map.class)
                    .marshal().json(JsonLibrary.Jackson)
                .endChoice()
            .otherwise()
                .log("REDIS GET result for key: ${headers.CamelRedis.Key} is ${body}")
                .unmarshal().json(Map.class)
                .marshal().json(JsonLibrary.Jackson)
        .endDoTry()
        .doCatch(Exception.class)
            .to("direct:RestExceptionHandlingRoute")
        .doFinally()
            .to("direct:LogResponseRoute")
        .end();
		
	}
	
}
