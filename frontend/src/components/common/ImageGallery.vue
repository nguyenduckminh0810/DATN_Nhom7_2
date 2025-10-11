<template>
  <div class="image-gallery" :class="containerClass">
    <!-- Main Image -->
    <div class="main-image-container" :class="mainImageClass">
      <div class="main-image-clickable" @click="openLightbox">
        <LazyImage
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
      </div>
      
      <!-- Navigation Arrows -->
      <button 
        v-if="showNavigation"
        class="gallery-nav gallery-nav-prev"
        @click="previousImage"
        :disabled="images.length <= 1 || currentIndex === 0"
      >
        <i class="bi bi-caret-left"></i>
      </button>
      <button 
        v-if="showNavigation"
        class="gallery-nav gallery-nav-next"
        @click="nextImage"
        :disabled="images.length <= 1 || currentIndex === images.length - 1"
      >
        <i class="bi bi-caret-right"></i>
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
          <LazyImage
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

    <!-- Lightbox Modal - Using Teleport to body -->
    <Teleport to="body">
      <div 
        v-if="showLightbox" 
        class="lightbox-overlay"
        @click="closeLightbox"
      >
        <div class="lightbox-container" @click.stop>
          <button class="lightbox-close" @click="closeLightbox">
            <i class="bi bi-x"></i>
          </button>
          
          <div class="lightbox-content">
            <img
              :src="currentImage.src"
              :alt="currentImage.alt"
              class="lightbox-image-img"
              style="max-width: 90vw; max-height: 90vh; object-fit: contain;"
            />
          </div>

          <!-- Lightbox Navigation -->
          <button 
            v-if="images.length > 1"
            class="lightbox-nav lightbox-nav-prev"
            @click="previousImage"
            :disabled="currentIndex === 0"
          >
            <i class="bi bi-caret-left"></i>
          </button>
          <button 
            v-if="images.length > 1"
            class="lightbox-nav lightbox-nav-next"
            @click="nextImage"
            :disabled="currentIndex === images.length - 1"
          >
            <i class="bi bi-caret-right"></i>
          </button>

          <!-- Lightbox Counter -->
          <div v-if="images.length > 1" class="lightbox-counter">
            {{ currentIndex + 1 }} / {{ images.length }}
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, Teleport } from 'vue'
import LazyImage from './LazyImage.vue'

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
  
  // Force navbar z-index lower
  const navbar = document.querySelector('.modern-navbar')
  const topBar = document.querySelector('.top-bar')
  if (navbar) navbar.style.zIndex = '1000'
  if (topBar) topBar.style.zIndex = '1000'
  
  // Create lightbox element manually
  const existingLightbox = document.querySelector('.manual-lightbox')
  if (existingLightbox) {
    existingLightbox.remove()
  }
  
  const lightbox = document.createElement('div')
  lightbox.className = 'manual-lightbox'
  lightbox.style.cssText = `
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    background: rgba(0, 0, 0, 0.9) !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    z-index: 999999999 !important;
    visibility: visible !important;
    opacity: 1 !important;
    pointer-events: auto !important;
    transform: none !important;
    margin: 0 !important;
    padding: 0 !important;
  `
  
  // Create lightbox content
  const container = document.createElement('div')
  container.style.cssText = `
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
    display: flex;
    align-items: center;
    justify-content: center;
  `
  
  // Create close button
  const closeBtn = document.createElement('button')
  closeBtn.innerHTML = '<i class="bi bi-x"></i>'
  closeBtn.style.cssText = `
    position: absolute;
    top: -50px;
    right: 0;
    background: none;
    border: none;
    color: white;
    font-size: 2rem;
    cursor: pointer;
    z-index: 10000;
  `
  closeBtn.onclick = closeLightbox
  
  // Create navigation arrows (only if multiple images)
  if (props.images.length > 1) {
    // Previous button
    const prevBtn = document.createElement('button')
    prevBtn.innerHTML = '<i class="bi bi-caret-left"></i>'
    prevBtn.style.cssText = `
      position: absolute;
      left: -60px;
      top: 50%;
      transform: translateY(-50%);
      background: rgba(108, 117, 125, 0.8);
      border: none;
      color: white;
      font-size: 1.5rem;
      width: 40px;
      height: 40px;
      border-radius: 6px;
      cursor: pointer;
      z-index: 10000;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0.7;
      transition: all 0.2s ease;
    `
    prevBtn.onclick = () => {
      if (currentIndex.value > 0) {
        selectImage(currentIndex.value - 1)
        updateLightboxImage()
      }
    }
    
    // Next button
    const nextBtn = document.createElement('button')
    nextBtn.innerHTML = '<i class="bi bi-caret-right"></i>'
    nextBtn.style.cssText = `
      position: absolute;
      right: -60px;
      top: 50%;
      transform: translateY(-50%);
      background: rgba(108, 117, 125, 0.8);
      border: none;
      color: white;
      font-size: 1.5rem;
      width: 40px;
      height: 40px;
      border-radius: 6px;
      cursor: pointer;
      z-index: 10000;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0.7;
      transition: all 0.2s ease;
    `
    nextBtn.onclick = () => {
      if (currentIndex.value < props.images.length - 1) {
        selectImage(currentIndex.value + 1)
        updateLightboxImage()
      }
    }
    
    // Add hover effects
    prevBtn.onmouseenter = () => {
      prevBtn.style.opacity = '1'
      prevBtn.style.background = 'rgba(108, 117, 125, 1)'
    }
    prevBtn.onmouseleave = () => {
      prevBtn.style.opacity = '0.7'
      prevBtn.style.background = 'rgba(108, 117, 125, 0.8)'
    }
    
    nextBtn.onmouseenter = () => {
      nextBtn.style.opacity = '1'
      nextBtn.style.background = 'rgba(108, 117, 125, 1)'
    }
    nextBtn.onmouseleave = () => {
      nextBtn.style.opacity = '0.7'
      nextBtn.style.background = 'rgba(108, 117, 125, 0.8)'
    }
    
    // Store buttons for later access
    lightbox.prevBtn = prevBtn
    lightbox.nextBtn = nextBtn
    
    container.appendChild(prevBtn)
    container.appendChild(nextBtn)
  }
  
  // Create image counter (only if multiple images)
  if (props.images.length > 1) {
    const counter = document.createElement('div')
    counter.innerHTML = `${currentIndex.value + 1} / ${props.images.length}`
    counter.style.cssText = `
      position: absolute;
      bottom: -50px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(0, 0, 0, 0.7);
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 0.9rem;
      backdrop-filter: blur(10px);
    `
    lightbox.counter = counter
    container.appendChild(counter)
  }
  
  // Create image
  const img = document.createElement('img')
  img.src = currentImage.value?.src || ''
  img.alt = currentImage.value?.alt || ''
  img.style.cssText = `
    max-width: 90vw;
    max-height: 90vh;
    object-fit: contain;
  `
  lightbox.img = img
  
  // Store current index for updates
  lightbox.currentIndex = currentIndex.value
  
  // Assemble lightbox
  container.appendChild(closeBtn)
  container.appendChild(img)
  lightbox.appendChild(container)
  document.body.appendChild(lightbox)
  
  // Add click to close
  lightbox.onclick = (e) => {
    if (e.target === lightbox) {
      closeLightbox()
    }
  }
  
  // Add keyboard navigation
  const handleKeydown = (e) => {
    if (e.key === 'ArrowLeft' && currentIndex.value > 0) {
      selectImage(currentIndex.value - 1)
      updateLightboxImage()
    } else if (e.key === 'ArrowRight' && currentIndex.value < props.images.length - 1) {
      selectImage(currentIndex.value + 1)
      updateLightboxImage()
    } else if (e.key === 'Escape') {
      closeLightbox()
    }
  }
  
  document.addEventListener('keydown', handleKeydown)
  lightbox.handleKeydown = handleKeydown
  
}

