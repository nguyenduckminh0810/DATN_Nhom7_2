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
<p class="text-muted fs-5">{{ totalElements }} sản phẩm</p>

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

          <!-- Loading State -->
          <div v-if="isLoading" :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
            <div 
              v-for="n in 12" 
              :key="`skeleton-${n}`" 
              :class="viewMode === 'list' ? 'col-12' : 'col-md-6 col-lg-4'"
            >
              <SkeletonLoader variant="product" />
            </div>
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="col-12">
            <div class="alert alert-danger text-center">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
              <button 
                @click="route.params.slug ? fetchProductsByCategory(route.params.slug) : fetchAllProducts()" 
                class="btn btn-sm btn-outline-danger ms-3"
              >
                <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
              </button>
            </div>
          </div>
          
          <!-- Products -->
          <div v-else-if="filteredProducts.length > 0" :class="['row', viewMode === 'list' ? 'g-3' : 'g-4']">
            <div v-for="product in filteredProducts" :key="product.id" 
                 :class="viewMode === 'list' ? 'col-12' : 'col-md-6 col-lg-4'">
              <ProductCard
                :id="product.id"
                :name="product.name || product.ten"
                :img="product.anhDaiDien"
                :priceNow="product.price || product.gia"
                :priceOld="product.originalPrice || product.giaGoc"
                :discount="product.discount || product.giamGia"
                :promotionalBadge="product.promotionalBadge"
                :colorOptions="product.colorOptions || product.mauSac"
                :sizes="product.sizes || product.kichCo"
                :availableSizes="product.availableSizes || []"
                :stock="product.stock || product.tonKho"
                :bienThes="product.bienThes || []"
              />
            </div>
          </div>

          <!-- No Products -->
          <div v-else class="text-center py-5">
            <div class="empty-state">
              <div class="empty-state-icon">
                <i class="bi bi-search"></i>
              </div>
              <h3 class="empty-state-title">Không tìm thấy sản phẩm</h3>
              <p class="empty-state-description">
                Chúng tôi không tìm thấy sản phẩm nào phù hợp với tiêu chí của bạn.<br>
                Hãy thử điều chỉnh bộ lọc hoặc xem các sản phẩm khác.
              </p>
              <div class="empty-state-actions">
                <button @click="clearFilters" class="btn btn-primary btn-lg">
                  <i class="bi bi-funnel me-2"></i>Xóa bộ lọc
                </button>
                <router-link to="/" class="btn btn-outline-secondary btn-lg ms-3">
                  <i class="bi bi-house me-2"></i>Về trang chủ
                </router-link>
              </div>
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
import { useRoute } from 'vue-router'
import axios from 'axios'

import ProductFilters from '../components/product/ProductFilters.vue'
import ProductCard from '../components/product/ProductCard.vue'
import SkeletonLoader from '../components/common/SkeletonLoader.vue'
import { useSearchStore } from '../stores/search'
import { useCartStore } from '../stores/cart'
import sanPhamService from '@/services/sanPhamService'
const route = useRoute()
const searchStore = useSearchStore()
const cartStore = useCartStore()

// ======= STATE =======
const products = ref([])                // data trang hiện tại
const categoryName = ref('')
const isLoading = ref(true)
const error = ref(null)
const viewMode = ref('grid')

// server-side pagination
const currentPage = ref(1)              // UI hiển thị 1-based
const itemsPerPage = 12
const totalPages = ref(0)
const totalElements = ref(0)


// ======= HELPERS =======
const toTitle = (slug) =>
  (slug || '').split('-').map(w => w.toUpperCase()).join(' ')

const buildParams = () => {
  const params = {
    page: Math.max(currentPage.value - 1, 0),  // BE 0-based
    size: itemsPerPage
  }
  // nếu bạn có ô search/filters riêng, có thể truyền thêm ở đây:
  if (searchStore?.keyword && searchStore.keyword.trim()) {
    params.search = searchStore.keyword.trim()
  }
  return params
}

// ======= API CALLS =======
const fetchProductsByCategory = async (slug) => {
  isLoading.value = true; error.value = null
  try {
    // lấy response thô (có thể là axios response hoặc service wrapper)
    const resp = await sanPhamService.getByCategorySlug(slug, { params: buildParams() })
    // lấy payload thực sự (resp.data nếu axios, hoặc resp nếu service trả thẳng)
    const payload = resp?.data ?? resp
    // debug nhanh nếu payload không như mong đợi
    if (!payload) {
      console.error('fetchProductsByCategory: empty payload', resp)
      throw new Error('Server trả về dữ liệu không hợp lệ')
    }

    products.value = payload.content ?? []
    totalPages.value = payload.totalPages ?? 0
    totalElements.value = payload.totalElements ?? products.value.length
    currentPage.value = (payload.number ?? 0) + 1
    categoryName.value = toTitle(slug)
  } catch (e) {
    console.error('fetchProductsByCategory error', e)
    error.value = e?.response?.data?.message || e?.message || 'Không thể tải sản phẩm theo danh mục'
    products.value = []; totalPages.value = 0; totalElements.value = 0
  } finally {
    isLoading.value = false
  }
}

