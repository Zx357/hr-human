import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// UNI_INPUT_DIR 为相对路径时，easycom 会生成 vite 无法解析的裸模块导入（如 uni_modules/...），这里统一转为绝对路径
process.env.UNI_INPUT_DIR = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  process.env.UNI_INPUT_DIR || '.'
)

export default defineConfig({
  plugins: [uni()]
})
