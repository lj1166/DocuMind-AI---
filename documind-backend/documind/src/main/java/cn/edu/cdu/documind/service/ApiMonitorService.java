package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.mapper.ApiCallLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API监控服务
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiMonitorService {

    private final ApiCallLogMapper apiCallLogMapper;

    /**
     * 获取API端点统计（今日）
     */
    public List<Map<String, Object>> getEndpointStatistics() {
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusDays(1);
        return apiCallLogMapper.statisticsByEndpoint(startTime, endTime);
    }

    /**
     * 获取API端点统计（指定时间范围）
     */
    public List<Map<String, Object>> getEndpointStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return apiCallLogMapper.statisticsByEndpoint(startTime, endTime);
    }

    /**
     * 获取总体统计（今日）
     */
    public Map<String, Object> getOverallStatistics() {
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusDays(1);
        
        Map<String, Object> stats = apiCallLogMapper.statisticsOverall(startTime, endTime);
        if (stats == null || stats.isEmpty()) {
            stats = new HashMap<>();
            stats.put("totalCalls", 0L);
            stats.put("successCalls", 0L);
            stats.put("failedCalls", 0L);
            stats.put("avgResponseTime", 0L);
            stats.put("successRate", 100.0);
            return stats;
        }
        
        // 安全地获取数值（处理BigDecimal/Long/Integer等类型）
        long totalCalls = getNumberValue(stats.get("totalCalls"));
        long successCalls = getNumberValue(stats.get("successCalls"));
        long failedCalls = getNumberValue(stats.get("failedCalls"));
        long avgResponseTime = getNumberValue(stats.get("avgResponseTime"));
        
        // 计算成功率
        double successRate = 100.0;
        if (totalCalls > 0) {
            successRate = Math.round((successCalls * 100.0 / totalCalls) * 10) / 10.0;
        }

        // 重新构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCalls", totalCalls);
        result.put("successCalls", successCalls);
        result.put("failedCalls", failedCalls);
        result.put("avgResponseTime", avgResponseTime);
        result.put("successRate", successRate);

        return result;
    }

    /**
     * 获取总体统计（指定时间范围）
     */
    public Map<String, Object> getOverallStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = apiCallLogMapper.statisticsOverall(startTime, endTime);
        if (stats == null || stats.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("totalCalls", 0L);
            result.put("successCalls", 0L);
            result.put("failedCalls", 0L);
            result.put("avgResponseTime", 0L);
            result.put("successRate", 100.0);
            return result;
        }

        // 安全地获取数值
        long totalCalls = getNumberValue(stats.get("totalCalls"));
        long successCalls = getNumberValue(stats.get("successCalls"));
        long failedCalls = getNumberValue(stats.get("failedCalls"));
        long avgResponseTime = getNumberValue(stats.get("avgResponseTime"));

        // 计算成功率
        double successRate = 100.0;
        if (totalCalls > 0) {
            successRate = Math.round((successCalls * 100.0 / totalCalls) * 10) / 10.0;
        }

        // 重新构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCalls", totalCalls);
        result.put("successCalls", successCalls);
        result.put("failedCalls", failedCalls);
        result.put("avgResponseTime", avgResponseTime);
        result.put("successRate", successRate);

        return result;
    }

    /**
     * 安全地将Object转换为long（处理BigDecimal、Long、Integer等类型）
     */
    private long getNumberValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 获取今日调用量
     */
    public Long getTodayCallCount() {
        Long count = apiCallLogMapper.countTodayCalls();
        return count != null ? count : 0L;
    }

    /**
     * 获取今日成功率
     */
    public Double getTodaySuccessRate() {
        Double rate = apiCallLogMapper.getTodaySuccessRate();
        return rate != null ? rate : 100.0;
    }
}

