package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.request.AgentCreateRequest;
import cn.edu.cdu.documind.dto.request.AgentUpdateRequest;
import cn.edu.cdu.documind.dto.response.AgentResponse;
import cn.edu.cdu.documind.dto.response.PageResponse;
import cn.edu.cdu.documind.service.AgentService;
import cn.edu.cdu.documind.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体控制器
 * 
 * @author DocuMind Team
 */
@Slf4j
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "智能体管理", description = "智能体的增删改查接口")
public class AgentController {

    private final AgentService agentService;

    /**
     * 创建智能体
     */
    @PostMapping
    @Operation(summary = "创建智能体", description = "创建一个新的智能体")
    public Result<AgentResponse> createAgent(
            Authentication authentication,
            @Valid @RequestBody AgentCreateRequest request) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        AgentResponse response = agentService.createAgent(userId, request);
        return Result.success(response);
    }

    /**
     * 获取智能体列表（不分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取智能体列表", description = "获取当前用户的所有智能体（按创建时间倒序）")
    public Result<List<AgentResponse>> getAgentList(Authentication authentication) {
        Long userId = SecurityUtil.getUserId(authentication);
        List<AgentResponse> agents = agentService.getAgentList(userId);
        return Result.success(agents);
    }

    /**
     * 分页查询智能体
     */
    @GetMapping
    @Operation(summary = "分页查询智能体", description = "支持分页、排序、筛选、搜索")
    public Result<PageResponse<AgentResponse>> getAgentPage(
            Authentication authentication,
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向: asc/desc") @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @Parameter(description = "状态筛选: 0-禁用 1-启用") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词（名称或角色名称）") @RequestParam(required = false) String keyword) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        PageResponse<AgentResponse> response = agentService.getAgentPage(
                userId, page, pageSize, sortBy, sortOrder, status, keyword);
        return Result.success(response);
    }

    /**
     * 获取智能体详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取智能体详情", description = "根据ID获取智能体详细信息")
    public Result<AgentResponse> getAgentDetail(
            Authentication authentication,
            @Parameter(description = "智能体ID") @PathVariable Long id) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        AgentResponse response = agentService.getAgentDetail(userId, id);
        return Result.success(response);
    }

    /**
     * 更新智能体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新智能体", description = "更新智能体信息")
    public Result<AgentResponse> updateAgent(
            Authentication authentication,
            @Parameter(description = "智能体ID") @PathVariable Long id,
            @Valid @RequestBody AgentUpdateRequest request) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        AgentResponse response = agentService.updateAgent(userId, id, request);
        return Result.success(response);
    }

    /**
     * 删除智能体
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除智能体", description = "删除智能体及其关联数据")
    public Result<Void> deleteAgent(
            Authentication authentication,
            @Parameter(description = "智能体ID") @PathVariable Long id) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        agentService.deleteAgent(userId, id);
        return Result.success(null);
    }

    /**
     * 切换智能体状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "切换智能体状态", description = "启用/禁用智能体")
    public Result<AgentResponse> toggleStatus(
            Authentication authentication,
            @Parameter(description = "智能体ID") @PathVariable Long id) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        AgentResponse response = agentService.toggleStatus(userId, id);
        return Result.success(response);
    }

    /**
     * 统计用户的智能体数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计智能体数量", description = "获取当前用户的智能体总数")
    public Result<Long> countAgents(Authentication authentication) {
        Long userId = SecurityUtil.getUserId(authentication);
        Long count = agentService.countUserAgents(userId);
        return Result.success(count);
    }
}

