package cn.edu.cdu.documind.mapper;

import cn.edu.cdu.documind.entity.ChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 对话历史Mapper
 * 
 * @author DocuMind Team
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 统计指定时间范围内的Token消耗总和
     */
    @Select("SELECT COALESCE(SUM(tokens), 0) FROM chat_history " +
            "WHERE user_id = #{userId} " +
            "AND created_at >= #{startTime} " +
            "AND created_at < #{endTime}")
    Long sumTokensByTimeRange(@Param("userId") Long userId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的对话次数（只统计assistant角色的消息）
     */
    @Select("SELECT COUNT(*) FROM chat_history " +
            "WHERE user_id = #{userId} " +
            "AND role = 'assistant' " +
            "AND created_at >= #{startTime} " +
            "AND created_at < #{endTime}")
    Long countChatsByTimeRange(@Param("userId") Long userId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}

