package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

  private final JwtUtil jwtUtil;

  @Bean
  public FilterRegistrationBean<JwtFilter> jwtFilter() {
    FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new JwtFilter(jwtUtil));
    registrationBean.setOrder(1);
    registrationBean.addUrlPatterns("/*"); // 필터를 적용할 URL 패턴을 지정합니다.

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<AdminLogDataFilter> adminLogDataFilter() {
    FilterRegistrationBean<AdminLogDataFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new AdminLogDataFilter());
    registrationBean.setOrder(2);
    registrationBean.addUrlPatterns("/admin/*");
    return registrationBean;
  }
}
