# DocuMind AI - 智能文档问答系统

> 基于 RAG（检索增强生成）技术的个性化AI智能体平台

---

## 📖 项目简介

DocuMind AI 是一个基于大语言模型和RAG技术的智能文档问答系统。用户可以创建个性化AI智能体，上传私有文档，系统会自动进行向量化处理，实现基于本地知识库的智能问答。

### 核心特性

- 🤖 **个性化智能体** - 创建专属AI助手，自定义角色和提示词
- 📚 **RAG本地知识库** - 上传文档自动向量化，基于私有知识精准回答
- 💬 **流式对话体验** - 实时流式输出，支持Markdown渲染
- 📊 **Excel数据支持** - 支持Excel表格向量化和检索
- 🎯 **多提示词模板** - 智能匹配场景，自动选择最佳提示词
- 🔒 **数据隔离安全** - 每个智能体的文档独立存储，互不干扰
- 📈 **使用统计分析** - Token消耗、对话统计、API监控

---

## 🛠️ 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.3.5 | 核心框架 |
| Spring AI Alibaba | 1.0.0.2 | AI集成框架 |
| **SimpleVectorStore** | **内置** | **向量数据库（框架内置，无需安装）** ⭐ |
| MyBatis-Plus | 3.5.11 | ORM框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.0 | 缓存和会话 |
| Spring Security | 3.3.5 | 认证授权 |
| JWT | 0.12.5 | Token认证 |
| Apache POI | 5.2.5 | Excel文件处理 |
| 通义千问 | qwen-plus | 大语言模型 |
| text-embedding-v2 | - | 文本向量化模型 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Element Plus | 2.x | UI组件库 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP请求 |
| Marked | 12.x | Markdown渲染 |

---

## 🎯 主要功能

### 1. 智能体管理

- ✅ 创建个性化AI智能体（医疗助手、客服、烹饪专家等）
- ✅ 配置系统提示词、模型参数（temperature、topP、maxTokens）
- ✅ RAG参数配置（Top-K、相似度阈值）
- ✅ 单一提示词/多提示词模板切换

### 2. 文档管理

**支持的文件格式**：
- 📄 PDF
- 📝 Word（.doc, .docx）
- 📊 Excel（.xls, .xlsx）⭐ 新增
- 📃 文本（.txt）
- 📑 Markdown（.md）

**文档处理流程**：
```
上传文档 → 文本提取 → 智能分块 → 向量化（Embedding） 
→ 存储到SimpleVectorStore → 支持RAG检索
```

**特点**：
- Excel自动转Markdown表格格式
- 文档按智能体隔离存储
- 异步向量化，不阻塞上传
- 向量化状态实时跟踪

### 3. 对话功能

- 💬 流式对话（SSE实时输出）
- 🔍 RAG检索增强（基于上传文档回答）
- 📜 历史记录管理
- 🔄 重新生成回答
- 🗑️ 删除单条消息
- 🔎 对话内容搜索
- 📄 显示参考文档来源

### 4. 提示词模板系统

**多场景提示词**：
- 支持一个智能体配置多个提示词模板
- 基于关键词自动匹配场景
- 优先级机制（紧急场景优先级高）

**触发条件**：
```json
{
  "keywords": ["录取分数", "学费"],
  "message_length": {"min": 5, "max": 100}
}
```

### 5. 用户系统

- 🔐 用户注册/登录（JWT认证）
- 👤 个人信息管理
- 🔑 密码修改
- 📊 个人统计数据

### 6. 数据统计

- 📈 智能体使用统计（对话次数、Token消耗）
- 📊 系统总览（用户数、智能体数、文档数）
- 🔍 API调用监控
- 💰 Token消耗趋势

---

## 💡 典型应用场景

### 场景1：智能客服助手
**示例**：智能手环X1客服
- 上传：产品手册、FAQ、售后政策
- 功能：自动回答购买、使用、故障、售后问题
- 优势：24小时在线，准确率高，减少人工成本

### 场景2：个人健康顾问
**示例**：小K的私人医生
- 上传：体检报告、就诊记录、用药指南
- 功能：解读体检报告、用药咨询、健康建议
- 优势：个性化建议，隐私保护

### 场景3：教育信息助手
**示例**：成都大学百事通
- 上传：招生简章、专业介绍、录取分数Excel
- 功能：招生咨询、专业查询、教材查询
- 优势：信息查询快速准确，支持表格数据

### 场景4：生活助手
**示例**：烹饪指导专家
- 上传：食谱、烹饪技巧
- 功能：菜谱查询、做菜指导
- 优势：步骤清晰，个性化推荐

