<template>
  <div 
    class="lazy-image-container" 
    :class="containerClass"
    :style="containerStyle"
  >
    <!-- Placeholder -->
    <div 
      v-if="!loaded && !error"
      class="lazy-image-placeholder"
      :class="placeholderClass"
    >
      <div class="placeholder-content">
        <div v-if="showSpinner" class="spinner"></div>
        <i v-else-if="placeholderIcon" :class="placeholderIcon"></i>
        <span v-if="placeholderText">{{ placeholderText }}</span>
      </div>
    </div>

    <!-- Error State -->
    <div 
      v-if="error"
      class="lazy-image-error"
      :class="errorClass"
    >
      <div class="error-content">
        <i :class="errorIcon"></i>
        <span>{{ errorText }}</span>
      </div>
    </div>

    <!-- Actual Image -->
    <img
      v-show="loaded && !error"
      ref="imageRef"
      :src="actualSrc"
      :alt="alt"
      :class="imageClass"
      :style="imageStyle"
      @load="handleLoad"
      @error="handleError"
      loading="lazy"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  // Image source
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  
  // Placeholder
  placeholder: {
    type: String,
    default: ''
  },
  placeholderIcon: {
    type: String,
    default: 'bi bi-image'
  },
  placeholderText: {
    type: String,
    default: ''
  },
  showSpinner: {
    type: Boolean,
    default: true
  },
  
  // Error handling
  errorIcon: {
    type: String,
    default: 'bi bi-exclamation-triangle-fill'
  },
  errorText: {
    type: String,
    default: 'Không thể tải hình ảnh'
  },
  fallbackSrc: {
    type: String,
    default: ''
  },
  
  // Styling
  width: {
    type: [String, Number],
    default: null
  },
  height: {
    type: [String, Number],
    default: null
  },
  objectFit: {
    type: String,
    default: 'cover',
    validator: (value) => ['cover', 'contain', 'fill', 'scale-down', 'none'].includes(value)
  },
  
  // Aspect ratio
  aspectRatio: {
    type: [String, Number],
    default: null
  },
  
  // Loading behavior
  threshold: {
    type: Number,
    default: 0.1
  },
  rootMargin: {
    type: String,
    default: '50px'
  },
  
  // Animation
  fadeIn: {
    type: Boolean,
    default: true
  },
  fadeInDuration: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['load', 'error', 'intersect'])

// Refs
const imageRef = ref(null)
const containerRef = ref(null)

// State
const loaded = ref(false)
const error = ref(false)
const intersecting = ref(false)
const observer = ref(null)

// Computed
const actualSrc = computed(() => {
  if (error.value && props.fallbackSrc) {
    return props.fallbackSrc
  }
  return props.src
})

const containerClass = computed(() => [
  'lazy-image-container',
  {
    'is-loaded': loaded.value,
    'has-error': error.value,
    'is-intersecting': intersecting.value,
    'fade-in': props.fadeIn && loaded.value
  }
])

const containerStyle = computed(() => {
  const style = {}
  
  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  
  if (props.height) {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  
  if (props.aspectRatio) {
    style.aspectRatio = typeof props.aspectRatio === 'number' 
      ? `${props.aspectRatio}` 
      : props.aspectRatio
  }
  
  if (props.fadeIn && loaded.value) {
    style.transition = `opacity ${props.fadeInDuration}ms ease-in-out`
  }
  
  return style
})

const imageClass = computed(() => [
  'lazy-image',
  {
    'fade-in': props.fadeIn && loaded.value
  }
])

const imageStyle = computed(() => ({
  objectFit: props.objectFit,
  width: '100%',
  height: '100%'
}))

const placeholderClass = computed(() => [
  'lazy-image-placeholder',
  {
    'with-spinner': props.showSpinner,
    'with-icon': props.placeholderIcon,
    'with-text': props.placeholderText
  }
])

const errorClass = computed(() => [
  'lazy-image-error',
  {
    'with-icon': props.errorIcon,
    'with-text': props.errorText
  }
])

// Methods
const handleLoad = () => {
  loaded.value = true
  error.value = false
  emit('load', actualSrc.value)
}

const handleError = () => {
  if (!error.value && props.fallbackSrc && actualSrc.value !== props.fallbackSrc) {
    // Try fallback first
    error.value = true
    setTimeout(() => {
      error.value = false
    }, 100)
  } else {
    error.value = true
    emit('error', actualSrc.value)
  }
}

const handleIntersection = (entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      intersecting.value = true
      emit('intersect')
      
      // Disconnect observer after first intersection
      if (observer.value) {
        observer.value.disconnect()
        observer.value = null
      }
    }
  })
}

const setupIntersectionObserver = () => {
  if (!containerRef.value || typeof IntersectionObserver === 'undefined') {
    return
  }
  
  observer.value = new IntersectionObserver(handleIntersection, {
    threshold: props.threshold,
    rootMargin: props.rootMargin
  })
  
  observer.value.observe(containerRef.value)
}

// Lifecycle
onMounted(() => {
  containerRef.value = imageRef.value?.parentElement
  setupIntersectionObserver()
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})

// Watch for src changes
watch(() => props.src, () => {
  loaded.value = false
  error.value = false
  intersecting.value = false
})

// Expose methods
defineExpose({
  loaded,
  error,
  intersecting,
  reload: () => {
    loaded.value = false
    error.value = false
  }
})
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: #f3f4f6;
  border-radius: 0.5rem;
}

.lazy-image-container.is-loaded {
  background-color: transparent;
}

.lazy-image-placeholder,
.lazy-image-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
  color: #6b7280;
}

.lazy-image-placeholder {
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.placeholder-content,
.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  text-align: center;
}

.spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.lazy-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.lazy-image.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.lazy-image-container.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

.lazy-image-error {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.lazy-image-error .error-content i {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.lazy-image-error .error-content span {
  font-size: 0.875rem;
}

/* Responsive behavior */
@media (max-width: 640px) {
  .lazy-image-container {
    border-radius: 0.375rem;
  }
  
  .spinner {
    width: 1.5rem;
    height: 1.5rem;
    border-width: 2px;
  }
}

/* Aspect ratio utilities */
.lazy-image-container[style*="aspect-ratio"] {
  height: auto;
}

/* Common aspect ratios */
.aspect-square {
  aspect-ratio: 1 / 1;
}

.aspect-video {
  aspect-ratio: 16 / 9;
}

.aspect-4-3 {
  aspect-ratio: 4 / 3;
}

.aspect-3-2 {
  aspect-ratio: 3 / 2;
}
</style>