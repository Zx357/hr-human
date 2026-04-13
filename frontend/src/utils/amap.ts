import { AMAP_KEY, AMAP_SDK_URL, AMAP_SECURITY_JS_CODE } from '@/constants/map-sdk';

let amapPromise: Promise<any> | null = null;

function getAmapFromWindow() {
  return (window as Window & { AMap?: any }).AMap;
}

function applyAmapSecurityConfig() {
  if (typeof window === 'undefined') {
    return;
  }

  if (AMAP_SECURITY_JS_CODE) {
    const amapWindow = window as Window & Record<string, any>;
    const currentConfig = Reflect.get(amapWindow, '_AMapSecurityConfig') || {};
    Reflect.set(amapWindow, '_AMapSecurityConfig', {
      ...currentConfig,
      securityJsCode: AMAP_SECURITY_JS_CODE
    });
  }
}

export function loadAmapSdk() {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('AMap can only be loaded in browser'));
  }

  if (!AMAP_KEY) {
    return Promise.reject(new Error('AMap key is missing'));
  }

  const existingAmap = getAmapFromWindow();
  if (existingAmap) {
    return Promise.resolve(existingAmap);
  }

  if (amapPromise) {
    return amapPromise;
  }

  amapPromise = new Promise((resolve, reject) => {
    applyAmapSecurityConfig();

    const scriptId = 'kadmin-amap-sdk';
    const existingScript = document.getElementById(scriptId) as HTMLScriptElement | null;

    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(getAmapFromWindow()));
      existingScript.addEventListener('error', () => {
        amapPromise = null;
        reject(new Error('Failed to load AMap SDK'));
      });
      return;
    }

    const script = document.createElement('script');
    script.id = scriptId;
    script.src = AMAP_SDK_URL;
    script.async = true;
    script.onload = () => {
      const amap = getAmapFromWindow();
      if (!amap) {
        amapPromise = null;
        reject(new Error('AMap SDK loaded but window.AMap is missing'));
        return;
      }
      resolve(amap);
    };
    script.onerror = () => {
      amapPromise = null;
      reject(new Error('Failed to load AMap SDK'));
    };
    document.head.appendChild(script);
  });

  return amapPromise;
}

export async function loadAmapPlugins(plugins: string[] = []) {
  const AMap = await loadAmapSdk();

  if (!plugins.length) {
    return AMap;
  }

  await new Promise<void>(resolve => {
    AMap.plugin(plugins, () => resolve());
  });

  return AMap;
}
