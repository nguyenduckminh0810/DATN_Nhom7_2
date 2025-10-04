<template>
  <div class="optimized-product-image" :class="containerClass">
    <!-- Lazy Image with optimization -->
    <LazyImage
      :src="optimizedSrc"
      :alt="alt"
      :width="width"
      :height="height"
      :lazy="lazy"
      :webp="webp"
      :quality="quality"
      :container-class="imageContainerClass"
      :image-class="imageClass"
      :placeholder-class="placeholderClass"
      :error-class="errorClass"
      :loading-class="loadingClass"
      @load="handleImageLoad"
      @error="handleImageError"
    />

    <!-- Loading Overlay -->
    <div v-if="showLoadingOverlay && isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Đang tải...</span>
        </div>
        <div v-if="loadingText" class="loading-text">{{ loadingText }}</div>
      </div>
    </div>

    <!-- Error Overlay -->
    <div v-if="showErrorOverlay && hasError" class="error-overlay">
      <div class="error-content">
        <i class="ph-warning text-danger" style="font-size: 2rem;"></i>
        <div v-if="errorText" class="error-text">{{ errorText }}</div>
        <button v-if="showRetryButton" class="btn btn-sm btn-outline-primary mt-2" @click="retryLoad">
          <i class="ph-arrow-clockwise me-1"></i>Thử lại
        </button>
      </div>
    </div>

    <!-- Zoom Overlay (for product detail) -->
    <div v-if="showZoomOverlay && isHovered" class="zoom-overlay">
      <div class="zoom-content">
        <i class="ph-magnifying-glass-plus text-white" style="font-size: 1.5rem;"></i>
        <div class="zoom-text">Nhấp để phóng to</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import LazyImage from './LazyImage.vue'
import { useImageOptimization } from '../../composables/useImageOptimization'

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
  webp: {
    type: Boolean,
    default: true
  },
  quality: {
    type: Number,
    default: 85,
    validator: (value) => value >= 0 && value <= 100
  },
  // Overlay options
  showLoadingOverlay: {
    type: Boolean,
    default: false
  },
  showErrorOverlay: {
    type: Boolean,
    default: true
  },
  showZoomOverlay: {
    type: Boolean,
    default: false
  },
  showRetryButton: {
    type: Boolean,
    default: true
  },
  // Text options
  loadingText: {
    type: String,
    default: 'Đang tải hình ảnh...'
  },
  errorText: {
    type: String,
    default: 'Không thể tải hình ảnh'
  },
  // CSS classes
  containerClass: {
    type: [String, Array, Object],
    default: ''
  },
  imageContainerClass: {
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

const emit = defineEmits(['load', 'error', 'retry'])

// Composables
const { getOptimizedImageUrl } = useImageOptimization()

// Local state
const isLoading = ref(false)
const hasError = ref(false)
const isHovered = ref(false)

// Computed
const optimizedSrc = computed(() => {
  return getOptimizedImageUrl(props.src, {
    width: props.width,
    height: props.height,
    quality: props.quality,
    format: props.webp ? 'auto' : 'original',
    fit: 'cover'
  })
})

// Methods
const handleImageLoad = (event) => {
  isLoading.value = false
  hasError.value = false
  emit('load', event)
}

const handleImageError = (event) => {
  isLoading.value = false
  hasError.value = true
  emit('error', event)
}

const retryLoad = () => {
  hasError.value = false
  isLoading.value = true
  emit('retry')
}

const handleMouseEnter = () => {
  isHovered.value = true
}

const handleMouseLeave = () => {
  isHovered.value = false
}

// Lifecycle
onMounted(() => {
  if (props.showZoomOverlay) {
    const container = document.querySelector('.optimized-product-image')
    if (container) {
      container.addEventListener('mouseenter', handleMouseEnter)
      container.addEventListener('mouseleave', handleMouseLeave)
    }
  }
})
</script>

<style scoped>
.optimized-product-image {
  position: relative;
  display: inline-block;
  overflow: hidden;
  border-radius: 8px;
}

.loading-overlay,
.error-overlay,
.zoom-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.loading-overlay {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(2px);
}

.error-overlay {
  background-color: rgba(248, 249, 250, 0.95);
  backdrop-filter: blur(2px);
}

.zoom-overlay {
  background-color: rgba(0, 0, 0, 0.6);
  opacity: 0;
  pointer-events: none;
}

.optimized-product-image:hover .zoom-overlay {
  opacity: 1;
}

.loading-content,
.error-content,
.zoom-content {
  text-align: center;
  color: #6c757d;
}

.zoom-content {
  color: white;
}

.loading-text,
.error-text,
.zoom-text {
  font-size: 0.875rem;
  margin-top: 8px;
  opacity: 0.8;
}

.zoom-text {
  color: white;
  opacity: 0.9;
}

/* Product image specific styles */
.optimized-product-image.product-image {
  width: 100%;
  aspect-ratio: 1 / 1;
}

.optimized-product-image.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.optimized-product-image.product-image:hover img {
  transform: scale(1.05);
}

/* Responsive styles */
@media (max-width: 768px) {
  .loading-text,
  .error-text {
    font-size: 0.75rem;
  }
  
  .zoom-overlay {
    display: none; /* Hide zoom on mobile */
  }
}

/* Animation for loading state */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.loading-overlay .spinner-border {
  animation: pulse 1.5s ease-in-out infinite;
}
</style>
