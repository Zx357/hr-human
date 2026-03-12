# KAdmin - 人力资源管理系统

## 项目简介

KAdmin 是一个基于 Spring Boot + Vue 3 的人力资源管理系统，包含 PC 端管理后台和移动端 App，提供完整的人事管理、考勤打卡、审批流程等功能。

## 技术栈

### 后端

| 技术            | 版本  | 说明       |
| --------------- | ----- | ---------- |
| Spring Boot     | 3.2.1 | 基础框架   |
| Spring Security | -     | 认证授权   |
| JWT             | -     | Token 鉴权 |
| MyBatis-Plus    | -     | ORM 框架   |
| MySQL           | 8.0+  | 数据库     |
| Redis           | 6.0+  | 缓存       |
| Knife4j         | -     | API 文档   |
| Hutool          | -     | 工具库     |

### PC 前端（frontend）

| 技术                 | 版本 | 说明       |
| -------------------- | ---- | ---------- |
| Vue                  | 3.5  | 前端框架   |
| Vite (rolldown-vite) | 7.x  | 构建工具   |
| Element Plus         | 2.x  | UI 组件库  |
| Pinia                | 3.x  | 状态管理   |
| Vue Router           | 4.x  | 路由       |
| Vue I18n             | 11.x | 国际化     |
| UnoCSS               | -    | 原子化 CSS |
| TypeScript           | -    | 类型系统   |

> 基于 [Soybean Admin](https://github.com/soybeanjs/soybean-admin) 框架开发

### 移动端（uniapp）

| 技术       | 版本  | 说明       |
| ---------- | ----- | ---------- |
| UniApp     | -     | 跨平台框架 |
| Vue        | 2     | 前端框架   |
| uView Plus | 3.7.x | UI 组件库  |
| Vuex       | -     | 状态管理   |

> 支持编译到 H5、微信小程序、Android/iOS App

## 功能模块

### 系统管理

- 用户管理 / 角色管理 / 权限管理
- 菜单管理（支持多语言）
- 字典管理（支持多语言）

### 组织架构

- 公司管理 / 部门管理
- 树形组织结构

### 人事管理

- 员工信息（基本信息、教育经历、工作经历、证书管理）
- 合同管理
- 转正管理 / 调动管理 / 离职管理
- 奖惩管理

### 考勤管理

- 班次管理 / 员工排班
- 日考勤 / 月考勤
- 节假日登记

### 审批管理

- 审批流程配置（多级审批节点）
- 待审批 / 我的申请
- 支持请假、加班、补卡、换休、出差、转正、调动、离职等申请类型

### 移动端功能

- 员工登录（工号 + 密码）
- 首页统计（本月出勤、待处理、已通过）
- 定位打卡（上班/下班）
- 月度考勤日历
- 各类申请提交
- 我的申请列表（可按状态筛选）
- 个人信息查看 / 修改密码

## 项目结构

```
kadmin/
├── backend/                  # Spring Boot 后端
│   └── src/main/
│       ├── java/com/kadmin/
│       │   ├── common/       # 公共模块（Result、BaseEntity）
│       │   ├── config/       # 配置（Security、MyBatis-Plus、CORS）
│       │   ├── controller/   # 控制器
│       │   ├── entity/       # 实体类
│       │   ├── mapper/       # Mapper 接口
│       │   ├── security/     # JWT 认证
│       │   ├── service/      # 业务逻辑
│       │   └── utils/        # 工具类
│       └── resources/
│           ├── mapper/       # MyBatis XML
│           └── application.yml
├── frontend/                 # Vue 3 PC 管理后台
│   └── src/
│       ├── views/            # 页面
│       ├── service/          # API 接口
│       ├── router/           # 路由
│       ├── store/            # 状态管理
│       └── locales/          # 国际化
├── uniapp/                   # UniApp 移动端
│   ├── pages/                # 页面
│   │   ├── index.vue         # 首页
│   │   ├── login.vue         # 登录
│   │   ├── clock/            # 打卡
│   │   ├── attendance/       # 考勤记录
│   │   ├── apply/            # 各类申请
│   │   ├── work/             # 工作台
│   │   └── mine/             # 我的
│   ├── api/                  # 接口封装
│   ├── store/                # Vuex 状态管理
│   └── static/               # 静态资源
└── sql/                      # 数据库脚本
    └── kadmin.sql
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- pnpm（前端）
- HBuilderX（移动端，可选）

### 1. 初始化数据库

```sql
CREATE DATABASE kadmin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入 `sql/kadmin.sql`。

### 2. 启动后端

```bash
cd backend
mvn compile
# 使用 IDE 运行 KadminApplication，或：
mvn org.springframework.boot:spring-boot-maven-plugin:run
```

默认端口 `8080`，API 前缀 `/api`。

### 3. 启动 PC 前端

```bash
cd frontend
pnpm install
pnpm dev
```

### 4. 启动移动端

使用 HBuilderX 打开 `uniapp/` 目录，运行到浏览器或模拟器。

## 默认账户

| 端      | 账号  | 密码   | 说明                 |
| ------- | ----- | ------ | -------------------- |
| PC 后台 | nhsys | 123456 | 系统管理员           |
| 移动端  | 001   | 123456 | 员工账号（工号登录） |

## License

MIT
