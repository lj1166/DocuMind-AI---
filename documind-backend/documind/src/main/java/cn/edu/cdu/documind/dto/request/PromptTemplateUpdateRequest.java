package cn.edu.cdu.documind.dto.request;

import lombok.Data;

/**
 * 更新提示词模板请求DTO
 * 
 * @author DocuMind Team
 */
@Data
public class PromptTemplateUpdateRequest {
    private String templateName;
    private String templateType;
    private String promptContent;
    private String description;
    private String triggerCondition;
    private Integer priority;
    private Integer isActive;
}

