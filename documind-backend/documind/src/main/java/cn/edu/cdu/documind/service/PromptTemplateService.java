package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.dto.request.PromptTemplateCreateRequest;
import cn.edu.cdu.documind.dto.request.PromptTemplateUpdateRequest;
import cn.edu.cdu.documind.dto.response.PromptTemplateResponse;
import cn.edu.cdu.documind.entity.PromptTemplate;
import cn.edu.cdu.documind.mapper.AgentMapper;
import cn.edu.cdu.documind.mapper.PromptTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 提示词模板服务
 * 
 * @author DocuMind Team
 */
@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateMapper templateMapper;
    private final AgentMapper agentMapper;

    /**
     * 创建提示词模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateResponse createTemplate(PromptTemplateCreateRequest request, Long userId) {
        // 验证智能体是否存在且属于该用户
        var agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }
        if (!agent.getUserId().equals(userId)) {
            throw new SecurityException("无权操作该智能体");
        }

        // 创建模板
        PromptTemplate template = new PromptTemplate();
        template.setAgentId(request.getAgentId());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateType(request.getTemplateType() != null ? request.getTemplateType() : "scenario");
        template.setPromptContent(request.getPromptContent());
        template.setDescription(request.getDescription());
        template.setTriggerCondition(request.getTriggerCondition());
        template.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        template.setIsActive(1);
        template.setUsageCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        templateMapper.insert(template);

        // 更新智能体模板数量统计
        agent.setTemplateCount(agent.getTemplateCount() + 1);
        agentMapper.updateById(agent);

        return convertToResponse(template);
    }

    /**
     * 获取智能体的模板列表（分页）
     */
    public Page<PromptTemplateResponse> getTemplatesByAgent(Long agentId, Long userId, int page, int pageSize, String sortBy) {
        // 验证智能体权限
        var agent = agentMapper.selectById(agentId);
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该智能体的模板");
        }

        LambdaQueryWrapper<PromptTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromptTemplate::getAgentId, agentId);
        
        // 排序
        if ("priority".equals(sortBy)) {
            queryWrapper.orderByDesc(PromptTemplate::getPriority);
        } else if ("usage".equals(sortBy)) {
            queryWrapper.orderByDesc(PromptTemplate::getUsageCount);
        } else {
            queryWrapper.orderByDesc(PromptTemplate::getCreatedAt);
        }

        Page<PromptTemplate> templatePage = templateMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        
        Page<PromptTemplateResponse> responsePage = new Page<>(templatePage.getCurrent(), templatePage.getSize(), templatePage.getTotal());
        responsePage.setRecords(
            templatePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList())
        );
        
        return responsePage;
    }

    /**
     * 获取模板详情
     */
    public PromptTemplateResponse getTemplateById(Long id, Long userId) {
        PromptTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(template.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该模板");
        }

        return convertToResponse(template);
    }

    /**
     * 更新模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateResponse updateTemplate(Long id, PromptTemplateUpdateRequest request, Long userId) {
        PromptTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(template.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权修改该模板");
        }

        // 更新字段
        if (request.getTemplateName() != null) {
            template.setTemplateName(request.getTemplateName());
        }
        if (request.getTemplateType() != null) {
            template.setTemplateType(request.getTemplateType());
        }
        if (request.getPromptContent() != null) {
            template.setPromptContent(request.getPromptContent());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getTriggerCondition() != null) {
            template.setTriggerCondition(request.getTriggerCondition());
        }
        if (request.getPriority() != null) {
            template.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }
        template.setUpdatedAt(LocalDateTime.now());

        templateMapper.updateById(template);

        return convertToResponse(template);
    }

    /**
     * 删除模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id, Long userId) {
        PromptTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(template.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权删除该模板");
        }

        templateMapper.deleteById(id);

        // 更新智能体模板数量统计
        int newCount = Math.max(0, agent.getTemplateCount() - 1);
        agent.setTemplateCount(newCount);
        agentMapper.updateById(agent);
    }

    /**
     * 复制模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateResponse copyTemplate(Long id, Long userId) {
        PromptTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(template.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权复制该模板");
        }

        // 创建副本
        PromptTemplate copy = new PromptTemplate();
        copy.setAgentId(template.getAgentId());
        copy.setTemplateName(template.getTemplateName() + " (副本)");
        copy.setTemplateType(template.getTemplateType());
        copy.setPromptContent(template.getPromptContent());
        copy.setDescription(template.getDescription());
        copy.setTriggerCondition(template.getTriggerCondition());
        copy.setPriority(template.getPriority());
        copy.setIsActive(0); // 默认禁用副本
        copy.setUsageCount(0);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());

        templateMapper.insert(copy);

        // 更新智能体模板数量统计
        agent.setTemplateCount(agent.getTemplateCount() + 1);
        agentMapper.updateById(agent);

        return convertToResponse(copy);
    }

    /**
     * 转换为响应DTO
     */
    private PromptTemplateResponse convertToResponse(PromptTemplate template) {
        return PromptTemplateResponse.builder()
            .id(template.getId())
            .agentId(template.getAgentId())
            .templateName(template.getTemplateName())
            .templateType(template.getTemplateType())
            .promptContent(template.getPromptContent())
            .description(template.getDescription())
            .triggerCondition(template.getTriggerCondition())
            .priority(template.getPriority())
            .isActive(template.getIsActive())
            .usageCount(template.getUsageCount())
            .lastUsedAt(template.getLastUsedAt())
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
    }
}

