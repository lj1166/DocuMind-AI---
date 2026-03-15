package cn.edu.cdu.documind.mapper;

import cn.edu.cdu.documind.entity.Agent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 智能体 Mapper
 * 
 * @author DocuMind Team
 */
public interface AgentMapper extends BaseMapper<Agent> {

    /**
     * 增加文档数量
     */
    @Update("UPDATE agent SET document_count = document_count + 1 WHERE id = #{agentId}")
    void incrementDocumentCount(@Param("agentId") Long agentId);

    /**
     * 减少文档数量
     */
    @Update("UPDATE agent SET document_count = CASE WHEN document_count > 0 THEN document_count - 1 ELSE 0 END WHERE id = #{agentId}")
    void decrementDocumentCount(@Param("agentId") Long agentId);

    /**
     * 增加会话数量
     */
    @Update("UPDATE agent SET session_count = session_count + 1 WHERE id = #{agentId}")
    void incrementSessionCount(@Param("agentId") Long agentId);

    /**
     * 减少会话数量
     */
    @Update("UPDATE agent SET session_count = CASE WHEN session_count > 0 THEN session_count - 1 ELSE 0 END WHERE id = #{agentId}")
    void decrementSessionCount(@Param("agentId") Long agentId);

    /**
     * 增加对话次数
     */
    @Update("UPDATE agent SET chat_count = chat_count + 1, last_chat_at = NOW() WHERE id = #{agentId}")
    void incrementChatCount(@Param("agentId") Long agentId);

    /**
     * 增加模板数量
     */
    @Update("UPDATE agent SET template_count = template_count + 1 WHERE id = #{agentId}")
    void incrementTemplateCount(@Param("agentId") Long agentId);

    /**
     * 减少模板数量
     */
    @Update("UPDATE agent SET template_count = CASE WHEN template_count > 0 THEN template_count - 1 ELSE 0 END WHERE id = #{agentId}")
    void decrementTemplateCount(@Param("agentId") Long agentId);

    /**
     * 更新总token消耗
     */
    @Update("UPDATE agent SET total_tokens = total_tokens + #{tokens} WHERE id = #{agentId}")
    void addTotalTokens(@Param("agentId") Long agentId, @Param("tokens") Long tokens);
}

