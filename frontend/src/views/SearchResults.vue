<template>
  <div class="search-results">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="py-3 bg-light">
      <div class="container">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <router-link to="/" class="text-decoration-none">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active">Tìm kiếm</li>
        </ol>
      </div>
    </nav>

    <div class="container py-5">
      <!-- Search Header -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="search-header">
            <h2 class="mb-3">
              <i class="bi bi-search me-2"></i>
              Kết quả tìm kiếm
            </h2>
            
            <!-- Search Input -->
            <div class="search-input-container mb-4">
              <div class="input-group">
                <span class="input-group-text bg-transparent border-0">
                  <i class="bi bi-search text-muted"></i>
                </span>
                <input 
                  type="text" 
                  class="form-control modern-search-input border-0" 
                  placeholder="Tìm kiếm sản phẩm..."
                  v-model="searchQuery"
                  @keyup.enter="performSearch"
                >
                <button 
                  class="btn btn-auro-primary" 
                  type="button"
                  @click="performSearch"
                  :disabled="!searchQuery.trim()"
                >
                  <i class="bi bi-search"></i>
                </button>
              </div>
            </div>

            <!-- Results Summary -->
            <div v-if="searchQuery.trim()" class="results-summary">
              <div class="row align-items-center">
                <div class="col-md-6">
                  <p class="mb-0">
                    <span class="fw-bold">{{ resultCount }}</span> sản phẩm cho 
                    <span class="fw-bold text-primary">"{{ searchQuery }}"</span>
                  </p>
                </div>
                <div class="col-md-6 text-md-end">
                  <div class="d-flex align-items-center justify-content-md-end gap-3">
                    <!-- Sort Options -->
                    <select class="form-select form-select-sm" v-model="sortBy" @change="sortResults">
                      <option value="relevance">Sắp xếp theo độ liên quan</option>
                      <option value="price-asc">Giá tăng dần</option>
                      <option value="price-desc">Giá giảm dần</option>
                      <option value="name-asc">Tên A-Z</option>
                      <option value="name-desc">Tên Z-A</option>
                      <option value="discount-desc">Giảm giá cao nhất</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isSearching" class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status">
          <span class="visually-hidden">Đang tìm kiếm...</span>
        </div>
        <h5>Đang tìm kiếm sản phẩm...</h5>
        <p class="text-muted">Vui lòng chờ trong giây lát</p>
      </div>

      <!-- No Results -->
      <div v-else-if="searchQuery.trim() && !hasResults" class="no-results text-center py-5">
        <div class="no-results-icon mb-4">
          <i class="bi bi-search text-muted" style="font-size: 4rem;"></i>
        </div>
        <h4 class="mb-3">Không tìm thấy sản phẩm</h4>
        <p class="text-muted mb-4">
          Không có sản phẩm nào phù hợp với từ khóa <strong>"{{ searchQuery }}"</strong>
        </p>
        <div class="suggestions">
          <h6 class="mb-3">Gợi ý:</h6>
          <ul class="list-unstyled">
            <li>• Kiểm tra chính tả từ khóa</li>
            <li>• Thử từ khóa khác ngắn hơn</li>
            <li>• Sử dụng từ khóa chung hơn</li>
            <li>• Thử tìm kiếm với từ đồng nghĩa</li>
          </ul>
        </div>
        <div class="mt-4">
          <router-link to="/category" class="btn btn-auro-primary">
            <i class="bi bi-grid-3x3-gap me-2"></i>Xem tất cả sản phẩm
          </router-link>
        </div>
      </div>

      <!-- Search Results -->
      <div v-else-if="hasResults" class="search-results-grid">
        <div class="row g-4">
          <div class="col-md-6 col-lg-4 col-xl-3" v-for="product in sortedResults" :key="product.id">
            <div class="card product-card h-100 animate-on-scroll">
              <div class="position-relative product-image-container">
                <img :src="product.image" :alt="product.name" class="card-img-top">
                <div class="position-absolute top-0 end-0 m-3">
                  <span class="badge modern-discount-badge">-{{ product.discount }}%</span>
                </div>
                <div class="product-overlay">
                  <button class="btn btn-auro-primary btn-sm" @click="addToCart(product)">
                    <i class="bi bi-cart3 me-1"></i>Thêm vào giỏ
                  </button>
                </div>
              </div>
              <div class="card-body d-flex flex-column">
                <h6 class="card-title fw-bold mb-2">{{ product.name }}</h6>
                <p class="card-text text-muted small flex-grow-1 mb-3">{{ product.description }}</p>
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <span class="price h5 mb-0">{{ formatPrice(product.price) }}</span>
                    <small class="text-muted text-decoration-line-through ms-2">
                      {{ formatPrice(product.originalPrice) }}
                    </small>
                  </div>
                  <div class="product-rating">
                    <i class="bi bi-star-fill text-warning"></i>
                    <i class="bi bi-star-fill text-warning"></i>
                    <i class="bi bi-star-fill text-warning"></i>
                    <i class="bi bi-star-fill text-warning"></i>
                    <i class="bi bi-star text-warning"></i>
                    <small class="ms-1 text-muted">(4.5)</small>
                  </div>
                </div>
                <router-link :to="`/product/${product.id}`" class="btn btn-outline-primary mt-3">
                  Xem chi tiết
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State (No search query) -->
      <div v-else class="empty-state text-center py-5">
        <div class="empty-state-icon mb-4">
          <i class="bi bi-search text-muted" style="font-size: 4rem;"></i>
        </div>
        <h4 class="mb-3">Tìm kiếm sản phẩm</h4>
        <p class="text-muted mb-4">
          Nhập từ khóa để tìm kiếm sản phẩm bạn quan tâm
        </p>
        
        <!-- Popular Searches -->
        <div class="popular-searches">
          <h6 class="mb-3">Tìm kiếm phổ biến:</h6>
          <div class="popular-tags">
            <span 
              v-for="tag in popularSearches" 
              :key="tag"
              class="badge popular-tag me-2 mb-2"
              @click="searchTag(tag)"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSearchStore } from '../stores/search'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const searchStore = useSearchStore()
