package cn.edu.cdu.documind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能体响应 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能体响应")
public class AgentResponse {

    @Schema(description = "智能体ID")
    private Long id;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "智能体描述")
    private String description;

    @Schema(description = "智能体头像URL")
    private String avatar;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "提示词模式")
    private String promptMode;

    @Schema(description = "AI模型名称")
    private String modelName;

    @Schema(description = "温度参数")
    private BigDecimal temperature;

    @Schema(description = "最大输出token数")
    private Integer maxTokens;

    @Schema(description = "Top-P采样参数")
    private BigDecimal topP;

    @Schema(description = "RAG检索开关")
    private Integer ragEnabled;

    @Schema(description = "RAG检索Top-K数量")
    private Integer ragTopK;

    @Schema(description = "RAG相似度阈值")
    private BigDecimal ragSimilarityThreshold;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "对话次数")
    private Integer chatCount;

    @Schema(description = "会话数量")
    private Integer sessionCount;

    @Schema(description = "模板数量")
    private Integer templateCount;

    @Schema(description = "累计消耗token数")
    private Long totalTokens;

    @Schema(description = "最后对话时间")
    private LocalDateTime lastChatAt;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

