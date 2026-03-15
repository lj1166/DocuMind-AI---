package cn.edu.cdu.documind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计概览响应 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计概览响应")
public class StatisticsOverviewResponse {

    @Schema(description = "智能体总数")
    private Long totalAgents;

    @Schema(description = "文档总数")
    private Long totalDocuments;

    @Schema(description = "对话总数")
    private Long totalChats;

    @Schema(description = "会话总数")
    private Long totalSessions;

    @Schema(description = "模板总数")
    private Long totalTemplates;

    @Schema(description = "累计Token消耗")
    private Long totalTokens;

    @Schema(description = "今日对话数")
    private Long todayChats;

    @Schema(description = "今日Token消耗")
    private Long todayTokens;

    @Schema(description = "本周对话数")
    private Long weekChats;

    @Schema(description = "本周Token消耗")
    private Long weekTokens;
}

