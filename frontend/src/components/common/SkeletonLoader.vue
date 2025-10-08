<template>
  <div class="skeleton-loader" :class="[`skeleton-${type}`, { 'skeleton-animated': animated }]">
    <!-- Product Card Skeleton -->
    <div v-if="type === 'product'" class="skeleton-product">
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-title"></div>
        <div class="skeleton-price"></div>
        <div class="skeleton-rating"></div>
      </div>
    </div>

    <!-- Category Card Skeleton -->
    <div v-else-if="type === 'category'" class="skeleton-category">
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-title"></div>
        <div class="skeleton-description"></div>
      </div>
    </div>

    <!-- Text Skeleton -->
    <div v-else-if="type === 'text'" class="skeleton-text">
      <div v-for="i in lines" :key="i" class="skeleton-line" :style="{ width: getRandomWidth() }"></div>
    </div>

    <!-- Avatar Skeleton -->
    <div v-else-if="type === 'avatar'" class="skeleton-avatar" :style="{ width: size, height: size }"></div>

    <!-- Button Skeleton -->
    <div v-else-if="type === 'button'" class="skeleton-button" :style="{ width: width, height: height }"></div>

    <!-- Card Skeleton -->
    <div v-else-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-header"></div>
      <div class="skeleton-body">
        <div class="skeleton-line"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line" style="width: 60%"></div>
      </div>
    </div>

    <!-- Default Skeleton -->
    <div v-else class="skeleton-default" :style="{ width: width, height: height }"></div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'product', 'category', 'text', 'avatar', 'button', 'card'].includes(value)
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '20px'
  },
  size: {
    type: String,
    default: '40px'
  },
  lines: {
    type: Number,
    default: 3
  },
  animated: {
    type: Boolean,
    default: true
  }
})

const getRandomWidth = () => {
  const widths = ['100%', '80%', '60%', '90%', '70%']
  return widths[Math.floor(Math.random() * widths.length)]
}
</script>

<style scoped>
.skeleton-loader {
  display: inline-block;
  position: relative;
  overflow: hidden;
}

.skeleton-animated::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: skeleton-shimmer 1.5s infinite;
}

@keyframes skeleton-shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* Product Skeleton */
.skeleton-product {
  background: var(--auro-card);
  border-radius: 12px;
  padding: 1rem;
  box-shadow: var(--auro-shadow);
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: var(--auro-hover);
  border-radius: 8px;
  margin-bottom: 1rem;
}

.skeleton-title {
  width: 80%;
  height: 16px;
  background: var(--auro-hover);
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-price {
  width: 40%;
  height: 14px;
  background: var(--auro-hover);
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-rating {
  width: 60%;
  height: 12px;
  background: var(--auro-hover);
  border-radius: 4px;
}

/* Category Skeleton */
.skeleton-category {
  background: var(--auro-card);
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: var(--auro-shadow);
}

.skeleton-category .skeleton-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 1rem;
}

.skeleton-category .skeleton-title {
  width: 70%;
  margin: 0 auto 0.5rem;
}

.skeleton-description {
  width: 90%;
  height: 12px;
  background: var(--auro-hover);
  border-radius: 4px;
  margin: 0 auto;
}

/* Text Skeleton */
.skeleton-text {
  width: 100%;
}

.skeleton-line {
  height: 12px;
  background: var(--auro-hover);
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-line:last-child {
  margin-bottom: 0;
}

/* Avatar Skeleton */
.skeleton-avatar {
  border-radius: 50%;
  background: var(--auro-hover);
}

/* Button Skeleton */
.skeleton-button {
  border-radius: 8px;
  background: var(--auro-hover);
}

/* Card Skeleton */
.skeleton-card {
  background: var(--auro-card);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--auro-shadow);
}

.skeleton-header {
  width: 100%;
  height: 20px;
  background: var(--auro-hover);
  border-radius: 4px;
  margin-bottom: 1rem;
}

.skeleton-body .skeleton-line {
  margin-bottom: 0.75rem;
}

/* Default Skeleton */
.skeleton-default {
  background: var(--auro-hover);
  border-radius: 4px;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .skeleton-animated::before {
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  }
  
  .skeleton-image,
  .skeleton-title,
  .skeleton-price,
  .skeleton-rating,
  .skeleton-description,
  .skeleton-line,
  .skeleton-avatar,
  .skeleton-button,
  .skeleton-header,
  .skeleton-default {
    background: rgba(255, 255, 255, 0.1);
  }
}
</style>
