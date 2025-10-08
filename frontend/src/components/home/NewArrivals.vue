<template>
  <section class="new-arrivals-section">
    <!-- Header with container -->
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Sản phẩm mới</h2>
        <router-link to="/san-pham?sort=createdAt" class="btn-view-all">
          Xem tất cả
        </router-link>
      </div>
    </div>
    
    <!-- Full-width products carousel -->
    <div class="products-carousel-container">
      <button class="prev-btn" @click="scrollProducts('prev')">
        ‹
      </button>
      
        <div class="products-grid" ref="productsGrid" @scroll="handleScroll">
          <div v-if="loading" v-for="n in 5" :key="n" class="product-card skeleton">
            <div class="skeleton-image"></div>
            <div class="skeleton-content">
              <div class="skeleton-title"></div>
              <div class="skeleton-price"></div>
            </div>
          </div>
          
          <ProductCard
            v-else
            v-for="(product, index) in displayProducts"
            :key="`${product.id}-${index}`"
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
          />
        </div>
      
      <button class="next-btn" @click="scrollProducts('next')">
        ›
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import ProductCard from '../product/ProductCard.vue'

const products = ref([])
const loading = ref(true)
const productsGrid = ref(null)
const currentIndex = ref(0)
const showButtons = ref(false)

const fetchNewArrivals = async () => {
  try {
    loading.value = true
    // Simulate API call - replace with actual API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Mock data - replace with actual API call
    products.value = [
      {
        id: 1,
        name: 'Áo thun nữ mới nhất',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 199000,
        originalPrice: 299000,
        discount: 33,
        promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
        colorOptions: ['#ff69b4', '#007bff', '#28a745'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL'],
        colorSizeMapping: {
          '#ff69b4': ['S', 'M', 'L'],
          '#007bff': ['M', 'L', 'XL'],
          '#28a745': ['L', 'XL', '2XL']
        }
      },
      {
        id: 2,
        name: 'Quần jean nữ skinny',
        image: 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        price: 499000,
        originalPrice: null,
        discount: null,
        promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
        colorOptions: ['#000080', '#000000', '#808080'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
        colorSizeMapping: {
          '#000080': ['S', 'M', 'L', 'XL'],
          '#000000': ['M', 'L', 'XL', '2XL'],
          '#808080': ['L', 'XL', '2XL']
        }
      },
      {
        id: 3,
        name: 'Áo sơ mi nữ công sở',
        image: 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        price: 399000,
        originalPrice: 499000,
        discount: 20,
        promotionalBadge: 'MUA 2 GIẢM THÊM 10%',
        colorOptions: ['#000000', '#ffffff', '#ff69b4'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        colorSizeMapping: {
          '#000000': ['S', 'M', 'L', 'XL'],
          '#ffffff': ['M', 'L', 'XL', '2XL'],
          '#ff69b4': ['L', 'XL', '2XL', '3XL']
        }
      },
      {
        id: 4,
        name: 'Váy nữ dạo phố',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 299000,
        originalPrice: null,
        discount: null,
        promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
        colorOptions: ['#ff69b4', '#000000', '#ffffff'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL'],
        colorSizeMapping: {
          '#ff69b4': ['S', 'M', 'L'],
          '#000000': ['M', 'L', 'XL'],
          '#ffffff': ['L', 'XL', '2XL']
        }
      },
      {
        id: 5,
        name: 'Áo khoác nữ dài tay',
        image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=500&h=600&fit=crop',
        price: 699000,
        originalPrice: 899000,
        discount: 22,
        promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
        colorOptions: ['#000000', '#808080', '#8b4513'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['L', 'XL', '2XL', '3XL'],
        colorSizeMapping: {
          '#000000': ['L', 'XL', '2XL'],
          '#808080': ['XL', '2XL', '3XL'],
          '#8b4513': ['2XL', '3XL']
        }
      },
      {
        id: 6,
        name: 'Áo sơ mi nữ mới',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 349000,
        originalPrice: 449000,
        discount: 22,
        promotionalBadge: 'MUA 2 GIẢM THÊM 20%',
        colorOptions: ['#ffffff', '#ff69b4', '#000000'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
        colorSizeMapping: {
          '#ffffff': ['S', 'M', 'L'],
          '#ff69b4': ['M', 'L', 'XL'],
          '#000000': ['L', 'XL', '2XL']
        }
      },
      {
        id: 7,
        name: 'Quần short nữ mới',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 229000,
        originalPrice: 329000,
        discount: 30,
        promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
        colorOptions: ['#ff69b4', '#007bff', '#28a745'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL'],
        colorSizeMapping: {
          '#ff69b4': ['S', 'M', 'L'],
          '#007bff': ['M', 'L', 'XL'],
          '#28a745': ['L', 'XL', '2XL']
        }
      },
      {
        id: 8,
        name: 'Áo tank top nữ mới',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 179000,
        originalPrice: 279000,
        discount: 36,
        promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
        colorOptions: ['#ff69b4', '#000000', '#ffffff'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
        colorSizeMapping: {
          '#ff69b4': ['S', 'M', 'L'],
          '#000000': ['M', 'L', 'XL'],
          '#ffffff': ['L', 'XL', '2XL']
        }
      }
    ]
  } catch (error) {
    console.error('Error fetching new arrivals:', error)
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
.prev-btn, .next-btn {
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

.prev-btn:hover, .next-btn:hover {
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
  
  .prev-btn, .next-btn {
    display: none;
  }
}

/* Skeleton loading styles */
.skeleton {
  background: #f8f9fa;
  border-radius: 24px;
  overflow: hidden;
  width: 500px;
  height: 650px; /* Giảm từ 700px để phù hợp với layout mới */
}

.skeleton-image {
  width: 100%;
  height: 580px; /* Tăng từ 550px để phù hợp với ảnh lớn hơn */
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 1rem; /* Giảm padding để phù hợp với product-info-section */
  display: flex;
  flex-direction: column;
  gap: 0.5rem; /* Giảm gap như product-info-section */
  border-top: 1px solid #f0f0f0; /* Đường phân cách */
}

.skeleton-title {
  height: 1.5rem; /* Giảm từ 2rem */
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0; /* Bỏ margin vì đã có gap */
}

.skeleton-price {
  height: 1.25rem; /* Giảm từ 1.5rem */
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
