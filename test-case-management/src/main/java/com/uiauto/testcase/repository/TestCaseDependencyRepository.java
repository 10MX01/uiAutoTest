package com.uiauto.testcase.repository;

import com.uiauto.testcase.entity.TestCaseDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试用例依赖关系Repository
 */
@Repository
public interface TestCaseDependencyRepository extends JpaRepository<TestCaseDependencyEntity, Long> {

    /**
     * 查找测试用例的所有前置依赖
     */
    List<TestCaseDependencyEntity> findByTestCaseId(Long testCaseId);

    /**
     * 查找测试用例的所有前置依赖ID
     */
    @Query("SELECT d.prerequisiteId FROM TestCaseDependencyEntity d WHERE d.testCaseId = :testCaseId")
    List<Long> findPrerequisiteIdsByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 查找依赖于某个测试用例的所有用例ID
     */
    List<TestCaseDependencyEntity> findByPrerequisiteId(Long prerequisiteId);

    /**
     * 查找依赖于某个测试用例的所有用例ID
     */
    @Query("SELECT d.testCaseId FROM TestCaseDependencyEntity d WHERE d.prerequisiteId = :prerequisiteId")
    List<Long> findDependentIdsByPrerequisiteId(@Param("prerequisiteId") Long prerequisiteId);

    /**
     * 查找特定的依赖关系
     */
    Optional<TestCaseDependencyEntity> findByTestCaseIdAndPrerequisiteId(Long testCaseId, Long prerequisiteId);

    /**
     * 检查依赖关系是否已存在
     */
    boolean existsByTestCaseIdAndPrerequisiteId(Long testCaseId, Long prerequisiteId);

    /**
     * 根据依赖类型查找依赖关系
     */
    List<TestCaseDependencyEntity> findByDependencyType(String dependencyType);

    /**
     * 批量查找测试用例的依赖关系
     */
    @Query("SELECT d FROM TestCaseDependencyEntity d WHERE d.testCaseId IN :testCaseIds")
    List<TestCaseDependencyEntity> findByTestCaseIdIn(@Param("testCaseIds") List<Long> testCaseIds);

    /**
     * 删除测试用例的所有依赖关系
     */
    void deleteByTestCaseId(Long testCaseId);

    /**
     * 删除测试用例的所有前置依赖关系
     */
    void deleteByPrerequisiteId(Long prerequisiteId);
}
