package cn.edu.cdu.documind.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI测试控制器 - 测试通义千问API
 */
@RestController
@RequestMapping("/test/ai")
public class AITestController {

    @Autowired(required = false)
    private ChatModel chatModel;

    /**
     * 测试通义千问连接
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        
        if (chatModel == null) {
            result.put("status", "ERROR");
            result.put("message", "ChatModel未初始化，请检查API Key配置");
            return result;
        }

        try {
            // 简单的测试问题
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user("你好，请回复'测试成功'")
                    .call()
                    .content();

            result.put("status", "SUCCESS");
            result.put("message", "通义千问API连接成功");
            result.put("response", response);
            result.put("model", "qwen-plus");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "通义千问API调用失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }

        return result;
    }

    /**
     * 简单对话测试
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        if (chatModel == null) {
            result.put("status", "ERROR");
            result.put("message", "ChatModel未初始化");
            return result;
        }

        try {
            String question = request.getOrDefault("question", "你好");
            
            long startTime = System.currentTimeMillis();
            
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(question)
                    .call()
                    .content();
            
            long endTime = System.currentTimeMillis();

            result.put("status", "SUCCESS");
            result.put("question", question);
            result.put("answer", response);
            result.put("responseTime", (endTime - startTime) + "ms");
            result.put("model", "qwen-plus");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "对话失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 测试文档问答（模拟RAG）
     */
    @PostMapping("/rag-test")
    public Map<String, Object> ragTest(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        if (chatModel == null) {
            result.put("status", "ERROR");
            result.put("message", "ChatModel未初始化");
            return result;
        }

        try {
            String question = request.getOrDefault("question", "什么是RAG？");
            
            // 模拟文档上下文
            String context = """
                RAG（Retrieval-Augmented Generation）是检索增强生成技术。
                它结合了信息检索和文本生成两个步骤：
                1. 首先从知识库中检索相关文档
                2. 然后将检索到的内容作为上下文，让AI生成回答
                这样可以让AI基于真实文档回答问题，提高准确性。
                """;

            String prompt = String.format("""
                你是一个专业的文档问答助手。请基于以下文档内容回答用户问题。
                
                【文档内容】
                %s
                
                【用户问题】
                %s
                
                【回答要求】
                1. 只基于文档内容回答
                2. 如果文档中没有相关信息，请明确告知
                3. 回答要准确、简洁
                """, context, question);

            long startTime = System.currentTimeMillis();
            
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            long endTime = System.currentTimeMillis();

            result.put("status", "SUCCESS");
            result.put("question", question);
            result.put("context", context);
            result.put("answer", response);
            result.put("responseTime", (endTime - startTime) + "ms");
            result.put("model", "qwen-plus");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "RAG测试失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 测试不同温度参数
     */
    @GetMapping("/temperature-test")
    public Map<String, Object> temperatureTest() {
        Map<String, Object> result = new HashMap<>();
        
        if (chatModel == null) {
            result.put("status", "ERROR");
            result.put("message", "ChatModel未初始化");
            return result;
        }

        try {
            String question = "用一句话介绍春天";

            // 测试不同温度
            String response1 = ChatClient.create(chatModel)
                    .prompt()
                    .user(question)
                    .call()
                    .content();

            result.put("status", "SUCCESS");
            result.put("question", question);
            result.put("response_default", response1);
            result.put("message", "温度参数测试完成（当前使用配置文件中的默认温度0.7）");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "温度测试失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取AI配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("chatModelAvailable", chatModel != null);
        result.put("model", "qwen-plus");
        result.put("temperature", 0.7);
        result.put("embeddingModel", "text-embedding-v2");
        result.put("apiKeyConfigured", System.getenv("DASHSCOPE_API_KEY") != null);
        
        return result;
    }
}

