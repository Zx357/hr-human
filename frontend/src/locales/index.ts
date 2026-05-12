import type { App } from 'vue';
import { createI18n } from 'vue-i18n';
import { localStg } from '@/utils/storage';
import messages from './locale';

const i18n = createI18n({
  locale: localStg.get('lang') || 'zh-CN',
  fallbackLocale: 'en-US',
  messages,
  legacy: false
});

/**
 * Setup plugin i18n
 *
 * @param app
 */
export function setupI18n(app: App) {
  app.use(i18n);
}

export const $t = i18n.global.t as App.I18n.$T;

export function setLocale(locale: App.I18n.LangType) {
  i18n.global.locale.value = locale;
}

export function getLocale(): App.I18n.LangType {
  return i18n.global.locale.value as App.I18n.LangType;
}

type LocalizedMeta = {
  i18nKey?: App.I18n.I18nKey | null;
  title?: unknown;
  titleEn?: unknown;
};

function translateI18nKey(i18nKey?: App.I18n.I18nKey | null) {
  if (!i18nKey) return '';

  const translated = $t(i18nKey);

  return translated === i18nKey ? '' : translated;
}

export function getLocalizedMetaTitle(meta: LocalizedMeta) {
  const title = typeof meta.title === 'string' ? meta.title : '';
  const titleEn = typeof meta.titleEn === 'string' ? meta.titleEn : '';

  if (getLocale() === 'en-US') {
    return titleEn || translateI18nKey(meta.i18nKey) || title;
  }

  return title || translateI18nKey(meta.i18nKey) || titleEn;
}
