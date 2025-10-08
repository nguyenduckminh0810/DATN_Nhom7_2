<template>
  <div class="category">
    <div class="container py-5">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb modern-breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/" class="breadcrumb-link">
              <i class="bi bi-house me-1"></i>Trang chủ
            </router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            {{ categoryName || 'Danh mục' }}
          </li>
        </ol>
      </nav>

      <!-- Category Header -->
      <div class="row mb-5">
        <div class="col-12">
          <div class="category-header text-center">
            <h1 class="display-4 fw-bold mb-3">{{ categoryName || 'Tất cả sản phẩm' }}</h1>
            <p class="text-muted fs-5">{{ products.length }} sản phẩm</p>
            <div class="section-divider"></div>
          </div>
        </div>
      </div>

      <div class="row">
        <!-- Sidebar Filters -->
        <div class="col-lg-3 mb-4">
          <ProductFilters />
        </div>

        <!-- Products Grid -->
        <div class="col-lg-9">
          <!-- Sort and View Options -->
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div class="d-flex align-items-center gap-3">
              <span class="text-muted fw-medium">Sắp xếp theo:</span>
              <select v-model="searchStore.activeFilters.sortBy" class="form-select modern-select">
                <option value="relevance">Độ liên quan</option>
                <option value="price-asc">Giá tăng dần</option>
                <option value="price-desc">Giá giảm dần</option>
                <option value="name-asc">Tên A-Z</option>
                <option value="name-desc">Tên Z-A</option>
                <option value="discount-desc">Giảm giá cao nhất</option>
                <option value="newest">Mới nhất</option>
                <option value="popular">Phổ biến nhất</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button @click="viewMode = 'grid'" :class="['btn btn-sm modern-view-btn', viewMode === 'grid' ? 'btn-warning' : 'btn-outline-secondary']">
                <i class="bi bi-grid-3x3-gap"></i>
              </button>
              <button @click="viewMode = 'list'" :class="['btn btn-sm modern-view-btn', viewMode === 'list' ? 'btn-warning' : 'btn-outline-secondary']">
                <i class="bi bi-list"></i>
              </button>
            </div>
          </div>

          <!-- Products -->
          <div v-if="filteredProducts.length > 0" :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
            <div v-for="product in filteredProducts" :key="product.id" 
                 :class="viewMode === 'list' ? 'col-12' : 'col-md-6 col-lg-4'">
              <div class="card product-card h-100">
                <div class="position-relative product-image-container">
                  <LazyImage
                    :src="product.image || 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'"
                    :alt="product.name"
                    :width="500"
                    :height="500"
                    :quality="85"
                    :webp="true"
                    :lazy="true"
                    container-class="product-image"
                    image-class="card-img-top"
                    show-zoom-overlay="true"
                  />
                  <div class="position-absolute top-0 end-0 m-3">
                    <span class="badge modern-discount-badge">-{{ product.discount || 20 }}%</span>
                  </div>
                  <div class="position-absolute top-0 start-0 m-3">
                    <WishlistButton :product="product" variant="icon" size="sm" />
                  </div>
                  <div class="product-overlay">
                    <button class="btn btn-auro-primary btn-sm" @click="addToCart(product)">
                      <i class="bi bi-cart-plus me-1"></i>Thêm vào giỏ
                    </button>
                  </div>
                </div>
                <div class="card-body d-flex flex-column">
                  <h6 class="card-title fw-bold mb-2">{{ product.name }}</h6>
                  <p class="card-text text-muted small flex-grow-1 mb-3">{{ product.description }}</p>
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <span class="price h5 mb-0">{{ formatPrice(product.price) }}</span>
                      <small class="text-muted text-decoration-line-through ms-2">{{ formatPrice(product.originalPrice || product.price * 1.2) }}</small>
                    </div>
                    <div class="product-rating">
                      <i class="bi bi-star-fill text-warning"></i>
                      <i class="bi bi-star-fill text-warning"></i>
                      <i class="bi bi-star-fill text-warning"></i>
                      <i class="bi bi-star-fill text-warning"></i>
                      <i class="bi bi-star text-warning"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- No Products -->
          <div v-else class="text-center py-5">
            <div class="empty-state">
              <i class="bi bi-search display-1 text-muted mb-4"></i>
              <h5 class="mt-3 mb-3">Không tìm thấy sản phẩm</h5>
              <p class="text-muted mb-4">Hãy thử điều chỉnh bộ lọc của bạn</p>
              <button @click="clearFilters" class="btn btn-auro-primary">
                <i class="bi bi-arrow-clockwise me-2"></i>Xóa bộ lọc
              </button>
            </div>
          </div>

          <!-- Pagination -->
          <nav v-if="totalPages > 1" class="mt-5">
            <ul class="pagination justify-content-center modern-pagination">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <button class="page-link" @click="changePage(currentPage - 1)">
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li v-for="page in visiblePages" :key="page" class="page-item" :class="{ active: page === currentPage }">
                <button class="page-link" @click="changePage(page)">{{ page }}</button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <button class="page-link" @click="changePage(currentPage + 1)">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useSearchStore } from '../stores/search'
