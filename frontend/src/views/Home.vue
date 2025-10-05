<template>
  <div class="home">
    <!-- Hero Carousel Section -->
    <section class="hero-carousel-section">
      <div class="custom-carousel">
        <div class="carousel-indicators">
          <button
            v-for="(slide, index) in heroSlides"
            :key="index"
            type="button"
            :class="{ active: index === visibleIndex }"
            @click="goToSlide(index)"
            :aria-label="`Slide ${index + 1}`"
          ></button>
        </div>

        <div
          class="carousel-inner"
          :style="{
            transform: `translateX(-${currentIndex * 20}%)`,
            transition: 'transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)'
          }"
          @transitionend="onTransitionEnd"
        >
          <!-- [cloneLast, ...heroSlides, cloneFirst] -->
          <div
            v-for="(slide, index) in extendedSlides"
            :key="'ext-' + index"
            class="carousel-slide"
          >
            <!-- giữ nguyên HTML bên trong slide của bạn -->
            <div class="hero-slide" :style="{ 
              backgroundImage: `linear-gradient(135deg, rgba(0,0,0,0.3), rgba(0,0,0,0.1)), url(${slide.backgroundImage})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              backgroundRepeat: 'no-repeat',
              minHeight: '100vh',
              width: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }">
              <div class="hero-content-wrapper" style="display: flex; width: 100%; max-width: 1200px; margin: 0 auto; padding: 2rem; align-items: center; position: relative; z-index: 2;">
                <div class="hero-text" style="flex: 1; padding-right: 2rem;">
                  <div class="badge-new" style="background: #cd7f32; color: white; padding: 0.5rem 1rem; border-radius: 20px; font-size: 0.875rem; font-weight: bold; display: inline-block; margin-bottom: 1rem;">{{ slide.badge }}</div>
                  <h1 class="hero-title" style="font-size: 3rem; font-weight: bold; color: white; margin-bottom: 1rem; line-height: 1.2;">{{ slide.title }}</h1>
                  <p class="hero-description" style="font-size: 1.125rem; color: rgba(255,255,255,0.9); margin-bottom: 2rem; line-height: 1.6;">{{ slide.description }}</p>
                  <div class="hero-promo" v-if="slide.promo" style="background: rgba(255,255,255,0.1); padding: 1rem; border-radius: 8px; margin-bottom: 2rem; display: flex; align-items: center; gap: 1rem;">
                    <span class="promo-text" style="color: white; flex: 1;">{{ slide.promo }}</span>
                    <div class="promo-item" style="display: flex; align-items: center; gap: 0.5rem;">
                      <i class="ph-plus-circle" style="color: #cd7f32; font-size: 1.5rem;"></i>
                      <img :src="slide.promoImage" :alt="slide.promoItem" class="promo-image" style="width: 40px; height: 40px; border-radius: 4px; object-fit: cover;">
                    </div>
                  </div>
                  <router-link :to="slide.ctaLink" class="btn-hero-cta" style="background: #cd7f32; color: white; padding: 1rem 2rem; border-radius: 8px; text-decoration: none; font-weight: bold; display: inline-flex; align-items: center; gap: 0.5rem; transition: all 0.3s ease;">
                    {{ slide.ctaText }}
                    <i class="ph-arrow-right" style="font-size: 1.25rem;"></i>
                  </router-link>
                </div>
                <div class="hero-image" style="flex: 1; text-align: center;">
                  <img :src="slide.mainImage" :alt="slide.title" class="hero-main-image" style="max-width: 100%; height: auto; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.3);">
                </div>
              </div>
            </div>
            <!-- /giữ nguyên -->
          </div>
        </div>

        <button class="carousel-control-prev" type="button" @click="previousSlide" style="position: absolute; left: 2rem; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); border: none; color: white; width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; z-index: 10; transition: all 0.3s ease;">
          <i class="ph-caret-left" style="font-size: 1.5rem;"></i>
        </button>
        <button class="carousel-control-next" type="button" @click="nextSlide" style="position: absolute; right: 2rem; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); border: none; color: white; width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; z-index: 10; transition: all 0.3s ease;">
          <i class="ph-caret-right" style="font-size: 1.5rem;"></i>
        </button>
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
          <SkeletonLoader v-for="n in 4" :key="n" type="product" />
        </div>

        <!-- Featured products -->
        <div class="row g-4" v-else-if="featuredProducts.length > 0">
          <div class="col-md-6 col-lg-3" v-for="product in featuredProducts" :key="product.id">
            <div class="card product-card h-100 animate-on-scroll">
              <div class="position-relative product-image-container">
                <LazyImage
                  :src="
                    product.image ||
                    'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
                  "
                  :alt="product.name"
                  :width="500"
                  :height="500"
                  :quality="85"
                  :webp="false"
                  :lazy="false"
                  container-class="product-image"
                  image-class="card-img-top"
                  :show-zoom-overlay="false"
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
                    <small class="text-muted text-decoration-line-through ms-2">{{
                      formatPrice(product.originalPrice || product.price * 1.2)
                    }}</small>
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
          <SkeletonLoader v-for="n in 3" :key="n" type="category" />
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
                <router-link
                  :to="`/category/${category.slug}`"
                  class="btn btn-outline-primary category-btn"
                >
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
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useCartStore } from '../stores/cart'
import SkeletonLoader from '../components/ui/SkeletonLoader.vue'
import WishlistButton from '../components/ui/WishlistButton.vue'
import LazyImage from '../components/ui/LazyImage.vue'

const cartStore = useCartStore()

// Loading state
const isLoading = ref(true)
const carouselInterval = ref(null)

// ===== Infinite loop state =====
const currentIndex = ref(1)       // hiển thị phần tử thật đầu tiên (sau cloneLast)
const isAnimating = ref(true)

// Hero carousel slides data
const heroSlides = ref([
  {
    id: 1,
    badge: 'NEW COLLECTION',
    title: 'AURO MENSWEAR',
    description:
      'Khám phá bộ sưu tập thời trang nam đẳng cấp với thiết kế tinh tế, chất liệu cao cấp và phong cách hiện đại.',
    promo: 'Nhập "AUROVIP" tặng ví da cho đơn từ 999k',
    promoItem: 'Ví da',
    promoImage:
      'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    ctaText: 'MUA NGAY',
    ctaLink: '/category/ao',
    backgroundImage:
      'https://images.unsplash.com/photo-1441986300917-64674bd600d8?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
    mainImage:
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
  },
  {
    id: 2,
    badge: 'PREMIUM QUALITY',
    title: 'BUSINESS COLLECTION',
    description:
      'Bộ sưu tập trang phục công sở cao cấp, mang đến sự tự tin và chuyên nghiệp cho phái mạnh hiện đại.',
    promo: 'Nhập "BUSINESS" tặng cà vạt cho đơn từ 1.5M',
    promoItem: 'Cà vạt',
    promoImage:
      'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    ctaText: 'KHÁM PHÁ',
    ctaLink: '/category/quan',
    backgroundImage:
      'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
    mainImage:
      'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
  },
  {
    id: 3,
    badge: 'SUMMER SALE',
    title: 'CASUAL STYLE',
    description:
      'Phong cách casual thoải mái với những thiết kế trẻ trung, năng động phù hợp cho mọi hoạt động.',
    promo: 'Nhập "SUMMER" giảm thêm 20% cho đơn từ 500k',
    promoItem: 'Áo thun',
    promoImage:
      'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
    ctaText: 'SHOPPING',
    ctaLink: '/category/sale',
    backgroundImage:
      'https://images.unsplash.com/photo-1558769132-cb1aea1f5d8c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
    mainImage:
      'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
  },
])

const extendedSlides = computed(() => {
  const list = heroSlides.value
  return [list[list.length - 1], ...list, list[0]]
})

// Index thực để tô active cho indicators
const visibleIndex = computed(() => {
  if (currentIndex.value === 0) return heroSlides.value.length - 1
  if (currentIndex.value === extendedSlides.value.length - 1) return 0
  return currentIndex.value - 1
})

const onTransitionEnd = async () => {
  // nếu đang ở cloneFirst, nhảy tức thời về real first
  if (currentIndex.value === extendedSlides.value.length - 1) {
    currentIndex.value = 1
  }
  // nếu đang ở cloneLast, nhảy tức thời về real last
  if (currentIndex.value === 0) {
    currentIndex.value = heroSlides.value.length
  }
}

// Mock data
const featuredProducts = ref([
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    description: 'Áo sơ mi nam chất liệu cotton 100%',
    price: 450000,
    originalPrice: 600000,
    discount: 25,
    image:
      'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 2,
    name: 'Quần âu nam',
    description: 'Quần âu nam thiết kế hiện đại',
    price: 650000,
    originalPrice: 800000,
    discount: 19,
    image:
      'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 3,
    name: 'Áo khoác nam',
    description: 'Áo khoác nam phong cách casual',
    price: 850000,
    originalPrice: 1200000,
    discount: 29,
    image:
      'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 4,
    name: 'Áo thun nam',
    description: 'Áo thun nam chất liệu cotton mềm mại',
    price: 250000,
    originalPrice: 350000,
    discount: 29,
    image:
      'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
  },
])

const categories = ref([
  {
    id: 1,
    name: 'Áo',
    slug: 'ao',
    description: 'Áo sơ mi, áo thun, áo khoác cao cấp',
    icon: 'ph-t-shirt',
  },
  {
    id: 2,
    name: 'Quần',
    slug: 'quan',
    description: 'Quần âu, quần jean, quần short',
    icon: 'ph-bag',
  },
  {
    id: 3,
    name: 'Phụ kiện',
    slug: 'phu-kien',
    description: 'Thắt lưng, ví, đồng hồ',
    icon: 'ph-watch',
  },
])

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Carousel methods
const goToSlide = (index) => {
  currentIndex.value = index + 1 // +1 vì trước nó có cloneLast
}

const nextSlide = () => {
  currentIndex.value += 1
}

const previousSlide = () => {
  currentIndex.value -= 1
}

// Auto-advance carousel
const startCarousel = () => {
  if (carouselInterval.value) {
    clearInterval(carouselInterval.value)
  }
  carouselInterval.value = setInterval(() => {
    nextSlide()  // dùng hàm mới
  }, 5000)
}

const stopCarousel = () => {
  if (carouselInterval.value) {
    clearInterval(carouselInterval.value)
    carouselInterval.value = null
  }
}

const addToCart = (product) => {
  cartStore.addItem(product)

  // Show success toast
  if (window.$toast) {
    window.$toast.success(
      `${product.name} đã được thêm vào giỏ hàng`,
      'Thêm vào giỏ hàng thành công',
    )
  }
}

// Intersection Observer for scroll animations
const observerOptions = {
  threshold: 0.1,
  rootMargin: '0px 0px -50px 0px',
}

const observer = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (entry.isIntersecting) {
      entry.target.classList.add('animate')
    }
  })
}, observerOptions)

onMounted(() => {
  // Start carousel auto-advance after a delay
  setTimeout(() => {
    startCarousel()
  }, 2000) // Start after 2 seconds

  // Simulate loading data from API
  setTimeout(() => {
    isLoading.value = false

    // Trigger scroll animations after loading
    setTimeout(() => {
      const elements = document.querySelectorAll('.animate-on-scroll')
      elements.forEach((el) => observer.observe(el))
    }, 100)
  }, 1500) // 1.5 seconds loading time

  // TODO: Replace with actual API calls
  // fetchFeaturedProducts()
  // fetchCategories()
})

onUnmounted(() => {
  // Clean up carousel interval
  stopCarousel()
})
</script>

<style scoped>
/* Hero Carousel Section */
.hero-carousel-section {
  position: relative;
  overflow: hidden;
  height: 100vh;
  padding-top: 120px;
}

.carousel-inner {
  display: flex;
  height: 100vh;
  width: 500%; /* 5 slides * 100% */
  will-change: transform;
  overflow: hidden;
}

.carousel-slide {
  flex: 0 0 20%; /* 5 slides * 20% = 100% */
  width: 20%;
  min-width: 20%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

/* Custom carousel styles - no Bootstrap interference */
.custom-carousel {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.hero-slide {
  height: 100vh;
  width: 100%;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* .hero-slide::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.1) 100%);
  z-index: 1;
  pointer-events: none;
} */

.hero-content {
  position: relative;
  z-index: 2;
  color: white;
  padding: 2rem 0;
}

.hero-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.badge-new {
  display: inline-block;
  background: var(--auro-secondary);
  color: var(--auro-primary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 1rem;
}

.hero-title {
  font-family: var(--auro-display-font);
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-description {
  font-size: 1.125rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.9);
  max-width: 500px;
}

.hero-promo {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.promo-text {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.9);
  flex: 1;
}

.promo-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.promo-item i {
  color: var(--auro-secondary);
  font-size: 1.25rem;
}

.promo-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 8px;
}

.btn-hero-cta {
  display: inline-flex;
  align-items: center;
  background: var(--auro-secondary);
  color: var(--auro-primary);
  padding: 1rem 2rem;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(205, 127, 50, 0.3);
}

.btn-hero-cta:hover {
  background: var(--auro-secondary-light);
  color: var(--auro-primary);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(205, 127, 50, 0.4);
}

.hero-main-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.hero-main-image:hover {
  transform: scale(1.05);
}

/* Carousel Controls */
.carousel-indicators {
  bottom: 2rem;
  z-index: 3;
}

.carousel-indicators button {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  border: none;
  margin: 0 0.5rem;
  transition: all 0.3s ease;
}

.carousel-indicators button.active {
  background: var(--auro-secondary);
  transform: scale(1.2);
}

.carousel-control-prev,
.carousel-control-next {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  z-index: 3;
}

.carousel-control-prev {
  left: 2rem;
}

.carousel-control-next {
  right: 2rem;
}

.carousel-control-prev:hover,
.carousel-control-next:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.carousel-control-prev i,
.carousel-control-next i {
  font-size: 1.5rem;
  color: white;
}

/* Responsive Design */
@media (max-width: 991.98px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-promo {
    flex-direction: column;
    text-align: center;
  }

  .carousel-control-prev,
  .carousel-control-next {
    width: 50px;
    height: 50px;
  }

  .carousel-control-prev {
    left: 1rem;
  }

  .carousel-control-next {
    right: 1rem;
  }
}

@media (max-width: 576px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-description {
    font-size: 0.875rem;
  }

  .btn-hero-cta {
    padding: 0.75rem 1.5rem;
    font-size: 0.875rem;
  }

  .badge-new {
    font-size: 0.75rem;
    padding: 0.375rem 0.75rem;
  }
}

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
.animate-on-scroll:nth-child(1) {
  transition-delay: 0.1s;
}
.animate-on-scroll:nth-child(2) {
  transition-delay: 0.2s;
}
.animate-on-scroll:nth-child(3) {
  transition-delay: 0.3s;
}
.animate-on-scroll:nth-child(4) {
  transition-delay: 0.4s;
}

/* Background */
.bg-light {
  background-color: var(--auro-light) !important;
}

/* Kaira Inspired Styles */
.section-title {
  font-family: var(--auro-heading-font);
  font-size: 2.5rem;
  font-weight: 400;
  color: var(--auro-text);
  margin-bottom: 1rem;
}

.banner-item {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: var(--auro-card);
  box-shadow: var(--auro-shadow);
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(140, 144, 126, 0.1) 0%, rgba(140, 144, 126, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.banner-item:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.banner-item:hover::before {
  opacity: 1;
}

.image-holder {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 1rem;
}

.image-holder img {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
  height: 300px;
  object-fit: cover;
  filter: brightness(1) contrast(1);
}

.image-zoom-effect:hover .image-holder img {
  transform: scale(1.08);
  filter: brightness(1.1) contrast(1.1);
}

.image-holder::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 70%
  );
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.image-zoom-effect:hover .image-holder::after {
  transform: translateX(100%);
}

.banner-content {
  padding: 1.5rem 0;
}

.element-title {
  font-family: var(--auro-heading-font);
  font-size: 1.25rem;
  font-weight: 400;
  margin-bottom: 0.75rem;
}

.element-title a {
  color: var(--auro-text);
  text-decoration: none;
  transition: color 0.3s ease;
}

.element-title a:hover {
  color: var(--auro-accent);
}

.banner-content p {
  color: var(--auro-text-light);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.btn-link {
  color: var(--auro-accent);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: inline-block;
  padding: 0.5rem 0;
}

.btn-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--auro-accent);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-link:hover {
  color: var(--auro-text);
  transform: translateY(-2px);
}

.btn-link:hover::after {
  width: 100%;
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