### 场景5：企业知识库
**示例**：技术文档助手、法律顾问
- 上传：技术文档、法律条文
- 功能：快速检索，准确解答
- 优势：提高工作效率

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────┐
│                   用户界面                       │
│           (Vue3 + Element Plus)                  │
└────────────────┬────────────────────────────────┘
                 │ HTTP/SSE
┌────────────────┴────────────────────────────────┐
│              后端服务层                          │
│         (Spring Boot + Spring AI)                │
├──────────────────────────────────────────────────┤
│  智能体管理 │ 文档管理 │ 对话服务 │ 用户管理    │
├──────────────────────────────────────────────────┤
│           RAG检索增强生成引擎                    │
│  ┌─────────────┐    ┌──────────────┐           │
│  │ VectorService│───>│SimpleVector  │           │
│  │  (向量检索)  │    │   Store      │           │
│  └─────────────┘    └──────────────┘           │
│         ↓                    ↑                   │
│  ┌─────────────┐    ┌──────────────┐           │
│  │  ChatService │    │ Document     │           │
│  │  (对话生成)  │    │ Processor    │           │
│  └─────────────┘    └──────────────┘           │
└────────┬──────────────────┬─────────────────────┘
         │                  │
    ┌────┴─────┐      ┌────┴─────┐
    │  MySQL   │      │  Redis   │
    │ (数据库) │      │  (缓存)  │
    └──────────┘      └──────────┘
         │
    ┌────┴────────────────────────────┐
    │      通义千问 API               │
    │  ┌──────────┐  ┌──────────┐   │
    │  │qwen-plus │  │embedding │   │
    │  │ (对话)   │  │ (向量化) │   │
    │  └──────────┘  └──────────┘   │
    └─────────────────────────────────┘
```

---

## 🚀 核心技术实现

### 1. RAG检索增强生成

**工作流程**：
```
用户提问 
  ↓
向量化问题 (Embedding)
  ↓
向量相似度搜索 (SimpleVectorStore)
  ↓
检索Top-K个相关文档片段
  ↓
构建Prompt = 系统提示词 + RAG文档 + 历史对话 + 用户问题
  ↓
调用大模型生成回答 (通义千问)
  ↓
流式返回结果 (SSE)
```

**技术要点**：
- 使用 **SimpleVectorStore** 作为向量数据库（内置于Spring AI，JSON文件存储）
- 文档分块策略：500 tokens/块，50 tokens重叠
- 相似度阈值：0.5（可配置）
- Top-K：10（可配置）
- 数据隔离：通过 metadata.agent_id 实现多租户

### 2. 向量化技术

**Embedding模型**：text-embedding-v2（通义千问）
- 维度：1536维向量
- 支持中英文
- 准确率高

**文档处理**：
- **文本类**（txt、md）：直接提取
- **Excel类**：转Markdown表格格式 ⭐
- **PDF/Word**：文本提取

**向量存储**：
- 格式：JSON文件（`data/vector-store.json`）
- 持久化：自动保存
- 加载：应用启动时自动加载到内存

### 3. 多提示词智能选择

**实现机制**：
- 基于关键词匹配（非向量检索）
- 支持优先级配置
- 自动选择最佳提示词

**示例**：
```
用户问："录取分数是多少？"
→ 匹配关键词 ["录取分数"]
→ 选择"专业录取查询"模板
→ 使用该模板的系统提示词
```

---

## 📊 数据库设计

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户表 | username, password, role |
| `agent` | 智能体表 | name, system_prompt, rag_enabled |
| `document` | 文档表 | file_path, vector_status, chunk_count |
| `chat_session` | 会话表 | session_id, title, message_count |
| `chat_history` | 对话历史 | role, content, tokens |
| `prompt_template` | 提示词模板 | template_name, trigger_condition |
| `api_call_log` | API调用日志 | api_name, response_time, tokens |

### 数据隔离机制

- 用户级隔离：每个用户只能看到自己的智能体
- 智能体级隔离：每个智能体的文档独立存储
- 向量级隔离：通过 `metadata.agent_id` 过滤，确保检索不会跨智能体

---

## 🎨 功能模块

### 1. 用户模块
- 用户注册/登录
- JWT Token认证
- 个人信息管理
- 密码修改

### 2. 智能体模块
- 智能体CRUD操作
- 配置管理（提示词、模型参数、RAG参数）
- 智能体统计（对话次数、Token消耗）
- 智能体分享（规划中）

### 3. 文档模块
- 文档上传（拖拽上传、批量上传）
- 文档列表（分页、搜索、过滤）
- 文档预览
- 文档删除
- 向量化状态跟踪
- 支持格式：PDF、Word、Excel、TXT、Markdown

### 4. 对话模块
- 流式对话（SSE）
- 历史记录管理
- 会话管理（创建、删除、重命名）
- 消息操作（删除、重新生成）
- 对话搜索
- Markdown渲染

### 5. 提示词模板模块
- 模板CRUD
- 触发条件配置
- 优先级管理
- 使用统计

### 6. 统计分析模块
- 系统总览（用户数、智能体数、文档数）
- Token消耗统计
- API调用监控
- 响应时间分析

---

## 📁 项目结构

```
D:\Doumind/
├── documind-backend/          # 后端项目
│   └── documind/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/cn/edu/cdu/documind/
│       │   │   │   ├── config/          # 配置类
│       │   │   │   ├── controller/      # 控制器
│       │   │   │   ├── service/         # 业务逻辑
│       │   │   │   ├── mapper/          # 数据访问
│       │   │   │   ├── entity/          # 实体类
│       │   │   │   ├── dto/             # 数据传输对象
│       │   │   │   ├── reader/          # 文档读取器（含ExcelReader）
│       │   │   │   ├── util/            # 工具类
│       │   │   │   └── DocumindApplication.java
│       │   │   └── resources/
│       │   │       ├── application.yml         # 主配置
│       │   │       └── application-dev.yml     # 开发环境配置
│       │   └── test/                    # 测试代码
│       ├── database/                    # 数据库脚本
│       │   ├── init-v2.sql             # 表结构
│       │   └── api-monitor.sql         # API监控表
│       └── pom.xml                      # Maven依赖
│
├── documind-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/              # API接口
│   │   ├── views/            # 页面组件
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── store/            # 状态管理
│   │   └── utils/            # 工具函数
│   ├── package.json          # 前端依赖
│   └── vite.config.ts        # Vite配置
│
└── 示例文档/                  # 测试用示例文档
    ├── 示例客服文档/         # 智能手环客服
    ├── 示例医疗文档/         # 小K健康档案
    ├── 示例食谱/             # 烹饪食谱
    └── 示例Excel文档/        # 成都大学数据
