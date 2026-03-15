package cn.edu.cdu.documind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 异步任务配置
 * 
 * @author DocuMind Team
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置 TransactionTemplate，用于编程式事务管理
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}

