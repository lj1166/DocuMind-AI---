package cn.edu.cdu.documind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API调用日志实体
 * 
 * @author DocuMind Team
 */
@TableName("api_call_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCallLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("endpoint")
    private String endpoint;

    @TableField("http_method")
    private String httpMethod;

    @TableField("request_params")
    private String requestParams;

    @TableField("response_status")
    private Integer responseStatus;

    @TableField("response_time")
    private Integer responseTime; // 毫秒

    @TableField("error_message")
    private String errorMessage;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

