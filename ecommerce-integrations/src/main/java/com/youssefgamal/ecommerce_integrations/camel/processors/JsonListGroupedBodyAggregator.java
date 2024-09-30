package com.youssefgamal.ecommerce_integrations.camel.processors;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Component
@Slf4j
public class JsonListGroupedBodyAggregator implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		ObjectMapper mapper = new ObjectMapper();
        
        // If it's the first exchange, just return the new exchange
        if (oldExchange == null) {
            // Initialize the body with an empty JSON array if it's the first exchange
            JSONArray jsonArray = new JSONArray();
            String newBody = newExchange.getIn().getBody(String.class);
            try {
                JSONObject newJson = mapper.readValue(newBody, JSONObject.class);
                jsonArray.add(newJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing JSON", e);
            }
            newExchange.getIn().setBody(jsonArray);
            return newExchange;
        }

        // Get the existing array from the old exchange
        String oldBody = oldExchange.getIn().getBody(String.class);
        JSONArray jsonArray;
        try {
            jsonArray = mapper.readValue(oldBody, JSONArray.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON", e);
        }

        // Add the new body to the array
        String newBody = newExchange.getIn().getBody(String.class);
        try {
            JSONObject newJson = mapper.readValue(newBody, JSONObject.class);
            jsonArray.add(newJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON", e);
        }

        // Set the updated array as the body
        oldExchange.getIn().setBody(jsonArray);
        log.info("aggregated-body-json-list="+jsonArray);
        return oldExchange;
	}

}
