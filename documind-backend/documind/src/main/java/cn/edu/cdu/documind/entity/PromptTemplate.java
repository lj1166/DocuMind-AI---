package cn.edu.cdu.documind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提示词模板实体类
 * 
 * @author DocuMind Team
 */
@TableName("prompt_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("agent_id")
    private Long agentId;

    @TableField("template_name")
    private String templateName;

    @TableField("template_type")
    private String templateType = "scenario"; // default-默认, scenario-场景, task-任务

    @TableField("prompt_content")
    private String promptContent;

    @TableField("description")
    private String description;

    @TableField("trigger_condition")
    private String triggerCondition; // JSON格式

    @TableField("priority")
    private Integer priority = 0;

    @TableField("is_active")
    private Integer isActive = 1; // 0-禁用 1-启用

    // 统计信息
    @TableField("usage_count")
    private Integer usageCount = 0;

    @TableField("last_used_at")
    private LocalDateTime lastUsedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

