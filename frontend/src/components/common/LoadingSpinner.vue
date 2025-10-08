<template>
  <div 
    class="loading-spinner-container"
    :class="containerClass"
    :style="containerStyle"
  >
    <!-- Spinner variants -->
    <div 
      v-if="variant === 'circle'"
      class="spinner-circle"
      :class="spinnerClass"
      :style="spinnerStyle"
    >
      <div class="circle-ring"></div>
    </div>

    <div 
      v-else-if="variant === 'dots'"
      class="spinner-dots"
      :class="spinnerClass"
    >
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>

    <div 
      v-else-if="variant === 'pulse'"
      class="spinner-pulse"
      :class="spinnerClass"
      :style="spinnerStyle"
    ></div>

    <div 
      v-else-if="variant === 'bars'"
      class="spinner-bars"
      :class="spinnerClass"
    >
      <div class="bar"></div>
      <div class="bar"></div>
      <div class="bar"></div>
      <div class="bar"></div>
    </div>

    <div 
      v-else-if="variant === 'ring'"
      class="spinner-ring"
      :class="spinnerClass"
      :style="spinnerStyle"
    ></div>

    <!-- Text label -->
    <div 
      v-if="text"
      class="spinner-text"
      :class="textClass"
    >
      {{ text }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Spinner variant
  variant: {
    type: String,
    default: 'circle',
    validator: (value) => ['circle', 'dots', 'pulse', 'bars', 'ring'].includes(value)
  },
  
  // Size
  size: {
    type: [String, Number],
    default: 'medium',
    validator: (value) => {
      if (typeof value === 'number') return true
      return ['small', 'medium', 'large'].includes(value)
    }
  },
  
  // Color
  color: {
    type: String,
    default: 'primary'
  },
  
  // Text
  text: {
    type: String,
    default: ''
  },
  
  // Fullscreen overlay
  fullscreen: {
    type: Boolean,
    default: false
  },
  
  // Overlay
  overlay: {
    type: Boolean,
    default: false
  },
  
  // Center in parent
  center: {
    type: Boolean,
    default: false
  }
})

// Computed
const containerClass = computed(() => [
  'loading-spinner-container',
  {
    'fullscreen-overlay': props.fullscreen,
    'with-overlay': props.overlay,
    'center-in-parent': props.center
  }
])

const containerStyle = computed(() => {
  const style = {}
  
  if (props.fullscreen || props.overlay) {
    style.backgroundColor = 'rgba(255, 255, 255, 0.9)'
  }
  
  return style
})

const spinnerClass = computed(() => {
  const sizeClass = typeof props.size === 'string' ? `spinner-${props.size}` : ''
  const colorClass = `spinner-${props.color}`
  
  return [sizeClass, colorClass]
})

const spinnerStyle = computed(() => {
  if (typeof props.size === 'number') {
    return {
      width: `${props.size}px`,
      height: `${props.size}px`
    }
  }
  return {}
})

const textClass = computed(() => [
  'spinner-text',
  `text-${props.size}`
])
</script>

