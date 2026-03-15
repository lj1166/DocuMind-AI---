-- =============================================
-- API监控表设计
-- =============================================

USE documind_ai;

-- API调用日志表
CREATE TABLE IF NOT EXISTS `api_call_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT COMMENT '用户ID（未登录为NULL）',
  `endpoint` VARCHAR(200) NOT NULL COMMENT 'API端点（如：/api/chat/stream）',
  `http_method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法（GET/POST/PUT/DELETE）',
  `request_params` TEXT COMMENT '请求参数（JSON格式）',
  `response_status` INT NOT NULL COMMENT 'HTTP状态码（200/400/500等）',
  `response_time` INT NOT NULL COMMENT '响应时间（毫秒）',
  `error_message` TEXT COMMENT '错误信息（失败时记录）',
  `ip_address` VARCHAR(50) COMMENT '客户端IP',
  `user_agent` VARCHAR(500) COMMENT '浏览器UA',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_endpoint` (`endpoint`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_response_status` (`response_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- API统计汇总表（可选，用于提升查询性能）
CREATE TABLE IF NOT EXISTS `api_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `endpoint` VARCHAR(200) NOT NULL COMMENT 'API端点',
  `http_method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
  `date` DATE NOT NULL COMMENT '统计日期',
  `total_calls` INT DEFAULT 0 COMMENT '总调用次数',
  `success_calls` INT DEFAULT 0 COMMENT '成功次数',
  `failed_calls` INT DEFAULT 0 COMMENT '失败次数',
  `avg_response_time` INT DEFAULT 0 COMMENT '平均响应时间（毫秒）',
  `max_response_time` INT DEFAULT 0 COMMENT '最大响应时间（毫秒）',
  `min_response_time` INT DEFAULT 0 COMMENT '最小响应时间（毫秒）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_endpoint_date` (`endpoint`, `http_method`, `date`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API统计汇总表';

SELECT '✅ API监控表创建完成！' AS message;

