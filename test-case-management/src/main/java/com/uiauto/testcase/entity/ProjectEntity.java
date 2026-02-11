package com.uiauto.testcase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uiauto.common.BaseEntity;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 项目实体类
 * 对应数据库表：projects
 */
@Entity
@Table(name = "projects")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEntity extends BaseEntity {

    /**
     * 项目名称
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 项目描述
     */
    @Lob
    @Column(name = "description")
    private String description;

    /**
     * 项目代码（唯一）
     */
    @Column(name = "code", length = 50, unique = true)
    private String code;

    /**
     * 目标URL（测试环境地址）
     */
    @Column(name = "target_url", length = 500)
    private String targetUrl;

    /**
     * 基础URL（可选）
     */
    @Column(name = "base_url", length = 500)
    private String baseUrl;

    /**
     * 关联的测试用例（一对多）
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<TestCaseEntity> testCases = new HashSet<>();
}
