package cn.edu.cdu.documind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能体实体类
 * 
 * @author DocuMind Team
 */
@TableName("agent")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("avatar")
    private String avatar;

    // ⭐ 核心字段：用户自定义配置
    @TableField("role_name")
    private String roleName;

    @TableField("system_prompt")
    private String systemPrompt;

    // ⭐ 提示词模式配置
    @TableField("prompt_mode")
    private String promptMode = "single"; // single-单一提示词, multi-多模板, dynamic-动态选择

    // AI 模型配置
    @TableField("model_name")
    private String modelName = "qwen-plus";

    @TableField("temperature")
    private BigDecimal temperature = new BigDecimal("0.70");

    @TableField("max_tokens")
    private Integer maxTokens = 2000;

    @TableField("top_p")
    private BigDecimal topP = new BigDecimal("0.90");

    // RAG 配置
    @TableField("rag_enabled")
    private Integer ragEnabled = 1; // 0-关闭 1-开启

    @TableField("rag_top_k")
    private Integer ragTopK = 10;  // 优化为10，提升检索全面性

    @TableField("rag_similarity_threshold")
    private BigDecimal ragSimilarityThreshold = new BigDecimal("0.50");

    // 统计信息（用于智能体详情页展示）
    @TableField("document_count")
    private Integer documentCount = 0;

    @TableField("chat_count")
    private Integer chatCount = 0;

    @TableField("session_count")
    private Integer sessionCount = 0;

    @TableField("template_count")
    private Integer templateCount = 0;

    @TableField("total_tokens")
    private Long totalTokens = 0L;

    // 时间信息
    @TableField("last_chat_at")
    private LocalDateTime lastChatAt;

    @TableField("status")
    private Integer status = 1; // 0-禁用 1-启用

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

