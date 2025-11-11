<template>
  <div class="product-skeleton" :class="{ 'list-mode': viewMode === 'list' }">
    <div class="skeleton-card">
      <!-- Image skeleton -->
      <div class="skeleton-image"></div>

      <!-- Content skeleton -->
      <div class="skeleton-content">
        <!-- Title -->
        <div class="skeleton-line title"></div>
        <div class="skeleton-line subtitle"></div>

        <!-- Price -->
        <div class="skeleton-price">
          <div class="skeleton-line price-current"></div>
          <div class="skeleton-line price-old"></div>
        </div>

        <!-- Colors (grid mode only) -->
        <div v-if="viewMode === 'grid'" class="skeleton-colors">
          <div v-for="n in 3" :key="n" class="skeleton-color"></div>
        </div>

        <!-- Sizes (list mode only) -->
        <div v-if="viewMode === 'list'" class="skeleton-sizes">
          <div v-for="n in 4" :key="n" class="skeleton-size"></div>
        </div>

        <!-- Button -->
        <div class="skeleton-button"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  viewMode: {
    type: String,
    default: 'grid',
  },
})
</script>

<style scoped>
.product-skeleton {
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #f5f5f5;
}

.skeleton-image {
  width: 100%;
  height: 250px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 20px;
}

.skeleton-line {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-line.title {
  height: 18px;
  width: 85%;
}

.skeleton-line.subtitle {
  height: 14px;
  width: 60%;
}

.skeleton-price {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}

.skeleton-line.price-current {
  height: 20px;
  width: 80px;
  margin-bottom: 0;
}

.skeleton-line.price-old {
  height: 14px;
  width: 60px;
  margin-bottom: 0;
}

.skeleton-colors {
  display: flex;
  gap: 8px;
  margin: 16px 0;
}

.skeleton-color {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-sizes {
  display: flex;
  gap: 8px;
  margin: 16px 0;
}

.skeleton-size {
  width: 40px;
  height: 32px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-button {
  height: 40px;
  width: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
  margin-top: 16px;
}

/* List mode styles */
.product-skeleton.list-mode .skeleton-card {
  display: flex;
  padding: 20px;
}

.product-skeleton.list-mode .skeleton-image {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  margin-right: 20px;
  border-radius: 12px;
}

.product-skeleton.list-mode .skeleton-content {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-skeleton.list-mode .skeleton-line.title {
  height: 24px;
  width: 70%;
}

.product-skeleton.list-mode .skeleton-line.subtitle {
  height: 16px;
  width: 90%;
}

.product-skeleton.list-mode .skeleton-button {
  width: 150px;
  margin-top: 20px;
}

/* Animations */
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .product-skeleton.list-mode .skeleton-card {
    flex-direction: column;
    padding: 16px;
  }

  .product-skeleton.list-mode .skeleton-image {
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 16px;
  }

  .skeleton-content {
    padding: 16px;
  }

  .skeleton-image {
    height: 200px;
  }
}
</style>