<style scoped>
.loading-spinner-container {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.loading-spinner-container.center-in-parent {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loading-spinner-container.with-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

.loading-spinner-container.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
}

/* Circle Spinner */
.spinner-circle {
  position: relative;
  width: 40px;
  height: 40px;
}

.circle-ring {
  width: 100%;
  height: 100%;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Dots Spinner */
.spinner-dots {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.spinner-dots .dot {
  width: 10px;
  height: 10px;
  background-color: #3b82f6;
  border-radius: 50%;
  animation: dotPulse 1.4s ease-in-out infinite;
}

.spinner-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.spinner-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}

/* Pulse Spinner */
.spinner-pulse {
  width: 40px;
  height: 40px;
  background-color: #3b82f6;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

/* Bars Spinner */
.spinner-bars {
  display: flex;
  gap: 0.25rem;
  align-items: center;
  height: 40px;
}

.spinner-bars .bar {
  width: 6px;
  height: 100%;
  background-color: #3b82f6;
  border-radius: 3px;
  animation: barStretch 1s ease-in-out infinite;
}

.spinner-bars .bar:nth-child(2) {
  animation-delay: 0.1s;
}

.spinner-bars .bar:nth-child(3) {
  animation-delay: 0.2s;
}

.spinner-bars .bar:nth-child(4) {
  animation-delay: 0.3s;
}

/* Ring Spinner */
.spinner-ring {
  width: 40px;
  height: 40px;
  border: 4px solid transparent;
  border-top-color: #3b82f6;
  border-right-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

/* Sizes */
.spinner-small {
  width: 24px;
  height: 24px;
}

.spinner-small .circle-ring,
.spinner-small.spinner-pulse,
.spinner-small.spinner-ring {
  border-width: 2px;
}

.spinner-small.spinner-dots .dot {
  width: 6px;
  height: 6px;
}

.spinner-small.spinner-bars {
  height: 24px;
}

.spinner-small.spinner-bars .bar {
  width: 4px;
}

.spinner-medium {
  width: 40px;
  height: 40px;
}

.spinner-large {
  width: 60px;
  height: 60px;
}

.spinner-large .circle-ring,
.spinner-large.spinner-pulse,
.spinner-large.spinner-ring {
  border-width: 4px;
}

.spinner-large.spinner-dots .dot {
  width: 14px;
  height: 14px;
}

.spinner-large.spinner-bars {
  height: 60px;
}

.spinner-large.spinner-bars .bar {
  width: 8px;
}

/* Colors */
.spinner-primary .circle-ring,
.spinner-primary.spinner-pulse,
.spinner-primary.spinner-ring {
  border-top-color: #3b82f6;
  border-right-color: #3b82f6;
  background-color: #3b82f6;
}

.spinner-primary.spinner-dots .dot,
.spinner-primary.spinner-bars .bar {
  background-color: #3b82f6;
}

.spinner-secondary .circle-ring,
.spinner-secondary.spinner-pulse,
.spinner-secondary.spinner-ring {
  border-top-color: #6b7280;
  border-right-color: #6b7280;
  background-color: #6b7280;
}

.spinner-secondary.spinner-dots .dot,
.spinner-secondary.spinner-bars .bar {
  background-color: #6b7280;
}

.spinner-success .circle-ring,
.spinner-success.spinner-pulse,
.spinner-success.spinner-ring {
  border-top-color: #10b981;
  border-right-color: #10b981;
  background-color: #10b981;
}

.spinner-success.spinner-dots .dot,
.spinner-success.spinner-bars .bar {
  background-color: #10b981;
}

.spinner-danger .circle-ring,
.spinner-danger.spinner-pulse,
.spinner-danger.spinner-ring {
  border-top-color: #ef4444;
  border-right-color: #ef4444;
  background-color: #ef4444;
}

.spinner-danger.spinner-dots .dot,
.spinner-danger.spinner-bars .bar {
  background-color: #ef4444;
}

.spinner-warning .circle-ring,
.spinner-warning.spinner-pulse,
.spinner-warning.spinner-ring {
  border-top-color: #f59e0b;
  border-right-color: #f59e0b;
  background-color: #f59e0b;
}

.spinner-warning.spinner-dots .dot,
.spinner-warning.spinner-bars .bar {
  background-color: #f59e0b;
}

.spinner-white .circle-ring,
.spinner-white.spinner-pulse,
.spinner-white.spinner-ring {
  border-color: rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-right-color: #ffffff;
  background-color: #ffffff;
}

.spinner-white.spinner-dots .dot,
.spinner-white.spinner-bars .bar {
  background-color: #ffffff;
}

/* Text */
.spinner-text {
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
  text-align: center;
}

.text-small {
  font-size: 0.75rem;
}

.text-medium {
  font-size: 0.875rem;
}

.text-large {
  font-size: 1rem;
}

/* Animations */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dotPulse {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes barStretch {
  0%, 100% {
    transform: scaleY(0.4);
  }
  50% {
    transform: scaleY(1);
  }
}

/* Responsive */
@media (max-width: 640px) {
  .spinner-text {
    font-size: 0.8125rem;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .circle-ring,
  .spinner-pulse,
  .spinner-ring {
    animation-duration: 2s;
  }
  
  .spinner-dots .dot,
  .spinner-bars .bar {
    animation-duration: 2s;
  }
}
</style>