package org.example.expert.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class AdminLoggingAspect {

  @Pointcut("@annotation(org.example.expert.domain.common.annotation.Admin)")
  public void adminAnnotation() {
  }

  @Around("adminAnnotation() &&"
          + "!execution(void *(*)) &&"
          + "args(.., body)")
  public void withRequestBody(ProceedingJoinPoint joinPoint, Object body) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    doLog(joinPoint, request, body);
  }

  @Around("adminAnnotation() && execution(void *(*))")
  public void withoutRequestBody(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    doLog(joinPoint, request, new HashMap<String, Object>());
  }

  public void doLog(ProceedingJoinPoint joinPoint, HttpServletRequest request, Object requestBody)
          throws Throwable {
    ObjectMapper om = new ObjectMapper();
    LocalDateTime requestTime = (LocalDateTime) request.getAttribute("requestTime");

    log.info("[{}] [REQUEST] [{} {}] {}",
            requestTime, request.getMethod(), request.getRequestURI(),
            om.writeValueAsString(requestBody));

    Object result = joinPoint.proceed();
    Object responseBody = result == null ? new HashMap<String, Object>() : result;

    log.info("[{}] [RESPONSE] [{} {}] {}",
            LocalDateTime.now(), request.getMethod(), request.getRequestURI(),
            om.writeValueAsString(responseBody));
  }
}
