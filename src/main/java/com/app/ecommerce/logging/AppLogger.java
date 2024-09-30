package com.app.ecommerce.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;


// Invoke AppLogger Filter before the security filter chain
// it gives my AppLogger an @Order which is the smallest value as possible to be the first filter ( negative id ) to be the first filter
// before the spring security filters executed
// in the chain to log request and response
// as an example : AppLogger : -101 --> JwtFilterChain assume order 1 --> .. and so on the
// remaining filter chains will be applied
@Aspect
@Component
@Log4j2
@AllArgsConstructor
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 1)
public class AppLogger extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpRequestResponseInterceptorUtils httpRequestResponseInterceptorUtils;

    @Autowired
    private LoggingUtils loggingUtils;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        loggingUtils.logSeparator();


        logRequest(request, requestWrapper);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long timeTaken = System.currentTimeMillis() - startTime;


        logResponse(response, responseWrapper, timeTaken);


        loggingUtils.logSeparator();
        responseWrapper.copyBodyToResponse();
    }

    public void logResponse(HttpServletResponse response, ContentCachingResponseWrapper responseWrapper, long timeTaken) throws JsonProcessingException {
        Map<String, Object> responseBodyMap = httpRequestResponseInterceptorUtils.getResponseInformationMap(response, responseWrapper);
        String responseBodyInformation = objectMapper.writeValueAsString(responseBodyMap);

        log.info(
                "\n\n RESPONSE = {}; \n\n TIME TAKEN = {} \n\n",
                    responseBodyInformation, timeTaken
        );
    }
    public void logResponse(HttpServletResponse response, ContentCachingResponseWrapper responseWrapper) throws JsonProcessingException {
        Map<String, Object> responseBodyMap = httpRequestResponseInterceptorUtils.getResponseInformationMap(response, responseWrapper);
        String responseBodyInformation = objectMapper.writeValueAsString(responseBodyMap);

        log.info(
                "\n\n RESPONSE = {}; \n\n",
                responseBodyInformation
        );
    }

    public void logRequest(HttpServletRequest request, ContentCachingRequestWrapper requestWrapper) throws JsonProcessingException {
        Map<String, Object> requestInformation = httpRequestResponseInterceptorUtils.getRequestInformationMap(request, requestWrapper);
        String requestInformationJsonString = objectMapper.writeValueAsString(requestInformation);

        log.info(
                " METHOD = {}; REQUESTURI = {}; \n\n REQUEST = {}; ",
                request.getMethod(), request.getRequestURI(), requestInformationJsonString
        );
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
