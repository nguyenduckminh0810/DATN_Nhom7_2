<template>
  <div class="image-uploader-admin">
    <!-- Header -->
    <div class="uploader-header">
      <h5 class="uploader-title">
        <i class="bi bi-images me-2"></i>
        Quản lý hình ảnh sản phẩm
      </h5>
      <div class="image-stats">
        <span class="stat-badge">
          <i class="bi bi-image"></i>
          {{ images.length }}/{{ maxImages }} ảnh
        </span>
      </div>
    </div>

    <!-- Drag & Drop Zone -->
    <div
      v-if="images.length < maxImages"
      class="drop-zone"
      :class="{ 'drag-over': isDragging, 'disabled': isUploading }"
      @dragenter.prevent="handleDragEnter"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/webp"
        multiple
        @change="handleFileSelect"
        style="display: none"
      />
      
      <div class="drop-zone-content">
        <i class="bi bi-cloud-upload drop-icon"></i>
        <h6 class="drop-title">Kéo thả ảnh vào đây hoặc click để chọn</h6>
        <p class="drop-subtitle">
          Hỗ trợ: JPG, PNG, WEBP (tối đa {{ maxFileSize }}MB/ảnh)
        </p>
        <div class="drop-actions">
          <button type="button" class="btn btn-outline-primary btn-sm">
            <i class="bi bi-folder2-open me-1"></i>
            Chọn ảnh từ máy
          </button>
        </div>
      </div>

      <!-- Upload Progress -->
      <div v-if="isUploading" class="upload-progress">
        <div class="progress-bar-container">
          <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
        </div>
        <span class="progress-text">Đang upload... {{ uploadProgress }}%</span>
      </div>
    </div>

    <!-- Image Grid -->
    <div v-if="images.length > 0" class="images-grid">
      <div
        v-for="(image, index) in images"
        :key="image.id"
        class="image-card"
        :class="{ 'is-primary': image.isPrimary, 'is-dragging': draggedIndex === index }"
        draggable="true"
        @dragstart="handleImageDragStart(index, $event)"
        @dragend="handleImageDragEnd"
        @dragover.prevent="handleImageDragOver(index)"
        @drop.prevent="handleImageDrop(index)"
      >
        <!-- Primary Badge -->
        <div v-if="image.isPrimary" class="primary-badge">
          <i class="bi bi-star-fill"></i>
          Ảnh chính
        </div>

        <!-- Image Preview -->
        <div class="image-preview">
          <img :src="image.url" :alt="image.alt || 'Product image'" />
          
          <!-- Hover Actions -->
          <div class="image-overlay">
            <button
              type="button"
              class="overlay-btn"
              @click="viewImage(image)"
              title="Xem ảnh"
            >
              <i class="bi bi-eye"></i>
            </button>
            <button
              type="button"
              class="overlay-btn"
              @click="setPrimaryImage(index)"
              title="Đặt làm ảnh chính"
              :disabled="image.isPrimary"
            >
              <i class="bi bi-star"></i>
            </button>
            <button
              type="button"
              class="overlay-btn delete-btn"
              @click="deleteImage(index)"
              title="Xóa ảnh"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>

        <!-- Image Info -->
        <div class="image-info">
          <input
            type="text"
            v-model="image.alt"
            class="form-control form-control-sm"
            placeholder="Alt text (SEO)"
            @input="emitUpdate"
          />
          
          <!-- Color Association (Optional) -->
          <div v-if="enableColorAssociation && colors.length > 0" class="color-association">
            <select
              v-model="image.associatedColor"
              class="form-select form-select-sm"
              @change="emitUpdate"
            >
              <option value="">Tất cả màu</option>
              <option
                v-for="color in colors"
                :key="color.hex"
                :value="color.hex"
              >
                {{ color.name }}
              </option>
            </select>
          </div>

          <!-- Image Order -->
          <div class="image-order">
            <span class="order-badge">{{ index + 1 }}</span>
            <span class="drag-hint">
              <i class="bi bi-arrows-move"></i>
              Kéo để sắp xếp
            </span>
          </div>
        </div>
      </div>

      <!-- Add More Button -->
      <div
        v-if="images.length < maxImages"
        class="add-more-card"
        @click="triggerFileInput"
      >
        <i class="bi bi-plus-circle"></i>
        <span>Thêm ảnh</span>
      </div>
    </div>

    <!-- Group by Color View -->
    <div v-if="enableColorAssociation && colors.length > 0 && images.length > 0" class="color-groups-section">
      <h6 class="section-subtitle">
        <i class="bi bi-palette me-2"></i>
        Ảnh theo màu sắc
      </h6>
      
      <div class="color-groups">
        <div
          v-for="color in colors"
          :key="color.hex"
          class="color-group"
        >
          <div class="color-group-header">
            <div class="color-dot" :style="{ backgroundColor: color.hex }"></div>
            <span class="color-group-name">{{ color.name }}</span>
            <span class="color-group-count">{{ getImagesByColor(color.hex).length }} ảnh</span>
          </div>
          
          <div class="color-group-images">
            <div
              v-for="image in getImagesByColor(color.hex)"
              :key="image.id"
              class="color-group-image"
            >
              <img :src="image.url" :alt="image.alt" />
            </div>
            <div v-if="getImagesByColor(color.hex).length === 0" class="no-images">
              Chưa có ảnh cho màu này
            </div>
          </div>
        </div>

        <!-- Unassigned Images -->
        <div v-if="getImagesByColor('').length > 0" class="color-group">
          <div class="color-group-header">
            <i class="bi bi-images"></i>
            <span class="color-group-name">Ảnh chung (tất cả màu)</span>
            <span class="color-group-count">{{ getImagesByColor('').length }} ảnh</span>
          </div>
          <div class="color-group-images">
            <div
              v-for="image in getImagesByColor('')"
              :key="image.id"
              class="color-group-image"
            >
              <img :src="image.url" :alt="image.alt" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <div v-if="previewImage" class="preview-modal" @click="closePreview">
      <div class="preview-content" @click.stop>
        <button class="preview-close" @click="closePreview">
          <i class="bi bi-x"></i>
        </button>
        <img :src="previewImage.url" :alt="previewImage.alt" />
        <div class="preview-info">
          <p><strong>Alt:</strong> {{ previewImage.alt || 'Chưa có' }}</p>
          <p><strong>Màu:</strong> {{ getColorNameByHex(previewImage.associatedColor) || 'Tất cả' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  initialImages: {
    type: Array,
    default: () => []
  },
  maxImages: {
    type: Number,
    default: 10
  },
  maxFileSize: {
    type: Number,
    default: 2 // MB
  },
  enableColorAssociation: {
    type: Boolean,
    default: true
  },
  colors: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update', 'upload'])

// Reactive state
const images = ref([...props.initialImages])
const isDragging = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)
const fileInput = ref(null)
const draggedIndex = ref(null)
const previewImage = ref(null)

// Computed
const hasImages = computed(() => images.value.length > 0)
const primaryImage = computed(() => images.value.find(img => img.isPrimary))

// Methods
const triggerFileInput = () => {
  if (isUploading.value) return
  fileInput.value?.click()
}

const handleDragEnter = (e) => {
  isDragging.value = true
}

const handleDragOver = (e) => {
  isDragging.value = true
}

const handleDragLeave = (e) => {
  if (e.target.className === 'drop-zone') {
    isDragging.value = false
  }
}

const handleDrop = (e) => {
  isDragging.value = false
  const files = Array.from(e.dataTransfer.files)
  processFiles(files)
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files)
  processFiles(files)
  // Reset input
  e.target.value = ''
}

