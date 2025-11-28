<template>
  <section id="new-arrivals" class="new-arrivals-section">
    <!-- Header with container -->
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Sản phẩm mới</h2>
        <router-link to="/san-pham?sort=createdAt" style="margin-right: 30px;" class="btn-view-all"> Xem tất cả </router-link>
      </div>
    </div>

    <!-- Full-width products carousel -->
    <div class="products-carousel-container">
      <button class="prev-btn" @click="scrollProducts('prev')">‹</button>

      <div class="products-grid" ref="productsGrid" @scroll="handleScroll">
        <template v-if="loading">
          <div v-for="n in 5" :key="n" class="product-card skeleton">
            <div class="skeleton-image"></div>
            <div class="skeleton-content">
              <div class="skeleton-title"></div>
              <div class="skeleton-price"></div>
            </div>
          </div>
        </template>

        <ProductCard
          v-else
          v-for="(product, index) in displayProducts"
          :key="`${product.id}-${index}`"
          :id="product.id"
          :name="product.name"
          :img="product.image"
          :hover-img="product.hoverImage"
          :price-now="product.price"
          :price-old="product.originalPrice"
          :discount="product.discount"
          :promotional-badge="product.promotionalBadge"
          :color-options="product.colorOptions"
          :sizes="product.sizes"
          :available-sizes="product.availableSizes"
          :color-size-mapping="product.colorSizeMapping"
          :rating="product.rating"
          :review-count="product.reviewCount"
        />
      </div>

      <button class="next-btn" @click="scrollProducts('next')">›</button>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import productService from '../../services/productService'
import ProductCard from '../product/ProductCard.vue'

const products = ref([])
const loading = ref(true)
const error = ref(null)
const productsGrid = ref(null)
const currentIndex = ref(0)

const fetchNewArrivals = async () => {
  try {
    loading.value = true
    error.value = null

    console.log('🆕 Loading new arrivals from backend API - sản phẩm thật từ admin')

    // Gọi API để lấy sản phẩm mới từ backend
    const response = await productService.getNewArrivals({
      size: 10, // Lấy 10 sản phẩm mới nhất
      sortBy: 'taoLuc', // Sắp xếp theo thời gian tạo
      sortOrder: 'desc', // Mới nhất trước
      status: 'active', // Chỉ lấy sản phẩm đang hoạt động
      inStock: true, // Chỉ lấy sản phẩm còn hàng
    })

    if (response.success && response.data) {
      let apiProducts = response.data

      // Nếu response.data có structure như { content: [...] }
      if (apiProducts.content && Array.isArray(apiProducts.content)) {
        apiProducts = apiProducts.content
      }

      // Nếu không phải array, chuyển thành array
      if (!Array.isArray(apiProducts)) {
        apiProducts = [apiProducts]
      }

      // Convert API response to component format
      products.value = apiProducts.map((product) => {
        // Helper function to build full image URL
        const buildImageUrl = (imageUrl) => {
          if (!imageUrl) return 'https://via.placeholder.com/300x400?text=No+Image'
          if (imageUrl.startsWith('http')) return imageUrl
          if (imageUrl.startsWith('/files/')) {
            return `http://localhost:8080${imageUrl}`
          }
          // If it's just filename, add the base path
          return `http://localhost:8080/files/${imageUrl}`
        }

        return {
          id: product.id,
          name: product.ten || product.name || 'Sản phẩm mới',
          image: buildImageUrl(product.anhDaiDien || product.anhChinh || product.image),
          hoverImage: buildImageUrl(
            product.anhPhu?.[0] ||
              product.hoverImage ||
              product.anhDaiDien ||
              product.anhChinh ||
              product.image,
          ),
          price: product.gia || product.giaBan || product.price || 0,
          originalPrice: product.giaGoc || product.originalPrice,
          discount: product.phanTramGiam || product.discount || 0,
          promotionalBadge: 'SẢN PHẨM MỚI',
          colorOptions: ['#000000', '#8B4513', '#2F4F4F'], // Default colors
          sizes: ['S', 'M', 'L', 'XL'],
          availableSizes: ['S', 'M', 'L', 'XL'],
          colorSizeMapping: {
            '#000000': ['S', 'M', 'L', 'XL'],
            '#8B4513': ['M', 'L', 'XL'],
            '#2F4F4F': ['L', 'XL'],
          },
          rating: product.danhGia !== null && product.danhGia !== undefined ? product.danhGia : null,
          reviewCount: product.soLuongDanhGia !== null && product.soLuongDanhGia !== undefined ? product.soLuongDanhGia : null,
        }
      })

      console.log(`✅ Loaded ${products.value.length} new arrivals from API successfully`)
    } else {
      console.warn('❌ API response unsuccessful or empty:', response)
      products.value = []
    }
  } catch (err) {
    error.value = err.message || 'Lỗi khi tải sản phẩm mới'
    console.error('❌ Error loading new arrivals from API:', err)

    // Log chi tiết để debug
    if (err.response) {
      console.error('Response status:', err.response.status)
      console.error('Response data:', err.response.data)
    }

    products.value = []
  } finally {
    loading.value = false
  }
}