```

---

## 🚀 快速开始

### 环境要求

- ✅ JDK 17
- ✅ Maven 3.6+
- ✅ Node.js 16+
- ✅ MySQL 8.0
- ✅ Redis 7.0
- ✅ **SimpleVectorStore（内置于框架，无需安装）** ⭐

### 安装步骤

#### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE documind_ai 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 导入表结构
执行 documind-backend/documind/database/init-v2.sql
执行 documind-backend/documind/database/api-monitor.sql
```

#### 2. 配置修改

**文件位置**：`documind-backend/documind/src/main/resources/application-dev.yml`

```yaml
# 1. 通义千问API Key（必改）
spring.ai.dashscope.api-key: sk-你的API密钥

# 2. MySQL配置（必改）
spring.datasource.username: root
spring.datasource.password: 你的密码

# 3. Redis配置（如有密码需改）
spring.data.redis.password: ''
```

**获取API Key**：
- 访问：https://dashscope.console.aliyun.com/apiKey
- 创建API Key并保存
- 开通模型：qwen-plus、text-embedding-v2

#### 3. 启动后端

```bash
cd documind-backend/documind

# 首次启动（Maven会自动下载依赖）
mvnw spring-boot:run

# 或使用IDEA
# 打开项目 → 运行 DocumindApplication
# ⚠️ 工作目录设置为：documind-backend
```

**启动成功标志**：
```
2024-10-12 18:00:00 - Started DocumindApplication
========== 向量存储加载诊断 ==========
向量文件不存在，将创建新的向量存储
```

**访问**：http://localhost:8080

#### 4. 启动前端

```bash
cd documind-frontend

# 安装依赖（首次必须）
npm install

# 启动开发服务器
npm run dev
```

**访问**：http://localhost:8848

**默认账号**：
- 用户名：`admin`
- 密码：`admin123`

---

## 📖 使用指南

### 1. 创建智能体

1. 登录系统 → 智能体管理 → 创建智能体
2. 填写基本信息：
   - 名称、描述、角色名称
   - 系统提示词（定义AI的角色和行为）
3. 配置模型参数：
   - 模型：qwen-plus（推荐）
   - Temperature：0.3-0.7（越低越稳定）
   - Max Tokens：2000
4. 配置RAG参数：
   - 启用RAG：开启
   - Top-K：10（检索10个相关片段）
   - 相似度阈值：0.5（0-1，越高越严格）

