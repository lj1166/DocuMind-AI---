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
 * 文档实体类
 * 
 * @author DocuMind Team
 */
@TableName("document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("agent_id")
    private Long agentId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_type")
    private String fileType; // pdf/doc/docx/txt/md

    @TableField("file_size")
    private Long fileSize;

    @TableField("chunk_count")
    private Integer chunkCount = 0;

    @TableField("vector_status")
    private Integer vectorStatus = 0; // 0-待处理 1-处理中 2-已完成 3-失败

    @TableField("vector_error")
    private String vectorError;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

