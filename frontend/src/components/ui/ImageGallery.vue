<template>
  <div class="image-gallery" :class="containerClass">
    <!-- Main Image -->
    <div class="main-image-container" :class="mainImageClass">
      <OptimizedProductImage
        :src="currentImage.src"
        :alt="currentImage.alt"
        :width="mainImageWidth"
        :height="mainImageHeight"
        :quality="90"
        :webp="true"
        :lazy="false"
        :show-zoom-overlay="true"
        container-class="main-image"
        image-class="main-image-img"
        @load="handleMainImageLoad"
        @error="handleMainImageError"
      />
      
      <!-- Navigation Arrows -->
      <button 
        v-if="showNavigation && images.length > 1"
        class="gallery-nav gallery-nav-prev"
        @click="previousImage"
        :disabled="currentIndex === 0"
      >
        <i class="ph-caret-left"></i>
      </button>
      <button 
        v-if="showNavigation && images.length > 1"
        class="gallery-nav gallery-nav-next"
        @click="nextImage"
        :disabled="currentIndex === images.length - 1"
      >
        <i class="ph-caret-right"></i>
      </button>

      <!-- Image Counter -->
      <div v-if="showCounter && images.length > 1" class="image-counter">
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>

      <!-- Zoom Button -->
      <button 
        v-if="showZoomButton"
        class="zoom-button"
        @click="openLightbox"
      >
        <i class="ph-magnifying-glass-plus"></i>
      </button>
    </div>

    <!-- Thumbnail Navigation -->
    <div v-if="showThumbnails && images.length > 1" class="thumbnails-container" :class="thumbnailsClass">
      <div class="thumbnails-scroll" ref="thumbnailsScroll">
        <div 
          v-for="(image, index) in images" 
          :key="index"
          class="thumbnail-item"
          :class="{ active: index === currentIndex }"
          @click="selectImage(index)"
        >
          <OptimizedProductImage
            :src="image.src"
            :alt="image.alt"
            :width="thumbnailWidth"
            :height="thumbnailHeight"
            :quality="70"
            :webp="true"
            :lazy="true"
            container-class="thumbnail"
            image-class="thumbnail-img"
          />
        </div>
      </div>
    </div>

    <!-- Lightbox Modal -->
    <div 
      v-if="showLightbox" 
      class="lightbox-overlay"
      @click="closeLightbox"
    >
      <div class="lightbox-container" @click.stop>
        <button class="lightbox-close" @click="closeLightbox">
          <i class="ph-x"></i>
        </button>
        
        <div class="lightbox-content">
          <OptimizedProductImage
            :src="currentImage.src"
            :alt="currentImage.alt"
            :width="lightboxWidth"
            :height="lightboxHeight"
            :quality="95"
            :webp="true"
            :lazy="false"
            container-class="lightbox-image"
            image-class="lightbox-image-img"
          />
        </div>

        <!-- Lightbox Navigation -->
        <button 
          v-if="images.length > 1"
          class="lightbox-nav lightbox-nav-prev"
          @click="previousImage"
          :disabled="currentIndex === 0"
        >
          <i class="ph-caret-left"></i>
        </button>
        <button 
          v-if="images.length > 1"
          class="lightbox-nav lightbox-nav-next"
          @click="nextImage"
          :disabled="currentIndex === images.length - 1"
        >
          <i class="ph-caret-right"></i>
        </button>

        <!-- Lightbox Counter -->
        <div v-if="images.length > 1" class="lightbox-counter">
          {{ currentIndex + 1 }} / {{ images.length }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import OptimizedProductImage from './OptimizedProductImage.vue'

const props = defineProps({
  images: {
    type: Array,
    required: true,
    validator: (images) => {
      return images.every(img => img.src && img.alt)
    }
  },
  initialIndex: {
    type: Number,
    default: 0
  },
  // Display options
  showNavigation: {
    type: Boolean,
    default: true
  },
  showThumbnails: {
    type: Boolean,
    default: true
  },
  showCounter: {
    type: Boolean,
    default: true
  },
  showZoomButton: {
    type: Boolean,
    default: true
  },
  // Dimensions
  mainImageWidth: {
    type: [String, Number],
    default: 600
  },
  mainImageHeight: {
    type: [String, Number],
    default: 600
  },
  thumbnailWidth: {
    type: [String, Number],
    default: 80
  },
  thumbnailHeight: {
    type: [String, Number],
    default: 80
  },
  lightboxWidth: {
    type: [String, Number],
    default: 1200
  },
  lightboxHeight: {
    type: [String, Number],
    default: 1200
  },
  // CSS classes
  containerClass: {
    type: [String, Array, Object],
    default: ''
  },
  mainImageClass: {
    type: [String, Array, Object],
    default: ''
  },
  thumbnailsClass: {
    type: [String, Array, Object],
    default: ''
  }
})

const emit = defineEmits(['imageChange', 'lightboxOpen', 'lightboxClose'])

// Reactive state
const currentIndex = ref(props.initialIndex)
const showLightbox = ref(false)
const thumbnailsScroll = ref(null)

// Computed
const currentImage = computed(() => {
  return props.images[currentIndex.value] || props.images[0]
})

// Methods
const selectImage = (index) => {
  if (index >= 0 && index < props.images.length) {
    currentIndex.value = index
    emit('imageChange', { index, image: props.images[index] })
  }
}

const previousImage = () => {
  if (currentIndex.value > 0) {
    selectImage(currentIndex.value - 1)
  }
}

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    selectImage(currentIndex.value + 1)
  }
}

