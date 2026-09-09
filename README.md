<div align="center">

# KAdmin 人资管理系统

基于 Spring Boot 3 + Vue 3 + UniApp 的企业人力资源管理系统

**一套系统,两端覆盖:PC 后台管理 + 移动端(H5 / 微信小程序)**

组织架构 · 人事管理 · 考勤打卡 · 审批流程 · 即时聊天

</div>

---

## 界面展示

### 后台管理端(PC)

| 仪表盘 | 员工管理 |
| --- | --- |
| ![后台仪表盘](docs/screenshots/admin-dashboard.png) | ![员工管理](docs/screenshots/admin-employee.png) |

| 考勤管理 | 用户管理 |
| --- | --- |
| ![考勤管理](docs/screenshots/admin-attendance.png) | ![用户管理](docs/screenshots/admin-user.png) |

| 移动端菜单配置 | 登录页 |
| --- | --- |
| ![移动端菜单配置](docs/screenshots/admin-mobile-menu.png) | ![后台登录](docs/screenshots/admin-login.png) |

### 移动端(H5 / 微信小程序)

| 登录 | 首页 |
| --- | --- |
| ![登录](docs/screenshots/login.png) | ![首页](docs/screenshots/home.png) |

| 时光动态 | 工作台 |
| --- | --- |
| ![时光](docs/screenshots/moment.png) | ![工作台](docs/screenshots/work.png) |

| 通讯录 | 组织架构 |
| --- | --- |
| ![通讯录](docs/screenshots/contact.png) | ![组织架构](docs/screenshots/contact-org.png) |

| 即时聊天(群聊/单聊) | 同事详情 |
| --- | --- |
| ![聊天](docs/screenshots/chat-group.png) | ![同事详情](docs/screenshots/user-detail.png) |

| 考勤打卡 | 考勤日历 |
| --- | --- |
| ![考勤打卡](docs/screenshots/clock.png) | ![考勤日历](docs/screenshots/calendar.png) |

| 请假申请 | 我的申请 |
| --- | --- |
| ![请假申请](docs/screenshots/leave.png) | ![我的申请](docs/screenshots/approval.png) |

| 待办审批 | 系统公告 |
| --- | --- |
| ![待办审批](docs/screenshots/pending.png) | ![系统公告](docs/screenshots/notice.png) |

| 个人中心 |  |
| --- | --- |
| ![个人中心](docs/screenshots/mine.png) |  |

---

## 项目简介

KAdmin 是一套前后端分离的人力资源管理系统,由三部分组成:

- **后端**:Spring Boot 3 多模块单体,统一提供 REST API(含 PC 后台与移动端接口)
- **后台管理端(frontend)**:基于 Soybean Admin 深度定制的 PC 管理界面
- **移动端(uniapp_h5)**:基于 UniApp + Vue 3,一套代码编译 H5 与微信小程序

移动端围绕员工日常场景设计:打卡、查考勤、提申请、等审批、发动态、和同事聊天,后台管理员在 PC 端完成组织与人事数据维护、审批处理和移动端菜单配置。

## 功能模块

### 后台管理端

- **组织架构**:公司管理、部门管理,树形结构
- **人事管理**:员工信息(教育/工作经历、证书)、合同管理、转正/调动/离职/奖惩
- **考勤管理**:班次管理、员工排班、日考勤、月考勤、节假日登记
- **申请管理**:请假、加班、补卡、出差、换休、离职等申请查询与处理
- **审批管理**:多级审批流程节点配置
- **报表管理**:考勤/人事统计报表
- **系统管理**:用户、角色、菜单(多语言)、字典(多语言)
- **移动管理**:移动端工作台菜单可视化配置、通知公告、意见反馈处理

### 移动端

