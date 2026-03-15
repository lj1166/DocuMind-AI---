package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.entity.ChatHistory;
import cn.edu.cdu.documind.mapper.ChatHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史服务
 * 
 * @author DocuMind Team
 */
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryMapper historyMapper;

    /**
     * 保存消息
     */
    public ChatHistory saveMessage(Long agentId, Long userId, String sessionId, 
                                   String role, String content, Integer tokens) {
        ChatHistory history = new ChatHistory();
        history.setAgentId(agentId);
        history.setUserId(userId);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setTokens(tokens);
        history.setCreatedAt(LocalDateTime.now());
        
        historyMapper.insert(history);
        return history;
    }

    /**
     * 获取会话的对话历史
     */
    public List<ChatHistory> getHistoryBySession(String sessionId, int limit) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getSessionId, sessionId)
                    .orderByDesc(ChatHistory::getCreatedAt)
                    .last("LIMIT " + limit);
        
        List<ChatHistory> histories = historyMapper.selectList(queryWrapper);
        // 反转顺序（从旧到新）
        java.util.Collections.reverse(histories);
        return histories;
    }

    /**
     * 分页查询对话历史
     */
    public Page<ChatHistory> getHistoryPage(String sessionId, int page, int pageSize) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getSessionId, sessionId)
                    .orderByAsc(ChatHistory::getCreatedAt);
        
        return historyMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
    }

    /**
     * 删除会话的所有历史
     */
    public void deleteBySession(String sessionId) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getSessionId, sessionId);
        historyMapper.delete(queryWrapper);
    }

    /**
     * 根据ID获取消息
     */
    public ChatHistory getMessageById(Long messageId) {
        return historyMapper.selectById(messageId);
    }

    /**
     * 删除单条消息
     */
    public void deleteMessage(Long messageId) {
        historyMapper.deleteById(messageId);
    }
}

