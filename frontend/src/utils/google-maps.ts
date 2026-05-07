interface LoadGoogleMapsOptions {
  apiKey: string;
  language?: string;
  libraries?: string[];
  region?: string;
  timeout?: number;
}

let googleMapsPromise: Promise<any> | null = null;

async function ensureGoogleMapsLibraries(maps: any, libraries: string[] = []) {
  if (!libraries.length || typeof maps?.importLibrary !== 'function') {
    return maps;
  }

  await Promise.all(libraries.map(library => maps.importLibrary(library)));
  return maps;
}

function buildGoogleMapsUrl(options: LoadGoogleMapsOptions, callbackName: string) {
  const params = [
    `key=${encodeURIComponent(options.apiKey)}`,
    'v=weekly',
    `callback=${encodeURIComponent(callbackName)}`,
    'loading=async'
  ];

  if (options.language) {
    params.push(`language=${encodeURIComponent(options.language)}`);
  }

  if (options.region) {
    params.push(`region=${encodeURIComponent(options.region)}`);
  }

  if (options.libraries?.length) {
    params.push(`libraries=${encodeURIComponent(options.libraries.join(','))}`);
  }

  return `https://maps.googleapis.com/maps/api/js?${params.join('&')}`;
}

export function loadGoogleMapsApi(options: LoadGoogleMapsOptions) {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return Promise.reject(new Error('Google Maps can only be loaded in browser environments'));
  }

  if (!options.apiKey) {
    return Promise.reject(new Error('Google Maps API key is missing'));
  }

  const existingMaps = (window as Window & { google?: any }).google?.maps;
  if (existingMaps) {
    return ensureGoogleMapsLibraries(existingMaps, options.libraries);
  }

  if (googleMapsPromise) {
    return googleMapsPromise;
  }

  googleMapsPromise = new Promise((resolve, reject) => {
    const callbackName = '__kadminFrontendGoogleMapsInit';
    const scriptId = 'kadmin-frontend-google-maps-sdk';
    let timeoutId: number | null = null;

    const cleanup = () => {
      if (timeoutId !== null) {
        window.clearTimeout(timeoutId);
        timeoutId = null;
      }

      const runtimeWindow = window as Window & Record<string, unknown>;
      if (runtimeWindow[callbackName]) {
        delete runtimeWindow[callbackName];
      }
    };

    const handleError = () => {
      cleanup();
      googleMapsPromise = null;
      reject(new Error('Failed to load Google Maps JavaScript API'));
    };

    const runtimeWindow = window as Window & Record<string, unknown>;
    runtimeWindow[callbackName] = () => {
      cleanup();
      const maps = (window as Window & { google?: any }).google?.maps;
      ensureGoogleMapsLibraries(maps, options.libraries).then(resolve).catch(reject);
    };

    const existingScript = document.getElementById(scriptId) as HTMLScriptElement | null;
    if (existingScript) {
      existingScript.addEventListener('error', handleError, { once: true });
      return;
    }

    const script = document.createElement('script');
    script.id = scriptId;
    script.src = buildGoogleMapsUrl(options, callbackName);
    script.async = true;
    script.defer = true;
    script.onerror = handleError;

    timeoutId = window.setTimeout(() => {
      handleError();
    }, options.timeout || 15000);

    document.head.appendChild(script);
  });

  return googleMapsPromise;
}
