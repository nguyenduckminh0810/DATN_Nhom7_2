<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="row align-items-center min-vh-100">
          <div class="col-lg-6">
            <h1 class="display-3 fw-bold mb-4 gradient-text">
              Thời trang nam cao cấp
            </h1>
            <p class="lead mb-4 text-muted">
              Khám phá bộ sưu tập thời trang nam đẳng cấp với thiết kế tinh tế, 
              chất liệu cao cấp và phong cách hiện đại. AURO mang đến cho bạn 
              những trải nghiệm mua sắm tuyệt vời.
            </p>
            <div class="d-flex gap-3 flex-wrap">
              <router-link to="/category" class="btn btn-auro-primary btn-lg">
                <i class="bi bi-arrow-right me-2"></i>Khám phá ngay
              </router-link>
              <router-link to="/category" class="btn btn-outline-primary btn-lg">
                <i class="bi bi-play-circle me-2"></i>Xem video
              </router-link>
            </div>
          </div>
          <div class="col-lg-6">
            <div class="hero-image">
              <img src="https://via.placeholder.com/600x400/6c757d/ffffff?text=AURO+Hero" 
                   alt="AURO Hero" class="img-fluid rounded-3 shadow-lg">
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
            <h2 class="display-5 fw-bold">Sản phẩm nổi bật</h2>
            <p class="text-muted">Những mẫu thiết kế được yêu thích nhất</p>
          </div>
        </div>
        
        <div class="row g-4" v-if="featuredProducts.length > 0">
          <div class="col-md-6 col-lg-3" v-for="product in featuredProducts" :key="product.id">
            <div class="card product-card h-100">
              <div class="position-relative">
                <img :src="product.image || 'https://via.placeholder.com/300x300'" 
                     class="card-img-top" :alt="product.name" style="height: 250px; object-fit: cover;">
                <div class="position-absolute top-0 end-0 m-2">
                  <span class="badge bg-danger">-{{ product.discount || 20 }}%</span>
                </div>
              </div>
              <div class="card-body d-flex flex-column">
                <h6 class="card-title fw-bold">{{ product.name }}</h6>
                <p class="card-text text-muted small flex-grow-1">{{ product.description }}</p>
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <span class="price h5 mb-0">{{ formatPrice(product.price) }}</span>
                    <small class="text-muted text-decoration-line-through ms-2">{{ formatPrice(product.originalPrice || product.price * 1.2) }}</small>
                  </div>
                  <button class="btn btn-auro-primary btn-sm" @click="addToCart(product)">
                    <i class="bi bi-cart-plus"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="text-center mt-4">
          <router-link to="/category" class="btn btn-auro-secondary btn-lg">
            Xem tất cả sản phẩm
          </router-link>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="py-5">
      <div class="container">
        <div class="row">
          <div class="col-12 text-center mb-5">
            <h2 class="display-5 fw-bold">Danh mục sản phẩm</h2>
            <p class="text-muted">Khám phá theo từng loại sản phẩm</p>
          </div>
        </div>
        
        <div class="row g-4">
          <div class="col-md-4" v-for="category in categories" :key="category.id">
            <div class="card category-card h-100">
              <div class="card-body text-center p-4">
                <div class="feature-icon mb-3">
                  <i :class="category.icon || 'bi bi-grid-3x3-gap'"></i>
                </div>
                <h5 class="card-title fw-bold">{{ category.name }}</h5>
                <p class="card-text text-muted">{{ category.description }}</p>
                <router-link :to="`/category/${category.slug}`" class="btn btn-outline-primary">
                  Xem sản phẩm
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

const cartStore = useCartStore()

// Mock data
const featuredProducts = ref([
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    price: 450000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+sơ+mi'
  },
  {
    id: 2,
    name: 'Quần âu nam',
    description: 'Quần âu nam thiết kế hiện đại',
    price: 650000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Quần+âu'
  },
  {
    id: 3,
    name: 'Áo khoác nam',
    description: 'Áo khoác nam phong cách casual',
    price: 850000,
    image: 'https://via.placeholder.com/300x300/6c757d/ffffff?text=Áo+khoác'
  }
])

const categories = ref([
  {
    id: 1,
    name: 'Áo',
    slug: 'ao',
    description: 'Áo sơ mi, áo thun, áo khoác cao cấp',
    icon: 'bi bi-shirt'
  },
  {
    id: 2,
    name: 'Quần',
    slug: 'quan',
    description: 'Quần âu, quần jean, quần short',
    icon: 'bi bi-pants'
  },
  {
    id: 3,
    name: 'Phụ kiện',
    slug: 'phu-kien',
    description: 'Thắt lưng, ví, đồng hồ',
    icon: 'bi bi-watch'
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
}

onMounted(() => {
  // Load featured products and categories from API
  // TODO: Implement API calls
})
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, rgba(203, 60, 255, 0.05), rgba(0, 194, 255, 0.05));
  border-radius: 2rem;
  padding: 3rem;
  margin: 2rem 0;
  border: 1px solid rgba(203, 60, 255, 0.1);
}

.card {
  transition: all 0.3s ease;
  border: 1px solid rgba(203, 60, 255, 0.1);
  border-radius: 1rem;
  background: white;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(203, 60, 255, 0.1), 0 10px 10px -5px rgba(0, 194, 255, 0.04);
  border-color: rgba(203, 60, 255, 0.2);
}

.product-card .card-img-top {
  transition: transform 0.3s ease;
}

.product-card:hover .card-img-top {
  transform: scale(1.05);
}
</style>
