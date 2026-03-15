package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.dto.request.AgentCreateRequest;
import cn.edu.cdu.documind.dto.request.AgentUpdateRequest;
import cn.edu.cdu.documind.dto.response.AgentResponse;
import cn.edu.cdu.documind.dto.response.PageResponse;
import cn.edu.cdu.documind.entity.Agent;
import cn.edu.cdu.documind.mapper.AgentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体业务逻辑层
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentMapper agentMapper;

    /**
     * 创建智能体
     */
    @Transactional
    public AgentResponse createAgent(Long userId, AgentCreateRequest request) {
        log.info("创建智能体 - 用户ID: {}, 名称: {}", userId, request.getName());

        Agent agent = new Agent();
        agent.setUserId(userId);
        agent.setName(request.getName());
        agent.setDescription(request.getDescription());
        agent.setAvatar(request.getAvatar());
        agent.setRoleName(request.getRoleName());
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setPromptMode(request.getPromptMode());
        agent.setModelName(request.getModelName());
        agent.setTemperature(request.getTemperature());
        agent.setMaxTokens(request.getMaxTokens());
        agent.setTopP(request.getTopP());
        agent.setRagEnabled(request.getRagEnabled());
        agent.setRagTopK(request.getRagTopK());
        agent.setRagSimilarityThreshold(request.getRagSimilarityThreshold());
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());

        agentMapper.insert(agent);
        log.info("智能体创建成功 - ID: {}", agent.getId());

        return convertToResponse(agent);
    }

    /**
     * 获取智能体列表（按创建时间倒序）
     */
    public List<AgentResponse> getAgentList(Long userId) {
        log.info("获取智能体列表 - 用户ID: {}", userId);
        List<Agent> agents = agentMapper.selectList(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getUserId, userId)
                        .orderByDesc(Agent::getCreatedAt)
        );
        return agents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询智能体
     */
    public PageResponse<AgentResponse> getAgentPage(Long userId, Integer page, Integer pageSize, 
                                                    String sortBy, String sortOrder, 
                                                    Integer status, String keyword) {
        log.info("分页查询智能体 - 用户ID: {}, 页码: {}, 每页数量: {}", userId, page, pageSize);

        // 构建分页对象
        Page<Agent> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getUserId, userId);
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Agent::getName, keyword).or().like(Agent::getRoleName, keyword));
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(Agent::getStatus, status);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortBy) {
                case "name" -> wrapper.orderBy(true, isAsc, Agent::getName);
                case "updatedAt" -> wrapper.orderBy(true, isAsc, Agent::getUpdatedAt);
                case "chatCount" -> wrapper.orderBy(true, isAsc, Agent::getChatCount);
                default -> wrapper.orderBy(true, isAsc, Agent::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(Agent::getCreatedAt);
        }

        // 执行分页查询
        IPage<Agent> agentPage = agentMapper.selectPage(pageParam, wrapper);

        List<AgentResponse> items = agentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<AgentResponse>builder()
                .items(items)
                .total(agentPage.getTotal())
                .page(page)
                .pageSize(pageSize)
                .totalPages((int) agentPage.getPages())
                .build();
    }

    /**
     * 获取智能体详情
     */
    public AgentResponse getAgentDetail(Long userId, Long agentId) {
        log.info("获取智能体详情 - 用户ID: {}, 智能体ID: {}", userId, agentId);

        Agent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getId, agentId)
                        .eq(Agent::getUserId, userId)
        );
        
        if (agent == null) {
            throw new RuntimeException("智能体不存在或无权访问");
        }

        return convertToResponse(agent);
    }

    /**
     * 更新智能体
     */
    @Transactional
    public AgentResponse updateAgent(Long userId, Long agentId, AgentUpdateRequest request) {
        log.info("更新智能体 - 用户ID: {}, 智能体ID: {}", userId, agentId);

        Agent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getId, agentId)
                        .eq(Agent::getUserId, userId)
        );
        
        if (agent == null) {
            throw new RuntimeException("智能体不存在或无权访问");
        }

        // 更新字段（只更新非空字段）
        if (request.getName() != null) {
            agent.setName(request.getName());
        }
        if (request.getDescription() != null) {
            agent.setDescription(request.getDescription());
        }
        if (request.getAvatar() != null) {
            agent.setAvatar(request.getAvatar());
        }
        if (request.getRoleName() != null) {
            agent.setRoleName(request.getRoleName());
        }
        if (request.getSystemPrompt() != null) {
            agent.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getPromptMode() != null) {
            agent.setPromptMode(request.getPromptMode());
        }
        if (request.getModelName() != null) {
            agent.setModelName(request.getModelName());
        }
        if (request.getTemperature() != null) {
            agent.setTemperature(request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            agent.setMaxTokens(request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            agent.setTopP(request.getTopP());
        }
        if (request.getRagEnabled() != null) {
            agent.setRagEnabled(request.getRagEnabled());
        }
        if (request.getRagTopK() != null) {
            agent.setRagTopK(request.getRagTopK());
        }
        if (request.getRagSimilarityThreshold() != null) {
            agent.setRagSimilarityThreshold(request.getRagSimilarityThreshold());
        }
        if (request.getStatus() != null) {
            agent.setStatus(request.getStatus());
        }
        
        agent.setUpdatedAt(LocalDateTime.now());
        agentMapper.updateById(agent);
        log.info("智能体更新成功 - ID: {}", agentId);

        return convertToResponse(agent);
    }

    /**
     * 删除智能体
     */
    @Transactional
    public void deleteAgent(Long userId, Long agentId) {
        log.info("删除智能体 - 用户ID: {}, 智能体ID: {}", userId, agentId);

        Agent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getId, agentId)
                        .eq(Agent::getUserId, userId)
        );
        
        if (agent == null) {
            throw new RuntimeException("智能体不存在或无权访问");
        }

        agentMapper.deleteById(agentId);
        log.info("智能体删除成功 - ID: {}", agentId);

        // TODO: 级联删除关联的文档、会话、对话历史、提示词模板
    }

    /**
     * 切换智能体状态
     */
    @Transactional
    public AgentResponse toggleStatus(Long userId, Long agentId) {
        log.info("切换智能体状态 - 用户ID: {}, 智能体ID: {}", userId, agentId);

        Agent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getId, agentId)
                        .eq(Agent::getUserId, userId)
        );
        
        if (agent == null) {
            throw new RuntimeException("智能体不存在或无权访问");
        }

        // 切换状态：1 -> 0, 0 -> 1
        agent.setStatus(agent.getStatus() == 1 ? 0 : 1);
        agent.setUpdatedAt(LocalDateTime.now());
        agentMapper.updateById(agent);

        log.info("智能体状态切换成功 - ID: {}, 新状态: {}", agentId, agent.getStatus());
        return convertToResponse(agent);
    }

    /**
     * 统计用户的智能体数量
     */
    public Long countUserAgents(Long userId) {
        return agentMapper.selectCount(
                new LambdaQueryWrapper<Agent>().eq(Agent::getUserId, userId)
        );
    }

    /**
     * 增加文档数量
     */
    @Transactional
    public void incrementDocumentCount(Long agentId) {
        agentMapper.incrementDocumentCount(agentId);
    }

    /**
     * 减少文档数量
     */
    @Transactional
    public void decrementDocumentCount(Long agentId) {
        agentMapper.decrementDocumentCount(agentId);
    }

    /**
     * 增加会话数量
     */
    @Transactional
    public void incrementSessionCount(Long agentId) {
        agentMapper.incrementSessionCount(agentId);
    }

    /**
     * 减少会话数量
     */
    @Transactional
    public void decrementSessionCount(Long agentId) {
        agentMapper.decrementSessionCount(agentId);
    }

    /**
     * 增加对话次数
     */
    @Transactional
    public void incrementChatCount(Long agentId) {
        agentMapper.incrementChatCount(agentId);
    }

    /**
     * 增加模板数量
     */
    @Transactional
    public void incrementTemplateCount(Long agentId) {
        agentMapper.incrementTemplateCount(agentId);
    }

    /**
     * 减少模板数量
     */
    @Transactional
    public void decrementTemplateCount(Long agentId) {
        agentMapper.decrementTemplateCount(agentId);
    }

    /**
     * 增加总token消耗
     */
    @Transactional
    public void addTotalTokens(Long agentId, Long tokens) {
        agentMapper.addTotalTokens(agentId, tokens);
    }

    /**
     * 转换为响应 DTO
     */
    private AgentResponse convertToResponse(Agent agent) {
        return AgentResponse.builder()
                .id(agent.getId())
                .userId(agent.getUserId())
                .name(agent.getName())
                .description(agent.getDescription())
                .avatar(agent.getAvatar())
                .roleName(agent.getRoleName())
                .systemPrompt(agent.getSystemPrompt())
                .promptMode(agent.getPromptMode())
                .modelName(agent.getModelName())
                .temperature(agent.getTemperature())
                .maxTokens(agent.getMaxTokens())
                .topP(agent.getTopP())
                .ragEnabled(agent.getRagEnabled())
                .ragTopK(agent.getRagTopK())
                .ragSimilarityThreshold(agent.getRagSimilarityThreshold())
                .documentCount(agent.getDocumentCount())
                .chatCount(agent.getChatCount())
                .sessionCount(agent.getSessionCount())
                .templateCount(agent.getTemplateCount())
                .totalTokens(agent.getTotalTokens())
                .lastChatAt(agent.getLastChatAt())
                .status(agent.getStatus())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }
}

