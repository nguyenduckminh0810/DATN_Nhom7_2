<template>
  <div class="progress-bar-container" :class="[`progress-${variant}`, { 'progress-animated': animated }]">
    <div class="progress-bar-track">
      <div 
        class="progress-bar-fill" 
        :style="{ 
          width: `${percentage}%`,
          transition: animated ? `width ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)` : 'none'
        }"
      >
        <div v-if="showLabel" class="progress-bar-label">
          {{ label || `${Math.round(percentage)}%` }}
        </div>
      </div>
    </div>
    <div v-if="showPercentage" class="progress-bar-percentage">
      {{ Math.round(percentage) }}%
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  percentage: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'success', 'warning', 'error'].includes(value)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  animated: {
    type: Boolean,
    default: true
  },
  duration: {
    type: Number,
    default: 1000
  },
  showLabel: {
    type: Boolean,
    default: false
  },
  showPercentage: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
}

.progress-bar-track {
  flex: 1;
  height: 8px;
  background: var(--auro-hover);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.progress-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

.progress-animated .progress-bar-fill::after {
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.progress-bar-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  white-space: nowrap;
}

.progress-bar-percentage {
  font-size: 14px;
  font-weight: 600;
  color: var(--auro-text);
  min-width: 40px;
  text-align: right;
}

/* Size variants */
.progress-small .progress-bar-track {
  height: 4px;
}

.progress-medium .progress-bar-track {
  height: 8px;
}

.progress-large .progress-bar-track {
  height: 12px;
}

/* Color variants */
.progress-primary .progress-bar-fill {
  background: var(--auro-gradient);
}

.progress-secondary .progress-bar-fill {
  background: var(--auro-gradient-accent);
}

.progress-success .progress-bar-fill {
  background: linear-gradient(90deg, #10b981, #059669);
}

.progress-warning .progress-bar-fill {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.progress-error .progress-bar-fill {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* Striped variant */
.progress-striped .progress-bar-fill {
  background-image: linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.15) 25%,
    transparent 25%,
    transparent 50%,
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.15) 75%,
    transparent 75%,
    transparent
  );
  background-size: 1rem 1rem;
  animation: progress-stripes 1s linear infinite;
}

@keyframes progress-stripes {
  0% {
    background-position: 1rem 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .progress-bar-container {
    gap: 0.5rem;
  }
  
  .progress-bar-percentage {
    font-size: 12px;
    min-width: 35px;
  }
  
  .progress-bar-label {
    font-size: 10px;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .progress-bar-track {
    background: rgba(255, 255, 255, 0.1);
  }
  
  .progress-bar-percentage {
    color: white;
  }
}
</style>
