<template>
  <div 
    class="modal fade" 
    id="searchModal" 
    tabindex="-1" 
    aria-labelledby="searchModalLabel" 
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content modern-modal">
        <div class="modal-header border-0">
          <div class="search-input-container w-100">
            <div class="input-group">
              <span class="input-group-text bg-transparent border-0">
                <i class="bi bi-search text-muted"></i>
              </span>
              <input 
                type="text" 
                class="form-control modern-search-input border-0" 
                placeholder="Tìm kiếm sản phẩm..."
                v-model="searchQuery"
                @input="handleSearch"
                @keyup.enter="performSearch"
                @keyup.escape="closeModal"
                ref="searchInput"
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
        </div>

        <div class="modal-body p-0">
          <!-- Search suggestions -->
          <div v-if="!searchQuery.trim() && suggestions.length > 0" class="suggestions-section">
            <div class="px-4 py-3 border-bottom">
              <h6 class="mb-3 text-muted">
                <i class="bi bi-lightbulb me-2"></i>Tìm kiếm phổ biến
              </h6>
              <div class="suggestion-tags">
                <span 
                  v-for="suggestion in suggestions" 
                  :key="suggestion"
                  class="badge suggestion-badge me-2 mb-2"
                  @click="selectSuggestion(suggestion)"
                >
                  {{ suggestion }}
                </span>
              </div>
            </div>
          </div>

          <!-- Search history -->
          <div v-if="!searchQuery.trim() && searchHistory.length > 0" class="history-section">
            <div class="px-4 py-3 border-bottom">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="mb-0 text-muted">
                  <i class="bi bi-clock me-2"></i>Lịch sử tìm kiếm
                </h6>
                <button 
                  class="btn btn-sm btn-outline-secondary"
                  @click="clearSearchHistory"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </div>
              <div class="history-items">
                <div 
                  v-for="item in searchHistory" 
                  :key="item"
                  class="history-item"
                  @click="selectSuggestion(item)"
                >
                  <i class="bi bi-clock text-muted me-2"></i>
                  {{ item }}
                </div>
              </div>
            </div>
          </div>

          <!-- Search results -->
          <div v-if="searchQuery.trim()" class="results-section">
            <div v-if="isSearching" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tìm kiếm...</span>
              </div>
              <p class="mt-3 text-muted">Đang tìm kiếm sản phẩm...</p>
            </div>

            <div v-else-if="hasResults" class="results-container">
              <div class="px-4 py-3 border-bottom bg-light">
                <h6 class="mb-0">
                  Tìm thấy {{ resultCount }} sản phẩm cho "{{ searchQuery }}"
                </h6>
              </div>
              
              <div class="results-list">
                <div 
                  v-for="product in searchResults" 
                  :key="product.id"
                  class="result-item"
                  @click="selectProduct(product)"
                >
                  <div class="result-image">
                    <img :src="product.image" :alt="product.name" class="img-fluid">
                  </div>
                  <div class="result-content">
                    <h6 class="result-title">{{ product.name }}</h6>
                    <p class="result-description">{{ product.description }}</p>
                    <div class="result-price">
                      <span class="price">{{ formatPrice(product.price) }}</span>
                      <small class="text-muted text-decoration-line-through ms-2">
                        {{ formatPrice(product.originalPrice) }}
                      </small>
                      <span class="badge bg-danger ms-2">-{{ product.discount }}%</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="no-results text-center py-5">
              <i class="bi bi-search text-muted" style="font-size: 3rem;"></i>
              <h6 class="mt-3 text-muted">Không tìm thấy sản phẩm</h6>
              <p class="text-muted">Thử tìm kiếm với từ khóa khác</p>
            </div>
          </div>
        </div>

        <div class="modal-footer border-0">
          <button type="button" class="btn btn-outline-secondary" @click="closeModal">
            Đóng
          </button>
          <router-link 
            v-if="hasResults" 
            to="/search" 
            class="btn btn-auro-primary"
            @click="closeModal"
          >
            Xem tất cả kết quả
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useSearchStore } from '../../stores/search'
import { useCartStore } from '../../stores/cart'

const router = useRouter()
const searchStore = useSearchStore()
const cartStore = useCartStore()

const searchInput = ref(null)

// Local state
const searchQuery = ref('')

// Computed
const suggestions = computed(() => searchStore.getSuggestions(searchQuery.value))
const searchHistory = computed(() => searchStore.searchHistory)
const isSearching = computed(() => searchStore.isSearching)
const hasResults = computed(() => searchStore.hasResults)
const resultCount = computed(() => searchStore.resultCount)
const searchResults = computed(() => searchStore.searchResults)

// Methods
const handleSearch = () => {
  // Debounce search
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    if (searchQuery.value.trim()) {
      searchStore.searchProducts(searchQuery.value)
    }
  }, 300)
}

const performSearch = () => {
  if (searchQuery.value.trim()) {
    searchStore.searchProducts(searchQuery.value)
  }
}

const selectSuggestion = (suggestion) => {
  searchQuery.value = suggestion
  performSearch()
}

const selectProduct = (product) => {
  closeModal()
  router.push(`/product/${product.id}`)
}

const closeModal = () => {
  const modal = document.getElementById('searchModal')
  if (modal) {
    const bootstrap = window.bootstrap
    if (bootstrap) {
      const modalInstance = bootstrap.Modal.getInstance(modal)
      if (modalInstance) {
        modalInstance.hide()
      }
    }
  }
}

const clearSearchHistory = () => {
  searchStore.clearSearchHistory()
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Watch for modal open
watch(() => document.getElementById('searchModal'), (modal) => {
  if (modal) {
    modal.addEventListener('shown.bs.modal', () => {
      nextTick(() => {
        searchInput.value?.focus()
      })
    })
  }
})

// Debounce timer
let searchTimeout = null
</script>

<style scoped>
.modern-modal {
  border-radius: 16px;
  border: none;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.search-input-container {
  position: relative;
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

.suggestion-badge {
  background: #e9ecef;
  color: #495057;
  padding: 8px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.suggestion-badge:hover {
  background: var(--auro-accent);
  color: var(--auro-dark);
  transform: translateY(-2px);
}

.history-item {
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.history-item:hover {
  background: #f8f9fa;
  color: var(--auro-primary);
}

.results-container {
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.result-item:hover {
  background: #f8f9fa;
}

.result-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 16px;
  flex-shrink: 0;
}

.result-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-content {
  flex: 1;
}

.result-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--auro-dark);
}

.result-description {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 8px;
  line-height: 1.4;
}

.result-price {
  display: flex;
  align-items: center;
  font-size: 0.9rem;
}

.result-price .price {
  font-weight: 600;
  color: var(--auro-accent);
}

.no-results {
  color: #6c757d;
}

/* Custom scrollbar */
.results-container::-webkit-scrollbar {
  width: 6px;
}

.results-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.results-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.results-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