const cartStore = useCartStore()

// Local state
const searchQuery = ref('')
const sortBy = ref('relevance')

// Computed
const isSearching = computed(() => searchStore.isSearching)
const hasResults = computed(() => searchStore.hasResults)
const resultCount = computed(() => searchStore.resultCount)
const searchResults = computed(() => searchStore.searchResults)
const popularSearches = computed(() => searchStore.popularSearches)

const sortedResults = computed(() => {
  const results = [...searchResults.value]
  
  switch (sortBy.value) {
    case 'price-asc':
      return results.sort((a, b) => a.price - b.price)
    case 'price-desc':
      return results.sort((a, b) => b.price - a.price)
    case 'name-asc':
      return results.sort((a, b) => a.name.localeCompare(b.name))
    case 'name-desc':
      return results.sort((a, b) => b.name.localeCompare(a.name))
    case 'discount-desc':
      return results.sort((a, b) => b.discount - a.discount)
    default:
      return results // relevance - keep original order
  }
})

// Methods
const performSearch = () => {
  if (searchQuery.value.trim()) {
    searchStore.searchProducts(searchQuery.value)
  }
}

const sortResults = () => {
  // Results are automatically sorted via computed property
}

const searchTag = (tag) => {
  searchQuery.value = tag
  performSearch()
}

const addToCart = (product) => {
  cartStore.addItem(product)
  
  if (window.$toast) {
    window.$toast.success(
      `${product.name} đã được thêm vào giỏ hàng`,
      'Thêm vào giỏ hàng thành công'
    )
  }
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Lifecycle
onMounted(() => {
  // Get search query from route params
  const query = route.query.q
  if (query) {
    searchQuery.value = query
    performSearch()
  }
})

// Watch for route changes
watch(() => route.query.q, (newQuery) => {
  if (newQuery && newQuery !== searchQuery.value) {
    searchQuery.value = newQuery
    performSearch()
  }
})
</script>

<style scoped>
.search-results {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 6.5rem 0 2rem;
}

@media (max-width: 768px) {
  .search-results {
    padding: 5rem 0 1.5rem;
  }
}

@media (max-width: 576px) {
  .search-results {
    padding: 4.5rem 0 1.5rem;
  }
}

.search-input-container {
  max-width: 600px;
  margin: 0 auto;
}

.modern-search-input {
  font-size: 1.1rem;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.modern-search-input:focus {
  background: white;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.results-summary {
  background: #f8f9fa;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.popular-tag {
  background: #e9ecef;
  color: #495057;
  padding: 8px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.popular-tag:hover {
  background: var(--auro-accent);
  color: var(--auro-dark);
  transform: translateY(-2px);
}

.no-results-icon,
.empty-state-icon {
  opacity: 0.6;
}

.suggestions {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  max-width: 400px;
  margin: 0 auto;
}

.suggestions ul li {
  color: #6c757d;
  margin-bottom: 4px;
}

/* Scroll animations */
.animate-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s ease;
}

.animate-on-scroll.animate {
  opacity: 1;
  transform: translateY(0);
}

/* Stagger animation for multiple elements */
.animate-on-scroll:nth-child(1) { transition-delay: 0.1s; }
.animate-on-scroll:nth-child(2) { transition-delay: 0.2s; }
.animate-on-scroll:nth-child(3) { transition-delay: 0.3s; }
.animate-on-scroll:nth-child(4) { transition-delay: 0.4s; }
</style>
