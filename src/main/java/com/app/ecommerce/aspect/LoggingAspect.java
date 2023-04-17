package com.app.ecommerce.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.StringUtils;
import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@Log4j2
public class LoggingAspect {

    private void logSeparator(){
        log.info(StringUtils.repeat("=" , 200).toString());
    }

    private void logRequestInformation() {
        this.logSeparator();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        log.info(request.getMethod() + " " + request.getRequestURI());
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethod(){}

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethod(){}

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethod(){}

    @Pointcut("execution(* com.app.*..*(..))")
    public void projectMethod(){}


    @Before("projectMethod() && ( restControllerMethod() || serviceMethod() || repositoryMethod() )")
    public void logBeforeAnyMethodExecution(JoinPoint joinPoint) {
        log.info("Method = [" + joinPoint.getSignature().toString() + "]" + ", Args = " + Arrays.toString(joinPoint.getArgs()));
    }


    @Around(value = "restControllerMethod() && projectMethod()")
    public Object loggingFunction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        this.logRequestInformation();

        Object obj = proceedingJoinPoint.proceed();

        this.logSeparator();
        return obj;
    }

    // this method will log separator line after each of function in each controller class annotated with @Controller
    @AfterReturning(value = "restControllerMethod() && projectMethod()" , returning = "obj")
    public void logAfterEndOfEachControllerFunction(JoinPoint joinPoint , Object obj) {
        log.info("End of Controller Method = " + joinPoint.getSignature().toString() + " ====> With Returned Object = " + obj.toString());
    }



}
