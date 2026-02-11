package com.uiauto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * UiAutoTest应用启动类
 * AI驱动的Web自动化测试平台
 */
@SpringBootApplication
public class UiAutoTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiAutoTestApplication.class, args);
        System.out.println("=================================");
        System.out.println("UiAutoTest应用启动成功！");
        System.out.println("前端访问地址: http://localhost:5173");
        System.out.println("后端API地址: http://localhost:8080");
        System.out.println("=================================");
    }
}
