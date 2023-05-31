package com.app.ecommerce.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.thymeleaf.util.StringUtils;

@Component
@Log4j2
public class AppLogger extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private void logSeparator(){
        log.info(StringUtils.repeat("=" , 200).toString());
    }

    private String getRequestOrResponseBodyStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            log.warn("Can Not Convert Request or Response to String to Log it");
        }
        return "";
    }

    private String getInlineJSONStringFrom(String objectString) {
        try {
            Object object = objectMapper.readValue(objectString, Object.class);
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        this.logSeparator();

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long timeTaken = System.currentTimeMillis() - startTime;




        // prepare request
        Map<String,String> queryParameters = this.getQueryParametersFrom(request);
        Map<String,String> pathVariables = this.getPathVariablesFrom(request);
        Map<String,Object> requestInformation = new HashMap<>();
        try {
            Object obj = objectMapper.readValue(
                                getRequestOrResponseBodyStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding()),
                                Object.class
                            );
            requestInformation.put("body", obj);
        }
        catch (Exception e) {
            log.info("obj  = {}" , e.getMessage());
        }
        requestInformation.put("pathVariables" , pathVariables);
        requestInformation.put("queryParameters" , queryParameters);
        String requestInformationJsonString = objectMapper.writeValueAsString(requestInformation);


        // prepare response
        Map<String,Object> responseBodyMap = new HashMap<>();
        responseBodyMap.put("httpStatusCode", response.getStatus());
        try {
            responseBodyMap.put("body", objectMapper.readValue(getInlineJSONStringFrom(getRequestOrResponseBodyStringValue(responseWrapper.getContentAsByteArray(),
                    response.getCharacterEncoding())), Object.class)
            );

        }
        catch (Exception e) {

        }

        String responseBodyInformation = objectMapper.writeValueAsString(responseBodyMap);

        log.info(
                "FINISHED PROCESSING : METHOD={}; REQUESTURI={}; \n REQUEST BODY ={}; \n RESPONSE={}; \n TIM TAKEN={}",
                request.getMethod(), request.getRequestURI(), requestInformationJsonString, responseBodyInformation,
                timeTaken);



        this.logSeparator();
        responseWrapper.copyBodyToResponse();
    }




    private Map<String,String> getQueryParametersFrom(HttpServletRequest request) {
        Map<String,String> map = new HashMap<>();


        for (Iterator<String> it = request.getParameterNames().asIterator(); it.hasNext(); ) {
            String key = it.next();
            map.put(key , request.getParameter(key));
        }

        return map;
    }


    private Map<String,String> getPathVariablesFrom(HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

}
