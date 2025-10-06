<template>
  <section class="collection-section section-full">
    <!-- Header -->
    <div class="section-header">
      <h2 class="section-title">Bộ sưu tập theo mục đích</h2>
    </div>
    
    <!-- Full-width collections carousel -->
    <div class="collections-carousel-container">
      <button class="section-nav-btn prev" @click="scrollCollections('prev')">
        ‹
      </button>
      
      <div class="section-list collections-grid" ref="collectionsGrid">
        <div v-if="isLoading" v-for="n in 6" :key="n" class="collection-card section-item skeleton">
          <div class="skeleton-image"></div>
          <div class="collection-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-description"></div>
          </div>
        </div>
        
        <div 
          v-else
          v-for="collection in displayCollections" 
          :key="collection.id"
          class="collection-card section-item"
          @click="goToCollection(collection.filter)"
        >
          <img :src="collection.image" :alt="collection.name" class="section-item__image"/>
          <div class="collection-content">
            <h3 class="collection-title">{{ collection.name }}</h3>
            <p class="collection-description">{{ collection.description }}</p>
          </div>
        </div>
      </div>
      
      <button class="section-nav-btn next" @click="scrollCollections('next')">
        ›
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const collectionsGrid = ref(null)
const isLoading = ref(false)
const currentCollectionIndex = ref(0)

const collections = [
  {
    id: 1,
    name: 'Đi làm',
    description: 'Áo sơ mi, quần âu chuyên nghiệp',
    image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'di-lam'
  },
  {
    id: 2,
    name: 'Đi chơi',
    description: 'Áo thun, quần jean năng động',
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'di-choi'
  },
  {
    id: 3,
    name: 'Tập gym',
    description: 'Đồ thể thao, áo thun co giãn',
    image: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'tap-gym'
  },
  {
    id: 4,
    name: 'Ở nhà',
    description: 'Áo thun, quần short thoải mái',
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'o-nha'
  },
  {
    id: 5,
    name: 'Du lịch',
    description: 'Đồ thoải mái, dễ di chuyển',
    image: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'du-lich'
  },
  {
    id: 6,
    name: 'Hẹn hò',
    description: 'Áo sơ mi, quần âu lịch lãm',
    image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'hen-ho'
  }
]

const goToCollection = (filter) => {
  router.push(`/san-pham?muc-dich=${filter}`)
}

const scrollCollections = (direction) => {
  // For fixed layout, we need to cycle through collections
  // This will be handled by changing the displayed collections
  if (direction === 'next') {
    // Move to next set of collections
    currentCollectionIndex.value = (currentCollectionIndex.value + 1) % collections.length
  } else {
    // Move to previous set of collections
    currentCollectionIndex.value = (currentCollectionIndex.value - 1 + collections.length) % collections.length
  }
}

// Display 5 collections starting from currentCollectionIndex
const displayCollections = computed(() => {
  if (collections.length === 0) return []
  
  const result = []
  for (let i = 0; i < 5; i++) {
    const index = (currentCollectionIndex.value + i) % collections.length
    result.push(collections[index])
  }
  return result
})
</script>

<style scoped>
/* CollectionByUse specific styles - only content styling */
.section-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
  width: 100%;
  text-align: center;
}

.collections-carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  width: 100%;
}

/* Collection content styling only */
.collection-content {
  padding: 1.5rem;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.collection-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #212529;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.collection-description {
  color: #6c757d;
  font-size: 0.9rem;
  margin: 0;
  font-weight: 500;
}

/* Skeleton loading for collections */
.skeleton {
  background: #f8f9fa;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}

.skeleton-image {
  width: 100%;
  height: 420px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-title {
  width: 70%;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin: 0 auto 0.5rem;
}

.skeleton-description {
  width: 90%;
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin: 0 auto;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Desktop responsive - handled by sections.css */

/* Mobile responsive adjustments */
@media (max-width: 576px) {
  .section-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    margin-bottom: 2rem;
    justify-content: center;
  }
  
  .section-title {
    font-size: 1.75rem;
  }
  
  .collection-section {
    padding: 2rem 0;
  }
}
</style>