const openLightbox = () => {
  showLightbox.value = true
  emit('lightboxOpen', { index: currentIndex.value, image: currentImage.value })
  
  // Prevent body scroll
  document.body.style.overflow = 'hidden'
}

const closeLightbox = () => {
  showLightbox.value = false
  emit('lightboxClose')
  
  // Restore body scroll
  document.body.style.overflow = ''
}

const handleMainImageLoad = (event) => {
  // Handle main image load
}

const handleMainImageError = (event) => {
  // Handle main image error
}

const scrollThumbnailIntoView = () => {
  if (thumbnailsScroll.value) {
    const activeThumbnail = thumbnailsScroll.value.querySelector('.thumbnail-item.active')
    if (activeThumbnail) {
      activeThumbnail.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'center'
      })
    }
  }
}

// Keyboard navigation
const handleKeydown = (event) => {
  if (!showLightbox.value) return
  
  switch (event.key) {
    case 'Escape':
      closeLightbox()
      break
    case 'ArrowLeft':
      event.preventDefault()
      previousImage()
      break
    case 'ArrowRight':
      event.preventDefault()
      nextImage()
      break
  }
}

// Watch for changes
watch(currentIndex, () => {
  nextTick(() => {
    scrollThumbnailIntoView()
  })
})

watch(() => props.initialIndex, (newIndex) => {
  if (newIndex >= 0 && newIndex < props.images.length) {
    currentIndex.value = newIndex
  }
})

// Lifecycle
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  // Restore body scroll in case component is unmounted while lightbox is open
  document.body.style.overflow = ''
})
</script>

<style scoped>
.image-gallery {
  position: relative;
}

.main-image-container {
  position: relative;
  margin-bottom: 1rem;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
}

.main-image {
  width: 100%;
  aspect-ratio: 1 / 1;
}

.main-image-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.main-image-container:hover .main-image-img {
  transform: scale(1.02);
}

/* Navigation Arrows */
.gallery-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
}

.gallery-nav:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.7);
  transform: translateY(-50%) scale(1.1);
}

.gallery-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.gallery-nav-prev {
  left: 15px;
}

.gallery-nav-next {
  right: 15px;
}

/* Image Counter */
.image-counter {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Zoom Button */
.zoom-button {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
}

.zoom-button:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

/* Thumbnails */
.thumbnails-container {
  margin-top: 1rem;
}

.thumbnails-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 5px 0;
  scrollbar-width: thin;
  scrollbar-color: #ccc transparent;
}

.thumbnails-scroll::-webkit-scrollbar {
  height: 6px;
}

.thumbnails-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.thumbnails-scroll::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.thumbnail-item {
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item:hover {
  border-color: #007bff;
  transform: scale(1.05);
}

.thumbnail-item.active {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.thumbnail {
  width: 80px;
  height: 80px;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Lightbox */
.lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.lightbox-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lightbox-close {
  position: absolute;
  top: -50px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  z-index: 10000;
  transition: opacity 0.3s ease;
}

.lightbox-close:hover {
  opacity: 0.7;
}

.lightbox-content {
  position: relative;
}

.lightbox-image {
  max-width: 100%;
  max-height: 100%;
}

.lightbox-image-img {
  width: 100%;
  height: auto;
  max-height: 80vh;
  object-fit: contain;
}

/* Lightbox Navigation */
.lightbox-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.5rem;
}

.lightbox-nav:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

.lightbox-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.lightbox-nav-prev {
  left: -70px;
}

.lightbox-nav-next {
  right: -70px;
}

/* Lightbox Counter */
.lightbox-counter {
  position: absolute;
  bottom: -50px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 1rem;
  font-weight: 500;
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .gallery-nav {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
  
  .gallery-nav-prev {
    left: 10px;
  }
  
  .gallery-nav-next {
    right: 10px;
  }
  
  .lightbox-nav {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
  
  .lightbox-nav-prev {
    left: -50px;
  }
  
  .lightbox-nav-next {
    right: -50px;
  }
  
  .thumbnail {
    width: 60px;
    height: 60px;
  }
}
</style>
