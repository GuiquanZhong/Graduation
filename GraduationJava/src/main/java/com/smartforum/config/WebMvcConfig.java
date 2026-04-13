package com.smartforum.config;

import com.smartforum.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/post/list",
                        "/api/post/search",
                        "/api/post/hot-search",
                        "/api/comment/list/**",
                        "/api/user/*/posts",
                        "/api/user/*/comments",
                        "/api/user/*/favorites",
                        "/api/user/*/likes",
                        "/api/user/*/following",
                        "/api/user/*/followers",
                        "/api/ai/search");
    }
}
