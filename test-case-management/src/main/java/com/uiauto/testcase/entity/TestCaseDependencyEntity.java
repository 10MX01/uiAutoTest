package com.uiauto.testcase.entity;

import com.uiauto.common.BaseEntity;
import javax.persistence.*;

import lombok.*;

/**
 * 测试用例依赖关系实体类
 * 对应数据库表：test_case_dependencies
 */
@Entity
@Table(name = "test_case_dependencies")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"testCase", "prerequisite"})
@ToString(callSuper = true, exclude = {"testCase", "prerequisite"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseDependencyEntity extends BaseEntity {

    /**
     * 当前测试用例ID（后置用例）
     */
    @Column(name = "test_case_id")
    private Long testCaseId;

    /**
     * 前置测试用例ID
     */
    @Column(name = "prerequisite_id")
    private Long prerequisiteId;

    /**
     * 依赖类型：HARD-强依赖（必须成功）/SOFT-弱依赖（可选执行）
     */
    @Column(name = "dependency_type", length = 20)
    @Builder.Default
    private String dependencyType = "HARD";

    /**
     * 当前测试用例（后置用例）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private TestCaseEntity testCase;

    /**
     * 前置测试用例
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TestCaseEntity prerequisite;

    /**
     * 判断是否为强依赖
     */
    public boolean isHardDependency() {
        return "HARD".equalsIgnoreCase(this.dependencyType);
    }

    /**
     * 判断是否为弱依赖
     */
    public boolean isSoftDependency() {
        return "SOFT".equalsIgnoreCase(this.dependencyType);
    }
}
