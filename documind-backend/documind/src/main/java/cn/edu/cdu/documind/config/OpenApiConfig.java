package cn.edu.cdu.documind.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置类
 * 
 * @author DocuMind Team
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * 配置 OpenAPI 文档
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("DocuMind AI - API 文档")
                        .description("基于 Spring AI Alibaba 和 Vue 3 的个人专属 RAG 知识库平台")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DocuMind Team")
                                .email("support@documind.ai")
                                .url("https://github.com/documind/documind-ai"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                
                // 服务器列表
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.documind.ai")
                                .description("生产环境")
                ))
                
                // 配置 JWT 认证
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token（格式：Bearer xxx）")))
                
                // 全局应用 JWT 认证
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

