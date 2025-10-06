<template>
  <section class="collection-section section-full">
    <!-- Header -->
    <div class="section-header">
      <h2 class="section-title">Bộ sưu tập theo mục đích</h2>
      <router-link to="/san-pham" class="btn-view-all">
        Xem tất cả
      </router-link>
    </div>
    
    <!-- Full-width collections carousel -->
    <div class="collections-carousel-container">
      <button class="section-nav-btn prev" @click="scrollCollections('prev')">
        ‹
      </button>
      
      <div class="section-list collections-grid" ref="collectionsGrid">
        <div 
          v-for="collection in collections" 
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const collectionsGrid = ref(null)

const collections = [
  {
    id: 1,
    name: 'Đi làm',
    description: 'Áo sơ mi, quần âu chuyên nghiệp',
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
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
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'tap-gym'
  },
  {
    id: 4,
    name: 'Ở nhà',
    description: 'Áo thun, quần short thoải mái',
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
    filter: 'o-nha'
  }
]

const goToCollection = (filter) => {
  router.push(`/san-pham?muc-dich=${filter}`)
}

const scrollCollections = (direction) => {
  const el = collectionsGrid.value
  if (el) {
    const distance = el.offsetWidth * 0.8
    el.scrollBy({ 
      left: direction === 'next' ? distance : -distance, 
      behavior: 'smooth' 
    })
  }
}
</script>

<style scoped>
/* CollectionByUse specific styles - only content styling */

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

/* Desktop responsive - handled by sections.css */

/* Mobile responsive adjustments */
@media (max-width: 576px) {
  .section-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    margin-bottom: 2rem;
  }
  
  .section-title {
    font-size: 1.75rem;
  }
  
  .collection-section {
    padding: 2rem 0;
  }
}
</style>
