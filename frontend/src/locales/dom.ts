import { nextTick, onBeforeUnmount, onMounted, watch } from 'vue';
import type { ComputedRef, Ref } from 'vue';
import { hasChineseText, translateHardCodedText } from './hardcoded';

const translatedTextNodes = new WeakMap<Text, string>();
const translatedAttrs = new WeakMap<Element, Map<string, string>>();
const translatedAttrNames = ['placeholder', 'title', 'aria-label', 'alt'] as const;
const ignoredTags = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA', 'CODE', 'PRE']);

function getOriginalAttribute(element: Element, attrName: string) {
  const origin = translatedAttrs.get(element)?.get(attrName);
  const currentValue = element.getAttribute(attrName) || '';

  if (hasChineseText(currentValue)) {
    setOriginalAttribute(element, attrName, currentValue);
    return currentValue;
  }

  return origin || currentValue;
}

function setOriginalAttribute(element: Element, attrName: string, value: string) {
  const map = translatedAttrs.get(element) || new Map<string, string>();

  map.set(attrName, value);
  translatedAttrs.set(element, map);
}

function restoreAttribute(element: Element, attrName: string) {
  const origin = translatedAttrs.get(element)?.get(attrName);

  if (origin !== undefined && element.getAttribute(attrName) !== origin) {
    element.setAttribute(attrName, origin);
  }
}

function translateAttribute(element: Element, attrName: string, lang: App.I18n.LangType) {
  const currentValue = element.getAttribute(attrName);

  if (!currentValue) return;

  if (lang !== 'en-US') {
    restoreAttribute(element, attrName);
    return;
  }

  const origin = getOriginalAttribute(element, attrName);
  const translated = translateHardCodedText(origin);

  if (translated !== currentValue) {
    element.setAttribute(attrName, translated);
  }
}

function getOriginalText(node: Text) {
  const currentValue = node.nodeValue || '';
  const origin = translatedTextNodes.get(node);

  if (hasChineseText(currentValue)) {
    translatedTextNodes.set(node, currentValue);
    return currentValue;
  }

  return origin || currentValue;
}

function translateTextNode(node: Text, lang: App.I18n.LangType) {
  const currentValue = node.nodeValue || '';

  if (!currentValue.trim()) return;

  if (lang !== 'en-US') {
    const origin = translatedTextNodes.get(node);

    if (origin !== undefined && currentValue !== origin) {
      node.nodeValue = origin;
    }

    return;
  }

  const origin = getOriginalText(node);
  const translated = translateHardCodedText(origin);

  if (translated !== currentValue) {
    node.nodeValue = translated;
  }
}

function shouldIgnoreElement(element: Element) {
  return ignoredTags.has(element.tagName) || Boolean(element.closest('[data-no-dom-i18n]'));
}

function translateNode(node: Node, lang: App.I18n.LangType) {
  if (node.nodeType === Node.TEXT_NODE) {
    translateTextNode(node as Text, lang);
    return;
  }

  if (node.nodeType !== Node.ELEMENT_NODE) return;

  const element = node as Element;

  if (shouldIgnoreElement(element)) return;

  translatedAttrNames.forEach(attrName => {
    translateAttribute(element, attrName, lang);
  });

  element.childNodes.forEach(child => translateNode(child, lang));
}

export function useHardCodedDomI18n(lang: Ref<App.I18n.LangType> | ComputedRef<App.I18n.LangType>) {
  let observer: MutationObserver | null = null;
  let currentLang = lang.value;

  function observe() {
    if (!observer || !document.body) return;

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: true,
      attributes: true,
      attributeFilter: [...translatedAttrNames]
    });
  }

  function applyTo(node: Node) {
    observer?.disconnect();
    translateNode(node, currentLang);
    observe();
  }

  async function applyDocument() {
    await nextTick();

    if (document.body) {
      applyTo(document.body);
    }
  }

  onMounted(() => {
    observer = new MutationObserver(records => {
      const nodes = new Set<Node>();

      records.forEach(record => {
        if (record.type === 'childList') {
          record.addedNodes.forEach(node => nodes.add(node));
        } else {
          nodes.add(record.target);
        }
      });

      nodes.forEach(node => applyTo(node));
    });

    applyDocument();
    observe();
  });

  watch(
    lang,
    value => {
      currentLang = value;
      applyDocument();
    },
    { flush: 'post' }
  );

  onBeforeUnmount(() => {
    observer?.disconnect();
    observer = null;
  });
}
