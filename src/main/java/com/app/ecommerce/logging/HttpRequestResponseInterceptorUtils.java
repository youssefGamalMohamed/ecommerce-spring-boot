package com.app.ecommerce.logging;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class HttpRequestResponseInterceptorUtils {
    @Autowired
    private ObjectMapper objectMapper;


    public Map<String,String> getQueryParametersFrom(HttpServletRequest request) {
        Map<String,String> map = new HashMap<>();


        for (Iterator<String> it = request.getParameterNames().asIterator(); it.hasNext(); ) {
            String key = it.next();
            map.put(key , request.getParameter(key));
        }

        return map;
    }


    public Map<String,String> getPathVariablesFrom(HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    public Map<String,String> getHeaderFromRequest(HttpServletRequest request) {
        return Map.of(HttpHeaders.AUTHORIZATION , request.getHeader(HttpHeaders.AUTHORIZATION) == null ? "" :  request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    public Map<String, Object> getResponseInformationMap(HttpServletResponse response, ContentCachingResponseWrapper responseWrapper) {
        // prepare response
        Map<String,Object> responseBodyMap = new HashMap<>();
        responseBodyMap.put("httpStatusCode", response.getStatus());

        try {
            responseBodyMap.put("body", objectMapper.readValue(getInlineJSONStringFrom(getRequestOrResponseBodyStringValue(responseWrapper.getContentAsByteArray(),
                    response.getCharacterEncoding())), Object.class)
            );
        } catch (JsonProcessingException | UnsupportedEncodingException e) {

        }

        return responseBodyMap;
    }
    public String getRequestOrResponseBodyStringValue(byte[] contentAsByteArray, String characterEncoding) throws UnsupportedEncodingException {
        return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
    }

    public String getInlineJSONStringFrom(String objectString) throws JsonProcessingException {
        Object object = objectMapper.readValue(objectString, Object.class);
        return objectMapper.writeValueAsString(object);
    }






    public Map<String, Object> getRequestInformationMap(HttpServletRequest request, ContentCachingRequestWrapper requestWrapper) {
        // prepare request
        Map<String,String> queryParameters = this.getQueryParametersFrom(request);
        Map<String,String> pathVariables = this.getPathVariablesFrom(request);
        Map<String,Object> requestInformation = new HashMap<>();
        Map<String,String> headers = this.getHeaderFromRequest(request);


        try {
            requestInformation.put("body", objectMapper.readValue(
                            getRequestOrResponseBodyStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding()),
                            Object.class
                    )
            );
        } catch (JsonProcessingException | UnsupportedEncodingException e) {

        }



        requestInformation.put("pathVariables" , pathVariables);
        requestInformation.put("queryParameters" , queryParameters);
        requestInformation.put("headers" , headers);

        return requestInformation;
    }

}