- **首页**:考勤/待办/审批统计卡片、消息中心、系统公告
- **时光**:企业内部动态社区,发布动态(图/文/话题)、点赞、浏览详情
- **工作台**:动态菜单、今日时钟、快捷发起各类申请
- **通讯录**:全员联系人(拼音索引)、组织架构、部门成员
- **即时聊天**:群聊(发起群聊、群成员校验)与同事单聊,自己与别人的消息分侧展示
- **考勤**:GPS 定位打卡(上/下班)、月度考勤日历(正常/迟到/早退/缺卡标注)
- **申请**:请假、加班、补卡、出差、换休、离职,提交后实时跟踪审批状态,待审批可撤销
- **我的**:个人信息、快捷导航、意见反馈、修改密码

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.2.1 | 基础框架 |
| Spring Security + JWT | - | 认证鉴权 |
| MyBatis-Plus | - | ORM |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存/Token |
| Knife4j | - | 接口文档 |
| Hutool | - | 工具库 |

### 后台管理端(frontend)

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 3.5 | 前端框架 |
| Vite (rolldown-vite) | 7.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Pinia | 3.x | 状态管理 |
| UnoCSS | - | 原子化 CSS |
| TypeScript | - | 类型系统 |

> 基于 [Soybean Admin](https://github.com/soybeanjs/soybean-admin) 二次开发

### 移动端(uniapp_h5)

| 技术 | 说明 |
| --- | --- |
| UniApp(3.0 alpha,Vue 3) | 跨平台框架,编译 H5 / 微信小程序 |
| 图鸟 UI tuniaoui-vue3 | 移动端 UI 组件库 |
| Vuex 4 | 状态管理 |
| Vite 5 | 构建工具 |

## 目录结构

```
RenZi
├── backend              # 后端(Maven 多模块)
│   ├── kadmin-admin     # 启动模块 + Controller(含 /mobile/* 移动端接口)
│   ├── kadmin-mobile    # 移动端业务(动态、聊天、考勤、通讯录)
│   ├── kadmin-hr        # 人事域(员工/合同/申请/审批)
│   ├── kadmin-attendance# 考勤域
│   ├── kadmin-organization # 组织架构域
│   ├── kadmin-system    # 系统管理域
│   └── kadmin-framework # 安全/认证/通用配置
├── frontend             # 后台管理端(Vue3 + Element Plus)
├── uniapp_h5            # 移动端(UniApp,编译 H5 / 微信小程序)
├── sql                  # 数据库初始化脚本
└── docs/screenshots     # 展示截图
```

## 快速开始

### 环境要求

- JDK 17+(推荐 21)
- Node.js 20.19+(pnpm 8.7+)
- MySQL 8.0+、Redis 6.0+

### 1. 初始化数据库

```sql
CREATE DATABASE kadmin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

导入 [`sql/kadmin.sql`](sql/kadmin.sql)。

### 2. 启动后端

修改 [`backend/kadmin-admin/src/main/resources/application-dev.yml`](backend/kadmin-admin/src/main/resources/application-dev.yml) 中的 MySQL / Redis 连接信息,然后:

```bash
cd backend
mvn package -DskipTests
java -jar kadmin-admin/target/kadmin-admin-1.0.0.jar --spring.profiles.active=dev
```

后端默认运行在 `http://localhost:8080`,接口前缀 `/api`,接口文档 `http://localhost:8080/api/doc.html`(Knife4j)。

### 3. 启动后台管理端

```bash
cd frontend
pnpm install
pnpm dev
```

访问 `http://localhost:9527`。

### 4. 启动移动端(H5)

```bash
cd uniapp_h5
npm install
npm run dev:h5
```

访问 `http://localhost:5173/h5/`。

### 5. 编译微信小程序

```bash
cd uniapp_h5
npx cross-env UNI_INPUT_DIR=. uni build -p mp-weixin
```

用微信开发者工具导入 `uniapp_h5/dist/build/mp-weixin` 即可(`manifest.json` 中替换为你自己的小程序 appid)。

## 默认账号

| 端 | 账号 | 密码 |
| --- | --- | --- |
| 后台管理 | admin | admin123 |
| 移动端(工号) | 001 | 123456 |

## 说明

- 移动端聊天为 4 秒轮询刷新,如需实时推送可在此基础上接入 WebSocket
- 移动端考勤打卡地图使用 Google Maps,请在 `uniapp_h5/config.local.js` 中配置自己的 API Key
- 移动端接口统一在 `backend/kadmin-admin/.../controller/mobile/` 下,前缀 `/mobile/*`
