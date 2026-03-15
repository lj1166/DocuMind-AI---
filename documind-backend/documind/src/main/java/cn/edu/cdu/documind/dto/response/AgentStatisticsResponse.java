package cn.edu.cdu.documind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 智能体统计响应 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能体统计响应")
public class AgentStatisticsResponse {

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "对话次数")
    private Integer chatCount;

    @Schema(description = "会话数量")
    private Integer sessionCount;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "模板数量")
    private Integer templateCount;

    @Schema(description = "Token消耗")
    private Long totalTokens;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastChatAt;
}

