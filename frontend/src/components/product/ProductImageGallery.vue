<template>
  <div class="product-image-gallery">
    <!-- Main Image -->
    <div class="main-image-container">
      <div class="main-image-wrapper" @click="openLightbox">
        <LazyImage
          :src="selectedImage"
          :alt="productName"
          :width="600"
          :height="600"
          :quality="90"
          :webp="true"
          :lazy="true"
          container-class="main-image"
          image-class="img-fluid"
          show-zoom-overlay="true"
        />
        
        <!-- Video Play Button Overlay -->
        <div v-if="hasVideo" class="video-overlay">
          <button class="video-play-btn" @click="playVideo">
            <i class="bi bi-play-circle-fill"></i>
          </button>
        </div>
        
        <!-- Zoom Indicator -->
        <div class="zoom-indicator">
          <i class="bi bi-search"></i>
          <span>Click để phóng to</span>
        </div>
      </div>
    </div>
    
    <!-- Thumbnail Gallery -->
    <div class="thumbnail-gallery">
      <div 
        v-for="(image, index) in images" 
        :key="index"
        class="thumbnail-item"
        :class="{ active: selectedImage === image }"
        @click="selectImage(image)"
      >
        <LazyImage
          :src="image"
          :alt="`${productName} - ${index + 1}`"
          :width="80"
          :height="80"
          :quality="80"
          :webp="true"
          :lazy="true"
          container-class="thumbnail-wrapper"
          image-class="thumbnail-image"
        />
        
        <!-- Video Thumbnail Indicator -->
        <div v-if="index === 0 && hasVideo" class="video-indicator">
          <i class="bi bi-play"></i>
        </div>
      </div>
    </div>
    
    <!-- Lightbox Modal -->
    <div v-if="showLightbox" class="lightbox-overlay" @click="closeLightbox">
      <div class="lightbox-content" @click.stop>
        <button class="lightbox-close" @click="closeLightbox">
          <i class="bi bi-x"></i>
        </button>
        <LazyImage
          :src="selectedImage"
          :alt="productName"
          :width="800"
          :height="800"
          :quality="95"
          :webp="true"
          :lazy="true"
          container-class="lightbox-image"
          image-class="img-fluid"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import LazyImage from '../common/LazyImage.vue'

const props = defineProps({
  images: {
    type: Array,
    required: true,
    default: () => []
  },
  productName: {
    type: String,
    required: true,
    default: ''
  },
  videoUrl: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['play-video'])

const selectedImage = ref(props.images[0] || '')
const showLightbox = ref(false)

const hasVideo = computed(() => !!props.videoUrl)

const selectImage = (image) => {
  selectedImage.value = image
}

const openLightbox = () => {
  showLightbox.value = true
}

const closeLightbox = () => {
  showLightbox.value = false
}

const playVideo = () => {
  emit('play-video', props.videoUrl)
}
</script>

<style scoped>
.product-image-gallery {
  position: relative;
}

.main-image-container {
  position: relative;
  margin-bottom: 1rem;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.main-image-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.main-image-wrapper:hover {
  transform: scale(1.02);
}

.main-image {
  width: 100%;
  height: 500px;
  object-fit: cover;
  border-radius: 12px;
}

.video-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
}

.video-play-btn {
  background: rgba(255,255,255,0.9);
  border: none;
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #007bff;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.video-play-btn:hover {
  background: white;
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(0,0,0,0.3);
}

.zoom-indicator {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  background: rgba(0,0,0,0.7);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.main-image-wrapper:hover .zoom-indicator {
  opacity: 1;
}

.thumbnail-gallery {
  display: flex;
  gap: 0.75rem;
  overflow-x: auto;
  padding: 0.5rem 0;
}

.thumbnail-item {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.2);
}

.thumbnail-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.thumbnail-wrapper {
  width: 80px;
  height: 80px;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0,0,0,0.7);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

/* Lightbox */
.lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.lightbox-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.lightbox-close {
  position: absolute;
  top: -3rem;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  z-index: 10000;
}

.lightbox-image {
  border-radius: 8px;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* Responsive */
@media (max-width: 768px) {
  .main-image {
    height: 400px;
  }
  
  .video-play-btn {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }
  
  .thumbnail-wrapper {
    width: 60px;
    height: 60px;
  }
  
  .zoom-indicator {
    display: none;
  }
}
</style>
