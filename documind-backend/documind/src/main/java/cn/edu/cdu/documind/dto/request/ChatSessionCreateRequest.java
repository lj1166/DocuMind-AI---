package cn.edu.cdu.documind.dto.request;

import lombok.Data;

/**
 * 创建会话请求DTO
 * 
 * @author DocuMind Team
 */
@Data
public class ChatSessionCreateRequest {
    private Long agentId;
    private String title; // 可选，如果不提供则自动生成
}

