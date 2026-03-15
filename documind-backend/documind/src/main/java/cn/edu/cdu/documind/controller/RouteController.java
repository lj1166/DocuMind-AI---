package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由控制器
 * 提供前端动态路由配置
 * 
 * @author DocuMind Team
 */
@Tag(name = "路由管理", description = "动态路由配置接口")
@RestController
@RequestMapping("/api")
public class RouteController {
    
    /**
     * 获取动态路由
     * 前端框架需要此接口来初始化路由
     */
    @Operation(summary = "获取动态路由", description = "返回前端路由配置")
    @GetMapping("/get-async-routes")
    public Result<List<Map<String, Object>>> getAsyncRoutes() {
        List<Map<String, Object>> routes = new ArrayList<>();
        
        // 首页路由
        Map<String, Object> homeRoute = new HashMap<>();
        homeRoute.put("path", "/welcome");
        homeRoute.put("name", "Welcome");
        homeRoute.put("component", "welcome");
        
        Map<String, Object> homeMeta = new HashMap<>();
        homeMeta.put("title", "首页");
        homeMeta.put("icon", "ep:home-filled");
        homeMeta.put("rank", 0);
        homeRoute.put("meta", homeMeta);
        
        routes.add(homeRoute);
        
        return Result.success(routes);
    }
}

