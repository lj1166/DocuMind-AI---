package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.response.AgentStatisticsResponse;
import cn.edu.cdu.documind.dto.response.StatisticsOverviewResponse;
import cn.edu.cdu.documind.service.StatisticsService;
import cn.edu.cdu.documind.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据 Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计管理", description = "统计数据查询接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取用户统计概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取统计概览", description = "获取当前用户的整体数据统计")
    public Result<StatisticsOverviewResponse> getOverview(Authentication authentication) {
        Long userId = SecurityUtil.getUserId(authentication);
        StatisticsOverviewResponse overview = statisticsService.getUserOverview(userId);
        return Result.success(overview);
    }

    /**
     * 获取智能体使用排行
     */
    @GetMapping("/agents/rankings")
    @Operation(summary = "获取智能体排行", description = "获取用户的智能体使用排行榜")
    public Result<List<AgentStatisticsResponse>> getAgentRankings(
            Authentication authentication,
            @Parameter(description = "排序依据: chatCount/totalTokens/lastChatAt") 
            @RequestParam(defaultValue = "chatCount") String rankBy,
            @Parameter(description = "返回数量限制") 
            @RequestParam(defaultValue = "10") Integer limit) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        List<AgentStatisticsResponse> rankings = statisticsService.getAgentRankings(userId, rankBy, limit);
        return Result.success(rankings);
    }

    /**
     * 获取单个智能体的详细统计
     */
    @GetMapping("/agents/{agentId}")
    @Operation(summary = "获取智能体统计", description = "获取指定智能体的详细统计信息")
    public Result<AgentStatisticsResponse> getAgentStatistics(
            Authentication authentication,
            @Parameter(description = "智能体ID") @PathVariable Long agentId) {
        
        Long userId = SecurityUtil.getUserId(authentication);
        AgentStatisticsResponse statistics = statisticsService.getAgentStatistics(agentId, userId);
        return Result.success(statistics);
    }
}

