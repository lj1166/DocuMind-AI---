package cn.edu.cdu.documind.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提示词模板响应DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateResponse {
    private Long id;
    private Long agentId;
    private String templateName;
    private String templateType;
    private String promptContent;
    private String description;
    private String triggerCondition;
    private Integer priority;
    private Integer isActive;
    private Integer usageCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

