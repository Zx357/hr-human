---
name: vue2-to-vue3-migration
description: Guide for migrating UniApp Vue 2 projects to Vue 3 with Composition API. Use when converting Options API to script setup, migrating Vuex to Vue 3, handling tn-icon components, creating composables, or converting TypeScript to JavaScript in UniApp projects.
---

# Vue 2 to Vue 3 Migration Guide

This skill provides guidance for migrating UniApp projects from Vue 2 to Vue 3 using Composition API and `<script setup>` syntax.

## Migration Overview

### Key Transformations

| Vue 2 Pattern | Vue 3 Equivalent |
|--------------|------------------|
| `data()` | `ref()` / `reactive()` |
| `this.$store` | `useStore()` from vuex |
| `Vue.prototype` | `getApp().globalData` |
| Options API | `<script setup>` Composition API |
| Vue 2 lifecycle | Vue 3 lifecycle (`onLaunch`, `onShow`, `onMounted`) |

## Icon Component Migration

### Button Component

 请把目录下的vue文件里的tn-button按示例代码改造,以下是示例代码：<tn-button bg-color="#00B9FE" padding="40rpx 0" width="100%" :fontSize="28" text-color="#FFFFFF" shape="round" @click="tn('')" > <text class="">获取手机号</text> </tn-button>


 请把目录下的vue文件里的tn-button中的padding属性改成custom-style="padding:xxx"


### Class to Component

Convert `tn-icon-*` class labels to `<tn-icon>` component:

```html
<!-- Before -->
<text class="tn-icon-left-arrow icon"></text>

<!-- After -->
<tn-icon name="left-arrow" class="icon"></tn-icon>
```

### Dynamic Icons

Dynamic icons should also use `tn-icon` component with dynamic `name` attribute:

```html
<!-- Before -->
<text :class="['tn-icon-' + item.icon]"></text>
<text :class="{ 'tn-icon-eye': showPassword, 'tn-icon-eye-hide': !showPassword }"></text>

<!-- After -->
<tn-icon :name="item.icon"></tn-icon>
<tn-icon :name="showPassword ? 'eye' : 'eye-hide'"></tn-icon>
```

## Composables Pattern

### Creating Composables

Structure composables in `libs/composables/`:

```
libs/
└── composables/
    ├── index.js              # Unified export entry
    ├── useCustomBarHeight.js # Navigation bar height
    └── useGoBack.js          # Go back logic
```

### Unified Export

```javascript
// libs/composables/index.js
export { useCustomBarHeight } from './useCustomBarHeight'
export { useGoBack } from './useGoBack'
```

### Integration Template

```javascript
// In page components
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
```

## Store Migration

### Store Definition

```javascript
// store/index.js
import { createStore } from 'vuex'

export default createStore({
  state: {
    vuex_custom_bar_height: 0
  },
  mutations: {
    // Keep $tStore for backward compatibility
    $tStore(state, payload) {
      for (const key in payload) {
        state[key] = payload[key]
      }
    }
  }
})
```

### Store Usage

```javascript
// In components
import { useStore } from 'vuex'

const store = useStore()
const vuex_custom_bar_height = store.state.vuex_custom_bar_height
```

## Page Migration Steps

### 1. Convert to script setup

```vue
<template>
  <!-- Template remains mostly unchanged -->
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

// State
const count = ref(0)
const user = reactive({ name: '', age: 0 })

// Store
const store = useStore()
const { vuex_custom_bar_height } = useCustomBarHeight()

// Composables
const { goBack } = useGoBack()

// Methods as plain functions
const increment = () => {
  count.value++
}

// Lifecycle
onMounted(() => {
  console.log('mounted')
})
</script>
```


## TypeScript to JavaScript

Remove type annotations when converting:

```vue
<!-- Before -->
<script setup lang="ts">
interface User {
  name: string
  age: number
}
const user: User = { name: '', age: 0 }
const updateName = (name: string): void => { }
</script>

<!-- After -->
<script setup>
const user = { name: '', age: 0 }
const updateName = (name) => { }
</script>
```

## Migration Checklist

### Per-File Verification

- [ ] Convert `<script>` to `<script setup>`
- [ ] Replace `data()` with `ref()`/`reactive()`
- [ ] Update store access to `useStore()`
- [ ] Convert methods to plain functions
- [ ] Update lifecycle hooks to Vue 3 equivalents
- [ ] Migrate `tn-icon-*` classes to `<tn-icon>` components
- [ ] Integrate composables for common patterns
- [ ] Remove TypeScript type annotations (if converting to JS)

### Special Handlings

| File | Action |
|------|--------|
| `main.js` | Use `app.use(store)` for Vuex mounting |
| `App.vue` | Import lifecycle from `@dcloudio/uni-app` |
| Tabbar pages | Use `TnTabbar` + `TnTabbarItem` component form |

## Progressive Migration Strategy

1. **Check First**: Identify files using `vuex_custom_bar_height` or `goBack`
2. **Create Composables**: Set up shared composables in `libs/composables/`
3. **Migrate Pages**: Update pages one directory at a time
4. **Icon Migration**: Convert static icons, preserve dynamic bindings
5. **Cleanup**: Remove redundant code and old patterns
