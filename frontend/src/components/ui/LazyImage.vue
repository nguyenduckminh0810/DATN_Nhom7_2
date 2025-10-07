<template>
  <div class="lazy-image-container" :class="containerClass">
    <!-- Placeholder/Skeleton -->
    <div 
      v-if="!loaded && !error" 
      class="lazy-image-placeholder"
      :class="placeholderClass"
      :style="placeholderStyle"
    >
      <div class="placeholder-content">
        <i class="ph-image text-muted" :style="{ fontSize: placeholderIconSize }"></i>
        <div v-if="showPlaceholderText" class="placeholder-text">
          {{ placeholderText }}
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div 
      v-if="error" 
      class="lazy-image-error"
      :class="errorClass"
      :style="errorStyle"
    >
      <div class="error-content">
        <i class="ph-warning text-danger" :style="{ fontSize: errorIconSize }"></i>
        <div v-if="showErrorText" class="error-text">
          {{ errorText }}
        </div>
      </div>
    </div>

    <!-- Actual Image -->
    <img
      v-show="loaded && !error"
      ref="imageElement"
      :src="optimizedSrc"
      :alt="alt"
      :class="imageClass"
      :style="imageStyle"
      @load="handleLoad"
      @error="handleError"
      :loading="lazy ? 'lazy' : 'eager'"
      :decoding="decoding"
    />

    <!-- Loading Overlay -->
    <div 
      v-if="loading && !error" 
      class="lazy-image-loading"
      :class="loadingClass"
    >
      <div class="loading-spinner">
        <div class="spinner-border spinner-border-sm text-primary" role="status">
          <span class="visually-hidden">Đang tải...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: null
  },
  height: {
    type: [String, Number],
    default: null
  },
  lazy: {
    type: Boolean,
    default: true
  },
  decoding: {
    type: String,
    default: 'async',
    validator: (value) => ['sync', 'async', 'auto'].includes(value)
  },
  // WebP support
  webp: {
    type: Boolean,
    default: true
  },
  // Quality for WebP (0-100)
  quality: {
    type: Number,
    default: 80,
    validator: (value) => value >= 0 && value <= 100
  },
  // Placeholder options
  placeholderText: {
    type: String,
    default: 'Đang tải hình ảnh...'
  },
  showPlaceholderText: {
    type: Boolean,
    default: false
  },
  // Error options
  errorText: {
    type: String,
    default: 'Không thể tải hình ảnh'
  },
  showErrorText: {
    type: Boolean,
    default: false
  },
  // CSS classes
  containerClass: {
    type: [String, Array, Object],
    default: ''
  },
  imageClass: {
    type: [String, Array, Object],
    default: ''
  },
  placeholderClass: {
    type: [String, Array, Object],
    default: ''
  },
  errorClass: {
    type: [String, Array, Object],
    default: ''
  },
  loadingClass: {
    type: [String, Array, Object],
    default: ''
  }
})

const emit = defineEmits(['load', 'error'])

// Reactive state
const loaded = ref(false)
const loading = ref(false)
const error = ref(false)
const imageElement = ref(null)

// Computed
const optimizedSrc = computed(() => {
  if (!props.src) return ''
  
  // If it's already a WebP image or external URL, return as is
  if (props.src.includes('.webp') || props.src.startsWith('http')) {
    return props.src
  }
  
  // For local images, we can't convert to WebP without a backend
  // But we can add query parameters for optimization
  const url = new URL(props.src, window.location.origin)
  
  if (props.webp && supportsWebP()) {
    url.searchParams.set('format', 'webp')
  }
  
  if (props.quality && props.quality !== 80) {
    url.searchParams.set('quality', props.quality.toString())
  }
  
  if (props.width) {
    url.searchParams.set('w', props.width.toString())
  }
  
  if (props.height) {
    url.searchParams.set('h', props.height.toString())
  }
  
  return url.toString()
})

