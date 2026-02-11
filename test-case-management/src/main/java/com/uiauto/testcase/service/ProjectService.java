package com.uiauto.testcase.service;

import com.uiauto.testcase.dto.ProjectCreateRequest;
import com.uiauto.testcase.dto.ProjectUpdateRequest;
import com.uiauto.testcase.vo.ProjectResponse;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 创建项目
     *
     * @param request 创建请求
     * @return 项目响应
     */
    ProjectResponse create(ProjectCreateRequest request);

    /**
     * 更新项目
     *
     * @param uniqueId 项目ID
     * @param request  更新请求
     * @return 项目响应
     */
    ProjectResponse update(Long uniqueId, ProjectUpdateRequest request);

    /**
     * 删除项目
     *
     * @param uniqueId 项目ID
     */
    void delete(Long uniqueId);

    /**
     * 查询项目列表
     *
     * @return 项目列表
     */
    List<ProjectResponse> listAll();

    /**
     * 根据ID查询项目详情
     *
     * @param uniqueId 项目ID
     * @return 项目响应
     */
    ProjectResponse getById(Long uniqueId);

    /**
     * 验证项目是否存在
     *
     * @param uniqueId 项目ID
     * @return 是否存在
     */
    boolean existsById(Long uniqueId);
}
