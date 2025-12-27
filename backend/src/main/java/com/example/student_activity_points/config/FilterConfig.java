package com.example.student_activity_points.config;

import com.example.student_activity_points.filters.ForgotPasswordRateLimitFilter;
import com.example.student_activity_points.filters.LoginRateLimitFilter;
import com.example.student_activity_points.filters.RegistrationRateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public ForgotPasswordRateLimitFilter forgotPasswordRateLimitFilterBean() {
        return new ForgotPasswordRateLimitFilter();
    }

    @Bean
    public FilterRegistrationBean<ForgotPasswordRateLimitFilter> forgotPasswordRateLimitFilterRegistration() {
        FilterRegistrationBean<ForgotPasswordRateLimitFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        registrationBean.setFilter(forgotPasswordRateLimitFilterBean());
        registrationBean.addUrlPatterns("/admin/forgot-password");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilterBean() {
        return new LoginRateLimitFilter();
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration() {
        FilterRegistrationBean<LoginRateLimitFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        registrationBean.setFilter(loginRateLimitFilterBean());
        registrationBean.addUrlPatterns("/api/auth/login", "/admin/login");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }

    @Bean
    public RegistrationRateLimitFilter registrationRateLimitFilterBean() {
        return new RegistrationRateLimitFilter();
    }

    @Bean
    public FilterRegistrationBean<RegistrationRateLimitFilter> registrationRateLimitFilterRegistration() {
        FilterRegistrationBean<RegistrationRateLimitFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        registrationBean.setFilter(registrationRateLimitFilterBean());
        registrationBean.addUrlPatterns("/admin/register", "/api/auth/register");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }
}