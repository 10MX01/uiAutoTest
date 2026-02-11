package com.uiauto.testcase.repository;

import com.uiauto.testcase.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目Repository
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    /**
     * 根据名称查找项目
     */
    Optional<ProjectEntity> findByName(String name);

    /**
     * 根据代码查找项目
     */
    Optional<ProjectEntity> findByCode(String code);

    /**
     * 根据名称或代码搜索项目（支持模糊查询）
     */
    @Query("SELECT p FROM ProjectEntity p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProjectEntity> searchByKeyword(@Param("keyword") String keyword);
}
