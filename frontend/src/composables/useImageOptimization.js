import { ref, computed } from 'vue'

export function useImageOptimization() {
  // State
  const webpSupported = ref(false)
  const avifSupported = ref(false)
  const loadingImages = ref(new Set())
  const loadedImages = ref(new Set())
  const failedImages = ref(new Set())

  // Check browser support
  const checkBrowserSupport = () => {
    // Check WebP support
    const webpCanvas = document.createElement('canvas')
    webpCanvas.width = 1
    webpCanvas.height = 1
    webpSupported.value = webpCanvas.toDataURL('image/webp').indexOf('data:image/webp') === 0

    // Check AVIF support
    const avifCanvas = document.createElement('canvas')
    avifCanvas.width = 1
    avifCanvas.height = 1
    avifSupported.value = avifCanvas.toDataURL('image/avif').indexOf('data:image/avif') === 0
  }

  // Generate optimized image URL
  const getOptimizedImageUrl = (src, options = {}) => {
    if (!src) return ''

    const {
      width = null,
      height = null,
      quality = 80,
      format = 'auto', // 'auto', 'webp', 'avif', 'original'
      fit = 'cover', // 'cover', 'contain', 'fill', 'inside', 'outside'
      position = 'center' // 'top', 'right top', 'right', 'right bottom', 'bottom', 'left bottom', 'left', 'left top', 'center'
    } = options

    // If it's an external URL, return as is
    if (src.startsWith('http') && !src.includes('unsplash.com')) {
      return src
    }

    // For Unsplash images, we can optimize them
    if (src.includes('unsplash.com')) {
      const url = new URL(src)
      
      // Add optimization parameters
      if (width) url.searchParams.set('w', width.toString())
      if (height) url.searchParams.set('h', height.toString())
      if (quality && quality !== 80) url.searchParams.set('q', quality.toString())
      if (fit && fit !== 'cover') url.searchParams.set('fit', fit)
      if (position && position !== 'center') url.searchParams.set('crop', position)
      
      // Add format
      if (format === 'auto') {
        if (avifSupported.value) {
          url.searchParams.set('fm', 'avif')
        } else if (webpSupported.value) {
          url.searchParams.set('fm', 'webp')
        }
      } else if (format !== 'original') {
        url.searchParams.set('fm', format)
      }
      
      return url.toString()
    }

    // For local images, we can't optimize without a backend
    // But we can add query parameters for potential backend optimization
    const url = new URL(src, window.location.origin)
    
    if (width) url.searchParams.set('w', width.toString())
    if (height) url.searchParams.set('h', height.toString())
    if (quality && quality !== 80) url.searchParams.set('q', quality.toString())
    if (fit && fit !== 'cover') url.searchParams.set('fit', fit)
    if (position && position !== 'center') url.searchParams.set('pos', position)
    
    if (format === 'auto') {
      if (avifSupported.value) {
        url.searchParams.set('format', 'avif')
      } else if (webpSupported.value) {
        url.searchParams.set('format', 'webp')
      }
    } else if (format !== 'original') {
      url.searchParams.set('format', format)
    }
    
    return url.toString()
  }

  // Generate responsive image sources
  const getResponsiveImageSources = (src, options = {}) => {
    const {
      sizes = [320, 640, 768, 1024, 1280, 1920],
      quality = 80,
      format = 'auto'
    } = options

    const sources = []

    // Generate sources for different sizes
    sizes.forEach(size => {
      const optimizedUrl = getOptimizedImageUrl(src, {
        ...options,
        width: size,
        quality,
        format
      })
      
      sources.push({
        url: optimizedUrl,
        width: size,
        descriptor: `${size}w`
      })
    })

    return sources
  }

  // Generate srcset string
  const getSrcSet = (sources) => {
    return sources
      .map(source => `${source.url} ${source.descriptor}`)
      .join(', ')
  }

  // Generate sizes attribute
  const getSizes = (breakpoints = {}) => {
    const defaultBreakpoints = {
      sm: '640px',
      md: '768px',
      lg: '1024px',
      xl: '1280px',
      '2xl': '1536px'
    }

    const merged = { ...defaultBreakpoints, ...breakpoints }
    
    return Object.entries(merged)
      .map(([breakpoint, size]) => `(min-width: ${size}) ${size}`)
      .join(', ') + ', 100vw'
  }

  // Preload image
  const preloadImage = (src, options = {}) => {
    return new Promise((resolve, reject) => {
      if (loadedImages.value.has(src)) {
        resolve(src)
        return
      }

      if (loadingImages.value.has(src)) {
        // Wait for existing load
        const checkLoaded = () => {
          if (loadedImages.value.has(src)) {
            resolve(src)
          } else if (failedImages.value.has(src)) {
            reject(new Error(`Failed to load image: ${src}`))
          } else {
            setTimeout(checkLoaded, 100)
          }
        }
        checkLoaded()
        return
      }

      loadingImages.value.add(src)
      
      const img = new Image()
      img.onload = () => {
        loadingImages.value.delete(src)
        loadedImages.value.add(src)
        resolve(src)
      }
      img.onerror = () => {
        loadingImages.value.delete(src)
        failedImages.value.add(src)
        reject(new Error(`Failed to load image: ${src}`))
      }
      
      img.src = getOptimizedImageUrl(src, options)
    })
  }

  // Batch preload images
  const preloadImages = async (sources, options = {}) => {
    const promises = sources.map(src => preloadImage(src, options))
    return Promise.allSettled(promises)
  }

  // Get image dimensions
  const getImageDimensions = (src) => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        resolve({
          width: img.naturalWidth,
          height: img.naturalHeight,
          aspectRatio: img.naturalWidth / img.naturalHeight
        })
      }
      img.onerror = () => {
        reject(new Error(`Failed to get dimensions for: ${src}`))
      }
      img.src = src
    })
  }

  // Generate placeholder
  const generatePlaceholder = (width, height, color = '#f0f0f0') => {
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = color
    ctx.fillRect(0, 0, width, height)
    
    return canvas.toDataURL('image/png')
  }

  // Generate blur placeholder
  const generateBlurPlaceholder = (src, width = 40, height = 40) => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // Apply blur effect
        ctx.filter = 'blur(5px)'
        ctx.drawImage(canvas, 0, 0)
        
        resolve(canvas.toDataURL('image/jpeg', 0.1))
      }
      img.onerror = () => {
        reject(new Error(`Failed to generate blur placeholder for: ${src}`))
      }
      img.src = src
    })
  }

  // Computed
  const isImageLoaded = computed(() => (src) => loadedImages.value.has(src))
  const isImageLoading = computed(() => (src) => loadingImages.value.has(src))
  const isImageFailed = computed(() => (src) => failedImages.value.has(src))

  // Initialize
  checkBrowserSupport()

  return {
    // State
    webpSupported,
    avifSupported,
    loadingImages,
    loadedImages,
    failedImages,

    // Computed
    isImageLoaded,
    isImageLoading,
    isImageFailed,

    // Methods
    getOptimizedImageUrl,
    getResponsiveImageSources,
    getSrcSet,
    getSizes,
    preloadImage,
    preloadImages,
    getImageDimensions,
    generatePlaceholder,
    generateBlurPlaceholder,
    checkBrowserSupport
  }
}
