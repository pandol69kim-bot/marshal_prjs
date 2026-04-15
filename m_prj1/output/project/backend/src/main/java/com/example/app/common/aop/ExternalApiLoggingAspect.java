package com.example.app.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExternalApiLoggingAspect {

    @Around("execution(* com.example.app.infrastructure.external.client.*.*(..))")
    public Object logExternalCall(ProceedingJoinPoint pjp) throws Throwable {
        String serviceName = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[External API] {}.{} - SUCCESS ({}ms)", serviceName, methodName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[External API] {}.{} - FAILED ({}ms): {}",
                    serviceName, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
