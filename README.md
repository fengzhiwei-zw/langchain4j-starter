# langchain4j-starter

基于 **LangChain4j + Spring Boot + Kotlin** 构建的 AI 应用学习 Demo，涵盖对话记忆、RAG 知识库（含重排序）、Function Calling、流式输出、异步文生图、限流与 AI 审计等一整套工程化实践。

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.2 + Java 21 |
| 框架 | Spring Boot 3.5 |
| AI 框架 | LangChain4j 1.13.1-beta23 |
| LLM 提供商 | 阿里云 DashScope（Qwen 系列） |
| 向量数据库 | Chroma（v2 API） |
| 重排序模型 | DashScope `qwen3-rerank`（自定义 `ScoringModel` 实现） |
| 对话记忆存储 | Redis（`RedisChatMemoryStore`） |
| 关系数据库 | MySQL 8 |
| 安全框架 | Spring Security（Session 认证） |
| 文档解析 | Apache Tika |
| 限流 | Bucket4j |
| 异步任务 | Spring `@Async`（图像生成后台轮询） |

---

## 功能特性

### AI 核心能力
- **多轮对话** — 基于 `MessageWindowChatMemory` + Redis 持久化的会话记忆，每个用户（`userId`）独立维护上下文（最近 10 条），支持多实例部署
- **流式输出** — SSE（Server-Sent Events）实时推送 AI 回答，低延迟体验
- **RAG 知识库（两阶段检索）**
  1. 文档上传 → Apache Tika 解析 → 切片 → Embedding → 存入 Chroma
  2. 检索时先由 `EmbeddingStoreContentRetriever` 向量召回 Top20，再由 `ReRankingContentAggregator` + DashScope `qwen3-rerank` 精排，取 Top5 作为最终上下文
- **Function Calling（Tool Use）** — AI 可自主调用以下工具：
  - `WeatherTool` — 查询城市天气、获取系统时间（模拟数据）
  - `UserTool` — 通过自然语言对用户数据库进行增删改查（含简单的管理员权限校验）
  - `DocumentTool` — 保存文件、检索文档内容、生成图片、生成并写入代码文件
- **图像生成（异步任务模式）** — 接入阿里云 Wan2.7 文生图模型：
  - `POST /ai/image` 提交任务，立即返回 `taskId`
  - 后台 `@Async` 线程池轮询 DashScope 任务状态（最长等待 3 分钟，每 5 秒轮询一次）
  - 前端通过 `GET /ai/imageResult` 轮询获取 `PENDING / SUCCESS / FAILED` 结果
  - 另提供 `POST /ai/image/block` 作为同步阻塞版本用于对比学习

### 工程化特性
- **AI 事件监听** — 通过 `AiServiceListener` 接口记录请求、响应、工具调用、完成、错误等事件（`AiRequestListener`、`AiResponseListener`、`AiToolExecutedListener`、`AiCompletedListener`、`AiErrorListener`）
- **AI 审计日志** — `MyAiObserver`（`ChatModelListener`）异步将每次对话的 prompt、response、Token 消耗、耗时、Tool 调用信息写入数据库
- **文档去重** — 上传文件时计算 MD5 哈希，以 `{hash}-{segmentIndex}` 为 ID 写入 Chroma，重复上传自动 Upsert
- **数据隔离** — 向量检索、对话记忆、图像任务均以 `userId` 隔离，确保用户只能访问自己的数据
- **限流保护** — 集成 Bucket4j 令牌桶（每用户每分钟 5 次），保护 `/ai/chatStream` 等接口不被滥用
- **用户认证** — Spring Security 表单登录 + Session（`JSESSIONID`），BCrypt 密码加密，支持自助注册（`/register`）
- **统一响应与异常处理** — `ApiResponse<T>` 统一返回结构 + `GlobalExceptionHandler` 全局异常处理 + `BusinessException` 业务异常

---

## 项目结构

