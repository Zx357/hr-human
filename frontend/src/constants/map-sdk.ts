/** baidu map sdk url */
export const BAIDU_MAP_SDK_URL = `https://api.map.baidu.com/getscript?v=3.0&ak=KSezYymXPth1DIGILRX3oYN9PxbOQQmU&services=&t=20210201100830&s=1`;

const amapKey = import.meta.env.VITE_AMAP_KEY || 'e7bd02bd504062087e6563daf4d6721d';

export const AMAP_KEY = amapKey;
export const AMAP_SECURITY_JS_CODE = import.meta.env.VITE_AMAP_SECURITY_JS_CODE || '';

/** Amap sdk url */
export const AMAP_SDK_URL = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}`;

/** tencent sdk url */
export const TENCENT_MAP_SDK_URL = 'https://map.qq.com/api/gljs?v=1.exp&key=A6DBZ-KXPLW-JKSRY-ONZF4-CPHY3-K6BL7';
