package com.uiauto.common;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类都应继承此类，包含统一的主键和审计字段
 *
 * 标准字段包含：
 * - unique_id: 唯一标识ID（自增主键）
 * - created_by: 创建人ID
 * - updated_by: 最后更新人ID
 * - created_time: 创建时间
 * - updated_time: 最后更新时间
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 唯一标识ID（自增主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unique_id", nullable = false)
    private Long uniqueId;

    /**
     * 创建人ID
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * 最后更新人ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 创建时间（创建时自动设置，后续不再修改）
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdTime;

    /**
     * 最后更新时间（每次更新时自动设置）
     */
    @Column(name = "updated_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
