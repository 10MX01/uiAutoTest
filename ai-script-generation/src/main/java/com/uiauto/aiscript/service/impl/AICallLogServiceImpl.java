package com.uiauto.aiscript.service.impl;

import com.uiauto.aiscript.entity.AICallLogEntity;
import com.uiauto.aiscript.repository.AICallLogRepository;
import com.uiauto.aiscript.service.AICallLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI调用日志Service实现
 */
@Slf4j
@Service
public class AICallLogServiceImpl implements AICallLogService {

    @Autowired
    private AICallLogRepository logRepository;

    @Override
    public AICallLogEntity logSuccess(String callType, String inputText, String outputJson,
                                      long durationMs, int tokensUsed, String modelUsed, Long serviceConfigId) {
        AICallLogEntity log = AICallLogEntity.builder()
                .callType(callType)
                .inputText(inputText)
                .outputJson(outputJson)
                .success(true)
                .durationMs(durationMs)
                .tokensUsed(tokensUsed)
                .modelUsed(modelUsed)
                .serviceConfigId(serviceConfigId)
                .build();

        // 设置父类字段
        log.setCreatedBy(0L);
        log.setUpdatedBy(0L);
        log.setCreatedTime(LocalDateTime.now());
        log.setUpdatedTime(LocalDateTime.now());

        return logRepository.save(log);
    }

    @Override
    public AICallLogEntity logFailure(String callType, String inputText, String errorMessage,
                                      long durationMs, Long serviceConfigId) {
        AICallLogEntity log = AICallLogEntity.builder()
                .callType(callType)
                .inputText(inputText)
                .success(false)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .serviceConfigId(serviceConfigId)
                .build();

        // 设置父类字段
        log.setCreatedBy(0L);
        log.setUpdatedBy(0L);
        log.setCreatedTime(LocalDateTime.now());
        log.setUpdatedTime(LocalDateTime.now());

        return logRepository.save(log);
    }

    @Override
    public AICallLogEntity logCall(AICallLogEntity log) {
        return logRepository.save(log);
    }

    @Override
    public List<AICallLogEntity> queryLogsByCallType(String callType) {
        return logRepository.findByCallTypeOrderByCreatedTimeDesc(callType);
    }

    @Override
    public List<AICallLogEntity> queryLogsBySuccess(boolean success) {
        return logRepository.findBySuccessOrderByCreatedTimeDesc(success);
    }

    @Override
    public List<AICallLogEntity> queryLogsByServiceConfigId(Long serviceConfigId) {
        return logRepository.findByServiceConfigIdOrderByCreatedTimeDesc(serviceConfigId);
    }
}
