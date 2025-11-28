<template>
  <section class="best-sellers-section section-full">
    <!-- Header -->
    <div class="section-header">
      <h2 class="section-title">Sản phẩm bán chạy</h2>
      <router-link to="/san-pham?sort=sales" class="btn-view-all"> Xem tất cả </router-link>
    </div>

    <!-- Full-width products carousel -->
    <div class="products-carousel-container">
      <button
        class="section-nav-btn prev"
        @click="scrollProducts('prev')"
        :disabled="products.length <= 5"
      >
        ‹
      </button>

      <div
        class="section-list products-grid"
        :class="{ centered: products.length <= 5 }"
        ref="productsGrid"
      >
        <!-- Skeleton Loading -->
        <div v-if="loading" v-for="n in 5" :key="n" class="product-card skeleton">
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-price"></div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-state">
          <p>{{ error }}</p>
          <button @click="fetchBestSellers" class="btn-retry">Thử lại</button>
        </div>

        <!-- Product Cards -->
        <ProductCard
          v-else
          v-for="(product, index) in displayProducts"
          :key="`${product.id}-${index}`"
          :id="product.id"
          :name="product.ten"
          :img="getProductImage(product)"
          :hover-img="getProductHoverImage(product)"
          :price-now="product.gia"
          :price-old="calculateOriginalPrice(product)"
          :discount="calculateDiscount(product)"
          :promotional-badge="getPromotionalBadge(product)"
          :color-options="getColorOptions(product)"
          :sizes="getSizes(product)"
          :available-sizes="getAvailableSizes(product)"
          :color-size-mapping="getColorSizeMapping(product)"
          :stock="product.tonKho || 0"
          :rating="getRating(product)"
          :review-count="getReviewCount(product)"
        />
      </div>

      <button
        class="section-nav-btn next"
        @click="scrollProducts('next')"
        :disabled="products.length <= 5"
      >
        ›
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useProductStore } from '../../stores/product'
import ProductCard from '../product/ProductCard.vue'

const productStore = useProductStore()

const products = ref([])
const loading = ref(true)
const error = ref(null)
const productsGrid = ref(null)
const currentIndex = ref(0)

