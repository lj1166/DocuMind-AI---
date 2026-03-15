package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.dto.request.ChatSessionCreateRequest;
import cn.edu.cdu.documind.dto.response.ChatSessionResponse;
import cn.edu.cdu.documind.entity.ChatSession;
import cn.edu.cdu.documind.mapper.AgentMapper;
import cn.edu.cdu.documind.mapper.ChatSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务
 * 
 * @author DocuMind Team
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionMapper sessionMapper;
    private final AgentMapper agentMapper;

    /**
     * 创建会话
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionResponse createSession(ChatSessionCreateRequest request, Long userId) {
        // 验证智能体是否存在且属于该用户
        var agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }
        if (!agent.getUserId().equals(userId)) {
            throw new SecurityException("无权操作该智能体");
        }

        // 创建会话
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setAgentId(request.getAgentId());
        session.setUserId(userId);
        session.setTitle(request.getTitle() != null ? request.getTitle() : "新会话");
        session.setMessageCount(0);
        session.setCreatedAt(LocalDateTime.now());

        sessionMapper.insert(session);

        // 更新智能体会话数量统计
        agent.setSessionCount(agent.getSessionCount() + 1);
        agentMapper.updateById(agent);

        return convertToResponse(session);
    }

    /**
     * 获取智能体的会话列表（分页）
     */
    public Page<ChatSessionResponse> getSessionsByAgent(Long agentId, Long userId, int page, int pageSize, String keyword) {
        // 验证智能体权限
        var agent = agentMapper.selectById(agentId);
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该智能体的会话");
        }

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getAgentId, agentId)
                    .eq(ChatSession::getUserId, userId);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(ChatSession::getTitle, keyword);
        }
        
        queryWrapper.orderByDesc(ChatSession::getLastMessageAt)
                    .orderByDesc(ChatSession::getCreatedAt);

        Page<ChatSession> sessionPage = sessionMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        
        Page<ChatSessionResponse> responsePage = new Page<>(sessionPage.getCurrent(), sessionPage.getSize(), sessionPage.getTotal());
        responsePage.setRecords(
            sessionPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList())
        );
        
        return responsePage;
    }

    /**
     * 获取会话详情
     */
    public ChatSessionResponse getSessionById(Long id, Long userId) {
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        // 验证权限
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该会话");
        }

        return convertToResponse(session);
    }

    /**
     * 通过sessionId获取会话
     */
    public ChatSessionResponse getSessionBySessionId(String sessionId, Long userId) {
        ChatSession session = sessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
        );
        
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        return convertToResponse(session);
    }

    /**
     * 更新会话标题
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionResponse updateSessionTitle(Long id, String title, Long userId) {
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        // 验证权限
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("无权修改该会话");
        }

        session.setTitle(title);
        sessionMapper.updateById(session);

        return convertToResponse(session);
    }

    /**
     * 删除会话
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id, Long userId) {
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        // 验证权限
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("无权删除该会话");
        }

        // 删除会话
        sessionMapper.deleteById(id);

        // TODO: 删除对话历史

        // 更新智能体会话数量统计
        var agent = agentMapper.selectById(session.getAgentId());
        if (agent != null) {
            int newCount = Math.max(0, agent.getSessionCount() - 1);
            agent.setSessionCount(newCount);
            agentMapper.updateById(agent);
        }
    }

    /**
     * 更新会话统计信息（由对话服务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionStats(String sessionId, int messageIncrement) {
        ChatSession session = sessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>().eq(ChatSession::getSessionId, sessionId)
        );
        
        if (session != null) {
            session.setMessageCount(session.getMessageCount() + messageIncrement);
            session.setLastMessageAt(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }

    /**
     * 转换为响应DTO
     */
    private ChatSessionResponse convertToResponse(ChatSession session) {
        return ChatSessionResponse.builder()
            .id(session.getId())
            .sessionId(session.getSessionId())
            .agentId(session.getAgentId())
            .userId(session.getUserId())
            .title(session.getTitle())
            .messageCount(session.getMessageCount())
            .lastMessageAt(session.getLastMessageAt())
            .createdAt(session.getCreatedAt())
            .build();
    }
}

