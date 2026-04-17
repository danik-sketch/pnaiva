package dev.notebook.notebook.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceExecutionTimeLoggingAspect {

  @Around("execution(public * dev.notebook.notebook.service..*(..))")
  public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    String method = joinPoint.getSignature().toShortString();
    Object result = joinPoint.proceed();
    log.info("Service method {} executed in {} ms", method, System.currentTimeMillis() - start);
    return result;
  }
}