const placeholderStyle = computed(() => {
  const style = {}
  if (props.width) style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  if (props.height) style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  return style
})

const errorStyle = computed(() => {
  const style = {}
  if (props.width) style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  if (props.height) style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  return style
})

const imageStyle = computed(() => {
  const style = {}
  if (props.width) style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  if (props.height) style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  return style
})

const placeholderIconSize = computed(() => {
  const size = Math.min(
    typeof props.width === 'number' ? props.width : 100,
    typeof props.height === 'number' ? props.height : 100
  )
  return `${Math.max(24, size * 0.2)}px`
})

const errorIconSize = computed(() => {
  const size = Math.min(
    typeof props.width === 'number' ? props.width : 100,
    typeof props.height === 'number' ? props.height : 100
  )
  return `${Math.max(24, size * 0.2)}px`
})

// Methods
const supportsWebP = () => {
  const canvas = document.createElement('canvas')
  canvas.width = 1
  canvas.height = 1
  return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
}

const handleLoad = () => {
  loaded.value = true
  loading.value = false
  error.value = false
  emit('load', { src: props.src, element: imageElement.value })
}

const handleError = () => {
  error.value = true
  loading.value = false
  loaded.value = false
  emit('error', { src: props.src, element: imageElement.value })
}

const preloadImage = () => {
  if (loaded.value || loading.value) return
  
  loading.value = true
  error.value = false
  
  const img = new Image()
  img.onload = () => {
    if (imageElement.value) {
      imageElement.value.src = optimizedSrc.value
    }
  }
  img.onerror = () => {
    handleError()
  }
  img.src = optimizedSrc.value
}

// Intersection Observer for lazy loading
let observer = null

const setupIntersectionObserver = () => {
  if (!props.lazy || !imageElement.value) return
  
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          preloadImage()
          if (observer) {
            observer.unobserve(entry.target)
          }
        }
      })
    },
    {
      rootMargin: '50px 0px',
      threshold: 0.1
    }
  )
  
  observer.observe(imageElement.value)
}

// Lifecycle
onMounted(() => {
  if (!props.lazy) {
    preloadImage()
  } else {
    // For lazy loading, we need to wait for the next tick
    // to ensure the image element is rendered
    setTimeout(() => {
      setupIntersectionObserver()
    }, 0)
  }
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})

// Watch for src changes
watch(() => props.src, () => {
  loaded.value = false
  loading.value = false
  error.value = false
  
  if (!props.lazy) {
    preloadImage()
  } else {
    setupIntersectionObserver()
  }
})
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  display: inline-block;
  overflow: hidden;
}

.lazy-image-placeholder,
.lazy-image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  min-height: 100px;
  min-width: 100px;
}

.placeholder-content,
.error-content {
  text-align: center;
  color: #6c757d;
}

.placeholder-text,
.error-text {
  font-size: 0.875rem;
  margin-top: 8px;
  opacity: 0.8;
}

.lazy-image-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(2px);
}

.loading-spinner {
  animation: fadeIn 0.3s ease-in-out;
}

.lazy-image-container img {
  display: block;
  max-width: 100%;
  height: auto;
  transition: opacity 0.3s ease-in-out;
}

.lazy-image-container img[src=""] {
  opacity: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Responsive image styles */
.lazy-image-container.responsive {
  width: 100%;
}

.lazy-image-container.responsive img {
  width: 100%;
  height: auto;
}

/* Aspect ratio containers */
.lazy-image-container.aspect-16-9 {
  aspect-ratio: 16 / 9;
}

.lazy-image-container.aspect-4-3 {
  aspect-ratio: 4 / 3;
}

.lazy-image-container.aspect-1-1 {
  aspect-ratio: 1 / 1;
}

.lazy-image-container.aspect-16-9 img,
.lazy-image-container.aspect-4-3 img,
.lazy-image-container.aspect-1-1 img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
