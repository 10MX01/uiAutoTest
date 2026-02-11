package com.uiauto.aiscript.service;

import com.uiauto.aiscript.model.PageSnapshot;
import com.uiauto.common.model.TestStep;
import com.uiauto.common.model.TestStepWithSelectors;

import java.util.List;

/**
 * 两阶段脚本生成Service接口
 */
public interface TwoPhaseScriptGenerationService {

    /**
     * 两阶段生成脚本
     * 阶段1：自然语言 → JSON（操作意图）
     * 阶段2：JSON + 快照 → 脚本（含选择器）
     *
     * @param naturalLanguage 自然语言描述
     * @param url 目标URL
     * @return 包含选择器的测试脚本
     */
    TwoPhaseGenerateResult generateScriptInTwoPhases(String naturalLanguage, String url) throws Exception;

    /**
     * 基于JSON和快照生成脚本（阶段2）
     *
     * @param stepsJson 操作步骤JSON（target字段为中文描述）
     * @param snapshot 页面快照
     * @return 包含选择器的测试脚本
     */
    List<TestStepWithSelectors> generateScriptFromJsonAndSnapshot(
            List<TestStep> stepsJson,
            PageSnapshot snapshot) throws Exception;

    /**
     * 两阶段生成结果
     */
    class TwoPhaseGenerateResult {
        private List<TestStep> phase1Result;
        private List<TestStepWithSelectors> phase2Result;
        private PageSnapshot snapshot;
        private GenerationMetadata metadata;

        public TwoPhaseGenerateResult(List<TestStep> phase1Result,
                                     List<TestStepWithSelectors> phase2Result,
                                     PageSnapshot snapshot,
                                     GenerationMetadata metadata) {
            this.phase1Result = phase1Result;
            this.phase2Result = phase2Result;
            this.snapshot = snapshot;
            this.metadata = metadata;
        }

        public List<TestStep> getPhase1Result() {
            return phase1Result;
        }

        public List<TestStepWithSelectors> getPhase2Result() {
            return phase2Result;
        }

        public PageSnapshot getSnapshot() {
            return snapshot;
        }

        public GenerationMetadata getMetadata() {
            return metadata;
        }
    }

    /**
     * 生成元数据
     */
    class GenerationMetadata {
        private int totalSteps;
        private int matchedElements;
        private int unmatchedTargets;
        private double averageConfidence;

        public GenerationMetadata(int totalSteps, int matchedElements,
                                 int unmatchedTargets, double averageConfidence) {
            this.totalSteps = totalSteps;
            this.matchedElements = matchedElements;
            this.unmatchedTargets = unmatchedTargets;
            this.averageConfidence = averageConfidence;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public int getMatchedElements() {
            return matchedElements;
        }

        public int getUnmatchedTargets() {
            return unmatchedTargets;
        }

        public double getAverageConfidence() {
            return averageConfidence;
        }
    }
}
