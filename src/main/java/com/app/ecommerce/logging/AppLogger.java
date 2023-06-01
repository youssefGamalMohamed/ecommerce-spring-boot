package com.app.ecommerce.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.thymeleaf.util.StringUtils;

@Aspect
@Component
@Log4j2
@AllArgsConstructor
public class AppLogger extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpRequestResponseLoggingUtils httpRequestResponseLoggingUtils;

    @Autowired
    private LoggingUtils loggingUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        loggingUtils.logSeparator();


        Map<String, Object> requestInformation = httpRequestResponseLoggingUtils.getRequestInformationMap(request, requestWrapper);
        String requestInformationJsonString = objectMapper.writeValueAsString(requestInformation);

        log.info(
                " METHOD = {}; REQUESTURI = {}; \n\n REQUEST = {}; ",
                request.getMethod(), request.getRequestURI(), requestInformationJsonString
        );

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long timeTaken = System.currentTimeMillis() - startTime;




        Map<String, Object> responseBodyMap = httpRequestResponseLoggingUtils.getResponseInformationMap(response, responseWrapper);
        String responseBodyInformation = objectMapper.writeValueAsString(responseBodyMap);

        log.info(
                "\n\n RESPONSE = {}; \n\n TIME TAKEN = {} \n\n",
                    responseBodyInformation, timeTaken
        );



        loggingUtils.logSeparator();
        responseWrapper.copyBodyToResponse();
    }






    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethod(){}

    @Pointcut("execution(* com.app.*..*(..))")
    public void projectMethod(){}


    @Around(value = "projectMethod() && serviceMethod()")
    public Object loggingFunction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        Map<String,Object> serviceMap = new HashMap<>();

        String name = proceedingJoinPoint.getSignature().toLongString();
        Object inputs = proceedingJoinPoint.getArgs();
        Object object = proceedingJoinPoint.proceed();
        Object outputs = object;


        serviceMap.put("serviceFunctionName" , name);
        serviceMap.put("inputs" , inputs);
        serviceMap.put("outputs" , outputs);


        log.info("\n\n" + objectMapper.writeValueAsString(serviceMap));

        return object;
    }


    @AfterThrowing(pointcut = "projectMethod() && serviceMethod()" , throwing = "exception")
    public void afterThrowingException(JoinPoint joinPoint , Exception exception)  {
        Map<String,Object> exceptionMap = new HashMap<>();
        String name = joinPoint.getSignature().getDeclaringTypeName();
        exceptionMap.put("serviceFunctionName" , name);
        exceptionMap.put("exception" , exception.getMessage());
        try {
            log.info("\n\n" + "Exception = " + objectMapper.writeValueAsString(exceptionMap) + "\n\n");
        } catch (JsonProcessingException e) {
            log.error(">>>>>>> Exception Occurred with Parsing Object to JSON , Details = " + e.getOriginalMessage());
        }
    }

}
