package cn.edu.cdu.documind.config;

import cn.edu.cdu.documind.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 * 
 * @author DocuMind Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 配置 Security 过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（使用JWT，不需要CSRF保护）
                .csrf(AbstractHttpConfigurer::disable)
                
                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（无需认证）
                        .requestMatchers(
                                "/api/auth/**",          // 认证接口
                                "/api/test/**",          // 测试接口
                                "/api/chat/**",          // 聊天接口（简化：暂时公开，实际应该验证token）
                                "/api/get-async-routes", // 路由接口
                                "/auth/**",              // 兼容无前缀
                                "/test/**",              // 兼容无前缀
                                "/get-async-routes",     // 路由接口（兼容）
                                "/h2-console/**",        // H2控制台
                                "/error",                // 错误页面
                                // Swagger/OpenAPI 相关路径
                                "/swagger-ui/**",        // Swagger UI 资源
                                "/swagger-ui.html",      // Swagger UI 页面
                                "/v3/api-docs/**",       // OpenAPI 文档
                                "/swagger-resources/**", // Swagger 资源
                                "/webjars/**"            // WebJars 资源
                        ).permitAll()
                        
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                
                // 配置会话管理（使用JWT，无状态）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                // 允许 H2 控制台的 frame（开发环境）
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );
        
        return http.build();
    }
    
    /**
     * 配置 CORS（跨域）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（开发环境允许所有，生产环境需要指定）
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允许携带认证信息（如Cookie）
        configuration.setAllowCredentials(true);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * 密码加密器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