const processFiles = (files) => {
  // Filter valid image files
  const validFiles = files.filter(file => {
    const isImage = file.type.startsWith('image/')
    const isValidSize = file.size <= props.maxFileSize * 1024 * 1024
    
    if (!isImage) {
      alert(`File ${file.name} không phải là ảnh!`)
      return false
    }
    if (!isValidSize) {
      alert(`File ${file.name} quá lớn! Tối đa ${props.maxFileSize}MB`)
      return false
    }
    return true
  })

  if (validFiles.length === 0) return

  // Check max images limit
  const remainingSlots = props.maxImages - images.value.length
  if (validFiles.length > remainingSlots) {
    alert(`Chỉ có thể upload thêm ${remainingSlots} ảnh!`)
    return
  }

  // Fake upload with progress (vì chưa có backend)
  uploadFiles(validFiles)
}

const uploadFiles = async (files) => {
  isUploading.value = true
  uploadProgress.value = 0

  // Simulate upload với FileReader để preview
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    
    // Fake progress
    uploadProgress.value = Math.round(((i + 0.5) / files.length) * 100)

    // Read file as data URL
    const reader = new FileReader()
    
    await new Promise((resolve) => {
      reader.onload = (e) => {
        const newImage = {
          id: Date.now() + i,
          url: e.target.result,
          alt: '',
          associatedColor: '',
          isPrimary: images.value.length === 0, // First image is primary
          file: file,
          name: file.name,
          size: file.size
        }
        
        images.value.push(newImage)
        resolve()
      }
      reader.readAsDataURL(file)
    })

    // Small delay for smooth progress
    await new Promise(resolve => setTimeout(resolve, 300))
  }

  uploadProgress.value = 100
  
  setTimeout(() => {
    isUploading.value = false
    uploadProgress.value = 0
    emitUpdate()
  }, 500)
}

const setPrimaryImage = (index) => {
  images.value.forEach((img, i) => {
    img.isPrimary = i === index
  })
  emitUpdate()
}

const deleteImage = (index) => {
  if (confirm('Bạn có chắc muốn xóa ảnh này?')) {
    const wasPrimary = images.value[index].isPrimary
    images.value.splice(index, 1)
    
    // If deleted was primary, set first image as primary
    if (wasPrimary && images.value.length > 0) {
      images.value[0].isPrimary = true
    }
    
    emitUpdate()
  }
}

const viewImage = (image) => {
  previewImage.value = image
}