```
src/main/kotlin/com/feng/langchain4jstarter/
├── config/
│   ├── AiConfig.kt              # AiServices 装配（Assistant、AssistantStream、Chroma、RAG、Redis 记忆）
│   ├── DashScopeConfig.kt       # DashScope 图像生成 SDK Bean
│   ├── DashScopeScoringModel.kt # 自定义 ScoringModel，调用 qwen3-rerank 做二次精排
│   ├── AsyncConfig.kt           # 图像生成异步线程池配置
│   └── SecurityConfig.kt        # Spring Security 配置（表单登录、CORS、权限）
├── controller/
│   ├── AiController.kt          # REST 接口（/ai/chat、/chatStream、/chromaEmbedding、/image 系列）
│   └── LoginController.kt       # 用户注册接口
├── service/
│   ├── Assistant.kt             # LangChain4j AI Service 接口（同步）
│   ├── AssistantStream.kt       # LangChain4j AI Service 接口（流式）
│   ├── AiService.kt             # 业务层接口
│   ├── FileService.kt           # 文档处理业务接口
│   ├── ImageTaskStore.kt        # 图像任务状态存储
│   ├── ImageTaskPollingService.kt # 图像任务后台异步轮询
│   └── impl/
│       ├── AiServiceImpl.kt         # 对话 + 流式 + 图像生成实现
│       ├── FileServiceImpl.kt       # 文档解析、切片、Embedding、检索
│       ├── UserServiceImpl.kt       # 用户 CRUD
│       └── CustomUserDetailsService.kt # Spring Security 用户认证实现
├── tool/
│   ├── WeatherTool.kt           # 天气 & 时间查询工具
│   ├── UserTool.kt              # 用户数据库操作工具（含权限校验）
│   └── DocumentTool.kt          # 文档/图片/代码生成工具
├── listener/
│   ├── AiRequestListener.kt       # 请求事件监听
│   ├── AiResponseListener.kt      # 响应事件监听
│   ├── AiToolExecutedListener.kt  # 工具调用监听
│   ├── AiCompletedListener.kt     # 完成事件监听
│   ├── AiErrorListener.kt         # 错误事件监听
│   └── MyAiObserver.kt            # ChatModelListener（审计日志写库）
├── pojo/
│   ├── User.kt                  # 用户实体
│   ├── AiAuditLog.kt            # AI 审计日志实体
│   └── ImageTask.kt             # 图像生成任务实体（PENDING/SUCCESS/FAILED）
├── repository/                  # Spring Data JPA 仓储
├── dto/                         # 数据传输对象
├── model/                       # 通用响应模型（ApiResponse）
├── exception/                   # 业务异常与全局异常处理
├── constant/                    # 枚举常量
└── util/                        # 工具类（限流、登录用户获取、安全工具）
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
- **Redis** → `localhost:6379`（对话记忆持久化）

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

> 应用启动时 JPA 会自动建表（`ddl-auto: update`）；`doc/sql/init.sql` 中也提供了初始化脚本可参考。

### 4. 调整本地路径配置（可选）

`application.yaml` 中 `ai.generate.code.path` 用于 `DocumentTool` 生成代码文件的落盘路径，按需改成本机可写目录。

### 5. 启动应用

```bash
mvn spring-boot:run
```

访问 `http://localhost:8080/login.html`，注册或登录后即可使用。

---

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/register` | 用户注册 |
| POST | `/login` | 登录（表单参数 username / password） |
| POST | `/logout` | 退出登录 |
| POST | `/ai/chat` | 普通对话（请求体为消息文本，按当前登录用户隔离上下文） |
| POST | `/ai/chatStream?message=xxx` | 流式对话（SSE，受 Bucket4j 限流保护） |
| POST | `/ai/chromaEmbedding` | 上传文档写入知识库（multipart/form-data） |
| POST | `/ai/image?message=xxx` | 提交文生图任务，立即返回 `taskId` |
| GET  | `/ai/imageResult?taskId=xxx` | 轮询查询图像生成结果（PENDING/SUCCESS/FAILED） |
| POST | `/ai/image/block?message=xxx` | 同步阻塞版文生图（学习对比用） |

> 所有 `/ai/**` 接口均需登录后携带 Session Cookie 访问，用户身份通过 `SecurityUtil.userId` 从当前登录态获取，无需再手动传 `sessionId`。

---

## 配置说明

核心配置项在 `application.yaml` 中，分为两部分：

```yaml
# 自定义业务配置，供 DashScopeConfig / DashScopeScoringModel 等手写 Bean 使用
ai:
  dash-scope:
    api-key: ${DASHSCOPE_API_KEY}
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    image-url: https://dashscope.aliyuncs.com/api/v1
    model-name: qwen3.7-plus
    image-model-name: wan2.7-image-pro

# LangChain4j Starter 自动装配的模型配置
langchain4j:
  open-ai:
    chat-model:
      model-name: qwen3.7-plus        # 同步对话模型
    streaming-chat-model:
      model-name: qwen3.7-plus        # 流式对话模型
    embedding-model:
      model-name: text-embedding-v4   # 向量化模型
    image-model:
      model-name: wan2.7-image        # 文生图模型（DocumentTool 中使用）
  community:
    redis:
      host: localhost
      port: 6379
```

> 所有模型均通过 OpenAI 兼容接口调用 DashScope，替换 `base-url` 和 `model-name` 即可切换为标准 OpenAI 或其他兼容提供商。

---

## 学习要点

本项目覆盖了 LangChain4j 的以下核心概念：

1. **AiServices** — 通过接口注解声明 AI 能力，LangChain4j 自动实现
2. **ChatMemory / ChatMemoryProvider** — 多用户会话隔离的对话记忆管理，配合 `RedisChatMemoryStore` 实现可持久化、可水平扩展的记忆存储
3. **@Tool / @ToolMemoryId** — 将普通方法暴露为 AI 可调用工具，并在工具内部获取当前会话/用户身份
4. **RAG Pipeline** — 文档加载 → 解析 → 切片 → Embedding → 向量存储 → 检索
5. **两阶段检索（召回 + 精排）** — `ContentRetriever` 粗召回 + `ContentAggregator` / `ScoringModel` 精排，是生产级 RAG 的常见做法
6. **EmbeddingStore 过滤** — 基于 metadata 的数据隔离查询
7. **TokenStream** — 流式输出与 SSE 集成
8. **AiServiceListener / ChatModelListener** — 请求/响应/工具调用/错误事件钩子与审计日志
9. **异步任务编排** — 用 `@Async` 处理耗时的第三方 API 轮询（文生图），避免阻塞请求线程

---

## License

MIT
