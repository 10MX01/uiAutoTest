package com.uiauto.config;

import com.uiauto.testcase.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置静态资源映射、跨域支持和JWT拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    /**
     * 配置静态资源映射
     * Spring Boot 默认会映射以下路径：
     * - classpath:/META-INF/resources/
     * - classpath:/resources/
     * - classpath:/static/
     * - classpath:/public/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源缓存配置（生产环境可启用）
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600); // 1小时缓存
    }

    /**
     * 配置 CORS 跨域
     * 如果前后端完全分离部署，需要配置此方法
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/users/login",           // 登录接口
                        "/users/generate-password", // 临时密码生成接口
                        "/assets/**",              // 静态资源
                        "/static/**",
                        "/index.html",
                        "/favicon.ico",
                        "/error"
                );
    }
}
