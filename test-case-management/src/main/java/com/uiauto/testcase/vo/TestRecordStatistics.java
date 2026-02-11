package com.uiauto.testcase.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试记录统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRecordStatistics {
    /**
     * 执行总数
     */
    private Long total;

    /**
     * 成功数
     */
    private Long success;

    /**
     * 失败数
     */
    private Long failed;

    /**
     * 跳过数
     */
    private Long skipped;
}
