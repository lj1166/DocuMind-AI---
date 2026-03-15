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
 * 会话实体类
 * 
 * @author DocuMind Team
 */
@TableName("chat_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("agent_id")
    private Long agentId;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("message_count")
    private Integer messageCount = 0;

    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

