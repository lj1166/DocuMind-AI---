package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.entity.PromptTemplate;
import cn.edu.cdu.documind.mapper.PromptTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提示词选择服务
 * 根据用户消息自动选择最合适的提示词模板
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptSelectorService {

    private final PromptTemplateMapper templateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 选择最合适的提示词模板
     */
    public PromptTemplate selectBestTemplate(Long agentId, String userMessage) {
        // 获取该智能体的所有启用的模板
        List<PromptTemplate> templates = templateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getAgentId, agentId)
                .eq(PromptTemplate::getIsActive, 1)
                .orderByDesc(PromptTemplate::getPriority)
        );

        if (templates.isEmpty()) {
            return null;
        }

        // 遍历模板，匹配触发条件
        for (PromptTemplate template : templates) {
            if (matchesTriggerCondition(template, userMessage)) {
                log.info("选中模板: {}, 优先级: {}", template.getTemplateName(), template.getPriority());
                return template;
            }
        }

        // 没有匹配的，返回优先级最高的（默认模板）
        return templates.get(0);
    }

    /**
     * 检查是否匹配触发条件
     */
    private boolean matchesTriggerCondition(PromptTemplate template, String userMessage) {
        String triggerCondition = template.getTriggerCondition();
        if (triggerCondition == null || triggerCondition.isEmpty()) {
            return false;
        }

        try {
            JsonNode condition = objectMapper.readTree(triggerCondition);
            
            // 检查关键词匹配
            if (condition.has("keywords")) {
                JsonNode keywords = condition.get("keywords");
                for (JsonNode keyword : keywords) {
                    if (userMessage.contains(keyword.asText())) {
                        return true;
                    }
                }
            }

            // 检查消息长度
            if (condition.has("message_length")) {
                JsonNode lengthNode = condition.get("message_length");
                int min = lengthNode.has("min") ? lengthNode.get("min").asInt() : 0;
                int max = lengthNode.has("max") ? lengthNode.get("max").asInt() : Integer.MAX_VALUE;
                
                if (userMessage.length() >= min && userMessage.length() <= max) {
                    return true;
                }
            }

            // 检查代码块
            if (condition.has("has_code_block") && condition.get("has_code_block").asBoolean()) {
                if (userMessage.contains("```") || userMessage.contains("代码")) {
                    return true;
                }
            }

        } catch (Exception e) {
            log.warn("解析触发条件失败: {}", e.getMessage());
        }

        return false;
    }
}

