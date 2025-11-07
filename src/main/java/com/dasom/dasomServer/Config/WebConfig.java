package com.dasom.dasomServer.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") // 💡 웹 접근 경로 (file.access-url 값)
                .addResourceLocations("file:///C:/Users/insen/devSource/App-Backend-Server/uploads/"); // 💡 저장된 물리적 경로 (file.upload-dir 값)
    }
}