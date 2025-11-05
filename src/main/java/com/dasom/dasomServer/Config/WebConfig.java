package com.dasom.dasomServer.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. application.yml에서 'file.upload-dir' 값을 읽어옵니다.
    // (예: "./uploads/")
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 2. application.yml에서 'file.access-path' 값을 읽어옵니다.
    // (예: "/uploads/")
    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 3. [수정] yml에서 읽어온 accessPath를 핸들러로 등록합니다.
        // (예: "/uploads/**")
        registry.addResourceHandler(accessPath + "**")

                // 4. [수정] yml에서 읽어온 uploadDir를 물리 경로로 연결합니다.
                // "file:" 접두사를 꼭 붙여야 합니다.
                // (예: "file:./uploads/")
                .addResourceLocations("file:" + uploadDir);
    }
}