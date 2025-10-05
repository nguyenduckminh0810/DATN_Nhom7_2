<template>
  <div class="loading-spinner" :class="[`spinner-${size}`, `spinner-${variant}`]">
    <div class="spinner-container">
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
    </div>
    <div v-if="showText" class="spinner-text">{{ text }}</div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'accent'].includes(value)
  },
  showText: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: 'Đang tải...'
  }
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.spinner-container {
  position: relative;
  display: inline-block;
}

.spinner-ring {
  position: absolute;
  border: 2px solid transparent;
  border-top: 2px solid var(--auro-accent);
  border-radius: 50%;
  animation: spinner-rotate 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.15s;
}

@keyframes spinner-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Size variants */
.spinner-small .spinner-ring {
  width: 20px;
  height: 20px;
}

.spinner-medium .spinner-ring {
  width: 32px;
  height: 32px;
}

.spinner-large .spinner-ring {
  width: 48px;
  height: 48px;
}

/* Color variants */
.spinner-primary .spinner-ring {
  border-top-color: var(--auro-primary);
}

.spinner-secondary .spinner-ring {
  border-top-color: var(--auro-secondary);
}

.spinner-accent .spinner-ring {
  border-top-color: var(--auro-accent);
}

/* Text styling */
.spinner-text {
  font-family: var(--auro-body-font);
  font-size: 0.875rem;
  color: var(--auro-text-light);
  font-weight: 500;
  text-align: center;
}

/* Pulse animation variant */
.spinner-pulse {
  animation: spinner-pulse 1.5s ease-in-out infinite;
}

@keyframes spinner-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Bounce animation variant */
.spinner-bounce {
  animation: spinner-bounce 1.4s ease-in-out infinite both;
}

@keyframes spinner-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* Overlay variant */
.spinner-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  z-index: 9999;
}

/* Inline variant */
.spinner-inline {
  display: inline-flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
}

.spinner-inline .spinner-text {
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .spinner-text {
    font-size: 0.75rem;
  }
  
  .spinner-small .spinner-ring {
    width: 16px;
    height: 16px;
  }
  
  .spinner-medium .spinner-ring {
    width: 24px;
    height: 24px;
  }
  
  .spinner-large .spinner-ring {
    width: 32px;
    height: 32px;
  }
}
</style>
