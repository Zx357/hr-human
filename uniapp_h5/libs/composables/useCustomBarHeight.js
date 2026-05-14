import { computed } from 'vue'
import { useStore } from 'vuex'

/**
 * 获取自定义导航栏高度的 Composable
 * @returns {Object} { vuex_custom_bar_height }
 */
export function useCustomBarHeight() {
  const store = useStore()
  
  // 使用 computed 保持响应式
  const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)
  
  return {
    vuex_custom_bar_height:vuex_custom_bar_height
  }
}

export default useCustomBarHeight
