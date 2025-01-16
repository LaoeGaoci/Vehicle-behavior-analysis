package com.cms.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig 类用于配置 Spring MVC 的拦截器和 CORS 设置
 *
 * @author Seamher
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    /**
     * 配置 CORS 设置
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS mappings");
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
        logger.info("CORS mappings configured to allow all origins, methods, and headers");
    }
}