// Function to update lightbox image and navigation
const updateLightboxImage = () => {
  const lightbox = document.querySelector('.manual-lightbox')
  if (lightbox && lightbox.img) {
    // Update image
    lightbox.img.src = currentImage.value?.src || ''
    lightbox.img.alt = currentImage.value?.alt || ''
    
    // Update counter
    if (lightbox.counter) {
      lightbox.counter.innerHTML = `${currentIndex.value + 1} / ${props.images.length}`
    }
    
    // Update button states
    if (lightbox.prevBtn) {
      lightbox.prevBtn.style.opacity = currentIndex.value > 0 ? '0.7' : '0.3'
      lightbox.prevBtn.style.cursor = currentIndex.value > 0 ? 'pointer' : 'not-allowed'
    }
    
    if (lightbox.nextBtn) {
      lightbox.nextBtn.style.opacity = currentIndex.value < props.images.length - 1 ? '0.7' : '0.3'
      lightbox.nextBtn.style.cursor = currentIndex.value < props.images.length - 1 ? 'pointer' : 'not-allowed'
    }
  }
}

const closeLightbox = () => {
  showLightbox.value = false
  emit('lightboxClose')
  
  // Restore body scroll
  document.body.style.overflow = ''
  
  // Remove manual lightbox
  const manualLightbox = document.querySelector('.manual-lightbox')
  if (manualLightbox) {
    // Remove keyboard event listener
    if (manualLightbox.handleKeydown) {
      document.removeEventListener('keydown', manualLightbox.handleKeydown)
    }
    manualLightbox.remove()
  }
  
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

<style>
.image-gallery {
  position: relative;
  width: 100%;
  height: 100%;
}

.main-image-container {
  position: relative;
  margin-bottom: 0;
  border-radius: 16px;
  overflow: hidden;
  background: #f8f9fa;
  width: 100%;
  aspect-ratio: 1 / 1;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.main-image-clickable {
  width: 100%;
  height: 100%;
  cursor: zoom-in;
}

.main-image {
  width: 100%;
  height: 100%;
  aspect-ratio: 1 / 1;
}

.main-image-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  display: block;
}

.main-image-container:hover .main-image-img {
  transform: scale(1.02);
}

/* Enhanced Navigation Arrows */
.gallery-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(108, 117, 125, 0.8);
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
  font-size: 1rem;
  backdrop-filter: blur(8px);
  opacity: 0.7;
}

