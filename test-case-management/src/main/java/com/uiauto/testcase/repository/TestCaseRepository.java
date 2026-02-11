package com.uiauto.testcase.repository;

import com.uiauto.testcase.entity.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 测试用例Repository
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {

    /**
     * 根据名称查找测试用例
     */
    Optional<TestCaseEntity> findByName(String name);

    /**
     * 根据状态查找测试用例
     */
    @Query("SELECT DISTINCT t FROM TestCaseEntity t " +
           "LEFT JOIN FETCH t.project " +
           "WHERE t.status = :status")
    List<TestCaseEntity> findByStatus(@Param("status") String status);

    /**
     * 根据优先级查找测试用例
     */
    @Query("SELECT DISTINCT t FROM TestCaseEntity t " +
           "LEFT JOIN FETCH t.project " +
           "WHERE t.priority = :priority")
    List<TestCaseEntity> findByPriority(@Param("priority") String priority);

    /**
     * 根据名称或描述搜索测试用例（支持模糊查询）
     */
    @Query("SELECT DISTINCT t FROM TestCaseEntity t " +
           "LEFT JOIN FETCH t.project " +
           "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TestCaseEntity> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据创建人ID查找测试用例
     */
    @Query("SELECT DISTINCT t FROM TestCaseEntity t " +
           "LEFT JOIN FETCH t.project " +
           "WHERE t.createdBy = :createdBy")
    List<TestCaseEntity> findByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 根据创建时间范围查找测试用例
     */
    @Query("SELECT t FROM TestCaseEntity t WHERE t.createdTime BETWEEN :startDate AND :endDate")
    List<TestCaseEntity> findByCreatedTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 统计测试用例数量
     */
    @Query("SELECT COUNT(t) FROM TestCaseEntity t")
    long countActive();

    /**
     * 统计各状态的测试用例数量
     */
    @Query("SELECT t.status, COUNT(t) FROM TestCaseEntity t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 统计指定项目下的测试用例数量
     */
    @Query("SELECT COUNT(t) FROM TestCaseEntity t " +
           "WHERE t.projectId = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询指定项目下的测试用例
     */
    @Query("SELECT DISTINCT t FROM TestCaseEntity t " +
           "LEFT JOIN FETCH t.project " +
           "LEFT JOIN FETCH t.dependencies " +
           "WHERE t.projectId = :projectId")
    List<TestCaseEntity> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用例编号和项目ID查找测试用例（用于前置条件关联）
     */
    Optional<TestCaseEntity> findByCaseNumberAndProjectId(String caseNumber, Long projectId);

    /**
     * 检查用例编号在项目内是否存在
     */
    boolean existsByCaseNumberAndProjectId(String caseNumber, Long projectId);

    /**
     * 查询所有测试用例按项目分组，项目按最后更新时间排序
     * 返回：项目ID, 项目名称, 用例数据
     */
    @Query(value = "SELECT " +
           "t.unique_id, t.case_number, t.name, t.description, t.steps_text, t.steps_json, t.expected_result, " +
           "t.priority, t.status, t.project_id, p.name as project_name, " +
           "t.created_by, t.updated_by, t.created_time, t.updated_time, " +
           "e.executed_by as execution_executed_by, e.created_time as execution_time " +
           "FROM test_cases t " +
           "LEFT JOIN projects p ON t.project_id = p.unique_id " +
           "LEFT JOIN (" +
           "  SELECT test_case_id, executed_by, created_time " +
           "  FROM test_case_executions " +
           "  WHERE (test_case_id, created_time) IN (" +
           "    SELECT test_case_id, MAX(created_time) " +
           "    FROM test_case_executions " +
           "    GROUP BY test_case_id" +
           "  )" +
           ") e ON t.unique_id = e.test_case_id " +
           "ORDER BY t.updated_time DESC, t.project_id, t.created_time DESC",
           nativeQuery = true)
    List<Object[]> findAllGroupedByProject();

    /**
     * 查询所有测试用例及其最新执行记录信息（关联test_case_executions）
     */
    @Query(value = "SELECT " +
           "t.unique_id, t.case_number, t.name, t.description, t.steps_text, t.steps_json, t.expected_result, " +
           "t.priority, t.status, t.created_by, t.updated_by, t.created_time, t.updated_time, " +
           "e.executed_by as execution_executed_by, e.created_time as execution_time " +
           "FROM test_cases t " +
           "LEFT JOIN (" +
           "  SELECT test_case_id, executed_by, created_time " +
           "  FROM test_case_executions " +
           "  WHERE (test_case_id, created_time) IN (" +
           "    SELECT test_case_id, MAX(created_time) " +
           "    FROM test_case_executions " +
           "    GROUP BY test_case_id" +
           "  )" +
           ") e ON t.unique_id = e.test_case_id",
           nativeQuery = true)
    List<Object[]> findAllWithLatestExecution();

    /**
     * 根据状态查询测试用例及其最新执行记录信息
     */
    @Query(value = "SELECT " +
           "t.unique_id, t.case_number, t.name, t.description, t.steps_text, t.steps_json, t.expected_result, " +
           "t.priority, t.status, t.created_by, t.updated_by, t.created_time, t.updated_time, " +
           "e.executed_by as execution_executed_by, e.created_time as execution_time " +
           "FROM test_cases t " +
           "LEFT JOIN (" +
           "  SELECT test_case_id, executed_by, created_time " +
           "  FROM test_case_executions " +
           "  WHERE (test_case_id, created_time) IN (" +
           "    SELECT test_case_id, MAX(created_time) " +
           "    FROM test_case_executions " +
           "    GROUP BY test_case_id" +
           "  )" +
           ") e ON t.unique_id = e.test_case_id " +
           "WHERE t.status = :status",
           nativeQuery = true)
    List<Object[]> findByStatusWithLatestExecution(@Param("status") String status);
}
