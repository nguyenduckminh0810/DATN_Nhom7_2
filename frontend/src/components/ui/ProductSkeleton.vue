<template>
  <div class="col-md-6 col-lg-3 mb-4">
    <div class="card product-card h-100">
      <!-- Image skeleton -->
      <div class="skeleton-image-container">
        <div class="skeleton skeleton-image"></div>
      </div>
      
      <div class="card-body">
        <!-- Title skeleton -->
        <div class="skeleton skeleton-title mb-2"></div>
        
        <!-- Price skeleton -->
        <div class="skeleton skeleton-price mb-3"></div>
        
        <!-- Description skeleton -->
        <div class="skeleton skeleton-description mb-2"></div>
        <div class="skeleton skeleton-description-short mb-3"></div>
        
        <!-- Button skeleton -->
        <div class="skeleton skeleton-button"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductSkeleton'
}
</script>

<style scoped>
.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 8px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.skeleton-image-container {
  height: 250px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.skeleton-image {
  width: 100%;
  height: 100%;
}

.skeleton-title {
  height: 24px;
  width: 80%;
}

.skeleton-price {
  height: 20px;
  width: 60%;
}

.skeleton-description {
  height: 16px;
  width: 100%;
}

.skeleton-description-short {
  height: 16px;
  width: 70%;
}

.skeleton-button {
  height: 40px;
  width: 100%;
  border-radius: 20px;
}

/* Dark mode skeleton */
@media (prefers-color-scheme: dark) {
  .skeleton {
    background: linear-gradient(90deg, #2a2a2a 25%, #3a3a3a 50%, #2a2a2a 75%);
    background-size: 200% 100%;
  }
}
</style>
