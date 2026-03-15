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
 * 对话历史实体类
 * 
 * @author DocuMind Team
 */
@TableName("chat_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("agent_id")
    private Long agentId;

    @TableField("user_id")
    private Long userId;

    @TableField("session_id")
    private String sessionId;

    @TableField("role")
    private String role; // user/assistant

    @TableField("content")
    private String content;

    @TableField("sources")
    private String sources; // JSON数组

    @TableField("tokens")
    private Integer tokens;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

