package com.uiauto.testScript.repository;

import com.uiauto.testScript.entity.TestScriptEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestScriptRepository 集成测试
 * 使用H2内存数据库测试JPA Repository
 */
@DataJpaTest
@ActiveProfiles("test")
class TestScriptRepositoryTest {

    @Autowired
    private TestScriptRepository scriptRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TestScriptEntity testScript1;
    private TestScriptEntity testScript2;
    private TestScriptEntity testScript3;

    @BeforeEach
    void setUp() {
        // 清理数据库
        scriptRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        // 创建测试数据
        testScript1 = TestScriptEntity.builder()
                .scriptName("登录测试脚本")
                .scriptDescription("测试用户登录功能")
                .scriptContent("test('login', () => { ... });")
                .language("typescript")
                .generationMethod("AI_GENERATED")
                .enabled(true)
                .aiGenerationStatus("SUCCESS")
                .aiRetryCount(0)
                .testCaseId(1L)
                .category("用户认证")
                .executionCount(10)
                .lastExecutionTime(now.minusDays(1))
                .lastExecutionResult("SUCCESS")
                .build();

        testScript2 = TestScriptEntity.builder()
                .scriptName("注册测试脚本")
                .scriptDescription("测试用户注册功能")
                .scriptContent("test('register', () => { ... });")
                .language("typescript")
                .generationMethod("EXCEL_IMPORT")
                .enabled(false)
                .aiGenerationStatus("SUCCESS")
                .aiRetryCount(0)
                .testCaseId(2L)
                .category("用户认证")
                .executionCount(5)
                .lastExecutionTime(now.minusDays(2))
                .lastExecutionResult("FAILED")
                .build();

        testScript3 = TestScriptEntity.builder()
                .scriptName("搜索测试脚本")
                .scriptDescription("测试搜索功能")
                .scriptContent("test('search', () => { ... });")
                .language("javascript")
                .generationMethod("AI_GENERATED")
                .enabled(false)
                .aiGenerationStatus("FAILED")
                .aiRetryCount(1)
                .aiErrorMessage("网络连接失败")
                .aiModelUsed("gpt-4")
                .testCaseId(1L)  // 同一个测试用例
                .category("搜索功能")
                .executionCount(0)
                .build();

        // 保存到数据库
        testScript1 = entityManager.persistAndFlush(testScript1);
        testScript2 = entityManager.persistAndFlush(testScript2);
        testScript3 = entityManager.persistAndFlush(testScript3);

        // 清除缓存
        entityManager.clear();
    }

    @Test
    void testFindAllActive() {
        // 测试查找所有脚本
        List<TestScriptEntity> activeScripts = scriptRepository.findAllActive();

        assertNotNull(activeScripts);
        assertEquals(3, activeScripts.size());
    }

