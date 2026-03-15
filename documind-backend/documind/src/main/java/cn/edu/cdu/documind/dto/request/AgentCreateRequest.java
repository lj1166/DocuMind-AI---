package cn.edu.cdu.documind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建智能体请求 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Schema(description = "创建智能体请求")
public class AgentCreateRequest {

    @NotBlank(message = "智能体名称不能为空")
    @Schema(description = "智能体名称", example = "我的私人医生")
    private String name;

    @Schema(description = "智能体描述", example = "专属健康顾问，解读我的体检报告")
    private String description;

    @Schema(description = "智能体头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "私人医生")
    private String roleName;

    @NotBlank(message = "系统提示词不能为空")
    @Schema(description = "系统提示词", example = "你是我的私人医生...")
    private String systemPrompt;

    @Schema(description = "提示词模式: single-单一提示词, multi-多模板", example = "single")
    private String promptMode = "single";

    @Schema(description = "AI模型名称", example = "qwen-plus")
    private String modelName = "qwen-plus";

    @Schema(description = "温度参数 0-1", example = "0.70")
    private BigDecimal temperature = new BigDecimal("0.70");

    @Schema(description = "最大输出token数", example = "2000")
    private Integer maxTokens = 2000;

    @Schema(description = "Top-P采样参数", example = "0.90")
    private BigDecimal topP = new BigDecimal("0.90");

    @Schema(description = "RAG检索开关: 0-关闭 1-开启", example = "1")
    private Integer ragEnabled = 1;

    @Schema(description = "RAG检索Top-K数量", example = "3")
    private Integer ragTopK = 3;

    @Schema(description = "RAG相似度阈值", example = "0.70")
    private BigDecimal ragSimilarityThreshold = new BigDecimal("0.70");
}

