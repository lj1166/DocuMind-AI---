package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.entity.Agent;
import cn.edu.cdu.documind.entity.ChatHistory;
import cn.edu.cdu.documind.entity.ChatSession;
import cn.edu.cdu.documind.entity.PromptTemplate;
import cn.edu.cdu.documind.mapper.AgentMapper;
import cn.edu.cdu.documind.mapper.ChatSessionMapper;
import cn.edu.cdu.documind.mapper.PromptTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 聊天服务（简化版）
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AgentMapper agentMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatHistoryService historyService;
    private final ChatClient.Builder chatClientBuilder;
    private final VectorService vectorService;
    private final PromptSelectorService promptSelectorService;
    private final PromptTemplateMapper promptTemplateMapper;

    /**
     * 发送消息并获取回复（同步版本 - 简化）
     */
    @Transactional(rollbackFor = Exception.class)
    public String chat(Long agentId, String sessionId, String userMessage, Long userId) {
        // 验证智能体
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }
        if (!agent.getUserId().equals(userId)) {
            throw new SecurityException("无权使用该智能体");
        }

        // 验证会话
        ChatSession session = sessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
        );
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        // 1. 保存用户消息
        historyService.saveMessage(agentId, userId, sessionId, "user", userMessage, 0);

        // 2. 获取历史对话（最近10条）
        List<ChatHistory> histories = historyService.getHistoryBySession(sessionId, 10);

        // 3. 构建消息列表（包含系统提示词和历史对话）
        List<Message> messages = new ArrayList<>();
        
        // 添加系统提示词
        messages.add(new SystemMessage(agent.getSystemPrompt()));
        
        // 添加历史对话
        for (ChatHistory history : histories) {
            if ("user".equals(history.getRole())) {
                messages.add(new UserMessage(history.getContent()));
            } else if ("assistant".equals(history.getRole())) {
                messages.add(new AssistantMessage(history.getContent()));
            }
        }
        
        // 添加当前用户消息
        messages.add(new UserMessage(userMessage));
        
        // 4. 调用AI生成回复
        String aiReply;
        long totalTokensConsumed = 0;
        try {
            // 创建ChatClient
            ChatClient chatClient = chatClientBuilder.build();
            
            // 构建Prompt（使用Agent的动态配置参数）
            DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                    .withModel(agent.getModelName())                        // 动态模型名称
                    .withTemperature(agent.getTemperature().doubleValue())  // 动态温度参数
                    .withTopP(agent.getTopP().doubleValue())                // 动态topP参数
                    .build();
            
            Prompt prompt = new Prompt(messages, chatOptions);
            
            log.info("调用AI - 参数配置: temperature={}, maxTokens={}, topP={}", 
                agent.getTemperature(), agent.getMaxTokens(), agent.getTopP());
            
            // 调用AI并获取完整响应（包含token信息）
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            aiReply = response.getResult().getOutput().getText();
            
            // 获取token消耗
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                Integer totalTokensInt = response.getMetadata().getUsage().getTotalTokens();
                totalTokensConsumed = (totalTokensInt != null) ? totalTokensInt.longValue() : 0L;
                log.info("Token消耗 - 总计: {}", totalTokensConsumed);
            }
            
            log.info("AI回复成功，sessionId: {}, 消息长度: {}, Token消耗: {}", 
                sessionId, aiReply.length(), totalTokensConsumed);
        } catch (Exception e) {
            log.error("AI调用失败: ", e);
            aiReply = "抱歉，AI服务暂时不可用，请稍后重试。错误: " + e.getMessage();
        }
        
        // 5. 保存AI回复（包含token消耗）
        historyService.saveMessage(agentId, userId, sessionId, "assistant", aiReply, (int)totalTokensConsumed);

        // 6. 更新统计
        session.setMessageCount(session.getMessageCount() + 2);
        session.setLastMessageAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        agent.setChatCount(agent.getChatCount() + 1);
        agent.setLastChatAt(LocalDateTime.now());
        // 更新总token消耗
        if (totalTokensConsumed > 0) {
            agent.setTotalTokens(agent.getTotalTokens() + totalTokensConsumed);
        }
        agentMapper.updateById(agent);

        return aiReply;
    }

    /**
     * 流式对话（SSE）
     */
    public Flux<String> chatStream(Long agentId, String sessionId, String userMessage, Long userId) {
        // 验证智能体
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            return Flux.error(new IllegalArgumentException("智能体不存在"));
        }
        if (!agent.getUserId().equals(userId)) {
            return Flux.error(new SecurityException("无权使用该智能体"));
        }

        // 验证会话
        ChatSession session = sessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
        );
        if (session == null) {
            return Flux.error(new IllegalArgumentException("会话不存在"));
        }

        // 保存用户消息
        historyService.saveMessage(agentId, userId, sessionId, "user", userMessage, 0);

        // 获取历史对话（最近5条，避免太长）
        List<ChatHistory> histories = historyService.getHistoryBySession(sessionId, 5);

        // 构建消息列表
        List<Message> messages = new ArrayList<>();
        
        // 选择系统提示词（多提示词模式）
        String systemPrompt = agent.getSystemPrompt();
        AtomicReference<Long> selectedTemplateId = new AtomicReference<>(null);
        if ("multi".equals(agent.getPromptMode())) {
            var selectedTemplate = promptSelectorService.selectBestTemplate(agentId, userMessage);
            if (selectedTemplate != null) {
                systemPrompt = selectedTemplate.getPromptContent();
                selectedTemplateId.set(selectedTemplate.getId());
                log.info("使用提示词模板: {} (ID: {})", selectedTemplate.getTemplateName(), selectedTemplate.getId());
            }
        }
        
        // 添加系统提示词
        messages.add(new SystemMessage(systemPrompt));
        
        // RAG检索（如果启用）
        String ragContext = "";
        if (agent.getRagEnabled() == 1) {
            List<org.springframework.ai.document.Document> relevantDocs = vectorService.searchRelevantDocuments(
                userMessage, 
                agentId, 
                agent.getRagTopK(),
                agent.getRagSimilarityThreshold().doubleValue()
            );
            
            if (!relevantDocs.isEmpty()) {
                StringBuilder contextBuilder = new StringBuilder("\n\n【参考文档】\n");
                StringBuilder sourcesBuilder = new StringBuilder();
                
                for (int i = 0; i < relevantDocs.size(); i++) {
                    org.springframework.ai.document.Document doc = relevantDocs.get(i);
                    String fileName = doc.getMetadata().get("file_name").toString();
                    
                    contextBuilder.append(String.format("%d. 来源：%s\n内容：%s\n\n", 
                        i + 1,
                        fileName,
                        doc.getText()
                    ));
                    
                    if (i > 0) sourcesBuilder.append(", ");
                    sourcesBuilder.append(fileName);
                }
                ragContext = contextBuilder.toString();
                
                log.info("RAG检索到{}个相关文档片段: {}", relevantDocs.size(), sourcesBuilder);
            }
        }
        
        // 添加历史对话（最近3条）
        int historyCount = Math.min(3, histories.size());
        for (int i = histories.size() - historyCount; i < histories.size(); i++) {
            ChatHistory history = histories.get(i);
            if ("user".equals(history.getRole())) {
                messages.add(new UserMessage(history.getContent()));
            } else if ("assistant".equals(history.getRole())) {
                messages.add(new AssistantMessage(history.getContent()));
            }
        }
        
        // 添加当前用户消息（包含RAG上下文）
        String finalMessage = ragContext.isEmpty() ? userMessage : (userMessage + ragContext);
        messages.add(new UserMessage(finalMessage));

        // 调用AI流式输出
        ChatClient chatClient = chatClientBuilder.build();
        
        // 构建Prompt（使用Agent的动态配置参数）
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(agent.getModelName())                        // 动态模型名称
                .withTemperature(agent.getTemperature().doubleValue())  // 动态温度参数
                .withTopP(agent.getTopP().doubleValue())                // 动态topP参数
                .build();
        
        Prompt prompt = new Prompt(messages, chatOptions);
        
        log.info("流式调用AI - 参数配置: temperature={}, maxTokens={}, topP={}, RAG检索数量: {}", 
            agent.getTemperature(), agent.getMaxTokens(), agent.getTopP(), 
            agent.getRagEnabled() == 1 ? agent.getRagTopK() : 0);
        
        // 用于收集完整回复和token信息
        StringBuilder fullReply = new StringBuilder();
        AtomicReference<Integer> totalTokens = new AtomicReference<>(0);
        
        return chatClient.prompt(prompt)
            .stream()
            .content()
            .doOnNext(chunk -> {
                fullReply.append(chunk);
                // 打印每个chunk（可选，调试用）
                // log.debug("收到AI chunk: {}", chunk);
            })
            .doOnComplete(() -> {
                // 流结束后保存完整回复
                String completeReply = fullReply.toString();
                
                // ⭐ 打印完整的AI回复内容
                log.info("==================== AI完整回复 ====================");
                log.info("SessionId: {}", sessionId);
                log.info("AgentId: {}", agentId);
                log.info("回复长度: {} 字符", completeReply.length());
                log.info("完整内容:\n{}", completeReply);
                log.info("===================================================");
                
                // 估算token消耗（流式响应通常不返回精确token数）
                // 估算规则：中文约1.5个字符/token，英文约4个字符/token
                int estimatedTokens = (int) (completeReply.length() * 0.7);
                totalTokens.set(estimatedTokens);
                
                historyService.saveMessage(agentId, userId, sessionId, "assistant", completeReply, estimatedTokens);
                
                // 更新统计
                session.setMessageCount(session.getMessageCount() + 2);
                session.setLastMessageAt(LocalDateTime.now());
                sessionMapper.updateById(session);
                
                agent.setChatCount(agent.getChatCount() + 1);
                agent.setLastChatAt(LocalDateTime.now());
                // 更新总token消耗
                if (estimatedTokens > 0) {
                    agent.setTotalTokens(agent.getTotalTokens() + estimatedTokens);
                }
                agentMapper.updateById(agent);
                
                // 更新模板使用统计
                if (selectedTemplateId.get() != null) {
                    PromptTemplate template = promptTemplateMapper.selectById(selectedTemplateId.get());
                    if (template != null) {
                        template.setUsageCount(template.getUsageCount() + 1);
                        template.setLastUsedAt(LocalDateTime.now());
                        promptTemplateMapper.updateById(template);
                        log.info("已更新模板使用统计: {} (使用次数: {})", template.getTemplateName(), template.getUsageCount());
                    }
                }
                
                log.info("流式对话完成，已保存到数据库，Token消耗(估算): {}", estimatedTokens);
            })
            .doOnError(error -> {
                log.error("流式对话失败: ", error);
                // 保存错误信息
                historyService.saveMessage(agentId, userId, sessionId, "assistant", 
                    "AI服务暂时不可用: " + error.getMessage(), 0);
            });
    }

    /**
     * 获取对话历史
     */
    public List<ChatHistory> getHistory(String sessionId, Long userId) {
        // TODO: 验证权限
        return historyService.getHistoryBySession(sessionId, 100);
    }

    /**
     * 删除单条消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        ChatHistory message = historyService.getMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        
        // 验证权限
        if (!message.getUserId().equals(userId)) {
            throw new SecurityException("无权删除该消息");
        }

        // 删除消息
        historyService.deleteMessage(messageId);
        
        log.info("已删除消息: messageId={}", messageId);
    }

    /**
     * 重新生成回答
     */
    public Flux<String> regenerateResponse(Long agentId, String sessionId, Long userId) {
        // 获取会话的最后一条用户消息
        List<ChatHistory> histories = historyService.getHistoryBySession(sessionId, 100);
        
        if (histories.isEmpty()) {
            return Flux.error(new IllegalArgumentException("会话中没有消息"));
        }

        // 找到最后一条用户消息
        String lastUserMessage = null;
        Long lastAssistantMessageId = null;
        
        for (int i = histories.size() - 1; i >= 0; i--) {
            ChatHistory history = histories.get(i);
            if ("assistant".equals(history.getRole()) && lastAssistantMessageId == null) {
                lastAssistantMessageId = history.getId();
            } else if ("user".equals(history.getRole())) {
                lastUserMessage = history.getContent();
                break;
            }
        }

        if (lastUserMessage == null) {
            return Flux.error(new IllegalArgumentException("未找到用户消息"));
        }

        // 删除最后一条AI回复
        if (lastAssistantMessageId != null) {
            historyService.deleteMessage(lastAssistantMessageId);
        }

        // 重新生成回答
        return chatStream(agentId, sessionId, lastUserMessage, userId);
    }
}

