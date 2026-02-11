package com.uiauto.aiscript.controller;

import com.uiauto.aiscript.dto.AIServiceConfigCreateRequest;
import com.uiauto.aiscript.dto.AIServiceConfigUpdateRequest;
import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import com.uiauto.aiscript.service.AIServiceConfigService;
import com.uiauto.aiscript.vo.AIServiceConfigResponse;
import com.uiauto.aiscript.vo.ConnectionTestResponse;
import com.uiauto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务配置Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-service")
public class AIServiceConfigController {

    @Autowired
    private AIServiceConfigService configService;

    /**
     * 查询所有AI服务配置
     */
    @GetMapping("/configs")
    public ApiResponse<List<AIServiceConfigResponse>> listConfigs() {
        try {
            List<AIServiceConfigEntity> configs = configService.listAllConfigs();
            List<AIServiceConfigResponse> responses = configs.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("查询AI服务配置失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询配置详情
     */
    @GetMapping("/configs/detail")
    public ApiResponse<AIServiceConfigResponse> getConfigDetail(@RequestParam Long id) {
        try {
            AIServiceConfigEntity config = configService.getConfigById(id);
            return ApiResponse.success(convertToResponse(config));
        } catch (Exception e) {
            log.error("查询配置详情失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询可用的AI服务配置
     */
    @GetMapping("/available")
    public ApiResponse<List<AIServiceConfigResponse>> getAvailableConfigs() {
        try {
            List<AIServiceConfigEntity> configs = configService.listActiveConfigs();
            List<AIServiceConfigResponse> responses = configs.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("查询可用配置失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建AI服务配置
     */
    @PostMapping("/configs/create")
    public ApiResponse<AIServiceConfigResponse> createConfig(@Valid @RequestBody AIServiceConfigCreateRequest request) {
        try {
            Long userId = 1L; // TODO: 从上下文获取用户ID
            AIServiceConfigEntity config = configService.createConfig(request, userId);
            return ApiResponse.success(convertToResponse(config));
        } catch (Exception e) {
            log.error("创建AI服务配置失败", e);
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新AI服务配置
     */
    @PostMapping("/configs/update")
    public ApiResponse<AIServiceConfigResponse> updateConfig(@Valid @RequestBody AIServiceConfigUpdateRequest request) {
        try {
            Long userId = 1L; // TODO: 从上下文获取用户ID
            AIServiceConfigEntity config = configService.updateConfig(request, userId);
            return ApiResponse.success(convertToResponse(config));
        } catch (Exception e) {
            log.error("更新AI服务配置失败", e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除AI服务配置
     */
    @PostMapping("/configs/delete")
    public ApiResponse<Void> deleteConfig(@RequestParam Long id) {
        try {
            configService.deleteConfig(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("删除AI服务配置失败", e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    @PostMapping("/configs/test")
    public ApiResponse<ConnectionTestResponse> testConnection(@RequestParam Long id) {
        try {
            ConnectionTestResponse response = configService.testConnection(id);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return ApiResponse.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 设置为默认配置
     */
    @PostMapping("/configs/set-default")
    public ApiResponse<Void> setAsDefault(@RequestParam Long id) {
        try {
            configService.setAsDefault(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("设置默认配置失败", e);
            return ApiResponse.error("设置失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应VO
     */
    private AIServiceConfigResponse convertToResponse(AIServiceConfigEntity entity) {
        AIServiceConfigResponse response = new AIServiceConfigResponse();
        BeanUtils.copyProperties(entity, response);

        // 脱敏API密钥
        response.setApiKey(configService.maskApiKey(entity.getApiKey()));

        // 格式化时间
        if (entity.getCreatedTime() != null) {
            response.setCreatedTime(entity.getCreatedTime().toString());
        }
        if (entity.getUpdatedTime() != null) {
            response.setUpdatedTime(entity.getUpdatedTime().toString());
        }

        return response;
    }
}
