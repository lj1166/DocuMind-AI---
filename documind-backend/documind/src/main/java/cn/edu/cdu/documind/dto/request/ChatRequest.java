package cn.edu.cdu.documind.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求DTO
 * 
 * @author DocuMind Team
 */
@Data
public class ChatRequest {
    
    @NotBlank(message = "智能体ID不能为空")
    private String agentId;
    
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    @NotBlank(message = "消息内容不能为空")
    private String message;
}