const closePreview = () => {
  previewImage.value = null
}

// Drag & Drop Reordering
const handleImageDragStart = (index, e) => {
  draggedIndex.value = index
  e.dataTransfer.effectAllowed = 'move'
}

const handleImageDragEnd = () => {
  draggedIndex.value = null
}

const handleImageDragOver = (index) => {
  if (draggedIndex.value === null || draggedIndex.value === index) return
  
  const draggedItem = images.value[draggedIndex.value]
  images.value.splice(draggedIndex.value, 1)
  images.value.splice(index, 0, draggedItem)
  draggedIndex.value = index
}

const handleImageDrop = (index) => {
  draggedIndex.value = null
  emitUpdate()
}

// Color-based grouping
const getImagesByColor = (colorHex) => {
  return images.value.filter(img => img.associatedColor === colorHex)
}

const getColorNameByHex = (hex) => {
  if (!hex) return 'Tất cả màu'
  const color = props.colors.find(c => c.hex === hex)
  return color ? color.name : hex
}

// Emit updates
const emitUpdate = () => {
  emit('update', images.value)
}

// Initialize
if (props.initialImages && props.initialImages.length > 0) {
  images.value = [...props.initialImages]
}
</script>

<style scoped>
.image-uploader-admin {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}

/* Header */
.uploader-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.uploader-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.image-stats {
  display: flex;
  gap: 0.5rem;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #4338ca;
  font-weight: 600;
}

/* Drop Zone */
.drop-zone {
  border: 3px dashed #cbd5e1;
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8fafb;
  position: relative;
  margin-bottom: 1.5rem;
}

.drop-zone:hover {
  border-color: #6366f1;
  background: #f1f5f9;
}

.drop-zone.drag-over {
  border-color: #6366f1;
  background: #eef2ff;
  border-style: solid;
  transform: scale(1.02);
}

.drop-zone.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

.drop-zone-content {
  pointer-events: none;
}

.drop-icon {
  font-size: 3rem;
  color: #6366f1;
  margin-bottom: 1rem;
}

.drop-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.drop-subtitle {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.drop-actions {
  pointer-events: auto;
}

/* Upload Progress */
.upload-progress {
  position: absolute;
  bottom: 1rem;
  left: 2rem;
  right: 2rem;
}

.progress-bar-container {
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.85rem;
  color: #6366f1;
  font-weight: 600;
}

/* Images Grid */
.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.image-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: move;
}

.image-card:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
  transform: translateY(-2px);
}

.image-card.is-primary {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1);
}

.image-card.is-dragging {
  opacity: 0.5;
  transform: scale(0.95);
}

/* Primary Badge */
.primary-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  box-shadow: 0 2px 8px rgba(217, 119, 6, 0.3);
}

/* Image Preview */
.image-preview {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f1f5f9;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-card:hover .image-preview img {
  transform: scale(1.05);
}

/* Image Overlay */
.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.overlay-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1.1rem;
}

.overlay-btn:hover {
  background: white;
  transform: scale(1.1);
}

.overlay-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.delete-btn {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.delete-btn:hover {
  background: #ef4444;
}

/* Image Info */
.image-info {
  padding: 1rem;
  background: #f8fafb;
}

.form-control-sm {
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.color-association {
  margin-bottom: 0.5rem;
}

.image-order {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #64748b;
  margin-top: 0.5rem;
}

.order-badge {
  background: #6366f1;
  color: white;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.75rem;
}

.drag-hint {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-style: italic;
}

/* Add More Card */
.add-more-card {
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-height: 200px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
}

.add-more-card:hover {
  border-color: #6366f1;
  background: #f8fafb;
  color: #6366f1;
}

.add-more-card i {
  font-size: 2rem;
}

/* Color Groups */
.color-groups-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e2e8f0;
}

.section-subtitle {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1rem;
}

.color-groups {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.color-group {
  background: #f8fafb;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.color-group-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e2e8f0;
}

.color-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 1px #cbd5e1;
}

.color-group-name {
  font-weight: 600;
  color: #1e293b;
  flex: 1;
}

.color-group-count {
  font-size: 0.85rem;
  color: #64748b;
  background: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
}

.color-group-images {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.color-group-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-group-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-images {
  color: #94a3b8;
  font-size: 0.85rem;
  font-style: italic;
  padding: 1rem;
}

/* Preview Modal */
.preview-modal {
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
  padding: 2rem;
}

.preview-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.preview-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.5rem;
  z-index: 10;
  transition: all 0.2s;
}

.preview-close:hover {
  background: rgba(0, 0, 0, 0.9);
  transform: scale(1.1);
}

.preview-content img {
  max-width: 100%;
  max-height: 80vh;
  display: block;
}

.preview-info {
  padding: 1rem;
  background: #f8fafb;
  font-size: 0.9rem;
}

.preview-info p {
  margin: 0.5rem 0;
  color: #475569;
}

/* Responsive */
@media (max-width: 768px) {
  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .drop-zone {
    padding: 2rem 1rem;
  }

  .color-group-images {
    overflow-x: auto;
  }
}
</style>

