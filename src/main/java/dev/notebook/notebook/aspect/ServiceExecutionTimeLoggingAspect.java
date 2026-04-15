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
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startedAt = System.nanoTime();
    try {
      return joinPoint.proceed();
    } finally {
      long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
      log.info("Service method {} executed in {} ms",
          joinPoint.getSignature().toShortString(), elapsedMs);
    }
  }
}
