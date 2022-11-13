package com.lingyi.RootGet.config;

import com.lingyi.RootGet.filter.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Component
public class InterceptorConfig implements WebMvcConfigurer {
    private final Interceptor interceptor;
    @Autowired
    public InterceptorConfig(Interceptor interceptor){
        this.interceptor = interceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(interceptor);
        //registration.addPathPatterns("/api/*");
        registration.excludePathPatterns("/Mine/*","/Video/*","/video/*","/getWords","/getWebs","/api/account/*","/api/video/*","/api/comments/getComments/*","/api/collection/getCollectionByUid/*");
    }
}
