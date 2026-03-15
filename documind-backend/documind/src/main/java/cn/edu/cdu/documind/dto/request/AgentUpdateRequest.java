package cn.edu.cdu.documind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新智能体请求 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Schema(description = "更新智能体请求")
public class AgentUpdateRequest {

    @Schema(description = "智能体名称", example = "我的私人医生")
    private String name;

    @Schema(description = "智能体描述", example = "专属健康顾问")
    private String description;

    @Schema(description = "智能体头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "角色名称", example = "私人医生")
    private String roleName;

    @Schema(description = "系统提示词", example = "你是我的私人医生...")
    private String systemPrompt;

    @Schema(description = "提示词模式: single-单一提示词, multi-多模板", example = "single")
    private String promptMode;

    @Schema(description = "AI模型名称", example = "qwen-plus")
    private String modelName;

    @Schema(description = "温度参数 0-1", example = "0.70")
    private BigDecimal temperature;

    @Schema(description = "最大输出token数", example = "2000")
    private Integer maxTokens;

    @Schema(description = "Top-P采样参数", example = "0.90")
    private BigDecimal topP;

    @Schema(description = "RAG检索开关: 0-关闭 1-开启", example = "1")
    private Integer ragEnabled;

    @Schema(description = "RAG检索Top-K数量", example = "3")
    private Integer ragTopK;

    @Schema(description = "RAG相似度阈值", example = "0.70")
    private BigDecimal ragSimilarityThreshold;

    @Schema(description = "状态: 0-禁用 1-启用", example = "1")
    private Integer status;
}

