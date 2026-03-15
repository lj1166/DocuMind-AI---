package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.service.ApiMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API监控Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "API监控", description = "API调用监控和统计接口")
public class ApiMonitorController {

    private final ApiMonitorService monitorService;

    /**
     * 获取API端点统计（今日）
     */
    @GetMapping("/endpoints")
    @Operation(summary = "获取端点统计", description = "获取各API端点的调用统计（今日数据）")
    public Result<List<Map<String, Object>>> getEndpointStatistics() {
        List<Map<String, Object>> stats = monitorService.getEndpointStatistics();
        return Result.success(stats);
    }

    /**
     * 获取API端点统计（指定时间范围）
     */
    @GetMapping("/endpoints/range")
    @Operation(summary = "获取端点统计（时间范围）", description = "获取指定时间范围内的API端点统计")
    public Result<List<Map<String, Object>>> getEndpointStatisticsByRange(
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<Map<String, Object>> stats = monitorService.getEndpointStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 获取总体统计（今日）
     */
    @GetMapping("/overview")
    @Operation(summary = "获取总体统计", description = "获取API调用的总体统计信息（今日数据）")
    public Result<Map<String, Object>> getOverallStatistics() {
        Map<String, Object> stats = monitorService.getOverallStatistics();
        return Result.success(stats);
    }

    /**
     * 获取总体统计（指定时间范围）
     */
    @GetMapping("/overview/range")
    @Operation(summary = "获取总体统计（时间范围）", description = "获取指定时间范围内的总体统计")
    public Result<Map<String, Object>> getOverallStatisticsByRange(
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Map<String, Object> stats = monitorService.getOverallStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 获取今日调用量
     */
    @GetMapping("/today/count")
    @Operation(summary = "获取今日调用量", description = "获取今天的API总调用次数")
    public Result<Long> getTodayCallCount() {
        Long count = monitorService.getTodayCallCount();
        return Result.success(count);
    }

    /**
     * 获取今日成功率
     */
    @GetMapping("/today/success-rate")
    @Operation(summary = "获取今日成功率", description = "获取今天的API调用成功率")
    public Result<Double> getTodaySuccessRate() {
        Double rate = monitorService.getTodaySuccessRate();
        return Result.success(rate);
    }
}

