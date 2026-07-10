# 普法知识网 - 后端

## 项目简介

基于 **Spring Boot 3.2 + MyBatis Plus** 构建的在线法律法规检索平台后端，提供法规查询、AI 法律咨询、用户权限管理等 RESTful API。

- **前端项目**：Vue 3 + Element Plus，见 `../frontend/`
- **数据库**：PostgreSQL 16

---

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 基础框架 | Spring Boot | 3.2.7 |
| ORM | MyBatis Plus | 3.5.7 |
| 安全认证 | Spring Security + JWT（jjwt） | 0.12.6 |
| 数据库 | PostgreSQL | 16 |
| 日志 | Log4j2（SLF4J 门面） | — |
| PDF 导出 | OpenPDF | 1.3.43 |
| AI 对话 | DeepSeek API（SSE 流式） | v1 |
| 线程上下文透传 | Alibaba TTL | 2.14.5 |
| 参数校验 | Jakarta Validation | — |
| AOP | Spring AOP（入参/出参日志） | — |

---

## 项目结构

```
src/main/java/com/legalknowledge/
│
├── LegalKnowledgeApplication.java    # 启动类
│
├── config/                           # Spring 配置
│   ├── SecurityConfig.java           # 安全：JWT 无状态 + 角色权限
│   ├── CorsConfig.java               # 跨域
│   └── MyBatisPlusConfig.java        # MyBatis Plus 配置 + 自动填充
│
├── entity/                           # 数据库实体
│   ├── User.java                     # 用户（id/username/email/password/role）
│   └── LegalDocument.java            # 法规（title/文号/发布机关/全文/分类…）
│
├── mapper/                           # MyBatis Mapper（替代 JPA Repository）
│   ├── UserMapper.java
│   └── LegalDocumentMapper.java
│
├── service/                          # 业务逻辑
│   ├── AuthService.java              # 注册/登录/Token 刷新
│   ├── LegalDocumentService.java     # 法规查询/CRUD
│   ├── UserService.java              # 用户列表/角色修改
│   ├── ChatService.java              # DeepSeek AI 对话（SSE 流）
│   └── PdfService.java               # 法规 PDF 生成
│
├── controller/                       # REST 接口
│   ├── AuthController.java           # /api/auth/*
│   ├── LegalDocumentController.java  # /api/documents/*
│   ├── ChatController.java           # /api/chat（SSE）
│   └── AdminController.java          # /api/admin/*（管理员）
│
├── dto/
│   ├── request/                      # 请求体
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── ChatRequest.java
│   └── response/                     # 响应体
│       ├── AuthResponse.java
│       ├── LegalDocumentDTO.java
│       └── UserInfoDTO.java
│
├── security/                         # 安全相关
│   ├── JwtTokenProvider.java         # JWT 签发/验证
│   └── JwtAuthenticationFilter.java  # 从请求头提取 Token 设置上下文
│
└── common/
    ├── aop/
    │   └── ControllerLogAspect.java   # 切面日志：入参/出参/耗时
    ├── filter/
    │   ├── TraceIdFilter.java         # 请求入口生成 traceId 放入 MDC
    │   └── TtlMdcAdapter.java         # TTL 桥接 Log4j2 的 MDC
    └── runner/
        └── AdminInitializer.java      # 首次启动创建 admin + 种子法规
```

---

## 核心特性

### 认证与权限
- JWT 双 Token 机制：Access Token（30 分钟）+ Refresh Token（7 天）
- 用户角色：**USER**（普通用户）/ **ADMIN**（管理员）
- 前端请求 401 时自动静默刷新 Token，用户无感知
- `/api/admin/**` 需要 `ADMIN` 角色，由 Spring Security 拦截

### 日志体系
- Log4j2 框架，SLF4J 门面
- **traceId 全链路追踪**：Filter 生成 UUID → MDC → Log4j2 自动打印
- 子线程透传：TTL + TtlMdcAdapter 确保 `new Thread()` 也有 traceId
- SQL 日志独立输出到 `sql.log`，不混在主日志里
- AOP 切面自动记录每个 Controller 的入参、出参、耗时

### AI 法律咨询
- 调用 DeepSeek API，SSE 流式返回（逐字显示）
- 系统 Prompt 约束法律场景，防止编造法条
- API 格式兼容 OpenAI，可切换为通义千问 / Claude 等

### PDF 导出
- 法规详情支持一键下载 PDF
- 中文无乱码（STSong-Light 字体）

### 种子数据
- 首次启动自动创建管理员 `admin / admin123`
- 自动导入 5 条劳动法种子数据（劳动法、劳动合同法、调解仲裁法、工伤保险条例、带薪年休假条例）

---

## API 接口

### 认证（公开）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 注册 USER 角色账号 |
| POST | `/api/auth/login` | 登录，返回双 Token |
| POST | `/api/auth/refresh` | 用 Refresh Token 换新 Access Token |

### 法规（公开 · GET）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/documents?keyword=劳动&category=劳动法` | 标题搜索 + 分类筛选 |
| GET | `/api/documents/categories` | 获取所有法规分类 |
| GET | `/api/documents/{id}` | 法规详情 |
| GET | `/api/documents/{id}/pdf` | 下载 PDF |

### AI 咨询（公开 · SSE 流式）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat` | AI 法律问答，SSE 流式返回 |

### 后台管理（需要 ADMIN 角色）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/role` | 修改用户角色 `{"role":"ADMIN"}` |
| POST | `/api/admin/documents` | 新增法规 |
| PUT | `/api/admin/documents/{id}` | 编辑法规 |
| DELETE | `/api/admin/documents/{id}` | 删除法规 |

---

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 16

### 步骤

```bash
# 1. 创建数据库
psql -U postgres -c "CREATE DATABASE legal_knowledge;"

# 2. 修改数据库密码（application.yml 第 7 行）
#      username: postgres
#      password: 你的密码

# 3. 修改 DeepSeek API Key（application.yml 第 34 行）
#      api-key: sk-你的Key

# 4. 启动
cd backend
mvn spring-boot:run
```

启动成功后访问 `http://localhost:8080`，前端项目访问 `http://localhost:5173`。
