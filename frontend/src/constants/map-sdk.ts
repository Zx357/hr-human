const googleMapsApiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';

export const GOOGLE_MAPS_API_KEY = googleMapsApiKey;
export const GOOGLE_MAPS_LANGUAGE = import.meta.env.VITE_GOOGLE_MAPS_LANGUAGE || 'zh-CN';
export const GOOGLE_MAPS_REGION = import.meta.env.VITE_GOOGLE_MAPS_REGION || 'CN';

