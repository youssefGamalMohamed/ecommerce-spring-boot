package com.youssefgamal.ecommerce_integrations.camel.routes.products;



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
public class FindProductByIdRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:FindProductByIdRoute")
	    .doTry()
	        .routeId("FindProductByIdRoute")
	        .to("direct:LogRequestRoute")
	        .log("Before Calling Backend ==> BODY = ${body} , HEADERS = ${headers}")
	        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
	        .toD("{{ecommerce.product-service.url}}/${header.id}?bridgeEndpoint=true")
		    .log("Product GET /categories/${headers.id} Successfully with id = ${headers.id}, body = ${body}")
		    .setProperty("copyOfBodyJSONProductAsString", simple("${bodyAs(String)}"))
		    .split(jsonpath("$.categories.*"), new JsonListGroupedBodyAggregator())
		    	.setHeader("id", simple("${body[id]}"))
            	.log("Full Category = ${body}")
	        	.log("ALL HEADERS = ${headers}")
		        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.GET))
		        .toD("{{ecommerce.category-service.url}}/${header.id}?bridgeEndpoint=true")
			    .log("Category GET /categories/${headers.id} Successfully with id = ${headers.id}, body = ${body}")
			    .end() 				// end of split
			    .process(this::setCategoriesInProductJsonResponse)
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
    	String product_dto_str = exchange.getProperty("copyOfBodyJSONProductAsString", String.class);
    	log.info("product_dto_str="+product_dto_str);
    	JSONObject productJSON = mapper.readValue(product_dto_str, JSONObject.class);
    	log.info("productJSON="+productJSON);
    	JSONArray categoriesJSONArr = exchange.getIn().getBody(JSONArray.class);
    	log.info("categoriesJSONArr="+categoriesJSONArr);
    	productJSON.put("categories", categoriesJSONArr);
    	log.info("productJSON="+productJSON);
    	exchange.getIn().setBody(productJSON);
	}
}
