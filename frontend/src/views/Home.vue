<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="row align-items-center min-vh-100">
          <div class="col-lg-6">
            <div class="hero-content">
              <h1 class="display-2 fw-bold mb-4 text-white">
                Thời trang nam
                <span class="gradient-text">cao cấp</span>
              </h1>
              <p class="lead mb-5 text-white-50">
                Khám phá bộ sưu tập thời trang nam đẳng cấp với thiết kế tinh tế, 
                chất liệu cao cấp và phong cách hiện đại. AURO mang đến cho bạn 
                những trải nghiệm mua sắm tuyệt vời.
              </p>
              <div class="d-flex gap-4 flex-wrap">
                <router-link to="/category" class="btn btn-auro-primary btn-lg">
                  <i class="bi bi-arrow-right me-2"></i>Khám phá ngay
                </router-link>
                <button class="btn btn-outline-light btn-lg">
                  <i class="bi bi-play-circle me-2"></i>Xem video
                </button>
              </div>
            </div>
          </div>
          <div class="col-lg-6">
            <div class="hero-image auro-float">
              <div class="hero-image-container">
                <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80" 
                     alt="AURO Hero" class="img-fluid hero-main-image">
                <div class="hero-overlay"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="py-5">
      <div class="container">
        <div class="row">
          <div class="col-12 text-center mb-5">
            <h2 class="display-4 fw-bold mb-3">Sản phẩm nổi bật</h2>
            <p class="text-muted fs-5">Những mẫu thiết kế được yêu thích nhất</p>
            <div class="section-divider"></div>
          </div>
        </div>
        
        <!-- Loading skeleton for featured products -->
        <div class="row g-4" v-if="isLoading">
          <ProductSkeleton v-for="n in 4" :key="n" />
        </div>
        
        <!-- Featured products -->
        <div class="row g-4" v-else-if="featuredProducts.length > 0">
          <div class="col-md-6 col-lg-3" v-for="product in featuredProducts" :key="product.id">
            <div class="card product-card h-100 animate-on-scroll">
              <div class="position-relative product-image-container">
                <OptimizedProductImage
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
        
        <div class="text-center mt-5">
          <router-link to="/category" class="btn btn-auro-secondary btn-lg">
            <i class="bi bi-arrow-right me-2"></i>Xem tất cả sản phẩm
          </router-link>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="py-5 bg-light">
      <div class="container">
        <div class="row">
          <div class="col-12 text-center mb-5">
            <h2 class="display-4 fw-bold mb-3">Danh mục sản phẩm</h2>
            <p class="text-muted fs-5">Khám phá theo từng loại sản phẩm</p>
            <div class="section-divider"></div>
          </div>
        </div>
        
        <!-- Loading skeleton for categories -->
        <div class="row g-4" v-if="isLoading">
          <CategorySkeleton v-for="n in 3" :key="n" />
        </div>
        
        <!-- Categories -->
        <div class="row g-4" v-else>
          <div class="col-md-4" v-for="category in categories" :key="category.id">
            <div class="card category-card h-100 animate-on-scroll">
              <div class="card-body text-center p-5">
                <div class="category-icon-container mb-4">
                  <div class="category-icon">
                    <i :class="category.icon || 'ph-grid-four'"></i>
                  </div>
                </div>
                <h5 class="card-title fw-bold mb-3">{{ category.name }}</h5>
                <p class="card-text text-muted mb-4">{{ category.description }}</p>
                <router-link :to="`/category/${category.slug}`" class="btn btn-outline-primary category-btn">
                  <i class="ph-arrow-right me-2"></i>Xem sản phẩm
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import ProductSkeleton from '../components/ui/ProductSkeleton.vue'
import CategorySkeleton from '../components/ui/CategorySkeleton.vue'
import WishlistButton from '../components/ui/WishlistButton.vue'
import OptimizedProductImage from '../components/ui/OptimizedProductImage.vue'

const cartStore = useCartStore()

// Loading state
const isLoading = ref(true)

