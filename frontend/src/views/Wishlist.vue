<template>
  <div class="wishlist">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="py-3 bg-light">
      <div class="container">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <router-link to="/" class="text-decoration-none">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active">Danh sách yêu thích</li>
        </ol>
      </div>
    </nav>

    <div class="container py-5">
      <!-- Wishlist Header -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="wishlist-header">
            <h2 class="mb-3">
              <i class="ph-heart-fill text-danger me-2"></i>
              Danh sách yêu thích
            </h2>
            <div class="d-flex justify-content-between align-items-center">
              <p class="text-muted mb-0">
                {{ itemCount }} sản phẩm trong danh sách yêu thích
              </p>
              <div v-if="!isEmpty" class="d-flex gap-2">
                <button 
                  class="btn btn-outline-primary btn-sm"
                  @click="moveAllToCart"
                >
                  <i class="ph-shopping-cart me-1"></i>Chuyển tất cả vào giỏ hàng
                </button>
                <button 
                  class="btn btn-outline-secondary btn-sm"
                  @click="clearWishlist"
                >
                  <i class="ph-trash me-1"></i>Xóa tất cả
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status">
          <span class="visually-hidden">Đang tải...</span>
        </div>
        <h5>Đang tải danh sách yêu thích...</h5>
      </div>

      <!-- Empty Wishlist -->
      <div v-else-if="isEmpty" class="empty-wishlist text-center py-5">
        <div class="empty-icon mb-4">
          <i class="ph-heart text-muted" style="font-size: 4rem;"></i>
        </div>
        <h4 class="mb-3">Danh sách yêu thích trống</h4>
        <p class="text-muted mb-4">
          Bạn chưa có sản phẩm nào trong danh sách yêu thích.<br>
          Hãy khám phá và thêm những sản phẩm bạn yêu thích!
        </p>
        <div class="d-flex justify-content-center gap-3">
          <router-link to="/category" class="btn btn-auro-primary">
            <i class="ph-grid-four me-2"></i>Khám phá sản phẩm
          </router-link>
          <router-link to="/" class="btn btn-outline-primary">
            <i class="ph-house me-2"></i>Về trang chủ
          </router-link>
        </div>
      </div>

      <!-- Wishlist Items -->
      <div v-else class="wishlist-items">
        <div class="row g-4">
          <div class="col-md-6 col-lg-4 col-xl-3" v-for="item in items" :key="item.id">
            <div class="card product-card h-100 animate-on-scroll">
              <!-- Product Image -->
              <div class="position-relative product-image-container">
                <LazyImage
                  :src="item.image || 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'"
                  :alt="item.name"
                  :width="500"
                  :height="500"
                  :quality="85"
                  :webp="true"
                  :lazy="true"
                  container-class="product-image"
                  image-class="card-img-top"
                  show-zoom-overlay="true"
                />
                
                <!-- Discount Badge -->
                <div class="position-absolute top-0 end-0 m-3">
                  <span class="badge modern-discount-badge">-{{ item.discount || 20 }}%</span>
                </div>

                <!-- Action Buttons -->
                <div class="position-absolute top-0 start-0 m-3">
                  <WishlistButton 
                    :product="item" 
                    variant="icon" 
                    size="sm"
                    @toggle="handleWishlistToggle"
                  />
                </div>

                <!-- Product Overlay -->
                <div class="product-overlay">
                  <div class="d-flex gap-2">
                    <button 
                      class="btn btn-auro-primary btn-sm" 
                      @click="addToCart(item)"
                    >
                      <i class="ph-shopping-cart me-1"></i>Thêm vào giỏ
                    </button>
                    <router-link 
                      :to="`/product/${item.id}`" 
                      class="btn btn-outline-light btn-sm"
                    >
                      <i class="ph-eye"></i>
                    </router-link>
                  </div>
                </div>
              </div>

              <!-- Product Info -->
              <div class="card-body d-flex flex-column">
                <h6 class="card-title fw-bold mb-2">{{ item.name }}</h6>
                <p class="card-text text-muted small flex-grow-1 mb-3">{{ item.description }}</p>
                
                <!-- Price -->
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div>
                    <span class="price h5 mb-0">{{ formatPrice(item.price) }}</span>
                    <small class="text-muted text-decoration-line-through ms-2">
                      {{ formatPrice(item.originalPrice || item.price * 1.2) }}
                    </small>
                  </div>
                  <div class="product-rating">
                    <i class="ph-star-fill text-warning"></i>
                    <i class="ph-star-fill text-warning"></i>
                    <i class="ph-star-fill text-warning"></i>
                    <i class="ph-star-fill text-warning"></i>
                    <i class="ph-star text-warning"></i>
                    <small class="ms-1 text-muted">(4.5)</small>
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="d-flex gap-2">
                  <button 
                    class="btn btn-auro-primary flex-grow-1"
                    @click="moveToCart(item.id)"
                  >
                    <i class="ph-shopping-cart me-1"></i>Chuyển vào giỏ hàng
                  </button>
                  <router-link 
                    :to="`/product/${item.id}`" 
                    class="btn btn-outline-primary"
                  >
                    <i class="ph-eye"></i>
                  </router-link>
                </div>

                <!-- Added Date -->
                <div class="mt-3 pt-3 border-top">
                  <small class="text-muted">
                    <i class="ph-clock me-1"></i>
                    Thêm vào {{ formatDate(item.addedAt) }}
                  </small>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Related Products -->
        <div class="mt-5">
          <div class="row">
            <div class="col-12">
              <h4 class="mb-4">
                <i class="ph-lightbulb me-2"></i>
                Sản phẩm gợi ý
              </h4>
              <div class="row g-4">
                <div class="col-md-6 col-lg-3" v-for="product in relatedProducts" :key="product.id">
                  <div class="card product-card h-100">
                    <div class="position-relative product-image-container">
                      <LazyImage
                        :src="product.image"
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
                        <span class="badge modern-discount-badge">-{{ product.discount }}%</span>
                      </div>
                      <div class="product-overlay">
                        <button class="btn btn-auro-primary btn-sm" @click="addToCart(product)">
                          <i class="ph-shopping-cart me-1"></i>Thêm vào giỏ
                        </button>
                      </div>
                    </div>
                    <div class="card-body d-flex flex-column">
                      <h6 class="card-title fw-bold mb-2">{{ product.name }}</h6>
                      <p class="card-text text-muted small flex-grow-1 mb-3">{{ product.description }}</p>
                      <div class="d-flex justify-content-between align-items-center">
                        <span class="price h6 mb-0">{{ formatPrice(product.price) }}</span>
                        <WishlistButton :product="product" variant="icon" size="sm" />
                      </div>
                      <router-link :to="`/product/${product.id}`" class="btn btn-outline-primary mt-3">
                        Xem chi tiết
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useProductStore } from '../stores/product'
import { useCartStore } from '../stores/cart'
import WishlistButton from '../components/ui/WishlistButton.vue'
import LazyImage from '../components/ui/LazyImage.vue'