// Fetch best sellers from API (using public API for homepage)
const fetchBestSellers = async () => {
  try {
    loading.value = true
    error.value = null

    // Use public best-sellers API (doesn't require authentication)
    await productStore.fetchBestSellers()
    products.value = productStore.bestSellers || []

    console.log('✅ Best sellers loaded:', products.value.length)
    console.log('📦 Best sellers data:', products.value)
  } catch (err) {
    console.error('Error fetching best sellers:', err)
    error.value = 'Đã có lỗi xảy ra khi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

// Navigation functions
const scrollProducts = (direction) => {
  if (products.value.length <= 5) return

  if (direction === 'next') {
    currentIndex.value = (currentIndex.value + 1) % products.value.length
  } else {
    currentIndex.value = (currentIndex.value - 1 + products.value.length) % products.value.length
  }
}

// Display 5 products starting from currentIndex
const displayProducts = computed(() => {
  if (products.value.length === 0) return []

  const displayCount = Math.min(5, products.value.length)
  const result = []

  for (let i = 0; i < displayCount; i++) {
    const index = (currentIndex.value + i) % products.value.length
    result.push(products.value[index])
  }

  return result
})

// Helper functions to map backend data to component props
const getProductImage = (product) => {
  // Backend trả về anhDaiDien từ bảng hinh_anh
  return product.anhDaiDien || 'https://via.placeholder.com/400x600?text=No+Image'
}

const getProductHoverImage = (product) => {
  // Có thể thêm logic để lấy ảnh hover từ biến thể hoặc hình ảnh thứ 2
  return null // Tạm thời không có hover image
}

const calculateOriginalPrice = (product) => {
  // Tính giá gốc nếu có giảm giá
  // Backend có thể cần thêm field giaGoc hoặc tính toán dựa trên promotion
  return null // Tạm thời không có giá gốc
}

const calculateDiscount = (product) => {
  // Tính % giảm giá
  // Backend có thể cần thêm logic này
  return null // Tạm thời không có discount
}

const getPromotionalBadge = (product) => {
  // Lấy promotional badge từ backend (nếu có)
  return null // Tạm thời không có badge
}

const getColorOptions = (product) => {
  // Lấy màu sắc từ biến thể
  if (!product.bienThes || product.bienThes.length === 0) return []

  const colors = new Set()
  product.bienThes.forEach((bt) => {
    if (bt.colorHex) {
      colors.add(bt.colorHex)
    }
  })
  return Array.from(colors)
}

const getSizes = (product) => {
  // Lấy tất cả sizes từ biến thể
  if (!product.bienThes || product.bienThes.length === 0) {
    return ['S', 'M', 'L', 'XL', '2XL', '3XL']
  }

  const sizes = new Set()
  product.bienThes.forEach((bt) => {
    if (bt.kichThuoc) {
      sizes.add(bt.kichThuoc)
    }
  })
  return Array.from(sizes)
}

const getAvailableSizes = (product) => {
  // Lấy sizes có sẵn (còn tồn kho > 0)
  if (!product.bienThes || product.bienThes.length === 0) {
    return ['S', 'M', 'L', 'XL', '2XL', '3XL']
  }

  const sizes = new Set()
  product.bienThes.forEach((bt) => {
    if (bt.kichThuoc && bt.tonKho && bt.tonKho > 0) {
      sizes.add(bt.kichThuoc)
    }
  })
  return Array.from(sizes)
}

const getColorSizeMapping = (product) => {
  // Mapping màu với size
  if (!product.bienThes || product.bienThes.length === 0) return {}

  const mapping = {}
  product.bienThes.forEach((bt) => {
    if (bt.colorHex && bt.kichThuoc) {
      if (!mapping[bt.colorHex]) {
        mapping[bt.colorHex] = []
      }
      if (!mapping[bt.colorHex].includes(bt.kichThuoc)) {
        mapping[bt.colorHex].push(bt.kichThuoc)
      }
    }
  })
  return mapping
}

const getRating = (product) => {
  // Lấy rating từ product (backend trả về danhGia)
  return product.danhGia !== null && product.danhGia !== undefined ? product.danhGia : null
}

const getReviewCount = (product) => {
  // Lấy số lượng đánh giá từ product (backend trả về soLuongDanhGia)
  return product.soLuongDanhGia !== null && product.soLuongDanhGia !== undefined
    ? product.soLuongDanhGia
    : null
}

onMounted(() => {
  fetchBestSellers()
})
</script>

<style scoped>
/* Giữ nguyên CSS từ file gốc */
.best-sellers-section {
  pointer-events: auto;
}

.best-sellers-section * {
  pointer-events: auto;
}

.section-header {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
  width: 100%;
  text-align: center;
}

.btn-view-all {
  position: absolute;
  bottom: -60px;
  right: -40px;
  background: #000;
  color: #fff;
  padding: 5px 10px;
  border-radius: 20px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  z-index: 10;
}

.btn-view-all:hover {
  background: #333;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.products-carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  width: 100%;
}

.section-list {
  display: flex;
  gap: 20px;
  overflow: hidden;
  scroll-behavior: smooth;
  width: 100%;
  padding: 0 20px;
  justify-content: flex-start;
  align-items: center;
  min-height: 500px;
  pointer-events: auto;
}

.section-list * {
  pointer-events: auto;
}

.section-nav-btn {
  position: absolute;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 2rem;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.section-nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.9);
  transform: scale(1.1);
}

.section-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.section-nav-btn.prev {
  left: -25px;
}

.section-nav-btn.next {
  right: -25px;
}

.section-list :deep(.product-card) {
  flex-shrink: 0 !important;
  width: calc((100vw - 40px - 80px) / 5) !important;
  max-width: calc((100vw - 40px - 80px) / 5) !important;
  min-width: calc((100vw - 40px - 80px) / 5) !important;
  flex: 0 0 calc((100vw - 40px - 80px) / 5) !important;
}

/* Đảm bảo tất cả cards có cùng kích thước */
.section-list :deep(.product-card.section-item) {
  width: calc((100vw - 40px - 80px) / 5) !important;
  max-width: calc((100vw - 40px - 80px) / 5) !important;
  min-width: calc((100vw - 40px - 80px) / 5) !important;
  flex: 0 0 calc((100vw - 40px - 80px) / 5) !important;
}

.section-list.centered {
  justify-content: center;
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
  width: 100%;
}

.error-state p {
  color: #dc3545;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.btn-retry {
  background: #007bff;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-retry:hover {
  background: #0056b3;
  transform: translateY(-2px);
}

/* Skeleton Styles */
.section-list :deep(.skeleton) {
  background: #f8f9fa;
  border-radius: 24px;
  overflow: hidden;
  width: calc((100vw - 40px - 80px) / 5) !important;
  height: 650px;
  flex-shrink: 0;
}

.skeleton-image {
  width: 100%;
  height: 580px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  border-top: 1px solid #f0f0f0;
}

.skeleton-title {
  height: 1.5rem;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton-price {
  height: 1.25rem;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Responsive */
@media (max-width: 1400px) {
  .section-list :deep(.product-card),
  .section-list :deep(.skeleton) {
    width: calc((100vw - 40px - 60px) / 4) !important;
    max-width: calc((100vw - 40px - 60px) / 4) !important;
    min-width: calc((100vw - 40px - 60px) / 4) !important;
  }
}

@media (max-width: 1024px) {
  .section-list :deep(.product-card),
  .section-list :deep(.skeleton) {
    width: calc((100vw - 40px - 40px) / 3) !important;
    max-width: calc((100vw - 40px - 40px) / 3) !important;
    min-width: calc((100vw - 40px - 40px) / 3) !important;
  }
}

@media (max-width: 768px) {
  .section-list :deep(.product-card),
  .section-list :deep(.skeleton) {
    width: calc((100vw - 40px - 20px) / 2) !important;
    max-width: calc((100vw - 40px - 20px) / 2) !important;
    min-width: calc((100vw - 40px - 20px) / 2) !important;
  }
}

@media (max-width: 576px) {
  .section-header {
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 2rem;
  }

  .btn-view-all {
    position: static;
    margin-top: 1rem;
  }
}
</style>
