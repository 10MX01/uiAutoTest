package com.uiauto.aiscript.service.impl;

import com.uiauto.aiscript.entity.PromptTemplateEntity;
import com.uiauto.aiscript.repository.PromptTemplateRepository;
import com.uiauto.aiscript.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Prompt模板Service实现
 */
@Slf4j
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    @Autowired
    private PromptTemplateRepository templateRepository;

    @Override
    @Transactional
    public PromptTemplateEntity createTemplate(PromptTemplateEntity template, Long userId) {
        template.setCreatedBy(userId);
        template.setUpdatedBy(userId);
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedTime(LocalDateTime.now());
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public PromptTemplateEntity updateTemplate(PromptTemplateEntity template, Long userId) {
        template.setUpdatedBy(userId);
        template.setUpdatedTime(LocalDateTime.now());
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }

    @Override
    public Optional<PromptTemplateEntity> getTemplateById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    @Override
    public Optional<PromptTemplateEntity> getTemplateByName(String templateName) {
        return templateRepository.findByTemplateName(templateName);
    }

    @Override
    public List<PromptTemplateEntity> getActiveTemplatesByType(String templateType) {
        return templateRepository.findByTemplateTypeAndIsActive(templateType, true);
    }

    @Override
    public Optional<PromptTemplateEntity> getActiveTemplate(String templateType) {
        List<PromptTemplateEntity> templates = getActiveTemplatesByType(templateType);
        return templates.isEmpty() ? Optional.empty() : Optional.of(templates.get(0));
    }

    @Override
    public List<PromptTemplateEntity> listAllTemplates() {
        return templateRepository.findAll();
    }

    @Override
    public List<PromptTemplateEntity> listTemplatesByType(String templateType) {
        return templateRepository.findByTemplateType(templateType);
    }
}
