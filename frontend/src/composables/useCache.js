import { ref, computed } from 'vue'

// Cache storage
const cache = ref(new Map())
const cacheStats = ref({
  hits: 0,
  misses: 0,
  size: 0,
  maxSize: 100
})

// Cache configuration
const defaultConfig = {
  ttl: 5 * 60 * 1000, // 5 minutes
  maxSize: 100,
  strategy: 'lru' // least recently used
}

export function useCache(config = {}) {
  const cacheConfig = { ...defaultConfig, ...config }
  
  // Update max size
  cacheStats.value.maxSize = cacheConfig.maxSize

  // Check if key exists and is not expired
  const isValid = (key) => {
    const item = cache.value.get(key)
    if (!item) return false
    
    const now = Date.now()
    return now < item.expires
  }

  // Get item from cache
  const get = (key) => {
    if (!isValid(key)) {
      cacheStats.value.misses++
      return null
    }
    
    const item = cache.value.get(key)
    
    // Update access time for LRU
    if (cacheConfig.strategy === 'lru') {
      item.lastAccessed = Date.now()
    }
    
    cacheStats.value.hits++
    return item.value
  }

  // Set item in cache
  const set = (key, value, customTtl = null) => {
    const ttl = customTtl || cacheConfig.ttl
    const now = Date.now()
    
    const item = {
      value,
      createdAt: now,
      expires: now + ttl,
      lastAccessed: now
    }
    
    cache.value.set(key, item)
    cacheStats.value.size = cache.value.size
    
    // Cleanup if over max size
    if (cache.value.size > cacheConfig.maxSize) {
      cleanup()
    }
  }

  // Remove item from cache
  const remove = (key) => {
    const deleted = cache.value.delete(key)
    if (deleted) {
      cacheStats.value.size = cache.value.size
    }
    return deleted
  }

  // Clear all cache
  const clear = () => {
    cache.value.clear()
    cacheStats.value.size = 0
    cacheStats.value.hits = 0
    cacheStats.value.misses = 0
  }

  // Cleanup expired items
  const cleanup = () => {
    const now = Date.now()
    const toDelete = []
    
    // Find expired items
    for (const [key, item] of cache.value.entries()) {
      if (now >= item.expires) {
        toDelete.push(key)
      }
    }
    
    // Remove expired items
    toDelete.forEach(key => cache.value.delete(key))
    
    // If still over max size, remove oldest items (LRU)
    if (cache.value.size > cacheConfig.maxSize) {
      const items = Array.from(cache.value.entries())
        .sort((a, b) => a[1].lastAccessed - b[1].lastAccessed)
      
      const toRemove = items.slice(0, cache.value.size - cacheConfig.maxSize)
      toRemove.forEach(([key]) => cache.value.delete(key))
    }
    
    cacheStats.value.size = cache.value.size
  }

  // Get cache statistics
  const getStats = () => {
    const total = cacheStats.value.hits + cacheStats.value.misses
    const hitRate = total > 0 ? (cacheStats.value.hits / total) * 100 : 0
    
    return {
      ...cacheStats.value,
      hitRate: Math.round(hitRate * 100) / 100,
      total
    }
  }

  // Check if cache has key
  const has = (key) => {
    return isValid(key)
  }

  // Get cache keys
  const keys = () => {
    return Array.from(cache.value.keys())
  }

  // Get cache size in bytes (approximate)
  const getSize = () => {
    let size = 0
    for (const [key, item] of cache.value.entries()) {
      size += key.length * 2 // Unicode characters
      size += JSON.stringify(item.value).length * 2
      size += 100 // Overhead for object properties
    }
    return size
  }

  return {
    get,
    set,
    remove,
    clear,
    cleanup,
    has,
    keys,
    getStats,
    getSize,
    isValid
  }
}

// API cache with automatic cleanup
export function useApiCache(config = {}) {
  const apiCache = useCache({
    ttl: 10 * 60 * 1000, // 10 minutes for API calls
    maxSize: 50,
    ...config
  })

  // Cached API call
  const cachedApiCall = async (key, apiCall, customTtl = null) => {
    // Check cache first
    const cached = apiCache.get(key)
    if (cached) {
      return cached
    }

    // Make API call
    try {
      const result = await apiCall()
      
      // Cache successful result
      apiCache.set(key, result, customTtl)
      
      return result
    } catch (error) {
      // Don't cache errors
      throw error
    }
  }

  // Cache with automatic key generation
  const cacheApiCall = (apiCall, customTtl = null) => {
    return async (...args) => {
      const key = `${apiCall.name || 'api'}_${JSON.stringify(args)}`
      return cachedApiCall(key, () => apiCall(...args), customTtl)
    }
  }

  // Invalidate cache by pattern
  const invalidatePattern = (pattern) => {
    const regex = new RegExp(pattern)
    const keys = apiCache.keys()
    
    keys.forEach(key => {
      if (regex.test(key)) {
        apiCache.remove(key)
      }
    })
  }

  // Invalidate all cache
  const invalidateAll = () => {
    apiCache.clear()
  }

  return {
    ...apiCache,
    cachedApiCall,
    cacheApiCall,
    invalidatePattern,
    invalidateAll
  }
}

