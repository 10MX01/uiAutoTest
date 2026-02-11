package com.uiauto.testcase.repository;

import com.uiauto.testcase.entity.TestCaseExecutionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 测试执行记录Repository
 */
@Repository
public interface TestCaseExecutionRepository extends JpaRepository<TestCaseExecutionEntity, Long> {

    /**
     * 分页查询测试记录（带测试用例和项目关联）
     */
    @Query("SELECT new com.uiauto.testcase.vo.TestRecordResponse(" +
           "e.uniqueId, " +
           "e.projectId, " +
           "p.name, " +
           "tc.caseNumber, " +
           "tc.name, " +
           "e.executionUrl, " +
           "e.status, " +
           "e.duration, " +
           "e.executedBy, " +
           "CONCAT('用户', e.executedBy), " +
           "e.createdTime) " +
           "FROM TestCaseExecutionEntity e " +
           "LEFT JOIN TestCaseEntity tc ON e.testCaseId = tc.uniqueId " +
           "LEFT JOIN ProjectEntity p ON e.projectId = p.uniqueId " +
           "WHERE " +
           "(:projectId IS NULL OR e.projectId = :projectId) AND " +
           "(:testCaseId IS NULL OR e.testCaseId = :testCaseId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:executorId IS NULL OR e.executedBy = :executorId) AND " +
           "(:startTime IS NULL OR e.createdTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.createdTime <= :endTime) AND " +
           "(:search IS NULL OR :search = '' OR tc.name LIKE :searchPattern OR tc.caseNumber LIKE :searchPattern)")
    Page<com.uiauto.testcase.vo.TestRecordResponse> findByConditions(
            @Param("projectId") Long projectId,
            @Param("testCaseId") Long testCaseId,
            @Param("status") String status,
            @Param("executorId") Long executorId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("search") String search,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

    /**
     * 统计各状态的记录数
     */
    @Query("SELECT new com.uiauto.testcase.vo.TestRecordStatistics(" +
           "COUNT(e), " +
           "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN e.status = 'FAILED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN e.status = 'SKIPPED' THEN 1 ELSE 0 END)) " +
           "FROM TestCaseExecutionEntity e")
    com.uiauto.testcase.vo.TestRecordStatistics getStatistics();

    /**
     * 根据测试用例ID查询执行记录（按创建时间倒序）
     */
    List<TestCaseExecutionEntity> findByTestCaseIdOrderByCreatedTimeDesc(Long testCaseId);
}