    @Test
    void testFindByScriptName() {
        // 测试根据脚本名称查找
        Optional<TestScriptEntity> found = scriptRepository.findByScriptName("登录测试脚本");

        assertTrue(found.isPresent());
        assertEquals("登录测试脚本", found.get().getScriptName());

        // 不存在的名称
        found = scriptRepository.findByScriptName("不存在的脚本");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByCategory() {
        // 测试按分类查询脚本
        List<TestScriptEntity> authScripts = scriptRepository.findByCategory("用户认证");

        assertNotNull(authScripts);
        assertEquals(2, authScripts.size());

        // 验证结果
        assertTrue(authScripts.stream().allMatch(s -> "用户认证".equals(s.getCategory())));
    }

    @Test
    void testFindEnabledByTestCaseId() {
        // 测试查询测试用例的启用脚本
        Optional<TestScriptEntity> enabled = scriptRepository.findEnabledByTestCaseId(1L);

        assertTrue(enabled.isPresent());
        assertEquals(testScript1.getUniqueId(), enabled.get().getUniqueId());
        assertTrue(enabled.get().getEnabled());

        // 测试没有启用脚本的情况（testCaseId=2的脚本都被禁用）
        Optional<TestScriptEntity> enabled2 = scriptRepository.findEnabledByTestCaseId(2L);
        assertFalse(enabled2.isPresent());
    }

    @Test
    void testFindAllByTestCaseId() {
        // 测试查询测试用例的所有脚本
        List<TestScriptEntity> testCase1Scripts = scriptRepository.findAllByTestCaseId(1L);

        assertNotNull(testCase1Scripts);
        assertEquals(2, testCase1Scripts.size());

        // 验证都是testCaseId=1的脚本
        assertTrue(testCase1Scripts.stream().allMatch(s -> s.getTestCaseId().equals(1L)));

        // testCaseId=2只有一个脚本
        List<TestScriptEntity> testCase2Scripts = scriptRepository.findAllByTestCaseId(2L);
        assertEquals(1, testCase2Scripts.size());
    }

    @Test
    void testDisableAllByTestCaseId() {
        // 测试禁用测试用例的所有脚本
        scriptRepository.disableAllByTestCaseId(1L);
        entityManager.flush();
        entityManager.clear();

        // 验证所有testCaseId=1的脚本都被禁用
        List<TestScriptEntity> testCase1Scripts = scriptRepository.findAllByTestCaseId(1L);
        assertTrue(testCase1Scripts.stream().allMatch(s -> !s.getEnabled()));
    }

    @Test
    void testCountByTestCaseId() {
        // 测试统计测试用例的脚本数量
        Long count1 = scriptRepository.countByTestCaseId(1L);
        assertEquals(2L, count1);

        Long count2 = scriptRepository.countByTestCaseId(2L);
        assertEquals(1L, count2);

        Long count3 = scriptRepository.countByTestCaseId(999L);
        assertEquals(0L, count3);
    }

    @Test
    void testFindByGenerationMethod() {
        // 测试按生成方式查询脚本
        List<TestScriptEntity> aiScripts = scriptRepository.findByGenerationMethod("AI_GENERATED");

        assertNotNull(aiScripts);
        assertEquals(2, aiScripts.size());

        List<TestScriptEntity> excelScripts = scriptRepository.findByGenerationMethod("EXCEL_IMPORT");
        assertEquals(1, excelScripts.size());
    }

    @Test
    void testFindFailedScriptForRetry() {
        // 测试查询可重试的失败脚本
        List<TestScriptEntity> failedScripts = scriptRepository.findFailedScriptForRetry();

        assertNotNull(failedScripts);
        assertEquals(1, failedScripts.size());
        assertEquals(testScript3.getUniqueId(), failedScripts.get(0).getUniqueId());

        // 验证条件：FAILED状态且重试次数<3
        assertTrue(failedScripts.get(0).getAiGenerationStatus().equals("FAILED"));
        assertTrue(failedScripts.get(0).getAiRetryCount() < 3);
    }

    @Test
    void testCountFailedAIGenerations() {
        // 测试统计AI生成失败的脚本数量
        Long count = scriptRepository.countFailedAIGenerations();
        assertEquals(1L, count);
    }

    @Test
    void testSearchByKeyword() {
        // 测试全文搜索
        // 搜索脚本名称
        List<TestScriptEntity> result1 = scriptRepository.searchByKeyword("登录");
        assertEquals(1, result1.size());
        assertEquals("登录测试脚本", result1.get(0).getScriptName());

        // 搜索描述
        List<TestScriptEntity> result2 = scriptRepository.searchByKeyword("用户注册");
        assertEquals(1, result2.size());

        // 搜索内容
        List<TestScriptEntity> result3 = scriptRepository.searchByKeyword("test");
        assertEquals(3, result3.size()); // 所有脚本都包含"test"

        // 大小写不敏感
        List<TestScriptEntity> result4 = scriptRepository.searchByKeyword("LOGIN");
        assertEquals(1, result4.size());
    }

    @Test
    void testFindTopExecutedScripts() {
        // 测试获取最常执行的脚本
        List<TestScriptEntity> topScripts = scriptRepository.findTopExecutedScripts(10);

        assertNotNull(topScripts);
        assertEquals(3, topScripts.size());

        // 验证按执行次数降序排列
        assertEquals(10, topScripts.get(0).getExecutionCount()); // testScript1
        assertEquals(5, topScripts.get(1).getExecutionCount());  // testScript2
        assertEquals(0, topScripts.get(2).getExecutionCount());  // testScript3

        // 测试限制数量
        List<TestScriptEntity> top2Scripts = scriptRepository.findTopExecutedScripts(2);
        assertEquals(2, top2Scripts.size());
    }

    @Test
    void testIncrementExecutionCount() {
        // 测试增加执行次数
        Long scriptId = testScript1.getUniqueId();
        Integer originalCount = testScript1.getExecutionCount();

        scriptRepository.incrementExecutionCount(scriptId);
        entityManager.flush();
        entityManager.clear();

        TestScriptEntity updated = scriptRepository.findById(scriptId).orElseThrow(
                () -> new RuntimeException("脚本不存在")
        );
        assertEquals(originalCount + 1, updated.getExecutionCount());
        assertNotNull(updated.getLastExecutionTime());
    }

    @Test
    void testCountByUserId() {
        // 测试统计用户的脚本数量
        // 设置createdBy
        testScript1.setCreatedBy(1L);
        testScript2.setCreatedBy(1L);
        testScript3.setCreatedBy(2L);
        entityManager.persistAndFlush(testScript1);
        entityManager.persistAndFlush(testScript2);
        entityManager.persistAndFlush(testScript3);
        entityManager.clear();

        Long countUser1 = scriptRepository.countByUserId(1L);
        assertEquals(2L, countUser1);

        Long countUser2 = scriptRepository.countByUserId(2L);
        assertEquals(1L, countUser2);
    }

    @Test
    void testFindByCreatedBy() {
        // 测试按创建人查询脚本
        testScript1.setCreatedBy(1L);
        testScript2.setCreatedBy(1L);
        testScript3.setCreatedBy(2L);
        entityManager.persistAndFlush(testScript1);
        entityManager.persistAndFlush(testScript2);
        entityManager.persistAndFlush(testScript3);
        entityManager.clear();

        List<TestScriptEntity> user1Scripts = scriptRepository.findByCreatedBy(1L);
        assertEquals(2, user1Scripts.size());

        List<TestScriptEntity> user2Scripts = scriptRepository.findByCreatedBy(2L);
        assertEquals(1, user2Scripts.size());
    }

    @Test
    void testFindRecentScripts() {
        // 测试查询最近创建的脚本
        List<TestScriptEntity> recentScripts = scriptRepository.findRecentScripts();

        assertNotNull(recentScripts);
        assertEquals(3, recentScripts.size());
    }

    @Test
    void testBasicCRUD() {
        // 测试基本的增删改查
        TestScriptEntity newScript = TestScriptEntity.builder()
                .scriptName("新脚本")
                .scriptContent("console.log('new');")
                .generationMethod("AI_GENERATED")
                .testCaseId(1L)
                .build();

        // Create
        TestScriptEntity saved = scriptRepository.save(newScript);
        assertNotNull(saved.getUniqueId());

        // Read
        Optional<TestScriptEntity> found = scriptRepository.findById(saved.getUniqueId());
        assertTrue(found.isPresent());

        // Update
        found.get().setScriptName("更新的脚本");
        scriptRepository.save(found.get());
        entityManager.flush();
        entityManager.clear();

        Optional<TestScriptEntity> updated = scriptRepository.findById(saved.getUniqueId());
        assertTrue(updated.isPresent());
        assertEquals("更新的脚本", updated.get().getScriptName());

        // Delete
        scriptRepository.deleteById(saved.getUniqueId());
        entityManager.flush();
        entityManager.clear();

        Optional<TestScriptEntity> deleted = scriptRepository.findById(saved.getUniqueId());
        assertFalse(deleted.isPresent());
    }
}
