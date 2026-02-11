package com.uiauto.testcase.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.uiauto.common.BaseEntity;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试用例实体类
 * 对应数据库表：test_cases
 */
@Entity
@Table(name = "test_cases")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseEntity extends BaseEntity {

    /**
     * 用例编号
     */
    @Column(name = "case_number", length = 50)
    private String caseNumber;

    /**
     * 测试用例名称
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 测试用例描述
     */
    @Lob
    @Column(name = "description")
    private String description;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 关联的项目实体（多对一）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private ProjectEntity project;

    /**
     * 测试步骤（自然语言描述）
     */
    @Lob
    @Column(name = "steps_text", nullable = false)
    private String stepsText;

    /**
     * 测试步骤（AI结构化后的JSON，不含选择器）
     */
    @Column(name = "steps_json", columnDefinition = "JSON")
    private String stepsJson;

    /**
     * 是否由AI生成
     */
    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    /**
     * 预期结果（自然语言）
     */
    @Lob
    @Column(name = "expected_result")
    private String expectedResult;

    /**
     * 优先级：P0/P1/P2/P3
     */
    @Column(name = "priority", length = 10)
    @Builder.Default
    private String priority = "P2";

    /**
     * 状态：NOT_EXECUTED/PASSED/FAILED
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "NOT_EXECUTED";


    /**
     * 自动化状态：MANUAL/AUTOMATED/PARTIAL
     */
    @Column(name = "automation_status", length = 20)
    @Builder.Default
    private String automationStatus = "MANUAL";

    /**
     * 前置依赖关系（一对多）
     */
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<TestCaseDependencyEntity> dependencies = new HashSet<>();
}
