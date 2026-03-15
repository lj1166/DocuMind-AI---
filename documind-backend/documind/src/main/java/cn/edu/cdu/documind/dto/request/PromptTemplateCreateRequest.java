package cn.edu.cdu.documind.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建提示词模板请求DTO
 * 
 * @author DocuMind Team
 */
@Data
public class PromptTemplateCreateRequest {
    private Long agentId;
    
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过100个字符")
    private String templateName;
    
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "default|scenario|task", message = "模板类型必须是default、scenario或task")
    private String templateType;
    
    @NotBlank(message = "提示词内容不能为空")
    private String promptContent;
    
    private String description;
    private String triggerCondition; // JSON格式
    
    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 10, message = "优先级不能大于10")
    private Integer priority;
}