// Image cache for lazy loading
export function useImageCache() {
  const imageCache = new Map()
  const loadingImages = new Map()

  const loadImage = (src) => {
    // Return cached promise if already loading
    if (loadingImages.has(src)) {
      return loadingImages.get(src)
    }

    // Return cached image if already loaded
    if (imageCache.has(src)) {
      return Promise.resolve(imageCache.get(src))
    }

    // Create new loading promise
    const promise = new Promise((resolve, reject) => {
      const img = new Image()
      
      img.onload = () => {
        imageCache.set(src, img)
        loadingImages.delete(src)
        resolve(img)
      }
      
      img.onerror = () => {
        loadingImages.delete(src)
        reject(new Error(`Failed to load image: ${src}`))
      }
      
      img.src = src
    })

    loadingImages.set(src, promise)
    return promise
  }

  const preloadImages = (srcs) => {
    return Promise.allSettled(srcs.map(src => loadImage(src)))
  }

  const clearImageCache = () => {
    imageCache.clear()
    loadingImages.clear()
  }

  const getImageCacheStats = () => {
    return {
      cached: imageCache.size,
      loading: loadingImages.size
    }
  }

  return {
    loadImage,
    preloadImages,
    clearImageCache,
    getImageCacheStats
  }
}

// Local storage cache with expiration
export function useLocalStorageCache(config = {}) {
  const defaultConfig = {
    prefix: 'cache_',
    ttl: 60 * 60 * 1000, // 1 hour
    ...config
  }

  const getStorageKey = (key) => {
    return `${defaultConfig.prefix}${key}`
  }

  const get = (key) => {
    try {
      const storageKey = getStorageKey(key)
      const item = localStorage.getItem(storageKey)
      
      if (!item) return null
      
      const parsed = JSON.parse(item)
      const now = Date.now()
      
      if (now >= parsed.expires) {
        localStorage.removeItem(storageKey)
        return null
      }
      
      return parsed.value
    } catch (error) {
      console.warn('Error reading from localStorage cache:', error)
      return null
    }
  }

  const set = (key, value, customTtl = null) => {
    try {
      const storageKey = getStorageKey(key)
      const ttl = customTtl || defaultConfig.ttl
      const now = Date.now()
      
      const item = {
        value,
        expires: now + ttl,
        createdAt: now
      }
      
      localStorage.setItem(storageKey, JSON.stringify(item))
      return true
    } catch (error) {
      console.warn('Error writing to localStorage cache:', error)
      return false
    }
  }

  const remove = (key) => {
    try {
      const storageKey = getStorageKey(key)
      localStorage.removeItem(storageKey)
      return true
    } catch (error) {
      console.warn('Error removing from localStorage cache:', error)
      return false
    }
  }

  const clear = () => {
    try {
      const keys = Object.keys(localStorage)
      const cacheKeys = keys.filter(key => key.startsWith(defaultConfig.prefix))
      
      cacheKeys.forEach(key => localStorage.removeItem(key))
      return true
    } catch (error) {
      console.warn('Error clearing localStorage cache:', error)
      return false
    }
  }

  const cleanup = () => {
    try {
      const keys = Object.keys(localStorage)
      const cacheKeys = keys.filter(key => key.startsWith(defaultConfig.prefix))
      const now = Date.now()
      
      cacheKeys.forEach(key => {
        try {
          const item = localStorage.getItem(key)
          if (item) {
            const parsed = JSON.parse(item)
            if (now >= parsed.expires) {
              localStorage.removeItem(key)
            }
          }
        } catch (error) {
          // Remove corrupted items
          localStorage.removeItem(key)
        }
      })
      
      return true
    } catch (error) {
      console.warn('Error cleaning up localStorage cache:', error)
      return false
    }
  }

  return {
    get,
    set,
    remove,
    clear,
    cleanup
  }
}

// Global cache instances
export const globalCache = useCache()
export const globalApiCache = useApiCache()
export const globalImageCache = useImageCache()
export const globalLocalStorageCache = useLocalStorageCache()
