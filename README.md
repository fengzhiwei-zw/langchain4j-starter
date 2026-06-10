# langchain4j-starter

基于 **LangChain4j + Spring Boot + Kotlin** 构建的 AI 应用学习 Demo，集成了对话记忆、RAG 知识库、Function Calling、流式输出、AI 审计等核心功能。

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.2 + Java 21 |
| 框架 | Spring Boot 3.5 |
| AI 框架 | LangChain4j 1.13.1-beta23 |
| LLM 提供商 | 阿里云 DashScope（Qwen 系列） |
| 向量数据库 | Chroma（v2 API） |
| 关系数据库 | MySQL 8 |
| 安全框架 | Spring Security（Session 认证） |
| 文档解析 | Apache Tika |
| 限流 | Bucket4j |

---

## 功能特性

### AI 核心能力
- **多轮对话** — 基于 `MessageWindowChatMemory` 的会话记忆，每个 sessionId 独立维护上下文（最近 10 条）
- **流式输出** — SSE（Server-Sent Events）实时推送 AI 回答，低延迟体验
- **RAG 知识库** — 文档上传 → Apache Tika 解析 → 切片 → Embedding → 存入 Chroma，支持语义检索
- **Function Calling（Tool Use）** — AI 可自主调用以下工具：
  - `WeatherTool` — 查询城市天气、获取系统时间（模拟数据）
  - `UserTool` — 通过自然语言对用户数据库进行增删改查
  - `DocumentTool` — 保存文件、检索文档内容、生成图片、生成并写入代码文件
- **图像生成** — 接入阿里云 Wan2.7 文生图模型，异步任务轮询获取结果

### 工程化特性
- **AI 事件监听** — 通过 `AiServiceListener` 接口记录请求、响应、工具调用、完成事件
- **AI 审计日志** — `MyAiObserver`（ChatModelListener）异步将每次对话的 prompt、response、token 消耗、耗时写入数据库
- **文档去重** — 上传文件时计算 MD5 哈希，以 `{hash}-{segmentIndex}` 为 ID 写入 Chroma，重复上传自动 Upsert
- **数据隔离** — 向量检索时以 `userId` 为 metadata 过滤，确保用户只能查到自己的文档
- **限流保护** — 集成 Bucket4j 令牌桶，防止 AI 接口被滥用
- **用户认证** — Spring Security 表单登录 + Session，BCrypt 密码加密

---

## 项目结构

```
src/main/kotlin/com/feng/langchain4jstarter/
├── config/
│   ├── AiConfig.kt           # AiServices 装配（Assistant、AssistantStream、Chroma、RAG）
│   ├── DashScopeConfig.kt    # DashScope 图像生成 Bean
│   └── SecurityConfig.kt     # Spring Security 配置
├── controller/
│   └── AiController.kt       # REST 接口（/ai/chat、/chatStream、/chromaEmbedding、/image）
├── service/
│   ├── Assistant.kt          # LangChain4j AI Service 接口（同步）
│   ├── AssistantStream.kt    # LangChain4j AI Service 接口（流式）
│   ├── AiService.kt          # 业务层接口
│   └── impl/
│       ├── AiServiceImpl.kt  # 对话 + 流式 + 图像生成实现
│       ├── FileServiceImpl.kt # 文档解析、切片、Embedding、检索
│       └── UserServiceImpl.kt # 用户 CRUD
├── tool/
│   ├── WeatherTool.kt        # 天气 & 时间查询工具
│   ├── UserTool.kt           # 用户数据库操作工具
│   └── DocumentTool.kt       # 文档/图片/代码生成工具
├── listener/
│   ├── AiRequestListener.kt  # 请求事件监听
│   ├── AiResponseListener.kt # 响应事件监听
│   ├── AiToolExecutedListener.kt # 工具调用监听
│   ├── AiCompletedListener.kt    # 完成事件监听
│   └── MyAiObserver.kt       # ChatModelListener（审计日志写库）
└── pojo/
    ├── User.kt               # 用户实体
    └── AiAuditLog.kt         # AI 审计日志实体
```

---

## 快速开始

### 前置依赖

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose

### 1. 启动基础服务

```bash
docker-compose up -d
```

这会启动：
- **Chroma** 向量数据库 → `localhost:8000`
- **MySQL** 关系数据库 → `localhost:3306`（密码 `root`）
- **Redis** → `localhost:6379`

### 2. 配置环境变量

```bash
export DASHSCOPE_API_KEY=your_dashscope_api_key
export MYSQL_PASSWORD=root
export DEFAULT_PASSWORD=your_default_user_password
```

> 阿里云 DashScope API Key 可在 [百炼控制台](https://bailian.console.aliyun.com/) 获取，开通后可免费试用 Qwen 系列模型。

### 3. 创建数据库

```sql
CREATE DATABASE ai_mysql CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> 应用启动时 JPA 会自动建表（`ddl-auto: update`）。

### 4. 启动应用

```bash
mvn spring-boot:run
```

访问 `http://localhost:8080/login.html` 登录后即可使用。

---

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat?sessionId=xxx` | 普通对话（请求体为消息文本） |
| POST | `/ai/chatStream?sessionId=xxx&message=xxx` | 流式对话（SSE） |
| POST | `/ai/chromaEmbedding?sessionId=xxx` | 上传文档写入知识库（multipart/form-data） |
| POST | `/ai/image?message=xxx` | 文生图 |
| POST | `/login` | 登录（username / password） |
| POST | `/logout` | 退出登录 |

---

## 配置说明

核心配置项在 `application.yaml` 中：

```yaml
langchain4j:
  open-ai:
    chat-model:
      model-name: qwen3.6-plus      # 同步对话模型
    streaming-chat-model:
      model-name: qwen3-8b          # 流式对话模型
    embedding-model:
      model-name: text-embedding-v4 # 向量化模型
    image-model:
      model-name: wan2.7-image      # 文生图模型
```

> 所有模型均通过 OpenAI 兼容接口调用 DashScope，替换 `base-url` 和 `model-name` 即可切换为标准 OpenAI 或其他兼容提供商。

---

## 学习要点

本项目覆盖了 LangChain4j 的以下核心概念：

1. **AiServices** — 通过接口注解声明 AI 能力，LangChain4j 自动实现
2. **ChatMemory / ChatMemoryProvider** — 多用户会话隔离的对话记忆管理
3. **@Tool 注解** — 将普通方法暴露为 AI 可调用工具
4. **@ToolMemoryId** — 工具方法中获取当前 memoryId（sessionId/userId）
5. **RAG Pipeline** — 文档加载 → 解析 → 切片 → Embedding → 向量存储 → 检索
6. **EmbeddingStore 过滤** — 基于 metadata 的数据隔离查询
7. **TokenStream** — 流式输出与 SSE 集成
8. **AiServiceListener / ChatModelListener** — 请求/响应事件钩子与审计日志

---

## License

MIT
