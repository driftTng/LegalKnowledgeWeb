# 普法知识网 - 前端

## 项目简介

基于 **Vue 3 + Element Plus + Vite** 构建的在线法律法规检索平台前端，提供法规搜索浏览、AI 法律咨询、后台管理等功能。

- **后端项目**：Spring Boot 3.2 + MyBatis Plus，见 `../backend/`
- **Node 版本**：v18+（推荐 v20）

---

## 技术栈

| 类别 | 技术 | 版本 |
|---|---|---|
| 框架 | Vue 3（Composition API + `<script setup>`） | ^3.4 |
| 构建工具 | Vite | ^5.3 |
| 路由 | Vue Router（懒加载） | ^4.4 |
| 状态管理 | Pinia | ^2.1 |
| UI 组件库 | Element Plus（中文语言包） | ^2.7 |
| HTTP 客户端 | Axios（拦截器 + Token 自动刷新） | ^1.7 |
| AI 流式对话 | Fetch API + ReadableStream（SSE） | 浏览器原生 |

---

## 项目结构

```
src/
├── main.js                    # 入口：挂载 Vue/Pinia/Router/Element Plus
├── App.vue                    # 根组件（<router-view />）
│
├── router/
│   └── index.js               # 路由表 + 管理员路由守卫
│
├── stores/
│   └── user.js                # 用户状态：登录/角色/Token（localStorage 持久化）
│
├── api/                       # 后端接口封装
│   ├── request.js             # Axios 实例 + 请求自动带 Token + 401 自动刷新
│   ├── auth.js                # 登录/注册/刷新 Token
│   ├── document.js            # 法规列表/详情/分类/PDF 下载
│   ├── admin.js               # 后台：法规 CRUD + 用户权限管理
│   └── (chat 接口直接 fetch，不走 Axios，因为要读 SSE 流)
│
├── components/
│   └── Layout.vue             # 公共布局：顶部导航 + 主内容区 + 底部
│
├── views/
│   ├── Home.vue               # 首页：搜索框 + 分类标签 + 法规卡片列表
│   ├── DocumentDetail.vue     # 法规详情：元信息 + 正文 + PDF 下载 + 语音朗读
│   ├── Chat.vue               # AI 法律咨询：SSE 流式对话 + 打字效果
│   ├── Login.vue              # 登录页
│   ├── Register.vue           # 注册页
│   └── admin/                 # 后台管理页面（仅管理员可访问）
│       ├── AdminLayout.vue    # 后台布局：侧边菜单 + 主内容
│       ├── DocumentManage.vue # 法规管理：列表/搜索/编辑/删除
│       ├── DocumentEdit.vue   # 新增/编辑法规表单
│       └── UserManage.vue     # 用户权限管理：列表/设为管理员
│
└── assets/                    # 静态资源（暂无，样式在组件内 scoped）
```

---

## 核心特性

### 路由与权限

```
路由守卫（router/index.js）

  用户访问 /admin/* 页面
    │
    ├── 没登录 → 跳转 /login
    ├── 登录了但不是 ADMIN → 跳回首页 /
    └── 是 ADMIN → 放行
```

- 导航栏根据 `userStore.isAdmin` 控制「后台管理」按钮显隐
- 所有路由懒加载（`() => import(...)`），按页面拆分 chunk

### Token 自动刷新（request.js）

```
请求 → 自动带 Authorization: Bearer <accessToken>
  │
  ├─ 正常返回 → onFulfilled 直接过
  │
  └─ 401 → onRejected
       │
       ├─ 用 refreshToken 调 /api/auth/refresh 换新 Token
       ├─ 更新 localStorage + Pinia
       ├─ 重放之前失败的请求
       └─ 同时有多个 401 → 排队复用同一个刷新请求，不并发调
```

用户完全无感知，不会因为 Token 过期而丢数据。

### AI 对话流式渲染（Chat.vue）

- 用 Fetch API 读 `ReadableStream`，解析 SSE `data:` 事件
- 调用 DeepSeek API，逐字追加到聊天气泡
- 支持打字动画、暂停/继续/停止 → **但注意**：目前暂停仅视觉，实际网络连接未断开
- 支持语速调节（Web Speech API 朗读，0.5x ~ 2x）
- 预置示例问题，点击即可提问

### 语音朗读（DocumentDetail.vue）

- 浏览器原生 Web Speech API，零依赖
- 逐段朗读，当前段落高亮（蓝色左边框）
- 底部固定控制条：播放/暂停/继续/停止 + 语速滑块
- 语法简单，断点明确，不依赖后端

### 法规搜索与分类

- 首页支持标题关键词模糊搜索 + 分类标签筛选
- 搜索和分类可组合使用
- 法规列表展示：标题/文号/发布机关/发布日期/时效性/摘要

---

## 页面路由

| 路径 | 页面 | 说明 |
|---|---|---|
| `/` | Home | 法规检索首页 |
| `/login` | Login | 登录页 |
| `/register` | Register | 注册页 |
| `/document/:id` | DocumentDetail | 法规详情 + PDF 下载 + 朗读 |
| `/chat` | Chat | AI 法律咨询 |
| `/admin` | DocumentManage | 后台·法规管理（管理员） |
| `/admin/edit` | DocumentEdit | 后台·新增法规（管理员） |
| `/admin/edit/:id` | DocumentEdit | 后台·编辑法规（管理员） |
| `/admin/users` | UserManage | 后台·用户权限管理（管理员） |

---

## 本地开发

### 环境要求

- Node.js ≥ 18
- npm ≥ 9

### 启动

```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器，默认 http://localhost:5173
```

Vite 已配置代理：`/api/**` → `http://localhost:8080`，开发时无需额外配置跨域。

### 构建

```bash
npm run build        # 输出到 dist/
npm run preview      # 本地预览构建结果
```

### 依赖

| 依赖 | 用途 |
|---|---|
| `vue` | 框架 |
| `vue-router` | 前端路由 |
| `pinia` | 状态管理 |
| `element-plus` | UI 组件库 |
| `@element-plus/icons-vue` | 图标库 |
| `axios` | HTTP 客户端 |
| `vite` | 构建工具 |
| `@vitejs/plugin-vue` | Vite 对 Vue 的 SFC 编译 |
