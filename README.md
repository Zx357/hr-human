# KAdmin - 人力资源管理系统

## 项目简介

KAdmin 是一个基于 Spring Boot 3 + Vue 3 的现代化人力资源管理系统，支持多语言国际化，提供完整的人事管理、考勤管理、审批流程等功能。

前端基于 [Soybean Admin Element Plus](https://github.com/soybeanjs/soybean-admin-element-plus) 框架开发，提供美观的 UI 界面和丰富的功能组件。

## 技术栈

### 后端

- Spring Boot 3.2.x
- Spring Security + JWT
- MyBatis Plus
- MySQL 8.0
- Redis (缓存)

### 前端

- Vue 3.5
- Vite 7 (rolldown-vite)
- Element Plus
- Vue Router 4
- Pinia 3
- Vue I18n (多语言)
- UnoCSS
- TypeScript

## 功能模块

### 1. 系统管理

- 菜单管理（支持多语言配置）
- 字典管理（支持多语言配置）
- 用户管理
- 角色管理
- 权限管理

### 2. 组织架构

- 公司管理
- 部门管理

### 3. 人事管理

- 人员信息管理
  - 基本信息
  - 教育经历（动态表单）
  - 工作经历（动态表单）
  - 证书管理（动态表单）
- 合同管理
- 转正管理
- 部门变更
- 职位变更
- 奖惩管理
- 离职管理

### 4. 考勤管理

- 班次管理
- 员工排班
- 节假日登记
- 日考勤管理
- 月考勤管理

### 5. 申请管理

- 请假申请
- 加班申请
- 补卡申请
- 换休申请
- 出差申请

### 6. 审批管理

- 审批流程配置（支持多人审批）
- 审批记录

### 7. 报表管理

- 人员统计报表
- 考勤统计报表
- 部门人员报表

## 项目结构

```
kadmin/
├── backend/                 # 后端项目
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/kadmin/
│       │   │       ├── common/          # 公共模块
│       │   │       ├── config/          # 配置类
│       │   │       ├── controller/      # 控制器
│       │   │       ├── entity/          # 实体类
│       │   │       ├── mapper/          # MyBatis映射
│       │   │       ├── service/         # 服务层
│       │   │       └── utils/           # 工具类
│       │   └── resources/
│       │       ├── mapper/              # MyBatis XML
│       │       └── application.yml      # 配置文件
│       └── test/
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # 公共组件
│   │   ├── i18n/           # 国际化
│   │   ├── layout/         # 布局组件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # 状态管理
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面组件
│   └── package.json
└── sql/                     # 数据库脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 数据库配置

- 数据库名：kadmin
- 用户名：root
- 密码：123456

### 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE kadmin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行 SQL 脚本（按顺序）：

```bash
mysql -u root -p123456 kadmin < sql/init.sql
mysql -u root -p123456 kadmin < sql/init_part2.sql
mysql -u root -p123456 kadmin < sql/init_data.sql
```

## 默认登录账户

| 用户名 | 密码     | 角色       |
| ------ | -------- | ---------- |
| admin  | admin123 | 系统管理员 |

> 注意：系统启动时会自动检查并修复 admin 用户的密码，确保可以正常登录。

## 常见问题

### 登录提示用户名或密码错误

1. 确保数据库已正确初始化
2. 重启后端服务（系统会自动修复 admin 密码）
3. 或手动执行 `sql/update_password.sql` 更新密码

### 前端无法连接后端

1. 确保后端服务已启动（默认端口 8080）
2. 确保 Redis 服务已启动（默认端口 6379）
3. 检查前端代理配置 `frontend/vite.config.js`

## License

MIT
