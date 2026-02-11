package com.uiauto.testcase.service.impl;

import com.uiauto.testcase.entity.TestCaseDependencyEntity;
import com.uiauto.testcase.entity.TestCaseEntity;
import com.uiauto.testcase.repository.TestCaseDependencyRepository;
import com.uiauto.testcase.repository.TestCaseRepository;
import com.uiauto.testcase.service.DependencyService;
import com.uiauto.testcase.vo.TestCaseDependencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试用例依赖关系Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DependencyServiceImpl implements DependencyService {

    private final TestCaseDependencyRepository dependencyRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    @Transactional
    public void addDependency(Long testCaseId, List<Long> prerequisiteIds, String dependencyType) {
        log.info("添加依赖关系: testCaseId={}, prerequisiteIds={}, type={}",
                testCaseId, prerequisiteIds, dependencyType);

        // 1. 验证测试用例存在
        TestCaseEntity testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("测试用例不存在: " + testCaseId));

        // 2. 验证前置用例存在
        List<TestCaseEntity> prerequisites = testCaseRepository.findAllById(prerequisiteIds);
        if (prerequisites.size() != prerequisiteIds.size()) {
            throw new RuntimeException("部分前置用例不存在");
        }

        // 3. 验证不自依赖
        if (prerequisiteIds.contains(testCaseId)) {
            throw new RuntimeException("不能添加对自己依赖");
        }

        // 4. 验证不重复
        for (Long prereqId : prerequisiteIds) {
            Optional<TestCaseDependencyEntity> existing = dependencyRepository
                    .findByTestCaseIdAndPrerequisiteId(testCaseId, prereqId);
            if (existing.isPresent()) {
                throw new RuntimeException("依赖关系已存在: " + testCaseId + " -> " + prereqId);
            }
        }

        // 5. 检测循环依赖（DFS算法）
        detectCircularDependency(testCaseId, prerequisiteIds);

        // 6. 保存依赖关系
        for (Long prereqId : prerequisiteIds) {
            TestCaseDependencyEntity dependency = TestCaseDependencyEntity.builder()
                    .testCaseId(testCaseId)
                    .prerequisiteId(prereqId)
                    .dependencyType(dependencyType)
                    .build();

            dependency.setCreatedBy(1L); // TODO: 从当前登录用户获取
            dependency.setUpdatedBy(1L);

            dependencyRepository.save(dependency);
        }

        log.info("依赖关系添加成功");
    }

    @Override
    @Transactional
    public void removeDependency(Long testCaseId, List<Long> prerequisiteIds) {
        log.info("移除依赖关系: testCaseId={}, prerequisiteIds={}", testCaseId, prerequisiteIds);

        // 1. 验证测试用例存在
        if (!testCaseRepository.existsById(testCaseId)) {
            throw new RuntimeException("测试用例不存在: " + testCaseId);
        }

        // 2. 删除依赖关系
        int deletedCount = 0;
        for (Long prereqId : prerequisiteIds) {
            Optional<TestCaseDependencyEntity> dependency = dependencyRepository
                    .findByTestCaseIdAndPrerequisiteId(testCaseId, prereqId);

            if (dependency.isPresent()) {
                dependencyRepository.delete(dependency.get());
                deletedCount++;
            }
        }

        log.info("成功移除 {} 条依赖关系", deletedCount);
    }

    @Override
    public List<Long> calculateExecutionOrder(List<Long> testCaseIds) {
        log.info("计算执行顺序: testCaseIds={}", testCaseIds);

        // 1. 构建图的邻接表和入度表
        Map<Long, Set<Long>> graph = new HashMap<>(); // 邻接表
        Map<Long, Integer> inDegree = new HashMap<>(); // 入度表

        // 初始化
        for (Long id : testCaseIds) {
            graph.put(id, new HashSet<>());
            inDegree.put(id, 0);
        }

        // 2. 构建图（只考虑指定用例之间的依赖关系）
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findAll();

        for (TestCaseDependencyEntity dep : dependencies) {
            Long from = dep.getPrerequisiteId();
            Long to = dep.getTestCaseId();

            // 只考虑指定用例集合中的依赖
            if (testCaseIds.contains(from) && testCaseIds.contains(to)) {
                graph.get(from).add(to);
                inDegree.put(to, inDegree.get(to) + 1);
            }
        }

        // 3. Kahn算法进行拓扑排序
        List<Long> result = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>();

        // 将入度为0的节点加入队列
        for (Long id : testCaseIds) {
            if (inDegree.get(id) == 0) {
                queue.offer(id);
            }
        }

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            result.add(current);

            // 减少邻居节点的入度
            for (Long neighbor : graph.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 4. 检查是否有环
        if (result.size() != testCaseIds.size()) {
            throw new RuntimeException("检测到循环依赖，无法计算执行顺序");
        }

        log.info("执行顺序计算完成: {}", result);
        return result;
    }

    @Override
    public List<TestCaseDependencyResponse> getPrerequisites(Long testCaseId) {
        log.info("查询前置依赖: testCaseId={}", testCaseId);

        // 验证测试用例存在
        if (!testCaseRepository.existsById(testCaseId)) {
            throw new RuntimeException("测试用例不存在: " + testCaseId);
        }

        // 查询前置依赖
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByTestCaseId(testCaseId);

        return dependencies.stream()
                .map(dep -> {
                    Optional<TestCaseEntity> prereq = testCaseRepository.findById(dep.getPrerequisiteId());
                    return TestCaseDependencyResponse.builder()
                            .uniqueId(dep.getUniqueId())
                            .testCaseId(dep.getTestCaseId())
                            .prerequisiteId(dep.getPrerequisiteId())
                            .prerequisiteName(prereq.map(TestCaseEntity::getName).orElse("Unknown"))
                            .dependencyType(dep.getDependencyType())
                            .createdBy(dep.getCreatedBy())
                            .updatedBy(dep.getUpdatedBy())
                            .createdTime(dep.getCreatedTime())
                            .updatedTime(dep.getUpdatedTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCaseDependencyResponse> getDependents(Long testCaseId) {
        log.info("查询后续依赖: testCaseId={}", testCaseId);

        // 验证测试用例存在
        if (!testCaseRepository.existsById(testCaseId)) {
            throw new RuntimeException("测试用例不存在: " + testCaseId);
        }

        // 查询依赖当前用例的其他用例
        List<TestCaseDependencyEntity> dependencies = dependencyRepository.findByPrerequisiteId(testCaseId);

        return dependencies.stream()
                .map(dep -> {
                    Optional<TestCaseEntity> dependent = testCaseRepository.findById(dep.getTestCaseId());
                    return TestCaseDependencyResponse.builder()
                            .uniqueId(dep.getUniqueId())
                            .testCaseId(dep.getTestCaseId())
                            .testCaseName(dependent.map(TestCaseEntity::getName).orElse("Unknown"))
                            .prerequisiteId(dep.getPrerequisiteId())
                            .dependencyType(dep.getDependencyType())
                            .createdBy(dep.getCreatedBy())
                            .updatedBy(dep.getUpdatedBy())
                            .createdTime(dep.getCreatedTime())
                            .updatedTime(dep.getUpdatedTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 检测循环依赖（DFS算法）
     *
     * @param testCaseId      要添加依赖的用例ID
     * @param prerequisiteIds 要添加的前置用例ID列表
     */
    private void detectCircularDependency(Long testCaseId, List<Long> prerequisiteIds) {
        // 获取所有现有的依赖关系
        List<TestCaseDependencyEntity> allDependencies = dependencyRepository.findAll();

        // 构建图
        Map<Long, Set<Long>> graph = buildGraph(allDependencies);

        // 临时添加新的依赖边
        for (Long prereqId : prerequisiteIds) {
            graph.computeIfAbsent(prereqId, k -> new HashSet<>()).add(testCaseId);
        }

        // 对每个前置用例进行DFS检测
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Long prereqId : prerequisiteIds) {
            if (hasCycleDFS(prereqId, graph, visited, recursionStack)) {
                throw new RuntimeException("检测到循环依赖: 添加依赖后 " + testCaseId + " 和 " + prereqId + " 形成环");
            }
        }
    }

    /**
     * 使用DFS检测图中是否有环
     *
     * @param node          当前节点
     * @param graph         图的邻接表
     * @param visited       已访问节点集合
     * @param recursionStack 递归栈中的节点集合
     * @return 是否有环
     */
    private boolean hasCycleDFS(Long node, Map<Long, Set<Long>> graph,
                                 Set<Long> visited, Set<Long> recursionStack) {
        // 如果节点在递归栈中，说明有环
        if (recursionStack.contains(node)) {
            return true;
        }

        // 如果节点已访问过，跳过
        if (visited.contains(node)) {
            return false;
        }

        // 标记为已访问，加入递归栈
        visited.add(node);
        recursionStack.add(node);

        // 递归检查所有邻居
        Set<Long> neighbors = graph.getOrDefault(node, new HashSet<>());
        for (Long neighbor : neighbors) {
            if (hasCycleDFS(neighbor, graph, visited, recursionStack)) {
                return true;
            }
        }

        // 回溯，从递归栈中移除
        recursionStack.remove(node);

        return false;
    }

    /**
     * 从依赖关系列表构建图的邻接表
     *
     * @param dependencies 依赖关系列表
     * @return 图的邻接表
     */
    private Map<Long, Set<Long>> buildGraph(List<TestCaseDependencyEntity> dependencies) {
        Map<Long, Set<Long>> graph = new HashMap<>();

        for (TestCaseDependencyEntity dep : dependencies) {
            Long from = dep.getPrerequisiteId();
            Long to = dep.getTestCaseId();

            graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
            graph.computeIfAbsent(to, k -> new HashSet<>()); // 确保所有节点都在图中
        }

        return graph;
    }
}
