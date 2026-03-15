package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.dto.response.AgentStatisticsResponse;
import cn.edu.cdu.documind.dto.response.StatisticsOverviewResponse;
import cn.edu.cdu.documind.entity.Agent;
import cn.edu.cdu.documind.mapper.AgentMapper;
import cn.edu.cdu.documind.mapper.ChatHistoryMapper;
import cn.edu.cdu.documind.mapper.ChatSessionMapper;
import cn.edu.cdu.documind.mapper.DocumentMapper;
import cn.edu.cdu.documind.mapper.PromptTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计服务
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final AgentMapper agentMapper;
    private final DocumentMapper documentMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatHistoryMapper historyMapper;
    private final PromptTemplateMapper templateMapper;

    /**
     * 获取用户的统计概览
     */
    public StatisticsOverviewResponse getUserOverview(Long userId) {
        // 获取用户的所有智能体
        List<Agent> agents = agentMapper.selectList(
            new LambdaQueryWrapper<Agent>()
                .eq(Agent::getUserId, userId)
        );

        long totalAgents = agents.size();
        long totalDocuments = agents.stream().mapToLong(a -> a.getDocumentCount() != null ? a.getDocumentCount() : 0).sum();
        long totalChats = agents.stream().mapToLong(a -> a.getChatCount() != null ? a.getChatCount() : 0).sum();
        long totalSessions = agents.stream().mapToLong(a -> a.getSessionCount() != null ? a.getSessionCount() : 0).sum();
        long totalTemplates = agents.stream().mapToLong(a -> a.getTemplateCount() != null ? a.getTemplateCount() : 0).sum();
        long totalTokens = agents.stream().mapToLong(a -> a.getTotalTokens() != null ? a.getTotalTokens() : 0).sum();

        // 今日统计（从chat_history表查询）
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        Long todayChats = historyMapper.countChatsByTimeRange(userId, todayStart, todayEnd);
        Long todayTokens = historyMapper.sumTokensByTimeRange(userId, todayStart, todayEnd);

        // 本周统计（从chat_history表查询）
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long weekChats = historyMapper.countChatsByTimeRange(userId, weekStart, weekEnd);
        Long weekTokens = historyMapper.sumTokensByTimeRange(userId, weekStart, weekEnd);

        return StatisticsOverviewResponse.builder()
                .totalAgents(totalAgents)
                .totalDocuments(totalDocuments)
                .totalChats(totalChats)
                .totalSessions(totalSessions)
                .totalTemplates(totalTemplates)
                .totalTokens(totalTokens)
                .todayChats(todayChats != null ? todayChats : 0L)
                .todayTokens(todayTokens != null ? todayTokens : 0L)
                .weekChats(weekChats != null ? weekChats : 0L)
                .weekTokens(weekTokens != null ? weekTokens : 0L)
                .build();
    }

    /**
     * 获取用户的智能体使用排行
     */
    public List<AgentStatisticsResponse> getAgentRankings(Long userId, String rankBy, Integer limit) {
        List<Agent> agents = agentMapper.selectList(
            new LambdaQueryWrapper<Agent>()
                .eq(Agent::getUserId, userId)
        );

        // 排序
        if ("chatCount".equals(rankBy)) {
            agents.sort((a, b) -> Integer.compare(
                b.getChatCount() != null ? b.getChatCount() : 0,
                a.getChatCount() != null ? a.getChatCount() : 0
            ));
        } else if ("totalTokens".equals(rankBy)) {
            agents.sort((a, b) -> Long.compare(
                b.getTotalTokens() != null ? b.getTotalTokens() : 0,
                a.getTotalTokens() != null ? a.getTotalTokens() : 0
            ));
        } else if ("lastChatAt".equals(rankBy)) {
            agents.sort((a, b) -> {
                if (b.getLastChatAt() == null) return -1;
                if (a.getLastChatAt() == null) return 1;
                return b.getLastChatAt().compareTo(a.getLastChatAt());
            });
        }

        // 限制数量
        if (limit != null && limit > 0) {
            agents = agents.stream().limit(limit).collect(Collectors.toList());
        }

        return agents.stream()
                .map(this::convertToAgentStatistics)
                .collect(Collectors.toList());
    }

    /**
     * 获取智能体详细统计
     */
    public AgentStatisticsResponse getAgentStatistics(Long agentId, Long userId) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }
        if (!agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该智能体");
        }

        return convertToAgentStatistics(agent);
    }

    /**
     * 转换为智能体统计响应
     */
    private AgentStatisticsResponse convertToAgentStatistics(Agent agent) {
        return AgentStatisticsResponse.builder()
                .agentId(agent.getId())
                .agentName(agent.getName())
                .chatCount(agent.getChatCount())
                .sessionCount(agent.getSessionCount())
                .documentCount(agent.getDocumentCount())
                .templateCount(agent.getTemplateCount())
                .totalTokens(agent.getTotalTokens())
                .lastChatAt(agent.getLastChatAt())
                .build();
    }
}

