package org.example.expert.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.web.filter.OncePerRequestFilter;

public class AdminLogDataFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
          FilterChain filterChain) throws ServletException, IOException {
    request.setAttribute("requestTime", LocalDateTime.now());

    filterChain.doFilter(request, response);
  }
}
