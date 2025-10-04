<template>
  <div class="col-md-4 mb-4">
    <div class="card category-card h-100">
      <div class="card-body text-center p-5">
        <!-- Icon skeleton -->
        <div class="category-icon-container mb-4">
          <div class="skeleton skeleton-icon"></div>
        </div>
        
        <!-- Title skeleton -->
        <div class="skeleton skeleton-title mb-3"></div>
        
        <!-- Description skeleton -->
        <div class="skeleton skeleton-description mb-2"></div>
        <div class="skeleton skeleton-description-short mb-4"></div>
        
        <!-- Button skeleton -->
        <div class="skeleton skeleton-button"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CategorySkeleton'
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

.category-icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80px;
}

.skeleton-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.skeleton-title {
  height: 28px;
  width: 60%;
  margin: 0 auto;
}

.skeleton-description {
  height: 16px;
  width: 90%;
  margin: 0 auto;
}

.skeleton-description-short {
  height: 16px;
  width: 70%;
  margin: 0 auto;
}

.skeleton-button {
  height: 40px;
  width: 70%;
  margin: 0 auto;
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
