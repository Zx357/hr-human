import type { Language } from 'element-plus/es/locale';
// 值导入走 dist 路径：element-plus 的 exports 映射未暴露 "./es/locale"，
// Vite 运行时解析会失败（类型检查能过，dev 启动报 import-analysis 错误）
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import en from 'element-plus/dist/locale/en.mjs';

export const UILocales: Record<App.I18n.LangType, Language> = {
  'zh-CN': zhCn as unknown as Language,
  'en-US': en as unknown as Language
};

// export const naiveDateLocales: Record<App.I18n.LangType, NDateLocale> = {
//   'zh-CN': dateZhCN,
//   'en-US': dateEnUS
// };
