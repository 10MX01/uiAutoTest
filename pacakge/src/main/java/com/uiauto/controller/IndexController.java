package com.uiauto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 首页控制器
 * 用于处理 Vue Router 的 history 模式和根路径访问
 */
@Controller
public class IndexController {

    /**
     * 根路径转发到 index.html
     * 访问 http://localhost:8080/api/ 时自动加载前端页面
     */
    @RequestMapping({"", "/", "/index"})
    public String index() {
        return "forward:/index.html";
    }

    /**
     * 处理 Vue Router 前端路由
     * 所有前端路由页面都转发到 index.html，由 Vue Router 处理
     * 注意：前端路由路径不要与后端 API 路径冲突
     */
    @RequestMapping({
        "/ui",
        "/ui/**",
        "/script",
        "/script/**",
        "/test",
        "/test/**",
        "/settings",
        "/settings/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
