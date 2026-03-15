-- =============================================
-- DocuMind AI 数据库初始化脚本 V2
-- 核心需求：用户自定义智能体 + 历史对话 + 文档集合
-- =============================================

-- 1. 创建数据库
-- DocuMind AI 智能文档问答系统
CREATE DATABASE IF NOT EXISTS documind_ai 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE documind_ai;

-- 3. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `email` VARCHAR(100) COMMENT '邮箱',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 4. 智能体表（核心表 - 用户自定义配置）
CREATE TABLE IF NOT EXISTS `agent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '智能体ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '智能体名称',
  `description` TEXT COMMENT '智能体描述',
  `avatar` VARCHAR(255) COMMENT '智能体头像URL',
  
  -- ⭐ 核心字段：用户自定义配置
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称（用户自定义，如：健康顾问、编程导师）',
  `system_prompt` TEXT NOT NULL COMMENT '系统提示词（默认/基础提示词）',
  
  -- ⭐ 提示词模式配置
  `prompt_mode` VARCHAR(20) DEFAULT 'single' COMMENT '提示词模式: single-单一提示词, multi-多模板, dynamic-动态选择',
  
  -- AI 模型配置
  `model_name` VARCHAR(50) DEFAULT 'qwen-plus' COMMENT 'AI模型名称: qwen-plus/qwen-max/qwen-turbo',
  `temperature` DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数 0-1（控制创造性）',
  `max_tokens` INT DEFAULT 2000 COMMENT '最大输出token数',
  `top_p` DECIMAL(3,2) DEFAULT 0.90 COMMENT 'Top-P采样参数',
  
  -- RAG 配置
  `rag_enabled` TINYINT DEFAULT 1 COMMENT 'RAG检索开关: 0-关闭 1-开启',
  `rag_top_k` INT DEFAULT 10 COMMENT 'RAG检索Top-K数量（优化为10，更全面）',
  `rag_similarity_threshold` DECIMAL(3,2) DEFAULT 0.50 COMMENT 'RAG相似度阈值（调整为0.5更容易匹配）',
  
  -- 统计信息（用于智能体详情页展示）
  `document_count` INT DEFAULT 0 COMMENT '文档数量',
  `chat_count` INT DEFAULT 0 COMMENT '对话次数',
  `session_count` INT DEFAULT 0 COMMENT '会话数量',
  `template_count` INT DEFAULT 0 COMMENT '提示词模板数量',
  `total_tokens` BIGINT DEFAULT 0 COMMENT '累计消耗token数',
  
  -- 时间信息
  `last_chat_at` DATETIME COMMENT '最后对话时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_prompt_mode` (`prompt_mode`),
  KEY `idx_last_chat_at` (`last_chat_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体表';

-- 5. 文档表
CREATE TABLE IF NOT EXISTS `document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `agent_id` BIGINT NOT NULL COMMENT '所属智能体ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型: pdf/doc/docx/txt/md',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `chunk_count` INT DEFAULT 0 COMMENT '分块数量',
  `vector_status` TINYINT DEFAULT 0 COMMENT '向量化状态: 0-待处理 1-处理中 2-已完成 3-失败',
  `vector_error` TEXT COMMENT '向量化错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_vector_status` (`vector_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 6. 会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话唯一标识',
  `agent_id` BIGINT NOT NULL COMMENT '智能体ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) COMMENT '会话标题（自动生成或用户修改）',
  `message_count` INT DEFAULT 0 COMMENT '消息数量',
  `last_message_at` DATETIME COMMENT '最后消息时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_agent_user` (`agent_id`, `user_id`),
  KEY `idx_last_message` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 7. 对话历史表
CREATE TABLE IF NOT EXISTS `chat_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `agent_id` BIGINT NOT NULL COMMENT '智能体ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `sources` JSON COMMENT '来源文档(JSON数组)',
  `tokens` INT COMMENT '消耗的token数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_agent_user` (`agent_id`, `user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- 8. 提示词模板表（⭐ 新增 - 多提示词功能）
CREATE TABLE IF NOT EXISTS `prompt_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `agent_id` BIGINT NOT NULL COMMENT '所属智能体ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称（如：日常咨询、风险评估、报告分析）',
  `template_type` VARCHAR(50) NOT NULL DEFAULT 'scenario' COMMENT '模板类型: default-默认, scenario-场景, task-任务',
  `prompt_content` TEXT NOT NULL COMMENT '提示词内容',
  `description` TEXT COMMENT '模板描述（说明使用场景）',
  
  -- 触发条件配置（JSON 格式）
  `trigger_condition` JSON COMMENT '触发条件配置',
  /* 触发条件示例：
  {
    "keywords": ["急性", "剧烈", "疼痛", "出血"],           // 关键词匹配
    "document_type": "medical_report",                    // 文档类型
    "has_code_block": true,                               // 是否包含代码块
    "message_length": {"min": 100, "max": 1000},         // 消息长度范围
    "time_range": "emergency"                             // 时间范围（紧急/日常）
  }
  */
  
  `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高，匹配多个时选择优先级最高的）',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  
  -- 统计信息
  `usage_count` INT DEFAULT 0 COMMENT '使用次数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_type` (`template_type`),
  KEY `idx_active` (`is_active`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';

-- 9. 文档元数据表（可选 - 用于增强检索）
CREATE TABLE IF NOT EXISTS `document_metadata` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '元数据ID',
  `document_id` BIGINT NOT NULL COMMENT '文档ID',
  `metadata_key` VARCHAR(100) NOT NULL COMMENT '元数据键',
  `metadata_value` TEXT NOT NULL COMMENT '元数据值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_key` (`metadata_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档元数据表';

-- 10. 插入测试用户
-- 密码: test123 (BCrypt 加密后)
INSERT INTO `user` (`username`, `password`, `email`) 
VALUES ('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 11. 插入示例智能体（展示"我的"智能体理念）
INSERT INTO `agent` (`user_id`, `name`, `description`, `role_name`, `system_prompt`, `prompt_mode`, `temperature`) 
VALUES (
  1,
  '我的私人医生',
  '专属健康顾问，解读我的体检报告，提供个性化健康建议',
  '私人医生',
  '你是我的私人医生，名叫小康医生。

你的职责是：
1. 解读我的个人体检报告和医疗检查结果
2. 用通俗易懂的语言解释医学术语和指标
3. 基于我的健康历史提供个性化建议
4. 提醒我需要注意的健康风险
5. 保持温和、专业、耐心的沟通方式

你了解我的情况：
- 你可以查看我上传的所有体检报告和医疗文档
- 你记得我们之前讨论过的健康问题
- 你知道我的健康关注点和生活习惯

重要原则：
⚠️ 所有建议仅供参考，严重问题必须建议及时就医
💡 基于我的专属文档和历史对话，给出个性化的健康建议
🔒 保护我的隐私，所有数据仅我可见',
  'multi',  -- 启用多提示词模式
  0.70
)
ON DUPLICATE KEY UPDATE `name` = `name`;

INSERT INTO `agent` (`user_id`, `name`, `description`, `role_name`, `system_prompt`, `prompt_mode`, `temperature`) 
VALUES (
  1,
  '我的学习助理',
  '专属编程导师，基于我的学习资料提供个性化辅导',
  '学习助理',
  '你是我的专属学习助理，名叫小艾老师。

你的教学理念：
1. 完全了解我的学习进度和知识盲区
2. 基于我上传的课程资料和笔记进行辅导
3. 循序渐进，从我的薄弱点开始
4. 提供针对我的水平的代码示例和练习
5. 记住我们讨论过的每个知识点

你的教学风格：
📚 根据我的学习资料提供精准讲解
💻 提供适合我当前水平的代码示例
🎯 针对我的薄弱点设计练习题
💡 鼓励我独立思考，而不是直接给答案
📝 记录我的学习进度，持续关注我的成长

我的专属知识库：
- 我上传的所有课程资料
- 我的学习笔记和代码
- 我们的历史对话记录

你是我的专属导师，只为我一个人服务！',
  'multi',  -- 启用多提示词模式
  0.80
)
ON DUPLICATE KEY UPDATE `name` = `name`;

INSERT INTO `agent` (`user_id`, `name`, `description`, `role_name`, `system_prompt`, `prompt_mode`, `temperature`) 
VALUES (
  1,
  '我的私人律师',
  '专属法律顾问，解读我的合同和法律文件',
  '私人律师',
  '你是我的私人法律顾问，名叫法律小助手。

你的专业服务：
1. 解读我上传的合同、法律文件和协议
2. 用通俗语言解释法律条文和风险点
3. 基于我的具体情况提供法律建议
4. 提醒我需要注意的法律风险
5. 保持专业、严谨、负责的态度

你了解我的情况：
📄 我上传的所有合同和法律文档
💼 我们讨论过的法律问题
🔍 我关注的法律领域和风险点

服务原则：
⚠️ 所有建议仅供参考，重大决策建议咨询专业律师
💡 基于我的专属文档提供个性化法律建议
🔒 严格保密，保护我的隐私和商业机密

我是你的专属客户，请为我提供最专业的法律咨询！',
  'single',  -- 单一提示词模式
  0.60
)
ON DUPLICATE KEY UPDATE `name` = `name`;

INSERT INTO `agent` (`user_id`, `name`, `description`, `role_name`, `system_prompt`, `prompt_mode`, `temperature`) 
VALUES (
  1,
  '我的智能客服',
  '专属客服机器人，基于我的产品文档回答客户问题',
  '智能客服',
  '你是我的专属智能客服，名叫小智。

你的服务范围：
1. 基于我上传的产品手册、FAQ等文档回答客户问题
2. 提供准确、友好、高效的客户服务
3. 记住常见问题的标准答案
4. 识别客户情绪，提供人性化服务
5. 无法解答时，建议转人工客服

你的知识库：
📚 我上传的产品说明书、使用手册
❓ 常见问题FAQ文档
📝 客户服务话术和标准回复
🔧 技术支持文档

服务标准：
✅ 友好热情，始终保持专业态度
✅ 快速准确，基于文档提供精准答案
✅ 主动服务，预判客户需求
✅ 升级机制，复杂问题转人工

你是我的专属客服团队，代表我的品牌形象！',
  'single',  -- 单一提示词模式
  0.70
)
ON DUPLICATE KEY UPDATE `name` = `name`;

-- 12. 插入示例提示词模板（健康顾问智能体）
INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  1,
  '日常健康咨询',
  'scenario',
  '你是友好的健康顾问小康，用轻松、温暖的语气回答日常健康问题。

你的特点：
- 使用通俗易懂的语言，避免过多专业术语
- 保持轻松友好的沟通风格
- 给出实用的日常健康建议
- 鼓励健康的生活方式

回答时：
1. 先理解用户的具体情况
2. 给出清晰的建议
3. 提供简单易行的改善方法
4. 保持积极正面的态度',
  '用于日常健康咨询，如饮食、运动、作息等问题',
  '{"keywords": ["怎么", "如何", "建议", "平时", "日常", "吃什么", "运动"]}',
  1
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  1,
  '紧急风险评估',
  'scenario',
  '你是专业的医疗风险评估助手，必须以严谨、谨慎的态度判断症状的严重性。

⚠️ 核心原则：
- 对任何可能的紧急情况，立即建议就医
- 使用专业但清晰的语言说明风险
- 不要低估症状的严重性
- 明确告知可能的危险信号

评估流程：
1. 识别关键症状和危险信号
2. 评估紧急程度（轻度/中度/重度/紧急）
3. 给出明确的就医建议
4. 说明可能的风险和后果

记住：生命安全永远是第一位的！',
  '用于评估紧急或严重症状，如剧烈疼痛、急性症状等',
  '{"keywords": ["急性", "剧烈", "突然", "疼痛", "出血", "昏迷", "呼吸困难", "胸痛", "严重"]}',
  10
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  1,
  '体检报告分析',
  'scenario',
  '你是专业的体检报告分析专家，擅长解读各类医学检查报告。

分析要求：
1. 对比参考范围，标注异常指标
2. 如果有历史数据，进行趋势对比
3. 用通俗语言解释医学术语和指标含义
4. 分析异常指标的可能原因
5. 给出针对性的改善建议

报告解读结构：
📊 整体评估：总体健康状况
🔴 异常指标：详细分析每个异常项
📈 趋势分析：与历史数据对比（如有）
💡 改善建议：具体可行的改善措施
⚠️ 就医建议：是否需要进一步检查

保持专业、客观、准确。',
  '用于分析体检报告、血常规、生化检查等医学报告',
  '{"keywords": ["报告", "检查结果", "体检", "血常规", "生化", "指标", "偏高", "偏低", "异常"]}',
  5
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

-- 13. 插入示例提示词模板（编程导师智能体）
INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  2,
  '概念讲解',
  'task',
  '你是编程概念讲解专家 CodeMaster，擅长用通俗易懂的方式解释复杂的编程概念。

教学方法：
1. 使用生活中的类比和比喻
2. 从简单到复杂，循序渐进
3. 配合图解和示意（用文字描述）
4. 提供简单的代码示例
5. 总结关键要点

讲解结构：
💡 概念定义：用一句话说清楚是什么
🌰 生活类比：用熟悉的事物类比
📝 详细解释：深入讲解原理
💻 代码示例：简单易懂的例子
🎯 关键要点：总结核心内容
🤔 思考练习：帮助理解的小问题

保持耐心、鼓励、循循善诱的教学风格。',
  '用于解释编程概念、原理、术语等',
  '{"keywords": ["是什么", "什么是", "原理", "概念", "理解", "解释", "含义"]}',
  1
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  2,
  '代码调试',
  'task',
  '你是代码调试专家 CodeMaster，擅长分析和解决代码问题。

调试流程：
1. 理解代码意图和预期行为
2. 定位问题所在（语法/逻辑/运行时）
3. 分析错误原因
4. 提供修复方案
5. 解释为什么会出错

回答结构：
🔍 问题诊断：错误类型和位置
💡 原因分析：为什么会出现这个问题
✅ 修复方案：如何修复（提供修改后的代码）
📚 知识点：相关的编程知识
⚠️ 注意事项：如何避免类似错误

不要直接给答案，要引导思考，培养调试能力。',
  '用于调试代码错误、分析 bug、解决报错等',
  '{"keywords": ["报错", "错误", "bug", "不工作", "不运行", "调试", "问题", "异常"]}',
  5
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

INSERT INTO `prompt_template` (`agent_id`, `template_name`, `template_type`, `prompt_content`, `description`, `trigger_condition`, `priority`) 
VALUES (
  2,
  '代码审查',
  'task',
  '你是代码审查专家 CodeMaster，从多个维度评估代码质量。

审查维度：
1. 正确性：代码是否正确实现功能
2. 可读性：代码是否清晰易懂
3. 性能：是否有性能问题
4. 最佳实践：是否符合 Python 规范
5. 安全性：是否存在安全隐患

审查报告结构：
✅ 优点：代码做得好的地方
⚠️ 改进建议：需要改进的地方
🔧 重构建议：如何重构更好
📖 最佳实践：推荐的写法
💯 评分：各维度评分

保持客观、建设性的反馈，鼓励优点，指出不足。',
  '用于代码审查、代码优化、重构建议等',
  '{"keywords": ["审查", "优化", "改进", "重构", "更好", "规范", "最佳实践"], "has_code_block": true}',
  3
)
ON DUPLICATE KEY UPDATE `template_name` = `template_name`;

-- 完成
SELECT '========================================' AS '';
SELECT '数据库初始化完成！' AS message;
SELECT '========================================' AS '';
SELECT '✅ 已创建 7 张核心表：' AS '';
SELECT '   1. user - 用户表' AS '';
SELECT '   2. agent - 智能体表（支持多提示词模式 + RAG配置）' AS '';
SELECT '   3. document - 文档表（支持向量化状态追踪）' AS '';
SELECT '   4. chat_session - 会话表' AS '';
SELECT '   5. chat_history - 对话历史表（支持来源标注）' AS '';
SELECT '   6. prompt_template - 提示词模板表 ⭐' AS '';
SELECT '   7. document_metadata - 文档元数据表（可选）' AS '';
SELECT '========================================' AS '';
SELECT '✅ 已创建测试数据（展示"我的"智能体理念）：' AS '';
SELECT '   用户: test / test123' AS '';
SELECT '   智能体1: 我的私人医生（专属健康顾问）' AS '';
SELECT '   智能体2: 我的学习助理（专属编程导师）' AS '';
SELECT '   智能体3: 我的私人律师（专属法律顾问）' AS '';
SELECT '   智能体4: 我的智能客服（专属客服机器人）' AS '';
SELECT '   提示词模板: 6个示例模板' AS '';
SELECT '========================================' AS '';
SELECT '🎯 核心特点：' AS '';
SELECT '   ✅ 完全个性化 - 用户自定义智能体' AS '';
SELECT '   ✅ 专属知识库 - 上传私人文档' AS '';
SELECT '   ✅ 数据隔离 - agent_id核心设计' AS '';
SELECT '   ✅ RAG检索 - 向量相似度搜索' AS '';
SELECT '   ✅ 多提示词 - 智能场景切换' AS '';
SELECT '========================================' AS '';

