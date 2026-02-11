package com.uiauto.aiscript.entity;

import com.uiauto.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * AI调用日志实体
 * 记录所有AI调用的日志，用于追溯和优化
 */
@Entity
@Table(name = "ai_call_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AICallLogEntity extends BaseEntity {

    /**
     * 调用类型：natural_language_parse/script_generation
     */
    @Column(name = "call_type", nullable = false, length = 50)
    private String callType;

    /**
     * 输入文本
     */
    @Lob
    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    /**
     * 输出JSON结果
     */
    @Column(name = "output_json", columnDefinition = "JSON")
    private String outputJson;

    /**
     * 是否成功
     */
    @Column(name = "success", nullable = false)
    private Boolean success;

    /**
     * 错误信息
     */
    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 使用的token数
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /**
     * 使用的AI模型
     */
    @Column(name = "model_used", length = 100)
    private String modelUsed;

    /**
     * 使用的AI服务配置ID
     */
    @Column(name = "service_config_id")
    private Long serviceConfigId;
}
