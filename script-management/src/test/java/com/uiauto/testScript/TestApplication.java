package com.uiauto.testScript;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 测试应用主类
 * 用于Repository和Controller的集成测试
 */
@SpringBootApplication
@EntityScan(basePackages = {"com.uiauto.entity", "com.uiauto.testScript.entity"})
@EnableJpaRepositories(basePackages = {"com.uiauto.repository", "com.uiauto.testScript.repository"})
public class TestApplication {
    // 测试应用主类
}