import { useProductStore } from '../stores/product'
import ProductFilters from '../components/product/ProductFilters.vue'
import WishlistButton from '../components/product/WishlistButton.vue'
import LazyImage from '../components/common/LazyImage.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const searchStore = useSearchStore()
const productStore = useProductStore()

// Reactive data
const products = ref([])
const categoryName = ref('')
const isLoading = ref(true)
const error = ref(null)
const viewMode = ref('grid')
const currentPage = ref(1)
const itemsPerPage = 12

// Category slug to ID mapping (will be replaced with API call to get category by slug)
const categorySlugMap = {
  'ao-thun': { name: 'ÁO THUN', id: 'ao-thun' },
  'ao-so-mi': { name: 'ÁO SƠ MI', id: 'ao-so-mi' },
  'ao-khoac': { name: 'ÁO KHOÁC', id: 'ao-khoac' },
  'ao-polo': { name: 'ÁO POLO', id: 'ao-polo' },
  'quan-au': { name: 'QUẦN ÂU', id: 'quan-au' },
  'quan-jean': { name: 'QUẦN JEAN', id: 'quan-jean' },
  'quan-short': { name: 'QUẦN SHORT', id: 'quan-short' },
  'quan-jogger': { name: 'QUẦN JOGGER', id: 'quan-jogger' }
}

// API Functions
const fetchProductsByCategory = async (categorySlug) => {
  try {
    isLoading.value = true
    error.value = null
    
    const categoryInfo = categorySlugMap[categorySlug]
    
    if (categoryInfo) {
      categoryName.value = categoryInfo.name
      
      // Fetch products by category
      const result = await productStore.fetchProductsByCategory(categoryInfo.id)
      
      if (result.success && result.data?.products) {
        products.value = result.data.products
      } else {
        error.value = result.message || 'Không thể tải danh sách sản phẩm'
        products.value = []
        if (window.$toast) {
          window.$toast.error(error.value, 'Lỗi tải dữ liệu')
        }
      }
    } else {
      categoryName.value = 'Danh mục không tìm thấy'
      products.value = []
      error.value = 'Danh mục không tồn tại'
    }
  } catch (err) {
    error.value = err.message || 'Có lỗi xảy ra khi tải sản phẩm'
    products.value = []
    if (window.$toast) {
      window.$toast.error(error.value, 'Lỗi')
    }
  } finally {
    isLoading.value = false
  }
}

const fetchAllProducts = async () => {
  try {
    isLoading.value = true
    error.value = null
    categoryName.value = 'Tất cả sản phẩm'
    
    const result = await productStore.fetchProducts()
    
    if (result.success && result.data?.products) {
      products.value = result.data.products
    } else {
      error.value = result.message || 'Không thể tải danh sách sản phẩm'
      products.value = []
      if (window.$toast) {
        window.$toast.error(error.value, 'Lỗi tải dữ liệu')
      }
    }
  } catch (err) {
    error.value = err.message || 'Có lỗi xảy ra khi tải sản phẩm'
    products.value = []
    if (window.$toast) {
      window.$toast.error(error.value, 'Lỗi')
    }
  } finally {
    isLoading.value = false
  }
}

// Computed properties
const filteredProducts = computed(() => {
  // Apply filters using filters store
  const filtered = searchStore.applyFilters(products.value)
  
  // Apply pagination
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return filtered.slice(startIndex, endIndex)
})

