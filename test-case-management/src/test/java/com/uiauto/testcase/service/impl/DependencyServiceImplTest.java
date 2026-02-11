package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.entity.TestCaseDependencyEntity;
import com.uiauto.testcase.repository.TestCaseDependencyRepository;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testcase.service.DependencyService;
import com.uiauto.testcase.vo.TestCaseDependencyResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DependencyService集成测试（使用H2内存数据库）
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class DependencyServiceImplTest {

    @Autowired
    private DependencyService dependencyService;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestCaseDependencyRepository dependencyRepository;

    private static Long testCase1Id;
    private static Long testCase2Id;
    private static Long testCase3Id;

    @BeforeEach
    void setUp() {
        // 清理数据库
        dependencyRepository.deleteAll();
        testCaseRepository.deleteAll();

        // 创建测试用例
        TestCaseEntity testCase1 = TestCaseEntity.builder()
                .name("测试用例1")
                .description("第一个测试用例")
                .stepsText("步骤1")
                .status("ACTIVE")
                .priority("P1")
                .build();
        testCase1.setCreatedBy(1L);
        testCase1.setUpdatedBy(1L);
        testCase1 = testCaseRepository.save(testCase1);
        testCase1Id = testCase1.getUniqueId();

        TestCaseEntity testCase2 = TestCaseEntity.builder()
                .name("测试用例2")
                .description("第二个测试用例")
                .stepsText("步骤2")
                .status("ACTIVE")
                .priority("P1")
                .build();
        testCase2.setCreatedBy(1L);
        testCase2.setUpdatedBy(1L);
        testCase2 = testCaseRepository.save(testCase2);
        testCase2Id = testCase2.getUniqueId();

        TestCaseEntity testCase3 = TestCaseEntity.builder()
                .name("测试用例3")
                .description("第三个测试用例")
                .stepsText("步骤3")
                .status("ACTIVE")
                .priority("P1")
                .build();
        testCase3.setCreatedBy(1L);
        testCase3.setUpdatedBy(1L);
        testCase3 = testCaseRepository.save(testCase3);
        testCase3Id = testCase3.getUniqueId();
    }

    @Test
    @Order(1)
    @DisplayName("应该成功添加依赖关系")
    void shouldAddDependency() {
        // When - 添加依赖关系：用例2依赖于用例1
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");

        // Then
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByTestCaseId(testCase2Id);
        assertEquals(1, dependencies.size());
        assertEquals(testCase1Id, dependencies.get(0).getPrerequisiteId());
        assertEquals(testCase2Id, dependencies.get(0).getTestCaseId());
        assertEquals("HARD", dependencies.get(0).getDependencyType());
    }

    @Test
    @Order(2)
    @DisplayName("应该成功添加多个前置依赖")
    void shouldAddMultiplePrerequisites() {
        // When - 添加多个前置依赖
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase1Id, testCase2Id), "SOFT");

        // Then
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByTestCaseId(testCase3Id);
        assertEquals(2, dependencies.size());
    }

    @Test
    @Order(3)
    @DisplayName("添加自依赖应该抛出异常")
    void shouldThrowExceptionWhenAddSelfDependency() {
        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(testCase1Id, Arrays.asList(testCase1Id), "HARD");
        });

        assertTrue(exception.getMessage().contains("不能添加对自己依赖"));
    }

    @Test
    @Order(4)
    @DisplayName("添加重复依赖应该抛出异常")
    void shouldThrowExceptionWhenAddDuplicateDependency() {
        // Given
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "SOFT");
        });

        assertTrue(exception.getMessage().contains("依赖关系已存在"));
    }

    @Test
    @Order(5)
    @DisplayName("检测循环依赖应该抛出异常")
    void shouldThrowExceptionWhenCircularDependencyDetected() {
        // Given - 创建依赖链：用例2 -> 用例1
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");

        // When & Then - 尝试创建：用例1 -> 用例2，这会形成循环
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(testCase1Id, Arrays.asList(testCase2Id), "HARD");
        });

        assertTrue(exception.getMessage().contains("检测到循环依赖"));
    }

    @Test
    @Order(6)
    @DisplayName("检测复杂的循环依赖应该抛出异常")
    void shouldThrowExceptionWhenComplexCircularDependencyDetected() {
        // Given - 创建依赖链：用例3 -> 用例2 -> 用例1
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase2Id), "HARD");

        // When & Then - 尝试创建：用例1 -> 用例3，这会形成循环
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(testCase1Id, Arrays.asList(testCase3Id), "HARD");
        });

        assertTrue(exception.getMessage().contains("检测到循环依赖"));
    }

    @Test
    @Order(7)
    @DisplayName("应该成功移除依赖关系")
    void shouldRemoveDependency() {
        // Given
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");

        // When
        dependencyService.removeDependency(testCase2Id, Arrays.asList(testCase1Id));

        // Then
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByTestCaseId(testCase2Id);
        assertEquals(0, dependencies.size());
    }

    @Test
    @Order(8)
    @DisplayName("应该成功移除多个依赖关系")
    void shouldRemoveMultipleDependencies() {
        // Given
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase1Id, testCase2Id), "HARD");

        // When
        dependencyService.removeDependency(testCase3Id, Arrays.asList(testCase1Id, testCase2Id));

        // Then
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByTestCaseId(testCase3Id);
        assertEquals(0, dependencies.size());
    }

    @Test
    @Order(9)
    @DisplayName("应该成功查询前置依赖")
    void shouldGetPrerequisites() {
        // Given
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");

        // When
        List<TestCaseDependencyResponse> prerequisites = dependencyService.getPrerequisites(testCase2Id);

        // Then
        assertEquals(1, prerequisites.size());
        assertEquals(testCase2Id, prerequisites.get(0).getTestCaseId());
        assertEquals(testCase1Id, prerequisites.get(0).getPrerequisiteId());
        assertEquals("HARD", prerequisites.get(0).getDependencyType());
        assertEquals("测试用例1", prerequisites.get(0).getPrerequisiteName());
    }

    @Test
    @DisplayName("应该成功查询后续依赖")
    void shouldGetDependents() {
        // Given
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase1Id), "SOFT");

        // When
        List<TestCaseDependencyResponse> dependents = dependencyService.getDependents(testCase1Id);

        // Then
        assertEquals(2, dependents.size());
        assertTrue(dependents.stream().anyMatch(d -> d.getTestCaseId().equals(testCase2Id)));
        assertTrue(dependents.stream().anyMatch(d -> d.getTestCaseId().equals(testCase3Id)));
    }

    @Test
    @DisplayName("应该成功计算执行顺序（无依赖）")
    void shouldCalculateExecutionOrderWithoutDependencies() {
        // Given
        List<Long> testCaseIds = Arrays.asList(testCase1Id, testCase2Id, testCase3Id);

        // When
        List<Long> order = dependencyService.calculateExecutionOrder(testCaseIds);

        // Then
        assertEquals(3, order.size());
        assertTrue(order.containsAll(testCaseIds));
    }

    @Test
    @DisplayName("应该成功计算执行顺序（有简单依赖）")
    void shouldCalculateExecutionOrderWithSimpleDependencies() {
        // Given - 创建依赖：用例2 -> 用例1
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");
        List<Long> testCaseIds = Arrays.asList(testCase1Id, testCase2Id);

        // When
        List<Long> order = dependencyService.calculateExecutionOrder(testCaseIds);

        // Then - 用例1应该在用例2之前
        assertEquals(2, order.size());
        assertEquals(testCase1Id, order.get(0));
        assertEquals(testCase2Id, order.get(1));
    }

    @Test
    @DisplayName("应该成功计算执行顺序（有复杂依赖）")
    void shouldCalculateExecutionOrderWithComplexDependencies() {
        // Given - 创建依赖链：用例3 -> 用例2 -> 用例1
        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase2Id), "HARD");
        List<Long> testCaseIds = Arrays.asList(testCase1Id, testCase2Id, testCase3Id);

        // When
        List<Long> order = dependencyService.calculateExecutionOrder(testCaseIds);

        // Then - 执行顺序应该是：用例1 -> 用例2 -> 用例3
        assertEquals(3, order.size());
        assertEquals(testCase1Id, order.get(0));
        assertEquals(testCase2Id, order.get(1));
        assertEquals(testCase3Id, order.get(2));
    }

    @Test
    @DisplayName("应该成功计算执行顺序（钻石型依赖）")
    void shouldCalculateExecutionOrderWithDiamondDependencies() {
        // Given - 创建钻石型依赖：
        //       用例1
        //       /    \
        //   用例2    用例3
        //       \    /
        //       用例4
        TestCaseEntity testCase4 = TestCaseEntity.builder()
                .name("测试用例4")
                .description("第四个测试用例")
                .stepsText("步骤4")
                .status("ACTIVE")
                .priority("P1")
                .build();
        testCase4.setCreatedBy(1L);
        testCase4.setUpdatedBy(1L);
        testCase4 = testCaseRepository.save(testCase4);
        Long testCase4Id = testCase4.getUniqueId();

        dependencyService.addDependency(testCase2Id, Arrays.asList(testCase1Id), "HARD");
        dependencyService.addDependency(testCase3Id, Arrays.asList(testCase1Id), "HARD");
        dependencyService.addDependency(testCase4Id, Arrays.asList(testCase2Id, testCase3Id), "HARD");

        List<Long> testCaseIds = Arrays.asList(testCase1Id, testCase2Id, testCase3Id, testCase4Id);

        // When
        List<Long> order = dependencyService.calculateExecutionOrder(testCaseIds);

        // Then - 用例1应该最先，用例4应该最后
        assertEquals(4, order.size());
        assertEquals(testCase1Id, order.get(0));
        assertEquals(testCase4Id, order.get(3));
    }

    @Test
    @DisplayName("查询不存在的测试用例的前置依赖应该抛出异常")
    void shouldThrowExceptionWhenGetPrerequisitesForNonExistentTestCase() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.getPrerequisites(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("测试用例不存在"));
    }

    @Test
    @DisplayName("添加不存在的测试用例作为前置依赖应该抛出异常")
    void shouldThrowExceptionWhenAddNonExistentPrerequisite() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(testCase1Id, Arrays.asList(nonExistentId), "HARD");
        });

        assertTrue(exception.getMessage().contains("部分前置用例不存在"));
    }

    @Test
    @DisplayName("对不存在的测试用例添加依赖应该抛出异常")
    void shouldThrowExceptionWhenAddDependencyForNonExistentTestCase() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(nonExistentId, Arrays.asList(testCase1Id), "HARD");
        });

        assertTrue(exception.getMessage().contains("测试用例不存在"));
    }
}