const productStore = useProductStore()
const cartStore = useCartStore()

// Local state
const isLoading = ref(false)

// Computed
const items = computed(() => productStore.wishlistItems)
const itemCount = computed(() => productStore.wishlistCount)
const isEmpty = computed(() => productStore.isWishlistEmpty)

// Mock related products
const relatedProducts = ref([
  {
    id: 101,
    name: 'Áo sơ mi nam cao cấp',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    price: 450000,
    originalPrice: 600000,
    discount: 25,
    image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 102,
    name: 'Quần âu nam',
    description: 'Quần âu nam thiết kế hiện đại',
    price: 650000,
    originalPrice: 800000,
    discount: 19,
    image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 103,
    name: 'Áo khoác nam',
    description: 'Áo khoác nam phong cách casual',
    price: 850000,
    originalPrice: 1200000,
    discount: 29,
    image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 104,
    name: 'Áo thun nam',
    description: 'Áo thun nam chất liệu cotton mềm mại',
    price: 250000,
    originalPrice: 350000,
    discount: 29,
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  }
])

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const addToCart = (product) => {
  cartStore.addItem(product)
}

const moveToCart = (productId) => {
  productStore.moveWishlistToCart(productId)
}

const moveAllToCart = () => {
  productStore.moveAllWishlistToCart()
}

const clearWishlist = () => {
  if (confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi danh sách yêu thích?')) {
    productStore.clearWishlist()
  }
}

const handleWishlistToggle = (data) => {
  // Handle wishlist toggle event if needed
  console.log('Wishlist toggle:', data)
}

// Lifecycle
onMounted(() => {
  // Simulate loading
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 500)
})
</script>

<style scoped>
.wishlist-header {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
}

.empty-wishlist {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 60px 40px;
}

.empty-icon {
  opacity: 0.6;
}

.wishlist-items .product-card {
  transition: all 0.3s ease;
}

.wishlist-items .product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
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

/* Responsive */
@media (max-width: 768px) {
  .empty-wishlist {
    padding: 40px 20px;
  }
  
  .wishlist-header .d-flex {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start !important;
  }
}
</style>
