package cn.edu.cdu.documind.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证系统各组件是否正常工作
 */
@Tag(name = "0. 系统测试", description = "系统健康检查和组件连接测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "DocuMind AI is running!");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 测试MySQL连接
     */
    @Operation(summary = "测试MySQL连接", description = "验证MySQL数据库连接是否正常")
    @GetMapping("/mysql")
    public Map<String, Object> testMySQL() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            result.put("status", "SUCCESS");
            result.put("database", conn.getCatalog());
            result.put("url", conn.getMetaData().getURL());
            result.put("message", "MySQL连接成功");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "MySQL连接失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 测试Redis连接
     */
    @Operation(summary = "测试Redis连接", description = "验证Redis缓存连接是否正常")
    @GetMapping("/redis")
    public Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (redisTemplate == null) {
                result.put("status", "DISABLED");
                result.put("message", "Redis未配置或已禁用");
                return result;
            }

            // 写入测试数据
            String key = "documind:test:timestamp";
            String value = String.valueOf(System.currentTimeMillis());
            redisTemplate.opsForValue().set(key, value);

            // 读取测试数据
            String readValue = redisTemplate.opsForValue().get(key);

            // 删除测试数据
            redisTemplate.delete(key);

            result.put("status", "SUCCESS");
            result.put("message", "Redis连接成功");
            result.put("writeValue", value);
            result.put("readValue", readValue);
            result.put("dataMatch", value.equals(readValue));
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Redis连接失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 系统信息
     */
    @Operation(summary = "系统信息", description = "获取系统基本信息和运行环境")
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", "DocuMind AI");
        result.put("version", "1.0.0");
        result.put("description", "智能文档问答助手");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("osName", System.getProperty("os.name"));
        result.put("osVersion", System.getProperty("os.version"));
        return result;
    }
}

