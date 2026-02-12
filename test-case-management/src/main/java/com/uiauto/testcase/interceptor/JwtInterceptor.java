package com.uiauto.testcase.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT拦截器
 * 验证请求头中的Token，提取用户信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置CORS头（预检请求直接通过）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Token
        String token = extractToken(request);

        // 验证Token
        if (token == null || !JwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "未授权，请先登录");
            return false;
        }

        // 检查Token是否过期
        if (JwtUtil.isTokenExpired(token)) {
            sendErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 提取用户信息并存入request attribute
        try {
            Long userId = JwtUtil.getUserIdFromToken(token);
            String username = JwtUtil.getUsernameFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
        } catch (Exception e) {
            sendErrorResponse(response, 401, "Token解析失败");
            return false;
        }

        return true;
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