const scrollProducts = (direction) => {
  // For fixed layout, we need to cycle through products
  // This will be handled by changing the displayed products
  if (direction === 'next') {
    // Move to next set of products
    currentIndex.value = (currentIndex.value + 1) % products.value.length
  } else {
    // Move to previous set of products
    currentIndex.value = (currentIndex.value - 1 + products.value.length) % products.value.length
  }
}

// Display 5 products starting from currentIndex
const displayProducts = computed(() => {
  if (products.value.length === 0) return []

  const result = []
  for (let i = 0; i < 5; i++) {
    const index = (currentIndex.value + i) % products.value.length
    result.push(products.value[index])
  }
  return result
})

const handleScroll = () => {
  // Disable scroll handling for fixed layout
  // Cards are displayed in fixed positions with space-between
  return
}

onMounted(() => {
  fetchNewArrivals()
})

// Initialize infinite loop scroll position
const startAutoScroll = () => {
  if (productsGrid.value && products.value.length > 0) {
    // Set initial scroll position to middle section for infinite loop
    const container = productsGrid.value
    const cardWidth = container.offsetWidth / 5
    container.scrollLeft = products.value.length * cardWidth
  }
}

// Watch for products to be loaded
watch(products, () => {
  if (products.value.length > 0) {
    nextTick(() => {
      startAutoScroll()
    })
  }
})
</script>

<style scoped>
.new-arrivals-section {
  padding: 4rem 0;
  background: #fff;
  width: 100vw;
  margin-left: calc(-50vw + 50%);
  position: relative;
  /* Ensure ProductCard buttons are clickable */
  pointer-events: auto;
}

.new-arrivals-section * {
  pointer-events: auto;
}

.section-header {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 3rem;
  width: 100%;
  text-align: center;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #212529;
  margin: 0;
  line-height: 1.2;
}

.btn-view-all {
  position: absolute;
  bottom: -60px;
  right: -350px;
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
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.products-carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  width: 100%;
}

.products-grid {
  display: flex;
  overflow: hidden;
  justify-content: space-between;
  gap: 20px;
  scroll-behavior: smooth;
  padding: 40px 20px;
  width: 100%;
  /* Hide scrollbar */
  scrollbar-width: none;
  -ms-overflow-style: none;
  /* Ensure ProductCard buttons are clickable */
  pointer-events: auto;
}

.products-grid * {
  pointer-events: auto;
}

/* Ensure ProductCard buttons are specifically clickable */
.products-grid .product-card .btn-add-to-cart,
.products-grid .product-card .btn-detail {
  pointer-events: auto !important;
  cursor: pointer !important;
  z-index: 10 !important;
  position: relative !important;
}

/* ProductCard button styling */
.products-grid .product-card .btn-add-to-cart {
  background: #007bff !important;
  color: white !important;
}

.products-grid .product-card .btn-add-to-cart:hover {
  background: #0056b3 !important;
}

.products-grid::-webkit-scrollbar {
  display: none;
}

/* ProductCard sizing for 5 cards layout */
.products-grid .product-card {
  flex: 1;
  max-width: calc((100vw - 40px - 80px) / 5); /* 40px padding + 80px gaps (4 gaps x 20px) */
  min-width: calc((100vw - 40px - 80px) / 5);
}

/* Carousel Navigation Buttons */
.prev-btn,
.next-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: #fff;
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
}

.prev-btn:hover,
.next-btn:hover {
  background: #000;
  color: #fff;
}

.prev-btn {
  left: 16px;
}

.next-btn {
  right: 16px;
}

/* Responsive breakpoints */
@media (max-width: 1400px) {
  .products-grid {
    padding: 40px 20px;
  }
  .products-grid .product-card {
    flex: 1;
    max-width: calc((100vw - 40px - 60px) / 4);
    min-width: calc((100vw - 40px - 60px) / 4);
  }
}

@media (max-width: 1024px) {
  .products-grid {
    padding: 40px 20px;
  }
  .products-grid .product-card {
    flex: 1;
    max-width: calc((100vw - 40px - 40px) / 3);
    min-width: calc((100vw - 40px - 40px) / 3);
  }
}

@media (max-width: 768px) {
  .products-grid {
    padding: 40px 20px;
    overflow-x: auto;
  }
  .products-grid .product-card {
    flex: 1;
    max-width: calc((100vw - 40px - 20px) / 2);
    min-width: calc((100vw - 40px - 20px) / 2);
  }

  .prev-btn,
  .next-btn {
    display: none;
  }
}

/* Skeleton loading styles */
.skeleton {
  background: #f8f9fa;
  border-radius: 24px;
  overflow: hidden;
  width: 500px;
  height: 650px;
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
  margin-bottom: 0;
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

/* Responsive carousel layout */
@media (max-width: 992px) {
  .section-title {
    font-size: 2rem;
  }

  .section-header {
    margin-bottom: 2rem;
  }
}

@media (max-width: 768px) {
  .products-grid {
    gap: 16px;
  }

  .products-carousel-container {
    padding: 0 20px;
  }
}

@media (max-width: 576px) {
  .products-grid {
    gap: 14px;
  }

  .products-carousel-container {
    padding: 0 15px;
  }

  .section-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    margin-bottom: 2rem;
  }

  .btn-view-all {
    position: static;
    margin-top: 1rem;
    align-self: center;
  }

  .section-title {
    font-size: 1.75rem;
  }

  .new-arrivals-section {
    padding: 2rem 0;
  }
}
</style>