const totalPages = computed(() => {
  const totalFiltered = searchStore.applyFilters(products.value).length
  return Math.ceil(totalFiltered / itemsPerPage)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const addToCart = (product) => {
  cartStore.addItem(product)
}

const clearFilters = () => {
  searchStore.clearAllFilters()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// Watch for route changes
watch(() => route.params.slug, (newSlug) => {
  // Reset to first page when category changes
  currentPage.value = 1
  
  if (newSlug) {
    fetchProductsByCategory(newSlug)
  } else {
    fetchAllProducts()
  }
}, { immediate: false })

// Lifecycle
onMounted(() => {
  const slug = route.params.slug
  
  if (slug) {
    fetchProductsByCategory(slug)
  } else {
    fetchAllProducts()
  }
})
</script>

<style scoped>
/* Breadcrumb */
.modern-breadcrumb {
  background: none;
  padding: 0;
  margin: 0;
}

.breadcrumb-link {
  text-decoration: none;
  color: var(--auro-text-light);
  transition: color 0.3s ease;
  font-weight: 500;
}

.breadcrumb-link:hover {
  color: var(--auro-accent);
}

/* Section Divider */
.section-divider {
  width: 80px;
  height: 4px;
  background: var(--auro-gradient-accent);
  margin: 0 auto;
  border-radius: 2px;
}

/* Filter Card */
.modern-filter-card {
  background: var(--auro-card);
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  box-shadow: var(--auro-shadow);
  position: sticky;
  top: 100px;
}

.modern-filter-header {
  background: var(--auro-gradient);
  color: white;
  border-radius: 20px 20px 0 0;
  border: none;
  padding: 1.5rem;
}

.filter-title {
  color: var(--auro-text);
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
}

.filter-options {
  padding-left: 0.5rem;
}

.modern-check {
  margin-bottom: 0.75rem;
}

.modern-check .form-check-input {
  border-radius: 4px;
  border: 2px solid var(--auro-border);
  width: 18px;
  height: 18px;
}

.modern-check .form-check-input:checked {
  background-color: var(--auro-accent);
  border-color: var(--auro-accent);
}

.modern-check .form-check-label {
  font-weight: 500;
  color: var(--auro-text);
  cursor: pointer;
  margin-left: 0.5rem;
}

/* Sort and View */
.modern-select {
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  padding: 8px 16px;
  font-weight: 500;
  background: var(--auro-card);
  min-width: 200px;
}

.modern-select:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
}

.modern-view-btn {
  border-radius: 8px;
  padding: 8px 12px;
  border: 2px solid var(--auro-border);
  transition: all 0.3s ease;
}

.modern-view-btn:hover {
  border-color: var(--auro-accent);
  background: rgba(212, 175, 55, 0.1);
}

.modern-view-btn.btn-warning {
  background: var(--auro-gradient-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
}

/* Product Cards */
.product-image-container {
  overflow: hidden;
  border-radius: 16px 16px 0 0;
}

.product-card .card-img-top {
  height: 300px;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 26, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.product-card:hover .card-img-top {
  transform: scale(1.1);
}

.modern-discount-badge {
  background: var(--auro-gradient-accent) !important;
  color: var(--auro-dark) !important;
  font-weight: 700;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.3);
}

.product-rating {
  font-size: 14px;
}

/* Empty State */
.empty-state {
  padding: 3rem;
  background: var(--auro-card);
  border-radius: 20px;
  border: 1px solid var(--auro-border);
}

/* Pagination */
.modern-pagination .page-link {
  border: 2px solid var(--auro-border);
  color: var(--auro-text);
  padding: 12px 16px;
  margin: 0 4px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.modern-pagination .page-link:hover {
  background: var(--auro-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
  transform: translateY(-2px);
}

.modern-pagination .page-item.active .page-link {
  background: var(--auro-gradient-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
}

.modern-pagination .page-item.disabled .page-link {
  background: var(--auro-bg);
  border-color: var(--auro-border);
  color: var(--auro-text-light);
  opacity: 0.5;
}

/* Responsive */
@media (max-width: 991.98px) {
  .modern-filter-card {
    position: static;
    margin-bottom: 2rem;
  }
  
  .modern-select {
    min-width: 150px;
  }
}
</style>