.gallery-nav:hover:not(:disabled) {
  background: rgba(108, 117, 125, 1);
  transform: translateY(-50%) scale(1.05);
  opacity: 1;
}

.gallery-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  background: rgba(108, 117, 125, 0.5);
}

.gallery-nav-prev {
  left: 20px;
}

.gallery-nav-next {
  right: 20px;
}

/* Enhanced Image Counter */
.image-counter {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  box-shadow: 0 2px 10px rgba(220,53,69,0.3);
}

/* Enhanced Zoom Button */
.zoom-button {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  border: 2px solid rgba(255,255,255,0.2);
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  font-size: 1.1rem;
  backdrop-filter: blur(10px);
}

.zoom-button:hover {
  background: rgba(220, 53, 69, 1);
  transform: scale(1.15);
  box-shadow: 0 4px 15px rgba(220,53,69,0.4);
  border-color: rgba(255,255,255,0.4);
}

/* Enhanced Thumbnails */
.thumbnails-container {
  margin-top: 1.5rem;
  padding: 0;
}

.thumbnails-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 0;
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
  border-radius: 12px;
  overflow: hidden;
  border: 3px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.thumbnail-item:hover {
  border-color: rgba(220, 53, 69, 0.5);
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(220,53,69,0.2);
}

.thumbnail-item.active {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220,53,69,0.3), 0 4px 15px rgba(220,53,69,0.3);
  transform: scale(1.05);
}

.thumbnail {
  width: 85px;
  height: 85px;
  display: block;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* Lightbox - Global styles for Teleport */
.lightbox-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.9) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 999999999 !important;
  animation: none !important;
  visibility: visible !important;
  opacity: 1 !important;
  pointer-events: auto !important;
  transform: none !important;
}

/* Nuclear option: Force lightbox to be visible */
.lightbox-overlay,
.lightbox-overlay *,
.lightbox-container,
.lightbox-container *,
.lightbox-content,
.lightbox-content *,
.lightbox-image-img {
  visibility: visible !important;
  opacity: 1 !important;
  display: block !important;
}

.lightbox-overlay {
  display: flex !important;
}

.lightbox-container {
  display: flex !important;
}

/* Force navbar to be below lightbox */
body:has(.lightbox-overlay) .modern-navbar,
body:has(.lightbox-overlay) .top-bar {
  z-index: 1000 !important;
}

/* Alternative: Force all elements below lightbox */
.lightbox-overlay ~ * {
  z-index: 1000 !important;
}

/* Force lightbox to be on top of everything */
.lightbox-overlay,
.lightbox-overlay * {
  z-index: 999999999 !important;
  position: relative !important;
}

/* Override any conflicting z-index from other components */
.lightbox-overlay {
  z-index: 999999999 !important;
}

.lightbox-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
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
  bottom: -60px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 1rem;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
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
