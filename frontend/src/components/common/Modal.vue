<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="modelValue"
        class="modal-overlay"
        :class="overlayClass"
        @click="handleOverlayClick"
        @keydown.esc="handleEscape"
      >
        <Transition name="modal-slide">
          <div
            v-if="modelValue"
            ref="modalRef"
            class="modal-container"
            :class="modalClass"
            :style="modalStyle"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="titleId"
            @click.stop
          >
            <!-- Close button -->
            <button
              v-if="showClose"
              class="modal-close"
              @click="handleClose"
              aria-label="Đóng"
            >
              <i class="bi bi-x"></i>
            </button>

            <!-- Header -->
            <div v-if="$slots.header || title" class="modal-header">
              <slot name="header">
                <h3 :id="titleId" class="modal-title">{{ title }}</h3>
              </slot>
            </div>

            <!-- Body -->
            <div class="modal-body" :class="bodyClass">
              <slot />
            </div>

            <!-- Footer -->
            <div v-if="$slots.footer || showFooter" class="modal-footer">
              <slot name="footer">
                <button
                  v-if="showCancel"
                  class="btn btn-secondary"
                  @click="handleCancel"
                >
                  {{ cancelText }}
                </button>
                <button
                  v-if="showConfirm"
                  class="btn btn-primary"
                  :disabled="confirmDisabled"
                  @click="handleConfirm"
                >
                  {{ confirmText }}
                </button>
              </slot>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  // v-model
  modelValue: {
    type: Boolean,
    default: false
  },
  
  // Title
  title: {
    type: String,
    default: ''
  },
  
  // Size
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large', 'xlarge', 'full'].includes(value)
  },
  
  // Close button
  showClose: {
    type: Boolean,
    default: true
  },
  
  // Click outside to close
  closeOnClickOutside: {
    type: Boolean,
    default: true
  },
  
  // Press ESC to close
  closeOnEscape: {
    type: Boolean,
    default: true
  },
  
  // Footer
  showFooter: {
    type: Boolean,
    default: false
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  showConfirm: {
    type: Boolean,
    default: true
  },
  cancelText: {
    type: String,
    default: 'Hủy'
  },
  confirmText: {
    type: String,
    default: 'Xác nhận'
  },
  confirmDisabled: {
    type: Boolean,
    default: false
  },
  
  // Scrollable
  scrollable: {
    type: Boolean,
    default: false
  },
  
  // Centered
  centered: {
    type: Boolean,
    default: false
  },
  
  // Full height
  fullHeight: {
    type: Boolean,
    default: false
  },
  
  // Z-index
  zIndex: {
    type: Number,
    default: 1000
  }
})

const emit = defineEmits([
  'update:modelValue',
  'close',
  'cancel',
  'confirm',
  'opened',
  'closed'
])

// Refs
const modalRef = ref(null)
const titleId = `modal-title-${Math.random().toString(36).substr(2, 9)}`

// Computed
const overlayClass = computed(() => [
  'modal-overlay',
  {
    'modal-centered': props.centered
  }
])

const modalClass = computed(() => [
  'modal-container',
  `modal-${props.size}`,
  {
    'modal-scrollable': props.scrollable,
    'modal-full-height': props.fullHeight
  }
])

const modalStyle = computed(() => ({
  zIndex: props.zIndex + 1
}))

const bodyClass = computed(() => [
  'modal-body',
  {
    'modal-body-scrollable': props.scrollable
  }
])

// Methods
const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

const handleCancel = () => {
  emit('cancel')
  handleClose()
}

const handleConfirm = () => {
  emit('confirm')
}

const handleOverlayClick = () => {
  if (props.closeOnClickOutside) {
    handleClose()
  }
}

const handleEscape = () => {
  if (props.closeOnEscape) {
    handleClose()
  }
}

// Prevent body scroll when modal is open
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    document.body.style.overflow = 'hidden'
    nextTick(() => {
      emit('opened')
      // Focus modal
      if (modalRef.value) {
        modalRef.value.focus()
      }
    })
  } else {
    document.body.style.overflow = ''
    emit('closed')
  }
})

// Cleanup on unmount
onUnmounted(() => {
  if (props.modelValue) {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  overflow-y: auto;
  padding: 2rem 1rem;
}

.modal-overlay.modal-centered {
  align-items: center;
}

.modal-container {
  position: relative;
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-height: calc(100vh - 4rem);
  display: flex;
  flex-direction: column;
  outline: none;
}

.modal-small {
  max-width: 400px;
}

.modal-medium {
  max-width: 600px;
}

.modal-large {
  max-width: 800px;
}

.modal-xlarge {
  max-width: 1200px;
}

.modal-full {
  max-width: none;
  margin: 0;
  border-radius: 0;
  max-height: 100vh;
}

.modal-full-height {
  height: calc(100vh - 4rem);
}

.modal-scrollable {
  max-height: calc(100vh - 4rem);
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: 0.375rem;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}

.modal-close:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.modal-close i {
  font-size: 1.25rem;
}

.modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
}

.modal-body {
  padding: 1.5rem 2rem;
  overflow-y: visible;
  flex: 1;
}

.modal-body-scrollable {
  overflow-y: auto;
}

.modal-footer {
  padding: 1.5rem 2rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  align-items: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.625rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  line-height: 1.25rem;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
  outline: none;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}

.btn-secondary {
  background-color: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #f9fafb;
}

/* Transitions */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-slide-enter-active,
.modal-slide-leave-active {
  transition: all 0.3s ease;
}

.modal-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.modal-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* Responsive */
@media (max-width: 640px) {
  .modal-overlay {
    padding: 1rem;
    align-items: flex-end;
  }
  
  .modal-container {
    max-height: 90vh;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1rem 1.5rem;
  }
  
  .modal-title {
    font-size: 1.125rem;
  }
  
  .modal-footer {
    flex-direction: column-reverse;
    gap: 0.75rem;
  }
  
  .modal-footer .btn {
    width: 100%;
  }
  
  .modal-slide-enter-from {
    transform: translateY(100%) scale(1);
  }
}
</style>