// Mock data
const featuredProducts = ref([
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    price: 450000,
    originalPrice: 600000,
    discount: 25,
    image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 2,
    name: 'Quần âu nam',
    description: 'Quần âu nam thiết kế hiện đại',
    price: 650000,
    originalPrice: 800000,
    discount: 19,
    image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 3,
    name: 'Áo khoác nam',
    description: 'Áo khoác nam phong cách casual',
    price: 850000,
    originalPrice: 1200000,
    discount: 29,
    image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  },
  {
    id: 4,
    name: 'Áo thun nam',
    description: 'Áo thun nam chất liệu cotton mềm mại',
    price: 250000,
    originalPrice: 350000,
    discount: 29,
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  }
])

const categories = ref([
  {
    id: 1,
    name: 'Áo',
    slug: 'ao',
    description: 'Áo sơ mi, áo thun, áo khoác cao cấp',
    icon: 'ph-t-shirt'
  },
  {
    id: 2,
    name: 'Quần',
    slug: 'quan',
    description: 'Quần âu, quần jean, quần short',
    icon: 'ph-bag'
  },
  {
    id: 3,
    name: 'Phụ kiện',
    slug: 'phu-kien',
    description: 'Thắt lưng, ví, đồng hồ',
    icon: 'ph-watch'
  }
])

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const addToCart = (product) => {
  cartStore.addItem(product)
  
  // Show success toast
  if (window.$toast) {
    window.$toast.success(
      `${product.name} đã được thêm vào giỏ hàng`,
      'Thêm vào giỏ hàng thành công'
    )
  }
}

// Intersection Observer for scroll animations
const observerOptions = {
  threshold: 0.1,
  rootMargin: '0px 0px -50px 0px'
}

const observer = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('animate')
    }
  })
}, observerOptions)

onMounted(() => {
  // Simulate loading data from API
  setTimeout(() => {
    isLoading.value = false
    
    // Trigger scroll animations after loading
    setTimeout(() => {
      const elements = document.querySelectorAll('.animate-on-scroll')
      elements.forEach(el => observer.observe(el))
    }, 100)
  }, 1500) // 1.5 seconds loading time
  
  // TODO: Replace with actual API calls
  // fetchFeaturedProducts()
  // fetchCategories()
})
</script>

<style scoped>
/* Hero Section */
.hero-content {
  position: relative;
  z-index: 2;
}

.hero-image-container {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--auro-shadow-hover);
}

.hero-main-image {
  width: 100%;
  height: 500px;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(26, 26, 26, 0.1), rgba(212, 175, 55, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.hero-image:hover .hero-overlay {
  opacity: 1;
}

.hero-image:hover .hero-main-image {
  transform: scale(1.05);
}

/* Section Divider */
.section-divider {
  width: 80px;
  height: 4px;
  background: var(--auro-gradient-accent);
  margin: 0 auto;
  border-radius: 2px;
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

/* Category Cards */
.category-card {
  background: var(--auro-card);
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--auro-gradient-accent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.category-card:hover::before {
  transform: scaleX(1);
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--auro-shadow-hover);
  border-color: var(--auro-accent);
}

.category-icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-icon {
  width: 80px;
  height: 80px;
  background: var(--auro-gradient-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.3);
  transition: all 0.3s ease;
}

.category-card:hover .category-icon {
  transform: scale(1.1);
  box-shadow: 0 12px 35px rgba(212, 175, 55, 0.4);
}

.category-icon i {
  font-size: 2.5rem;
  color: var(--auro-dark);
  transition: all 0.3s ease;
  font-weight: 300;
}

.category-card:hover .category-icon i {
  transform: rotate(5deg);
}

.category-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 2px solid var(--auro-accent);
  color: var(--auro-accent);
  background: transparent;
}

.category-btn:hover {
  background: var(--auro-gradient-accent);
  border-color: var(--auro-accent);
  color: var(--auro-dark);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
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

/* Background */
.bg-light {
  background-color: #f8f9fa !important;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-main-image {
    height: 300px;
  }
  
  .display-2 {
    font-size: 2.5rem;
  }
  
  .display-4 {
    font-size: 2rem;
  }
}
</style>
