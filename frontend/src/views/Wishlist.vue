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
            <ProductCard
              :id="item.id"
              :name="item.name"
              :img="item.image"
              :hover-img="item.hoverImage || item.image"
              :price-now="item.price"
              :price-old="item.originalPrice"
              :discount="item.discount"
              :promotional-badge="item.promotionalBadge || ''"
              :color-options="item.colorOptions || ['#000000', '#ffffff', '#dc3545']"
              :sizes="item.sizes || ['S', 'M', 'L', 'XL']"
              :available-sizes="item.availableSizes || ['S', 'M', 'L']"
              :color-size-mapping="item.colorSizeMapping || {
                '#000000': ['S', 'M', 'L'],
                '#ffffff': ['M', 'L', 'XL'],
                '#dc3545': ['S', 'L', 'XL']
              }"
            />
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
import ProductCard from '../components/product/ProductCard.vue'

const productStore = useProductStore()
const cartStore = useCartStore()

// Local state
const isLoading = ref(false)

// Computed
const items = computed(() => productStore.wishlistItems)
const itemCount = computed(() => productStore.wishlistCount)
const isEmpty = computed(() => productStore.isWishlistEmpty)


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

