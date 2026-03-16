package com.smartforum.interceptor;

import com.smartforum.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 可选认证路径：有 Token 时解析 userId，无 Token 或无效 Token 时直接放行（不返回 401）
     */
    private static final List<String> OPTIONAL_AUTH_PATHS = Arrays.asList(
            "/api/post/detail",
            "/api/user/profile/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        boolean isOptionalAuth = OPTIONAL_AUTH_PATHS.stream()
                .anyMatch(requestURI::startsWith);

        String authHeader = request.getHeader("Authorization");

        // 尝试解析 Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserId(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }

        // Token 缺失或无效
        if (isOptionalAuth) {
            // 可选认证路径：直接放行，userId 为 null
            return true;
        }

        // 必须认证的路径：返回 401
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
        return false;
    }
}
