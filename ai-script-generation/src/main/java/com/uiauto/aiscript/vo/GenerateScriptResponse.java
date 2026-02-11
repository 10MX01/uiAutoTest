package com.uiauto.aiscript.vo;

import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepWithSelectors;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 生成脚本响应VO
 */
@Data
@Builder
public class GenerateScriptResponse {

    /**
     * 阶段1结果（操作意图）
     */
    private List<TestStep> phase1Result;

    /**
     * 阶段2结果（包含选择器）
     */
    private List<TestStepWithSelectors> phase2Result;

    /**
     * 页面快照
     */
    private Object pageSnapshot;

    /**
     * 生成元数据
     */
    private Object metadata;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;
}
