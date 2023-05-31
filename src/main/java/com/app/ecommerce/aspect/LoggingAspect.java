package com.app.ecommerce.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//@Aspect
//@Component
//@Log4j2
public class LoggingAspect {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//
//    private String getJSONStringFrom(Object object) {
//        if(object == null)
//            return "null";
//
//        try {
//            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//            return objectMapper.writeValueAsString(object);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void logSeparator(){
//        log.info(StringUtils.repeat("=" , 200).toString());
//    }
//
//    private void logRequestedURL() throws IOException {
//        this.logSeparator();
//        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//        log.info(request.getMethod() + " " + request.getRequestURI());
//    }
//
//    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void restControllerMethod(){}
//
//    @Pointcut("within(@org.springframework.web.bind.annotation.RestControllerAdvice *)")
//    public void restControllerAdviceMethod(){}
//
//    @Pointcut("execution(* com.app.*..*(..))")
//    public void projectMethod(){}
//
//
//    @Around(value = "restControllerMethod() && projectMethod()")
//    public Object loggingFunction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        this.logRequestedURL();
//        this.logRequestInformation();
//
//        return proceedingJoinPoint.proceed();
//    }
//
//    private void logRequestInformation() {
//        Map<String,String> queryParamsFromRequest = getQueryParamNameAndValueFromRequest();
//        Map<String,String> pathVariablesFromRequest = getPathVariablesNameAndValueFromRequest();
//
//        System.out.println(queryParamsFromRequest);
//
//        System.out.println(pathVariablesFromRequest);
//
//
//        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//
//
//        try {
//            String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//        } catch (IOException e) {
//            System.out.println("Failed ysta");
//        }
//
//
//    }
//
//    private Map<String,String> getQueryParamNameAndValueFromRequest() {
//        Map<String,String> map = new HashMap<>();
//
//        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//
//        for (Iterator<String> it = request.getParameterNames().asIterator(); it.hasNext(); ) {
//            String key = it.next();
//            map.put(key , request.getParameter(key));
//        }
//
//        return map;
//    }
//
//    private Map<String,String> getPathVariablesNameAndValueFromRequest() {
//        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//
//        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
//    }
//
//    private Map<String,String> getRequestBodyFromRequest() {
//        return null;
//    }
//
//    // this method will log separator line after each of function in each controller class annotated with @Controller
//    @AfterReturning(value = "restControllerMethod() && projectMethod()" , returning = "obj")
//    public void logAfterEndOfEachControllerFunction(JoinPoint joinPoint , Object obj) {
//        log.info("Response = \n" + this.getJSONStringFrom(obj));
//        this.logSeparator();
//    }
//
//
//    @AfterReturning(value = "restControllerAdviceMethod() && projectMethod()" , returning = "obj")
//    public void logAfterEndOfEachControllerAdviceFunction(JoinPoint joinPoint , Object obj) {
//        log.info("Response = \n" + this.getJSONStringFrom(obj));
//        this.logSeparator();
//    }

}
