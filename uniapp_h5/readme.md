# KAdmin 移动端(H5 / 微信小程序)

基于 UniApp + Vue 3 + 图鸟 UI(TuniaoUI)的移动办公端,一套代码编译 H5 与微信小程序。

## 功能

- **首页**:考勤/待办/审批统计卡片、消息中心(站内通知)、系统公告
- **时光**:企业内部动态社区,发布动态(图/文/话题)、点赞、评论、互动消息
- **工作台**:动态菜单、今日时钟、快捷发起各类申请(请假/加班/补卡/出差/换休/离职/费用报销/设备申请)
- **通讯录**:全员联系人(拼音索引)、组织架构、部门成员、同事详情
- **即时聊天**:单聊/群聊、最近会话(未读角标)、WebSocket 实时推送(轮询兜底)
- **考勤**:GPS 定位打卡、月度考勤日历
- **我的**:个人信息、头像上传、修改密码、意见反馈

## 快速开始

```bash
npm install

# 本地开发(H5),后端默认连接 http://127.0.0.1:8080,见 config.local.js
npm run dev:h5

# 生产构建 H5(读取 config.server.js)
npm run build:h5

# 微信小程序(构建后用微信开发者工具导入 dist/build/mp-weixin)
npm run build:mp-weixin
npm run dev:mp-weixin
```

## 配置说明

| 文件 | 用途 |
| --- | --- |
| `config.local.js` | 本地开发:baseUrl 指向本机后端 |
| `config.server.js` | 生产:H5 用同源相对路径 `/api`;小程序端用 `mpBaseUrl` 绝对地址(需 https 且在小程序后台配置 request/uploadFile 合法域名) |
| `manifest.json` | H5 高德地图 Key(`h5.sdkConfigs.maps.amap`)与小程序 appid、定位权限声明 |

注意事项:

- **小程序端**必须配置 `config.server.js` 的 `mpBaseUrl`(绝对地址),并把 `manifest.json` 的 `appid` 换成自己的
- **打卡地图**:高德 Key 需同时配置在 `config.*.js` 的 `amap.key` 与 `manifest.json` 的 h5 段,未配置时打卡页自动降级为坐标示意卡片(仅 H5 需要地图;小程序端用原生腾讯地图无需 Key)
- 会话过期、接口baseUrl、WebSocket 地址解析统一在 `utils/request.js` / `utils/websocket.js` / `config.js` 处理
