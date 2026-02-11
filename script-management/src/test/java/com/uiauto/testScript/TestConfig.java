package com.uiauto.testScript;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 测试配置类
 * 用于Repository和Controller的集成测试
 */
@TestConfiguration
@EntityScan(basePackages = {"com.uiauto.entity", "com.uiauto.testScript.entity"})
@EnableJpaRepositories(basePackages = {"com.uiauto.repository", "com.uiauto.testScript.repository"})
@EnableTransactionManagement
public class TestConfig {
    // 测试配置
}
