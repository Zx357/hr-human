# Google Maps 首次接入配置文档

本文档适用于当前 `kadmin` 项目，用来完成：

- 后台管理端“组织架构 -> 打卡设置 -> 地图选点”的 Google Maps 配置
- `uniapp` H5 打卡页的 Google Maps 配置

当前项目现状：

- 后台管理端选点地图：已切换为 Google Maps
- H5 打卡页地图：已切换为 Google Maps
- 后端和小程序打卡校验：仍然使用 `GCJ-02` 坐标
- 后台管理端 Google 地图选点后，会自动换算并保存为 `GCJ-02`

## 一、你需要准备什么

第一次使用 Google Maps Platform，至少要准备：

1. 一个 Google 账号
2. 一个已启用计费的 Google Cloud 项目
3. 一把 API Key
4. 为该项目启用以下 3 个 API
   - `Maps JavaScript API`
   - `Places API`
   - `Geocoding API`

## 二、注册 Google 账号

如果你还没有 Google 账号：

1. 打开 [Google 账号注册页](https://accounts.google.com/signup)
2. 按页面提示完成注册
3. 注册完成后，用这个账号登录 [Google Cloud Console](https://console.cloud.google.com/)

## 三、开通 Google Cloud 和计费

Google Maps Platform 必须绑定计费项目后才能正常使用。

操作步骤：

1. 打开 [Google Cloud Console](https://console.cloud.google.com/)
2. 登录后，页面如果提示“免费试用”或“开始使用”，点击进入
3. 按提示填写以下信息
   - 国家或地区
   - 姓名
   - 地址
   - 付款信息
4. 绑定银行卡或信用卡
5. 完成后，进入 [Billing 页面](https://console.cloud.google.com/billing)

说明：

- Google 官方当前提供试用额度和 Google Maps Platform 的免费额度政策，具体以控制台显示为准
- 即使最终使用量不大，项目本身也必须挂在“已启用计费”的 Cloud Project 下

官方参考：

- [Getting started with Google Maps Platform](https://developers.google.com/maps/get-started)

## 四、创建 Google Cloud 项目

1. 打开 [项目选择页](https://console.cloud.google.com/projectselector2/home/dashboard)
2. 点击顶部“选择项目”
3. 点击“新建项目”
4. 项目名称建议填写：`kadmin-map`
5. 点击“创建”
6. 创建完成后，确认左上角当前选中的项目就是你新建的项目

## 五、给项目绑定 Billing

1. 打开 [Billing 页面](https://console.cloud.google.com/billing)
2. 如果没有 Billing Account，先创建一个
3. 将刚刚新建的 Cloud Project 绑定到这个 Billing Account
4. 绑定完成后，再回到项目首页

## 六、启用本项目需要的 API

打开 [API Library](https://console.cloud.google.com/apis/library)，在同一个项目下依次启用以下 3 个 API：

1. [Maps JavaScript API](https://console.cloud.google.com/apis/library/maps-backend.googleapis.com)
2. [Places API](https://console.cloud.google.com/apis/library/places-backend.googleapis.com)
3. [Geocoding API](https://console.cloud.google.com/apis/library/geocoding-backend.googleapis.com)

说明：

- `Maps JavaScript API`：负责地图底图显示
- `Places API`：负责地点搜索
- `Geocoding API`：负责地址转经纬度、经纬度反查地址

如果少开其中一个，常见现象如下：

- 地图能显示，但搜索定位没有结果：大概率 `Places API` 没开
- 能看到地图，但点地图后地址不回填：大概率 `Geocoding API` 没开
- 页面地图区域空白：大概率 `Maps JavaScript API` 没开，或 key 限制不对

官方参考：

- [Places Library](https://developers.google.com/maps/documentation/javascript/places)
- [Geocoding Service](https://developers.google.com/maps/documentation/javascript/geocoding)

## 七、创建 API Key

1. 打开 [凭证页面](https://console.cloud.google.com/apis/credentials)
2. 点击“创建凭证”
3. 选择“API 密钥”
4. 页面弹出 API Key 后，先复制保存

建议立即给 key 起一个好记的名字，例如：

- `kadmin-web-dev`
- `kadmin-web-prod`

## 八、限制 API Key

这一步非常重要，不建议跳过。

### 1. 应用限制

打开刚刚创建的 API Key，设置：

- `应用限制`：选择 `网站 (HTTP 引用来源)`

本项目建议加入以下来源：

#### 本地开发

- `http://localhost:*/*`
- `http://127.0.0.1:*/*`

#### 服务器 IP 访问

- `http://112.126.27.123/*`
- `https://112.126.27.123/*`

#### 如果后续有正式域名

- `https://admin.你的域名/*`
- `https://m.你的域名/*`

注意：

- 一定要带 `http://` 或 `https://`
- 建议保留 `/*`
- 如果以后从 IP 改成正式域名，要把正式域名也补进去

### 2. API 限制

同一个页面里继续设置：

- `API 限制`：选择“限制密钥”

勾选以下 API：

- `Maps JavaScript API`
- `Places API`
- `Geocoding API`

官方参考：

- [Google Maps Platform security guidance](https://developers.google.com/maps/api-security-best-practices)

## 九、把 Key 填入当前项目

### 1. 后台管理端

编辑以下两个文件：

- `frontend/.env.dev`
- `frontend/.env.prod`

填写：

```env
VITE_GOOGLE_MAPS_API_KEY=你的GoogleMapsKey
VITE_GOOGLE_MAPS_LANGUAGE=zh-CN
VITE_GOOGLE_MAPS_REGION=CN
```

当前项目里对应位置：

- `frontend/.env.dev`
- `frontend/.env.prod`

### 2. uniapp H5

编辑以下两个文件：

- `uniapp/config.dev.js`
- `uniapp/config.prod.js`

把 `googleMaps.apiKey` 改成你的 Key，例如：

```js
googleMaps: {
  apiKey: '你的GoogleMapsKey',
  language: 'zh-CN',
  region: 'CN'
}
```

## 十、重新打包项目

### 1. 打包后台管理端

在项目根目录执行：

```bash
cd frontend
pnpm build
```

打包输出目录：

- `frontend/dist`

### 2. 打包 uniapp H5

当前项目建议使用 HBuilderX 发布 H5：

1. 用 HBuilderX 打开 `uniapp`
2. 选择“发行”
3. 选择“网站-H5”
4. 重新生成 H5 包

生成后，将 H5 发布文件重新上传到你的服务器目录。

## 十一、部署到服务器后检查什么

上线后按这个顺序验证：

### 后台管理端

1. 登录后台
2. 打开“组织架构”
3. 打开某个公司节点的“打卡设置”
4. 点击“地图选打卡点”
5. 检查：
   - 地图是否正常显示
   - 搜索地点是否有结果
   - 点击地图后，地址是否自动回填
   - 回填的经纬度是否有值

### H5 打卡页

1. 打开 H5
2. 进入打卡页面
3. 检查：
   - Google 地图是否正常显示
   - 当前定位是否正常
   - 公司打卡圈是否显示

## 十二、常见问题排查

### 1. 地图空白

优先检查：

1. `Maps JavaScript API` 是否已启用
2. API Key 是否已填写
3. API Key 的 `HTTP referrer` 是否包含当前访问地址
4. 是否重新打包并重新上传了前端文件

### 2. 地图能显示，但搜索定位不能用

优先检查：

1. `Places API` 是否已启用
2. API Key 的 API 限制里是否包含 `Places API`
3. 当前页面使用的 key 是否就是你刚配置的那把 key

### 3. 点击地图后地址不自动回填

优先检查：

1. `Geocoding API` 是否已启用
2. API Key 的 API 限制里是否包含 `Geocoding API`

### 4. 本地能用，服务器不能用

优先检查：

1. API Key 的来源限制里是否只写了 `localhost`
2. 是否忘了加 `http://112.126.27.123/*`
3. 如果线上启用了 HTTPS，是否也补了 `https://112.126.27.123/*`

### 5. 后台地图选点和后端保存坐标不一致

当前项目是刻意这样设计的：

- Google 地图显示和点选：使用更适合 Google Maps 的坐标体系
- 保存到后台：自动换算回 `GCJ-02`

这是正常的，不是 bug。因为后端和小程序的打卡距离校验仍然沿用 `GCJ-02`。

## 十三、当前项目里和 Google Maps 相关的文件

后台管理端：

- `frontend/src/views/organization/org-structure/components/attendance-location-picker.vue`
- `frontend/src/utils/google-maps.ts`
- `frontend/src/utils/coord-transform.ts`
- `frontend/.env.dev`
- `frontend/.env.prod`

H5：

- `uniapp/pages/clock/index.vue`
- `uniapp/utils/google-maps.js`
- `uniapp/config.dev.js`
- `uniapp/config.prod.js`

## 十四、官方入口汇总

Google Cloud：

- [Google Cloud Console](https://console.cloud.google.com/)
- [Billing](https://console.cloud.google.com/billing)
- [API Library](https://console.cloud.google.com/apis/library)
- [Credentials](https://console.cloud.google.com/apis/credentials)

Google Maps Platform 文档：

- [Getting started with Google Maps Platform](https://developers.google.com/maps/get-started)
- [Security best practices](https://developers.google.com/maps/api-security-best-practices)
- [Places Library](https://developers.google.com/maps/documentation/javascript/places)
- [Geocoding Service](https://developers.google.com/maps/documentation/javascript/geocoding)

## 十五、补充说明

1. 如果后台管理和 H5 主要面向中国大陆网络访问，Google Maps 的网络可达性可能会受外部网络环境影响。
2. 这类问题通常不是代码本身错误，而是地图服务访问链路问题。
3. 如果后续你确定要长期稳定在中国大陆环境使用，建议再评估是否切回腾讯地图或高德地图。

