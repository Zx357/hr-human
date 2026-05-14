let googleMapsPromise = null

function buildGoogleMapsUrl(options, callbackName) {
  const params = [
    `key=${encodeURIComponent(options.apiKey)}`,
    'v=weekly',
    `callback=${encodeURIComponent(callbackName)}`,
    'loading=async'
  ]

  if (options.language) params.push(`language=${encodeURIComponent(options.language)}`)
  if (options.region) params.push(`region=${encodeURIComponent(options.region)}`)

  return `https://maps.googleapis.com/maps/api/js?${params.join('&')}`
}

export function loadGoogleMapsApi(options = {}) {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return Promise.reject(new Error('Google Maps only runs in H5 browser environments'))
  }

  if (window.google && window.google.maps) {
    return Promise.resolve(window.google.maps)
  }

  if (!options.apiKey) {
    return Promise.reject(new Error('Google Maps API key is not configured'))
  }

  if (googleMapsPromise) {
    return googleMapsPromise
  }

  googleMapsPromise = new Promise((resolve, reject) => {
    const callbackName = '__kadminGoogleMapsInit'
    const scriptId = 'kadmin-google-maps-sdk'
    let timeoutId = null

    const cleanup = () => {
      if (timeoutId) {
        window.clearTimeout(timeoutId)
        timeoutId = null
      }
      if (window[callbackName]) delete window[callbackName]
    }

    const handleError = () => {
      cleanup()
      googleMapsPromise = null
      reject(new Error('Failed to load Google Maps JavaScript API'))
    }

    window[callbackName] = () => {
      cleanup()
      resolve(window.google.maps)
    }

    const existingScript = document.getElementById(scriptId)
    if (existingScript) {
      existingScript.addEventListener('error', handleError, { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = scriptId
    script.src = buildGoogleMapsUrl(options, callbackName)
    script.async = true
    script.defer = true
    script.onerror = handleError

    timeoutId = window.setTimeout(handleError, options.timeout || 15000)
    document.head.appendChild(script)
  })

  return googleMapsPromise
}
