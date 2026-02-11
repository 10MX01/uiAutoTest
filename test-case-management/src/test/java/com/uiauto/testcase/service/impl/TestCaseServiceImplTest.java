package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.dto.TestCaseUpdateRequest;
import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.vo.TestCaseResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestCaseService集成测试（使用H2内存数据库）
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class TestCaseServiceImplTest {

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private TestCaseCreateRequest createRequest;
    private TestCaseUpdateRequest updateRequest;
    private static Long createdId;

    @BeforeEach
    void setUp() {
        // 清理数据库
        testCaseRepository.deleteAll();

        // 准备测试数据
        createRequest = TestCaseCreateRequest.builder()
                .name("测试用例名称")
                .description("测试用例描述")
                .stepsText("1. 打开首页\n2. 点击搜索")
                .stepsJson("{\"action\":\"navigate\"}")
                .expectedResult("操作成功")
                .priority("P1")
                .status("ACTIVE")
                .build();

        updateRequest = TestCaseUpdateRequest.builder()
                .name("更新后的测试用例")
                .description("更新后的描述")
                .stepsText("1. 打开首页\n2. 点击搜索\n3. 验证结果")
                .expectedResult("验证通过")
                .priority("P0")
                .status("ACTIVE")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("应该成功创建测试用例")
    void shouldCreateTestCase() {
        // When
        Long id = testCaseService.create(createRequest);

        // Then
        assertNotNull(id);
        assertTrue(id > 0);

        // 验证数据库中存在该记录
        TestCaseEntity entity = testCaseRepository.findById(id).orElse(null);
        assertNotNull(entity);
        assertEquals("测试用例名称", entity.getName());
        assertEquals("P1", entity.getPriority());

        createdId = id;
    }

    @Test
    @Order(2)
    @DisplayName("应该成功更新测试用例")
    void shouldUpdateTestCase() {
        // Given - 先创建一个测试用例
        Long uniqueId = testCaseService.create(createRequest);

        // When
        testCaseService.update(uniqueId, updateRequest);

        // Then
        TestCaseEntity entity = testCaseRepository.findById(uniqueId).orElse(null);
        assertNotNull(entity);
        assertEquals("更新后的测试用例", entity.getName());
    }

    @Test
    @Order(3)
    @DisplayName("更新不存在的测试用例应该抛出异常")
    void shouldThrowExceptionWhenUpdatingNonExistentTestCase() {
        // Given
        Long uniqueId = 99999L;

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> testCaseService.update(uniqueId, updateRequest));

        assertTrue(exception.getMessage().contains("测试用例不存在"));
    }

    @Test
    @Order(4)
    @DisplayName("应该成功删除测试用例")
    void shouldDeleteTestCase() {
        // Given
        Long uniqueId = testCaseService.create(createRequest);

        // When
        testCaseService.delete(uniqueId);

        // Then - 物理删除，应该不存在
        TestCaseEntity entity = testCaseRepository.findById(uniqueId).orElse(null);
        assertNull(entity);
    }

    @Test
    @Order(5)
    @DisplayName("删除不存在的测试用例应该抛出异常")
    void shouldThrowExceptionWhenDeletingNonExistentTestCase() {
        // Given
        Long uniqueId = 99999L;

        // When & Then
        assertThrows(RuntimeException.class, () -> testCaseService.delete(uniqueId));
    }

    @Test
    @Order(6)
    @DisplayName("应该成功查询测试用例详情")
    void shouldGetTestCaseById() {
        // Given
        Long uniqueId = testCaseService.create(createRequest);

        // When
        TestCaseResponse response = testCaseService.getById(uniqueId);

        // Then
        assertNotNull(response);
        assertEquals(uniqueId, response.getUniqueId());
        assertEquals("测试用例名称", response.getName());
    }

    @Test
    @Order(7)
    @DisplayName("查询不存在的测试用例应该抛出异常")
    void shouldThrowExceptionWhenGettingNonExistentTestCase() {
        // Given
        Long uniqueId = 99999L;

        // When & Then
        assertThrows(RuntimeException.class, () -> testCaseService.getById(uniqueId));
    }

    @Test
    @Order(8)
    @DisplayName("应该成功查询所有测试用例")
    void shouldListAllTestCases() {
        // Given
        testCaseService.create(createRequest);
        TestCaseCreateRequest request2 = TestCaseCreateRequest.builder()
                .name("测试用例2")
                .stepsText("步骤2")
                .expectedResult("结果2")
                .build();
        testCaseService.create(request2);

        // When
        List<TestCaseResponse> responses = testCaseService.listAll();

        // Then
        assertNotNull(responses);
        assertTrue(responses.size() >= 2);
    }

    @Test
    @Order(9)
    @DisplayName("应该成功按状态查询测试用例")
    void shouldListTestCasesByStatus() {
        // Given
        TestCaseCreateRequest draftRequest = TestCaseCreateRequest.builder()
                .name("草稿用例")
                .stepsText("步骤")
                .expectedResult("结果")
                .status("DRAFT")
                .build();
        testCaseService.create(draftRequest);

        // When
        List<TestCaseResponse> responses = testCaseService.listByStatus("DRAFT");

        // Then
        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
        assertTrue(responses.stream().anyMatch(r -> "DRAFT".equals(r.getStatus())));
    }

    @Test
    @Order(10)
    @DisplayName("应该成功按优先级查询测试用例")
    void shouldListTestCasesByPriority() {
        // Given
        TestCaseCreateRequest p0Request = TestCaseCreateRequest.builder()
                .name("P0用例")
                .stepsText("步骤")
                .expectedResult("结果")
                .priority("P0")
                .build();
        testCaseService.create(p0Request);

        // When
        List<TestCaseResponse> responses = testCaseService.listByPriority("P0");

        // Then
        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
        assertTrue(responses.stream().anyMatch(r -> "P0".equals(r.getPriority())));
    }

    @Test
    @Order(11)
    @DisplayName("应该成功按创建人查询测试用例")
    void shouldListTestCasesByCreator() {
        // Given
        Long createdBy = 1L;
        // 创建一个测试用例，默认createdBy是1L
        testCaseService.create(createRequest);

        // When
        List<TestCaseResponse> responses = testCaseService.listByCreator(createdBy);

        // Then
        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
    }

    @Test
    @Order(12)
    @DisplayName("应该成功搜索测试用例")
    void shouldSearchTestCases() {
        // Given
        TestCaseCreateRequest searchRequest = TestCaseCreateRequest.builder()
                .name("用户登录测试")
                .stepsText("1. 打开登录页面\n2. 输入用户名密码\n3. 点击登录")
                .expectedResult("登录成功")
                .build();
        testCaseService.create(searchRequest);

        // When
        List<TestCaseResponse> responses = testCaseService.search("登录");

        // Then
        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
        assertTrue(responses.stream().anyMatch(r -> r.getName().contains("登录") ||
                                               (r.getDescription() != null && r.getDescription().contains("登录"))));
    }
}
