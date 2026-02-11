package com.uiauto.aiscript.entity;

import com.uiauto.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Prompt模板实体
 * 存储AI调用的Prompt模板
 */
@Entity
@Table(name = "prompt_templates")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateEntity extends BaseEntity {

    /**
     * 模板名称
     */
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    /**
     * 模板类型：natural_language_parse/script_generation/script_generation_from_json
     */
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    /**
     * Prompt内容
     */
    @Lob
    @Column(name = "prompt_content", nullable = false, columnDefinition = "TEXT")
    private String promptContent;

    /**
     * 模板版本
     */
    @Column(name = "version", nullable = false, length = 20)
    @Builder.Default
    private String version = "1.0";

    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 模板描述
     */
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