### 2. 上传文档

1. 进入智能体 → 文档管理
2. 点击上传或拖拽文件
3. 等待向量化完成（状态：已完成）
4. 可上传的格式：PDF、Word、Excel、TXT、Markdown

### 3. 开始对话

1. 进入智能体 → 开始对话
2. 输入问题，AI会基于上传的文档回答
3. 查看参考文档来源
4. 支持重新生成、删除消息

### 4. 创建提示词模板（可选）

1. 提示词模板管理 → 创建模板
2. 配置触发条件（关键词）
3. 设置优先级
4. 智能体提示词模式改为"多模板"

---

## 🎯 技术亮点

### 1. SimpleVectorStore - 内置向量数据库 ⭐

**特点**：
- ✅ **零配置**：内置于Spring AI框架，无需安装外部数据库
- ✅ **JSON存储**：数据保存在本地JSON文件
- ✅ **自动持久化**：向量化后自动保存
- ✅ **应用启动加载**：自动加载到内存
- ✅ **适合场景**：中小规模应用（<1万条向量）

**对比其他向量数据库**：
| 特性 | SimpleVectorStore | Qdrant/Milvus |
|------|-------------------|---------------|
| 安装部署 | ✅ 无需安装 | ❌ 需要独立部署 |
| 配置复杂度 | ✅ 零配置 | ⚠️ 需要配置 |
| 适用规模 | <1万向量 | 百万级以上 |
| 性能 | 中等 | 高 |
| 学习成本 | ✅ 低 | ⚠️ 高 |

### 2. Excel智能处理

**ExcelReader特性**：
- 自动识别 .xls 和 .xlsx 格式
- 转换为Markdown表格格式
- 保留表格结构，便于AI理解和展示
- 支持多工作表（每个Sheet独立向量化）

**处理示例**：
```
Excel原始数据：
| 专业 | 分数 |
| A   | 580 |

转换为Markdown：
## Sheet1
| 专业 | 分数 |
|------|------|
| A | 580 |

向量化后AI可以：
- 检索表格数据
- 回答时保持表格格式
```

### 3. 流式对话体验

- 使用SSE（Server-Sent Events）技术
- 实时显示AI生成过程
- 降低用户等待感知
- 支持中断生成

### 4. 多提示词智能匹配

- 关键词触发机制
- 优先级排序
- 使用统计跟踪
- 灵活配置

---

## 💾 数据存储说明

### 文件存储位置

```
documind-backend/
└── data/
    ├── uploads/              # 上传的文档
    │   ├── {agentId}/       # 按智能体ID分目录
    │   │   └── xxx_文件名
    └── vector-store.json     # 向量数据（SimpleVectorStore）
```

**vector-store.json 结构**：
```json
{
  "向量ID": {
    "text": "文档分块内容",
    "embedding": [0.123, 0.456, ...],  // 1536维向量
    "metadata": {
      "agent_id": "8",
      "document_id": "13",
      "file_name": "xxx.xlsx"
    }
  }
}
```

---

## 📈 性能指标

### 向量化性能
- 1000字文档：约2-3秒
- 1万字文档：约10-15秒
- Excel文件（100行）：约5-8秒

### RAG检索性能
- 检索速度：<100ms（SimpleVectorStore内存检索）
- Top-10检索：<50ms
- 支持并发检索

### 对话性能
- 首字响应：1-2秒
- 流式输出：实时（几十ms/chunk）
- 平均对话时长：5-10秒

---

## 🔒 安全特性

- ✅ JWT Token认证
- ✅ 密码加密存储（BCrypt）
- ✅ 数据隔离（用户级、智能体级）
- ✅ API请求限流（规划中）
- ✅ SQL注入防护（MyBatis-Plus）
- ✅ XSS防护（前端转义）

---

## 📝 开发团队

**项目名称**：DocuMind AI

**开发团队**：成都大学计算机学院

**技术架构**：Spring Boot + Spring AI + Vue 3


---

## 📞 联系方式

- 项目地址：（填写你的GitHub或Gitee仓库地址）
- 技术文档：（填写文档地址）
- 问题反馈：（填写Issue地址）

---

## 📄 许可证

本项目仅供学习交流使用。

---

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - AI应用框架
- [阿里云百炼](https://www.aliyun.com/product/bailian) - 大模型服务
- [Element Plus](https://element-plus.org/) - Vue3 UI组件库
- [Apache POI](https://poi.apache.org/) - Excel文件处理

---

**⭐ 如果这个项目对你有帮助，欢迎Star！**

