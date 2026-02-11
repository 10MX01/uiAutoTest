package com.uiauto.aiscript.repository;

import com.uiauto.aiscript.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Prompt模板Repository
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, Long> {

    /**
     * 根据模板类型和启用状态查询
     */
    List<PromptTemplateEntity> findByTemplateTypeAndIsActive(String templateType, Boolean isActive);

    /**
     * 根据模板名称查询
     */
    Optional<PromptTemplateEntity> findByTemplateName(String templateName);

    /**
     * 根据模板类型查询所有模板
     */
    List<PromptTemplateEntity> findByTemplateType(String templateType);

    /**
     * 查询所有启用的模板
     */
    List<PromptTemplateEntity> findByIsActiveTrue();
}
