package cn.edu.cdu.documind.mapper;

import cn.edu.cdu.documind.entity.ApiCallLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API调用日志Mapper
 * 
 * @author DocuMind Team
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {

    /**
     * 统计API端点调用情况
     */
    @Select("SELECT " +
            "endpoint, " +
            "http_method AS httpMethod, "+
            "COUNT(*) AS totalCalls, " +
            "SUM(CASE WHEN response_status = 200 THEN 1 ELSE 0 END) AS successCalls, " +
            "SUM(CASE WHEN response_status != 200 THEN 1 ELSE 0 END) AS failedCalls, " +
            "ROUND(AVG(response_time), 0) AS avgResponseTime, " +
            "MAX(response_time) AS maxResponseTime, " +
            "MIN(response_time) AS minResponseTime " +
            "FROM api_call_log " +
            "WHERE created_at >= #{startTime} AND created_at < #{endTime} " +
            "GROUP BY endpoint, http_method " +
            "ORDER BY totalCalls DESC")
    List<Map<String, Object>> statisticsByEndpoint(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计总体API调用情况
     */
    @Select("SELECT " +
            "COUNT(*) AS totalCalls, " +
            "SUM(CASE WHEN response_status = 200 THEN 1 ELSE 0 END) AS successCalls, " +
            "SUM(CASE WHEN response_status != 200 THEN 1 ELSE 0 END) AS failedCalls, " +
            "ROUND(AVG(response_time), 0) AS avgResponseTime " +
            "FROM api_call_log " +
            "WHERE created_at >= #{startTime} AND created_at < #{endTime}")
    Map<String, Object> statisticsOverall(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计今日调用量
     */
    @Select("SELECT COUNT(*) FROM api_call_log " +
            "WHERE DATE(created_at) = CURDATE()")
    Long countTodayCalls();

    /**
     * 统计今日成功率
     */
    @Select("SELECT " +
            "ROUND(SUM(CASE WHEN response_status = 200 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS successRate " +
            "FROM api_call_log " +
            "WHERE DATE(created_at) = CURDATE()")
    Double getTodaySuccessRate();
}

