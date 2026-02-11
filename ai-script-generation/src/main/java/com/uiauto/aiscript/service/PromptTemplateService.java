package com.uiauto.aiscript.service;

import com.uiauto.aiscript.entity.PromptTemplateEntity;

import java.util.List;
import java.util.Optional;

/**
 * Prompt模板Service接口
 */
public interface PromptTemplateService {

    /**
     * 创建模板
     */
    PromptTemplateEntity createTemplate(PromptTemplateEntity template, Long userId);

    /**
     * 更新模板
     */
    PromptTemplateEntity updateTemplate(PromptTemplateEntity template, Long userId);

    /**
     * 删除模板
     */
    void deleteTemplate(Long templateId);

    /**
     * 根据ID查询模板
     */
    Optional<PromptTemplateEntity> getTemplateById(Long templateId);

    /**
     * 根据模板名称查询
     */
    Optional<PromptTemplateEntity> getTemplateByName(String templateName);

    /**
     * 根据类型查询启用的模板
     */
    List<PromptTemplateEntity> getActiveTemplatesByType(String templateType);

    /**
     * 获取指定类型的活跃模板（优先返回第一个）
     */
    Optional<PromptTemplateEntity> getActiveTemplate(String templateType);

    /**
     * 列出所有模板
     */
    List<PromptTemplateEntity> listAllTemplates();

    /**
     * 根据类型列出所有模板
     */
    List<PromptTemplateEntity> listTemplatesByType(String templateType);
}
