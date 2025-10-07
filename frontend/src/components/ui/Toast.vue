<template>
  <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1055;">
    <div 
      v-for="toast in toasts" 
      :key="toast.id"
      class="toast show modern-toast"
      :class="`toast-${toast.type}`"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
    >
      <div class="toast-header">
        <div class="toast-icon me-2">
          <i :class="getIconClass(toast.type)"></i>
        </div>
        <strong class="me-auto">{{ toast.title }}</strong>
        <button 
          type="button" 
          class="btn-close" 
          @click="removeToast(toast.id)"
          aria-label="Close"
        ></button>
      </div>
      <div class="toast-body">
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const toasts = ref([])
let toastId = 0

const getIconClass = (type) => {
  const icons = {
    success: 'ph-check-circle text-success',
    error: 'ph-warning-circle text-danger',
    warning: 'ph-warning text-warning',
    info: 'ph-info text-info'
  }
  return icons[type] || icons.info
}

const addToast = (toast) => {
  const id = ++toastId
  const newToast = {
    id,
    type: toast.type || 'info',
    title: toast.title || 'Thông báo',
    message: toast.message,
    duration: toast.duration || 3000
  }
  
  toasts.value.push(newToast)
  
  // Auto remove after duration
  setTimeout(() => {
    removeToast(id)
  }, newToast.duration)
}

const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// Expose methods globally
const toastService = {
  success: (message, title = 'Thành công') => addToast({ type: 'success', title, message }),
  error: (message, title = 'Lỗi') => addToast({ type: 'error', title, message }),
  warning: (message, title = 'Cảnh báo') => addToast({ type: 'warning', title, message }),
  info: (message, title = 'Thông tin') => addToast({ type: 'info', title, message })
}

// Make toast service available globally
window.$toast = toastService

onMounted(() => {
  // Register global toast service
  window.$toast = toastService
})

onUnmounted(() => {
  // Cleanup
  if (window.$toast === toastService) {
    delete window.$toast
  }
})
</script>

<style scoped>
.modern-toast {
  border: none;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  margin-bottom: 1rem;
  min-width: 300px;
}

.toast-success {
  border-left: 4px solid var(--auro-success, #28a745);
}

.toast-error {
  border-left: 4px solid var(--auro-danger, #dc3545);
}

.toast-warning {
  border-left: 4px solid var(--auro-warning, #ffc107);
}

.toast-info {
  border-left: 4px solid var(--auro-info, #17a2b8);
}

.toast-header {
  background: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0.75rem 1rem 0.5rem;
}

.toast-body {
  padding: 0.5rem 1rem 0.75rem;
  font-size: 0.9rem;
  line-height: 1.4;
}

.toast-icon {
  width: 20px;
  text-align: center;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.btn-close:hover {
  opacity: 1;
}

/* Animation */
.toast {
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .modern-toast {
    background: rgba(33, 37, 41, 0.95);
    color: #fff;
  }
  
  .toast-header {
    border-bottom-color: rgba(255, 255, 255, 0.1);
  }
}
</style>
