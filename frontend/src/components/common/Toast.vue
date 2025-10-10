<template>
  <Transition name="toast" appear>
    <div v-if="isVisible" class="toast-container" :class="[`toast-${type}`, { 'toast-closable': closable }]">
      <div class="toast-content">
        <div class="toast-icon">
          <i :class="iconClass"></i>
        </div>
        <div class="toast-message">
          <div class="toast-title" v-if="title">{{ title }}</div>
          <div class="toast-text">{{ message }}</div>
        </div>
        <button v-if="closable" class="toast-close" @click="close">
          <i class="bi bi-x"></i>
        </button>
      </div>
      <div v-if="progress" class="toast-progress">
        <div class="toast-progress-bar" :style="{ width: progressWidth + '%' }"></div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    required: true
  },
  duration: {
    type: Number,
    default: 5000
  },
  closable: {
    type: Boolean,
    default: true
  },
  progress: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close'])

const isVisible = ref(true)
const progressWidth = ref(100)
let progressInterval = null
let timeoutId = null

const iconClass = computed(() => {
  const icons = {
    success: 'bi bi-check-circle',
    error: 'bi bi-x-circle',
    warning: 'bi bi-exclamation-triangle-fill',
    info: 'bi bi-info-circle'
  }
  return icons[props.type] || icons.info
})

const close = () => {
  isVisible.value = false
  if (timeoutId) {
    clearTimeout(timeoutId)
  }
  if (progressInterval) {
    clearInterval(progressInterval)
  }
  setTimeout(() => {
    emit('close')
  }, 300) // Wait for animation to complete
}

const startProgress = () => {
  if (!props.progress) return
  
  const startTime = Date.now()
  const updateProgress = () => {
    const elapsed = Date.now() - startTime
    const remaining = Math.max(0, props.duration - elapsed)
    progressWidth.value = (remaining / props.duration) * 100
    
    if (remaining <= 0) {
      close()
    }
  }
  
  progressInterval = setInterval(updateProgress, 50)
}

onMounted(() => {
  if (props.duration > 0) {
    timeoutId = setTimeout(close, props.duration)
    startProgress()
  }
})

onUnmounted(() => {
  if (timeoutId) {
    clearTimeout(timeoutId)
  }
  if (progressInterval) {
    clearInterval(progressInterval)
  }
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 320px;
  max-width: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-left: 4px solid;
  z-index: 10000;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.toast-success {
  border-left-color: #10b981;
}

.toast-error {
  border-left-color: #ef4444;
}

.toast-warning {
  border-left-color: #f59e0b;
}

.toast-info {
  border-left-color: var(--auro-accent);
}

.toast-content {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  gap: 12px;
}

.toast-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 16px;
}

.toast-success .toast-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.toast-error .toast-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.toast-warning .toast-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.toast-info .toast-icon {
  background: rgba(140, 144, 126, 0.1);
  color: var(--auro-accent);
}

.toast-message {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--auro-text);
  margin-bottom: 4px;
  line-height: 1.4;
}

.toast-text {
  font-size: 14px;
  color: var(--auro-text-light);
  line-height: 1.4;
}

.toast-close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  color: var(--auro-text-light);
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 16px;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--auro-text);
}

.toast-progress {
  height: 3px;
  background: rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.toast-progress-bar {
  height: 100%;
  background: currentColor;
  transition: width 0.1s linear;
}

.toast-success .toast-progress-bar {
  background: #10b981;
}

.toast-error .toast-progress-bar {
  background: #ef4444;
}

.toast-warning .toast-progress-bar {
  background: #f59e0b;
}

.toast-info .toast-progress-bar {
  background: var(--auro-accent);
}

/* Animations */
.toast-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .toast-container {
    top: 10px;
    right: 10px;
    left: 10px;
    min-width: auto;
    max-width: none;
  }
  
  .toast-content {
    padding: 12px 16px;
  }
  
  .toast-title {
    font-size: 13px;
  }
  
  .toast-text {
    font-size: 13px;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .toast-container {
    background: #1f2937;
    color: white;
  }
  
  .toast-title {
    color: white;
  }
  
  .toast-text {
    color: #d1d5db;
  }
  
  .toast-close {
    color: #d1d5db;
  }
  
  .toast-close:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
  
  .toast-progress {
    background: rgba(255, 255, 255, 0.1);
  }
}
</style>