const fetchAllProducts = async () => {
  isLoading.value = true; error.value = null
  try {
    categoryName.value = 'TẤT CẢ SẢN PHẨM'
    const resp = await sanPhamService.page(buildParams())
    const payload = resp?.data ?? resp
    if (!payload) {
      console.error('fetchAllProducts: empty payload', resp)
      throw new Error('Server trả về dữ liệu không hợp lệ')
    }

    products.value = payload.content ?? []
    totalPages.value = payload.totalPages ?? 0
    totalElements.value = payload.totalElements ?? products.value.length
    currentPage.value = (payload.number ?? 0) + 1
  } catch (e) {
    console.error('fetchAllProducts error', e)
    error.value = e?.response?.data?.message || e?.message || 'Có lỗi xảy ra từ server'
    products.value = []; totalPages.value = 0; totalElements.value = 0
  } finally {
    isLoading.value = false
  }
}

// ======= COMPUTED =======
// Nếu muốn áp bộ lọc FE trên trang hiện tại, giữ lại; nếu không thì return products.value
const filteredProducts = computed(() => {
  try {
    // Get base products list
    let productList = products.value
    
    // Áp bộ lọc trong trang hiện tại (nếu cần)
    if (searchStore?.applyFilters) {
      productList = searchStore.applyFilters(productList)
    }
    
    // Map each product to include availableSizes from bienThes
    return productList.map(product => {
      // Extract available sizes from bienThes (variants with stock > 0)
      const availableSizes = product.bienThes
        ? [...new Set(product.bienThes
            .filter(bt => bt.tonKho > 0)
            .map(bt => bt.kichThuoc))]
        : []
      
      return {
        ...product,
        availableSizes
      }
    })
  } catch {
    return products.value
  }
})

// Hiển thị số trang trong pagination
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// ======= UI ACTIONS =======
const clearFilters = () => {
  searchStore.clearAllFilters?.()
  // làm mới lại trang hiện tại sau khi clear filter
  reload()
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  reload()
}

const reload = () => {
  const slug = route.params.slug
  if (slug) {
    fetchProductsByCategory(slug)
  } else {
    fetchAllProducts()
  }
}

const addToCart = (product) => {
  cartStore.addItem(product)
}

// ======= WATCH & LIFECYCLE =======
watch(() => route.params.slug, (newSlug) => {
  currentPage.value = 1
  if (newSlug) fetchProductsByCategory(newSlug)
  else fetchAllProducts()
})

onMounted(() => {
  const slug = route.params.slug
  if (slug) fetchProductsByCategory(slug)
  else fetchAllProducts()
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
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  max-width: 500px;
  margin: 0 auto;
}

.empty-state-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.empty-state-icon i {
  font-size: 2.5rem;
  color: #6c757d;
}

.empty-state-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #212529;
  margin-bottom: 1rem;
}

.empty-state-description {
  font-size: 1rem;
  color: #6c757d;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.empty-state-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.empty-state-actions .btn {
  min-width: 160px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.empty-state-actions .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
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

/* Desktop Optimization */
.category {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.container {
  max-width: 1400px;
}

.category-header {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 3rem 2rem;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.category-header h1 {
  background: linear-gradient(135deg, #212529 0%, #495057 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 3rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.category-header p {
  font-size: 1.125rem;
  color: #6c757d;
  font-weight: 500;
}

.section-divider {
  width: 80px;
  height: 4px;
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  border-radius: 2px;
  margin: 1.5rem auto 0;
}

/* Desktop Grid Layout */
.col-lg-3 {
  min-width: 280px;
}

.col-lg-9 {
  min-width: 900px;
}

/* Enhanced Filter Cards */
.filter-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.filter-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.filter-card-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 20px 20px 0 0;
  font-weight: 700;
  font-size: 1.125rem;
}

/* Enhanced Product Grid - Compact */
.product-grid {
  gap: 1.5rem; /* Giảm từ 2rem */
}

.row {
  --bs-gutter-x: 1.5rem; /* Giảm gutter */
}

.product-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  overflow: hidden;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* Enhanced Sort & View Controls */
.sort-controls {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.modern-select {
  border-radius: 12px;
  border: 2px solid #e9ecef;
  padding: 0.75rem 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.modern-select:focus {
  border-color: #B8860B;
  box-shadow: 0 0 0 0.2rem rgba(184, 134, 11, 0.25);
}

.modern-view-btn {
  border-radius: 12px;
  padding: 0.75rem 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid #e9ecef;
}

.modern-view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Enhanced Pagination */
.modern-pagination {
  margin-top: 3rem;
}

.modern-pagination .page-link {
  border-radius: 12px;
  margin: 0 0.25rem;
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  color: #495057;
  font-weight: 600;
  transition: all 0.3s ease;
}

.modern-pagination .page-link:hover {
  background: #B8860B;
  border-color: #B8860B;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(184, 134, 11, 0.3);
}

.modern-pagination .page-item.active .page-link {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  border-color: #B8860B;
  color: white;
}

/* Enhanced Breadcrumb */
.modern-breadcrumb {
  background: white;
  padding: 1rem 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.breadcrumb-link {
  color: #6c757d;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.breadcrumb-link:hover {
  color: #B8860B;
}
</style>
