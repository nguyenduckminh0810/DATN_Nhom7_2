<template>
  <div 
    class="skeleton-loader"
    :class="skeletonClass"
    :style="skeletonStyle"
  >
    <div 
      v-if="variant === 'text'"
      class="skeleton-text"
      :style="{ width: width || '100%' }"
    ></div>

    <div 
      v-else-if="variant === 'circle'"
      class="skeleton-circle"
      :style="circleStyle"
    ></div>

    <div 
      v-else-if="variant === 'rect'"
      class="skeleton-rect"
      :style="rectStyle"
    ></div>

    <div 
      v-else-if="variant === 'card'"
      class="skeleton-card"
    >
      <div class="skeleton-card-image"></div>
      <div class="skeleton-card-content">
        <div class="skeleton-text skeleton-text-title"></div>
        <div class="skeleton-text skeleton-text-subtitle"></div>
        <div class="skeleton-text skeleton-text-description"></div>
      </div>
    </div>

    <div 
      v-else-if="variant === 'product'"
      class="skeleton-product"
    >
      <div class="skeleton-product-image"></div>
      <div class="skeleton-product-content">
        <div class="skeleton-text skeleton-text-title"></div>
        <div class="skeleton-text skeleton-text-price"></div>
        <div class="skeleton-product-colors">
          <div class="skeleton-circle skeleton-circle-small"></div>
          <div class="skeleton-circle skeleton-circle-small"></div>
          <div class="skeleton-circle skeleton-circle-small"></div>
        </div>
      </div>
    </div>

    <div 
      v-else-if="variant === 'list'"
      class="skeleton-list"
    >
      <div 
        v-for="i in count"
        :key="i"
        class="skeleton-list-item"
      >
        <div class="skeleton-circle skeleton-circle-small"></div>
        <div class="skeleton-list-text">
          <div class="skeleton-text skeleton-text-title"></div>
          <div class="skeleton-text skeleton-text-subtitle"></div>
        </div>
      </div>
    </div>

    <div 
      v-else-if="variant === 'avatar'"
      class="skeleton-avatar"
    >
      <div class="skeleton-circle skeleton-circle-medium"></div>
      <div class="skeleton-avatar-text">
        <div class="skeleton-text skeleton-text-name"></div>
        <div class="skeleton-text skeleton-text-subtitle"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Variant type
  variant: {
    type: String,
    default: 'text',
    validator: (value) => [
      'text', 'circle', 'rect', 'card', 'product', 'list', 'avatar'
    ].includes(value)
  },
  
  // Dimensions
  width: {
    type: [String, Number],
    default: null
  },
  height: {
    type: [String, Number],
    default: null
  },
  
  // Animation
  animated: {
    type: Boolean,
    default: true
  },
  
  // List count
  count: {
    type: Number,
    default: 3
  },
  
  // Rounded corners
  rounded: {
    type: [Boolean, String],
    default: false
  }
})

// Computed
const skeletonClass = computed(() => [
  'skeleton-loader',
  {
    'skeleton-animated': props.animated,
    [`skeleton-rounded-${props.rounded}`]: typeof props.rounded === 'string'
  }
])

const skeletonStyle = computed(() => {
  const style = {}
  
  if (props.width && typeof props.width === 'number') {
    style.width = `${props.width}px`
  } else if (props.width) {
    style.width = props.width
  }
  
  if (props.height && typeof props.height === 'number') {
    style.height = `${props.height}px`
  } else if (props.height) {
    style.height = props.height
  }
  
  if (props.rounded === true) {
    style.borderRadius = '0.5rem'
  }
  
  return style
})

const circleStyle = computed(() => {
  const size = props.width || props.height || 40
  const sizeValue = typeof size === 'number' ? `${size}px` : size
  
  return {
    width: sizeValue,
    height: sizeValue
  }
})

const rectStyle = computed(() => {
  return {
    width: props.width ? (typeof props.width === 'number' ? `${props.width}px` : props.width) : '100%',
    height: props.height ? (typeof props.height === 'number' ? `${props.height}px` : props.height) : '120px'
  }
})
</script>

<style scoped>
.skeleton-loader {
  position: relative;
  overflow: hidden;
}

/* Base skeleton elements */
.skeleton-text,
.skeleton-circle,
.skeleton-rect {
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
}

.skeleton-animated .skeleton-text,
.skeleton-animated .skeleton-circle,
.skeleton-animated .skeleton-rect,
.skeleton-animated .skeleton-card-image,
.skeleton-animated .skeleton-product-image {
  animation: shimmer 1.5s infinite;
}

/* Text skeleton */
.skeleton-text {
  height: 1rem;
  border-radius: 0.25rem;
  margin-bottom: 0.5rem;
}

.skeleton-text-title {
  width: 70%;
  height: 1.25rem;
}

.skeleton-text-subtitle {
  width: 50%;
  height: 1rem;
  margin-top: 0.5rem;
}

.skeleton-text-description {
  width: 90%;
  height: 0.875rem;
  margin-top: 0.5rem;
}

.skeleton-text-price {
  width: 40%;
  height: 1.125rem;
  margin-top: 0.5rem;
}

.skeleton-text-name {
  width: 60%;
  height: 1rem;
}

/* Circle skeleton */
.skeleton-circle {
  border-radius: 50%;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
}

.skeleton-circle-small {
  width: 32px;
  height: 32px;
}

.skeleton-circle-medium {
  width: 48px;
  height: 48px;
}

/* Rect skeleton */
.skeleton-rect {
  border-radius: 0.5rem;
}

/* Card skeleton */
.skeleton-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  overflow: hidden;
  background: white;
}

.skeleton-card-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
}

.skeleton-card-content {
  padding: 1.5rem;
}

/* Product skeleton */
.skeleton-product {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  overflow: hidden;
  background: white;
}

.skeleton-product-image {
  width: 100%;
  aspect-ratio: 1;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
}

.skeleton-product-content {
  padding: 1rem;
}

.skeleton-product-colors {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

/* List skeleton */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background: white;
}

.skeleton-list-text {
  flex: 1;
}

/* Avatar skeleton */
.skeleton-avatar {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.skeleton-avatar-text {
  flex: 1;
}

/* Rounded variants */
.skeleton-rounded-sm {
  border-radius: 0.25rem;
}

.skeleton-rounded-md {
  border-radius: 0.5rem;
}

.skeleton-rounded-lg {
  border-radius: 0.75rem;
}

.skeleton-rounded-xl {
  border-radius: 1rem;
}

.skeleton-rounded-full {
  border-radius: 9999px;
}

/* Animation */
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Responsive */
@media (max-width: 640px) {
  .skeleton-card-image,
  .skeleton-product-image {
    height: 180px;
  }
  
  .skeleton-card-content,
  .skeleton-product-content {
    padding: 1rem;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .skeleton-animated .skeleton-text,
  .skeleton-animated .skeleton-circle,
  .skeleton-animated .skeleton-rect,
  .skeleton-animated .skeleton-card-image,
  .skeleton-animated .skeleton-product-image {
    animation: none;
    background: #f3f4f6;
  }
}
</style>