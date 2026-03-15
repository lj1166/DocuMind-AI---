package cn.edu.cdu.documind.aspect;

import cn.edu.cdu.documind.entity.ApiCallLog;
import cn.edu.cdu.documind.mapper.ApiCallLogMapper;
import cn.edu.cdu.documind.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API监控切面 - 记录所有API调用
 * 
 * @author DocuMind Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiMonitorAspect {

    private final ApiCallLogMapper apiCallLogMapper;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    /**
     * 拦截所有Controller方法
     */
    @Around("execution(* cn.edu.cdu.documind.controller..*(..))")
    public Object monitorApi(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        
        // 跳过一些不需要监控的端点
        if (shouldSkip(endpoint)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        int responseStatus = 200;
        String errorMessage = null;
        Object result = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            responseStatus = 500;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // 计算响应时间
            long endTime = System.currentTimeMillis();
            int responseTime = (int) (endTime - startTime);

            // ✅ 在主线程中提前提取所有需要的数据，避免异步线程中访问失效的上下文
            String requestParams = getRequestParams(request);
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 500) {
                userAgent = userAgent.substring(0, 500);
            }
            
            // ✅ 提前获取用户ID（在主线程中，SecurityContext 可用）
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())
                    && authentication.getPrincipal() instanceof CustomUserDetails) {
                    userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
                }
            } catch (Exception e) {
                // 忽略获取用户信息的异常
            }

            // ✅ 传递提取的数据，而不是 request 对象
            saveApiLog(endpoint, method, responseStatus, responseTime, errorMessage,
                       requestParams, ipAddress, userAgent, userId);
        }
    }

    /**
     * 异步保存API调用日志
     * ✅ 只接收基本类型和不可变对象，不接收 HttpServletRequest 或依赖线程上下文的对象
     */
    @Async
    public void saveApiLog(String endpoint, String method, int responseStatus, 
                          int responseTime, String errorMessage,
                          String requestParams, String ipAddress, 
                          String userAgent, Long userId) {
        // 使用编程式事务管理，简洁优雅
        transactionTemplate.execute(status -> {
            try {
                // 构建并保存日志（所有数据已在主线程中提取）
                ApiCallLog log = ApiCallLog.builder()
                        .userId(userId)
                        .endpoint(endpoint)
                        .httpMethod(method)
                        .requestParams(requestParams)
                        .responseStatus(responseStatus)
                        .responseTime(responseTime)
                        .errorMessage(errorMessage)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .createdAt(LocalDateTime.now())
                        .build();

                apiCallLogMapper.insert(log);
                return null;
            } catch (Exception e) {
                // 记录日志失败不影响主流程
                log.error("保存API调用日志失败: {}", e.getMessage());
                // 返回 null 表示成功（即使失败也不回滚，因为日志记录是非关键操作）
                return null;
            }
        });
    }

    /**
     * 是否跳过监控
     */
    private boolean shouldSkip(String endpoint) {
        // 跳过测试接口、健康检查等
        return endpoint.contains("/test/") 
            || endpoint.contains("/health")
            || endpoint.contains("/swagger")
            || endpoint.contains("/api-docs");
    }

    /**
     * 获取请求参数（JSON格式）
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap.isEmpty()) {
                return null;
            }

            Map<String, Object> params = new HashMap<>();
            parameterMap.forEach((key, value) -> {
                if (value.length == 1) {
                    params.put(key, value[0]);
                } else {
                    params.put(key, value);
                }
            });

            String json = objectMapper.writeValueAsString(params);
            // 限制长度
            return json.length() > 1000 ? json.substring(0, 1000) : json;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

