package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidAdminAccessException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
          throws Exception {
    String role = (String) request.getAttribute("userRole");

    if (UserRole.of(role) != UserRole.ADMIN) {
      log.info("[{}] [{} {}] [userId: {}] BLOCKED.",
              LocalDateTime.now(), request.getMethod(),
              request.getRequestURI(), request.getAttribute("userId"));

      throw new InvalidAdminAccessException("관리자만 접근이 가능합니다.");
    }

    log.info("[{}] [{} {}] [userId: {}]admin task requested.",
            LocalDateTime.now(), request.getMethod(),
            request.getRequestURI(), request.getAttribute("userId"));
    return true;
  }
}
