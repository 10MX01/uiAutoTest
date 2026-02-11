package com.uiauto.testScript.repository;

import com.uiauto.testScript.entity.TestScriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试脚本Repository（重构版）
 *
 * 核心变更：
 * 1. 移除版本相关查询
 * 2. 移除标签关联查询
 * 3. 移除软删除功能
 * 4. 添加启用状态查询
 * 5. 添加AI生成相关查询
 * 6. 添加测试用例关联查询
 */
@Repository
public interface TestScriptRepository extends JpaRepository<TestScriptEntity, Long> {

    // ==================== 基础查询 ====================

    /**
     * 查找所有脚本
     */
    @Query("SELECT DISTINCT t FROM TestScriptEntity t " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findAllActive();

    /**
     * 根据脚本名称查找脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.scriptName = :scriptName")
    Optional<TestScriptEntity> findByScriptName(@Param("scriptName") String scriptName);

    /**
     * 按分类查询脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.category = :category " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findByCategory(@Param("category") String category);

    // ==================== 启用状态和测试用例关联 ====================

    /**
     * 查询测试用例的启用脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.testCaseId = :testCaseId AND t.enabled = TRUE")
    Optional<TestScriptEntity> findEnabledByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 查询测试用例的所有脚本（包括已禁用）
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.testCaseId = :testCaseId " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findAllByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 禁用测试用例的所有脚本
     */
    @Modifying
    @Query("UPDATE TestScriptEntity t SET t.enabled = FALSE " +
           "WHERE t.testCaseId = :testCaseId")
    void disableAllByTestCaseId(@Param("testCaseId") Long testCaseId);

    /**
     * 统计测试用例的脚本数量
     */
    @Query("SELECT COUNT(t) FROM TestScriptEntity t " +
           "WHERE t.testCaseId = :testCaseId")
    Long countByTestCaseId(@Param("testCaseId") Long testCaseId);

    // ==================== 生成方式查询 ====================

    /**
     * 按生成方式查询脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.generationMethod = :generationMethod " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findByGenerationMethod(@Param("generationMethod") String generationMethod);

    // ==================== AI生成相关 ====================

    /**
     * 查询AI生成失败的脚本（可重试）
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.aiGenerationStatus = 'FAILED' " +
           "AND t.aiRetryCount < 3 " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findFailedScriptForRetry();

    /**
     * 统计AI生成失败的脚本数量
     */
    @Query("SELECT COUNT(t) FROM TestScriptEntity t " +
           "WHERE t.aiGenerationStatus = 'FAILED' " +
           "AND t.aiRetryCount < 3")
    Long countFailedAIGenerations();

    // ==================== 搜索功能 ====================

    /**
     * 全文搜索脚本（名称、描述、内容）
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE " +
           "LOWER(t.scriptName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.scriptDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.scriptContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> searchByKeyword(@Param("keyword") String keyword);

    // ==================== 执行统计 ====================

    /**
     * 获取最常执行的脚本
     */
    @Query(value = "SELECT * FROM test_scripts " +
           "ORDER BY execution_count DESC " +
           "LIMIT ?1", nativeQuery = true)
    List<TestScriptEntity> findTopExecutedScripts(int limit);

    /**
     * 增加脚本执行次数
     */
    @Modifying
    @Query("UPDATE TestScriptEntity t SET " +
           "t.executionCount = t.executionCount + 1, " +
           "t.lastExecutionTime = CURRENT_TIMESTAMP " +
           "WHERE t.uniqueId = :scriptId")
    void incrementExecutionCount(@Param("scriptId") Long scriptId);

    /**
     * 统计用户的脚本数量
     */
    @Query("SELECT COUNT(t) FROM TestScriptEntity t " +
           "WHERE t.createdBy = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 按创建人查询脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "WHERE t.createdBy = :userId " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findByCreatedBy(@Param("userId") Long userId);

    /**
     * 查询最近创建的脚本
     */
    @Query("SELECT t FROM TestScriptEntity t " +
           "ORDER BY t.createdTime DESC")
    List<TestScriptEntity> findRecentScripts();
}
