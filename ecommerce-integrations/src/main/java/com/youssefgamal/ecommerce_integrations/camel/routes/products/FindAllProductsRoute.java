package com.youssefgamal.ecommerce_integrations.camel.routes.products;



import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youssefgamal.ecommerce_integrations.camel.processors.JsonListGroupedBodyAggregator;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;



@Component
@Slf4j
public class FindAllProductsRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindAllProductsRoute")
	    .doTry()
	        .routeId("FindAllProductsRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	        .toD("{{ecommerce.product-service.url}}?bridgeEndpoint=true")
		    .log("Find All Product Response headers = ${headers}, body = ${body}")
		    .split(jsonpath("$"), new JsonListGroupedBodyAggregator())
    			.setProperty("copiedProduct", simple("${bodyAs(java.util.Map)}"))
    			.log("copiedProduct = ${exchangeProperty.copiedProduct}")
		    	.log("Product is = ${body}")
		    	.marshal().json()
		    	.log("Product-JSON is = ${body}")
		    	.split(jsonpath("$.categories.*"), new JsonListGroupedBodyAggregator())
		    		.log("---> Category = ${body}")
		    		.setHeader("id", simple("${body[id]}"))
		    		.toD("{{ecommerce.category-service.url}}/${header.id}?bridgeEndpoint=true")
				    .log("---------> Category GET /categories/${headers.id} Successfully with id = ${headers.id}, body = ${body}")
		    	.end()
		    	.log("aggregated-categories= ${body}")
		    	.process(this::setCategoriesInProductJsonResponse)
		    	.log("product-upated=${body}")
		    .end()
		    .setBody(simple("${body}"))
		    .to("direct:LogResponseRoute")
		    .endDoTry()
	    .doCatch(Exception.class)
	    	.log(LoggingLevel.ERROR, "ERROR Exception")
	    	.to("direct:RestExceptionHandlingRoute")
	     .end()
	    ;
		
	}

	
	private void setCategoriesInProductJsonResponse(Exchange exchange) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
    	Map product_map = exchange.getProperty("copiedProduct", Map.class);
    	log.info("product_map="+product_map);
    	JSONObject productJSON = new JSONObject(product_map);
    	log.info("productJSON="+productJSON);
    	JSONArray categoriesJSONArr = exchange.getIn().getBody(JSONArray.class);
    	log.info("categoriesJSONArr="+categoriesJSONArr);
    	productJSON.put("categories", categoriesJSONArr);
    	log.info("productJSON="+productJSON);
    	exchange.getIn().setBody(productJSON);
	}
}
