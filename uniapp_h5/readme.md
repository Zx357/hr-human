以下是本次会话已完成的变更任务总结：

| 序号 | 文件路径 | 变更内容 |
|------|----------|----------|
| 1 | `pages/moment/moment.vue` | 改为 Vue 3 版本，使用 `<script setup>` 语法，数据使用 `ref` 定义，方法改为普通函数 |
| 2 | `components/index-list/index-list.vue` | **新建组件** - 按设计稿实现索引列表组件，支持字母分组、右侧索引栏、触摸滑动跳转、中文转拼音首字母 |
| 3 | `store/index.js` | 改为 Vue 3 版本，使用 `createStore` 替代 `new Vuex.Store`，保持 `$tStore` mutation 兼容旧代码 |
| 4 | `main.js` | 修改 store 引入方式，使用 `app.use(store)` 挂载 Vuex |
| 5 | `App.vue` | 改为 Vue 3 版本，使用 `<script setup>`，从 `@dcloudio/uni-app` 导入生命周期，使用 `useStore()` |
| 6 | `pages/index.vue` | 改为 Vue 3 版本，修改 tabbar 使用方式为 `TnTabbar` + `TnTabbarItem` 组件形式 |
| 7 | `pages/work/work.vue` | 改为 Vue 3 版本，使用 `<script setup>`，使用 `useStore()` 获取状态栏高度 |
| 8 | `package.json` | 添加 `vuex` 依赖（`^4.1.0`） |

**主要技术变更点：**
- Options API → `<script setup>` 组合式 API
- `data()` → `ref()` / `reactive()`
- `this.$store` → `useStore()`
- Vue 2 生命周期 → Vue 3 生命周期（`onLaunch`, `onShow`, `onMounted` 等）
- 移除 `Vue.prototype`，改为 `getApp().globalData`
- Tabbar 组件用法更新为最新图鸟 UI 方式


根据对话历史详细分析，以下是完整的迁移任务清单，包括之前遗漏的细节：

---

## 📋 Vue2 到 Vue3 完整迁移任务清单

### 一、图标组件迁移（Class → Component）

**任务：** 将 `tn-icon-*` class 标签改为 `<tn-icon>` 组件

**涉及目录：** `homePages`、`workPages`、`minePages`、`momentPages`、`partnerPages`

**转换示例：**
```html
<text class="tn-icon-left-arrow icon"></text>
→ <tn-icon name="left-arrow" class="icon"></tn-icon>
```

---

### 二、Composables 创建与封装

#### 2.1 创建 Composables 文件

| 文件路径 | 说明 |
|---------|------|
| `libs/composables/useCustomBarHeight.js` | libs 目录版本（最终使用） |
| `libs/composables/useGoBack.js` | 返回上一页逻辑 |
| `libs/composables/index.js` | 统一导出入口 |

#### 2.2 导出配置
```javascript
// libs/composables/index.js
export { useCustomBarHeight } from './useCustomBarHeight'
export { useGoBack } from './useGoBack'
```

---

### 三、Composables 集成（共 5 个目录，62 个文件）

#### 3.1 检查与集成流程

| 阶段 | 操作 | 涉及文件数 |
|------|------|-----------|
| 检查 | 检查 homePages 哪些文件使用 vuex_custom_bar_height/goBack | 10 个 |
| 集成 | homePages 集成 composables | 10 个 |
| 集成 | minePages 集成 composables | 9 个 |
| 集成 | momentPages 集成 composables | 13 个 |
| 集成 | partnerPages 集成 composables | 5 个 |
| 集成 | workPages 集成 composables | 25 个 |

#### 3.2 集成代码模板
```javascript
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
```

#### 3.3 特殊处理文件

| 文件 | 特殊处理内容 |
|------|-------------|
| `workPages/overtime.vue` | 替换原有 `useGoBack` 导入路径，删除 `useStore` |
| `workPages/create.vue` | 删除 `const vuex_custom_bar_height = store.state...` 和自定义 `goBack` 函数 |
| `momentPages/catalog.vue` | 删除原有的 store 直接访问方式 |
| `momentPages/dictionary.vue` | 从 Options API 转换为 `<script setup>` 语法 |
| `pages/moment/moment.vue` | 单独封装，替换原有 `useStore` 方式 |

---

### 四、TypeScript 转 JavaScript

| 文件 | 变更内容 |
|------|---------|
| `workPages/travel.vue` | `<script setup lang="ts">` → `<script setup>` |
| | 移除所有类型声明 `: string`, `: number`, `: boolean`, `: void` |
| | 移除所有 interface 定义 |
| | 移除函数参数类型 `: any` |

---

### 五、代码优化与清理

#### 5.1 删除的旧代码模式

```javascript
// 删除的 Vue2/mixin 方式：
import { useStore } from 'vuex'
const store = useStore()
const vuex_custom_bar_height = store.state.vuex_custom_bar_height

// 删除的自定义 goBack：
const goBack = () => { uni.navigateBack() }

// 删除的单独导入：
import { useGoBack } from "@/libs/composables/useGoBack.js"
```

#### 5.2 保留的动态绑定（未修改）

- `:class="['tn-icon-' + item.icon]"` - 动态图标类
- `:class="{ 'tn-icon-eye': showPassword }"` - 条件类

---

### 六、目录结构变更

```
libs/
└── composables/
    ├── index.js              # 统一导出入口（新增）
    ├── useCustomBarHeight.js # 导航栏高度（新增）
    └── useGoBack.js          # 返回逻辑（新增）

composables/                   # 根目录版本（早期创建，后被 libs 版本替代）
└── useCustomBarHeight.js
```

---

## 📊 完整统计

| 类别 | 数量 | 备注 |
|------|------|------|
| 图标组件转换 | ~63 个文件 | 5 个目录 |
| Composables 集成 | 62 个文件 | 5 个目录 |
| TS 转 JS | 1 个文件 | travel.vue |
| 创建 Composables | 3 个文件 | useCustomBarHeight, useGoBack, index |
| 特殊重构 | 4 个文件 | 删除旧代码、转换 API 风格 |
| **总计** | **~70 个文件** | |

---

## 🎯 迁移特点总结

1. **渐进式迁移**：先检查，后集成，再优化
2. **统一封装**：所有页面使用相同的 composables 模式
3. **向后兼容**：保留动态绑定等无法自动转换的代码
4. **标准化**：统一使用 `libs/composables` 作为功能入口
5. **去 TypeScript**：最终统一使用 JavaScript


import { useCustomBarHeight, useGoBack } from '@/libs/composables' // 使用 composable 获取自定义导航栏高度 const { vuex_custom_bar_height } = useCustomBarHeight() const { goBack } = useGoBack() // 处理返回事件