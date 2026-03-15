package cn.edu.cdu.documind.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档响应DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private Long agentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private Integer vectorStatus; // 0-待处理 1-处理中 2-已完成 3-失败
    private String vectorError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

