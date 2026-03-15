package cn.edu.cdu.documind.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 
 * @author DocuMind Team
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 配置 MyBatis-Plus 拦截器
     * 包含分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置分页参数
        paginationInterceptor.setMaxLimit(500L);  // 单页最大条数限制
        paginationInterceptor.setOverflow(false);  // 溢出总页数后是否进行处理（默认不处理）
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}

