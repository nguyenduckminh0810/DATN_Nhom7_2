<template>
  <div class="variant-image-uploader" :class="{ 'disabled': disabled }">
    <div class="upload-area" @click="disabled ? null : triggerFileInput()">
      <div v-if="!previewUrl && !imageUrl" class="upload-placeholder">
        <i class="bi bi-cloud-upload"></i>
        <p v-if="!disabled">Click để upload ảnh</p>
        <p v-else class="text-muted">Lưu sản phẩm trước</p>
        <span class="upload-hint">JPG, PNG (Max 2MB)</span>
      </div>
      
      <div v-else class="image-preview">
        <img :src="previewUrl || imageUrl" alt="Variant image" />
        <div v-if="!disabled" class="image-actions">
          <button 
            type="button" 
            class="btn-change" 
            @click.stop="triggerFileInput"
            title="Đổi ảnh"
          >
            <i class="bi bi-pencil"></i>
          </button>
          <button 
            type="button" 
            class="btn-remove" 
            @click.stop="removeImage"
            title="Xóa ảnh"
          >
            <i class="bi bi-trash"></i>
          </button>
        </div>
        <div v-if="isUploading" class="upload-overlay">
          <div class="spinner-border text-light" role="status">
            <span class="visually-hidden">Đang tải...</span>
          </div>
        </div>
      </div>
    </div>
    
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/jpg"
      style="display: none"
      @change="handleFileChange"
      :disabled="disabled"
    />
    
    <div v-if="error" class="error-message">
      <i class="bi bi-exclamation-circle"></i>
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  imageUrl: {
    type: String,
    default: null
  },
  productId: {
    type: [Number, String],
    required: true
  },
  size: {
    type: String,
    required: true
  },
  color: {
    type: String,
    required: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['uploaded', 'removed'])

const fileInput = ref(null)
const previewUrl = ref(null)
const isUploading = ref(false)
const error = ref('')

const triggerFileInput = () => {
  if (props.disabled) {
    error.value = 'Vui lòng lưu sản phẩm trước khi upload ảnh'
    setTimeout(() => {
      error.value = ''
    }, 3000)
    return
  }
  fileInput.value?.click()
}

const handleFileChange = async (event) => {
  if (props.disabled) return
  
  const file = event.target.files[0]
  if (!file) return
  
  // Reset error
  error.value = ''
  
  // Validate file
  if (!file.type.match(/^image\/(jpeg|png|jpg)$/)) {
    error.value = 'Chỉ chấp nhận file JPG, PNG'
    return
  }
  
  if (file.size > 2 * 1024 * 1024) {
    error.value = 'Kích thước file không được vượt quá 2MB'
    return
  }
  
  // Create preview
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  // Upload file
  await uploadImage(file)
}

const uploadImage = async (file) => {
  isUploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const { default: api } = await import('../../services/api')
    // Use temporary upload endpoint that doesn't require variant ID
    const response = await api.upload('/hinh-anh/bien-the-temp', file)
    
    if (response?.url) {
      emit('uploaded', response.url)
      error.value = ''
    } else {
      throw new Error('Không nhận được URL ảnh từ server')
    }
  } catch (err) {
    console.error('Upload error:', err)
    error.value = 'Lỗi khi upload ảnh: ' + (err.response?.data?.message || err.message || 'Vui lòng thử lại')
    previewUrl.value = null
  } finally {
    isUploading.value = false
  }
}

const removeImage = () => {
  if (props.disabled) return
  
  previewUrl.value = null
  error.value = ''
  emit('removed')
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
watch(() => props.imageUrl, (newVal) => {
  if (!newVal) {
    previewUrl.value = null
  }
})
</script>

<style scoped>
.variant-image-uploader {
  width: 100%;
}

.variant-image-uploader.disabled .upload-area {
  cursor: not-allowed;
  opacity: 0.6;
  background: #f1f5f9;
}

.variant-image-uploader.disabled .upload-area:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
}

.upload-area {
  width: 100px;
  height: 100px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
  position: relative;
  background: #f8fafb;
}

.upload-area:hover {
  border-color: #6366f1;
  background: #eef2ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0.5rem;
  text-align: center;
}

.upload-placeholder i {
  font-size: 1.5rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.upload-placeholder p {
  font-size: 0.75rem;
  color: #475569;
  margin: 0;
  font-weight: 500;
}

.upload-hint {
  font-size: 0.65rem;
  color: #94a3b8;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  gap: 0.25rem;
  padding: 0.25rem;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-preview:hover .image-actions {
  opacity: 1;
}

.btn-change,
.btn-remove {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-change {
  background: rgba(99, 102, 241, 0.9);
  color: white;
}

.btn-change:hover {
  background: #6366f1;
}

.btn-remove {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.btn-remove:hover {
  background: #ef4444;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-message {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: #fef2f2;
  border: 1px solid #fca5a5;
  border-radius: 4px;
  color: #dc2626;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-message i {
  font-size: 1rem;
}
</style>
