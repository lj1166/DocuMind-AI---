package cn.edu.cdu.documind.security;

import cn.edu.cdu.documind.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 拦截所有请求，验证 JWT Token
 * 
 * @author DocuMind Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 从请求头中获取 Token
        final String authHeader = request.getHeader("Authorization");
        
        // 如果没有 Token 或格式不正确，继续过滤链
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 提取 Token
            final String jwt = authHeader.substring(7);
            
            // 从 Token 中提取用户名和userId
            final String username = jwtUtil.extractUsername(jwt);
            final Long userId = jwtUtil.extractUserId(jwt); // ← 直接从JWT提取
            
            // 如果用户名不为空且当前未认证
            if (username != null && userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 验证 Token
                if (jwtUtil.validateToken(jwt, username)) {
                    
                    // 直接创建CustomUserDetails，无需查询数据库！
                    CustomUserDetails userDetails = new CustomUserDetails(
                            userId,
                            username,
                            "", // 密码不需要，因为已通过JWT验证
                            java.util.Collections.emptyList() // 权限可以从JWT提取或简化处理
                    );
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 设置详情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 将认证对象设置到 Security Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token 无效或过期，不设置认证信息
            logger.error("JWT Token validation failed: " + e.getMessage());
        }
        
        // 继续过滤链
        filterChain.doFilter(request, response);
    }
}

