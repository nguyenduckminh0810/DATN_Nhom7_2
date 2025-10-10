<template>
  <div class="home">
    <!-- USP Bar -->
    <UspBar />
    
    
   
    
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
            transition: isAnimating ? 'transform 0.8s cubic-bezier(0.25, 1, 0.5, 1)' : 'none'
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
                  <img :src="slide.mainImage" :alt="slide.title" class="hero-main-image" style="width: 100%; height: 100vh; object-fit: cover; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.3);">
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

    <!-- Categories Section -->
    <section class="categories-section section-full">
      <!-- Category Filter -->
      <div class="category-filters">
        <button 
          v-for="filter in categoryFilters" 
          :key="filter.id"
          :class="['category-filter-btn', { active: selectedFilter === filter.id }]"
          @click="selectFilter(filter.id)"
        >
          {{ filter.name }}
        </button>
      </div>
      
      <!-- Full-width categories carousel -->
      <div class="categories-carousel-container">
        <button class="section-nav-btn prev" @click="scrollCategories('prev')">
          ‹
        </button>
        
        <div class="section-list categories-grid" ref="categoriesGrid">
          <!-- Loading state -->
          <SkeletonLoader 
            v-if="isLoading" 
            v-for="n in 5" 
            :key="`category-skeleton-${n}`" 
            variant="card" 
            class="category-item section-item" 
          />
          
          <!-- Categories -->
          <div 
            v-else
            v-for="category in displayCategories" 
            :key="category.id"
            class="category-item section-item"
            @click="goToCategory(category.slug)"
          >
            <img :src="category.image" :alt="category.name" class="section-item__image"/>
            <div class="category-content">
              <h5 class="category-title">{{ category.name }}</h5>
              <p class="category-description">{{ category.description }}</p>
            </div>
          </div>
        </div>
        
        <button class="section-nav-btn next" @click="scrollCategories('next')">
          ›
        </button>
      </div>
    </section>


    <!-- Best Sellers -->
    <BestSellers />

    <!-- New Arrivals -->
    <NewArrivals />

    <!-- Featured Products -->
    <section class="featured-products-section">
      <!-- Header with container -->
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Sản phẩm nổi bật</h2>
          <router-link to="/san-pham?sort=featured" class="btn-view-all">
            Xem tất cả
          </router-link>
        </div>
      </div>
      
      <!-- Full-width products carousel -->
      <div class="products-carousel-container">
        <button class="prev-btn" @click="scrollFeatured('prev')">
          ‹
        </button>
        
          <<div class="products-grid" ref="featuredProductsGrid">
  <!-- Loading state -->
  <SkeletonLoader 
    v-if="isLoadingFeatured" 
    v-for="n in 5" 
    :key="`skeleton-${n}`" 
    variant="product" 
  />
  
  <!-- Error state -->
  <div v-else-if="featuredError" class="col-12 text-center py-5">
    <div class="alert alert-danger d-inline-block">
      <i class="ph-warning-circle me-2"></i>
      {{ featuredError }}
    </div>
  </div>
  
  <!-- Products -->
  <ProductCard
    v-else
    v-for="(product, index) in displayFeaturedProducts"
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
    :stock="product.stock || 10"
  />
</div>
        
        <button class="next-btn" @click="scrollFeatured('next')">
          ›
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useProductStore } from '../stores/product'
import LazyImage from '../components/common/LazyImage.vue'
import UspBar from '../components/home/UspBar.vue'
import Hero from '../components/home/Hero.vue'
import BestSellers from '../components/home/BestSellers.vue'
import NewArrivals from '../components/home/NewArrivals.vue'
import ProductCard from '../components/product/ProductCard.vue'
import SkeletonLoader from '../components/common/SkeletonLoader.vue'

const cartStore = useCartStore()
const productStore = useProductStore()
const router = useRouter()

// State
const featuredProducts = ref([])
const isLoadingFeatured = ref(true)
const featuredError = ref(null)

// API Function
const fetchFeaturedProducts = async () => {
  try {
    isLoadingFeatured.value = true
    featuredError.value = null
    
    const result = await productStore.fetchFeaturedProducts()
    
    if (result.success && result.data?.products) {
      featuredProducts.value = result.data.products
    } else {
      // Fallback mock data for development when API is not ready
      console.warn('API not available, using mock data for Featured Products')
      featuredProducts.value = [
        {
          id: 1,
          name: 'Áo thun nam cao cấp',
          image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500&h=600&fit=crop',
          hoverImage: 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500&h=600&fit=crop',
          price: 299000,
          originalPrice: 399000,
          discount: 25,
          promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
          colorOptions: ['#dc3545', '#007bff', '#28a745'],
          sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
          availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
          colorSizeMapping: {
            '#dc3545': ['S', 'M', 'L'],
            '#007bff': ['M', 'L', 'XL', '2XL'],
            '#28a745': ['L', 'XL', '2XL', '3XL']
          }
        },
        {
          id: 2,
          name: 'Quần short nữ thể thao',
          image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
          hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
          price: 199000,
          originalPrice: 299000,
          discount: 33,
          promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
          colorOptions: ['#ff69b4', '#007bff', '#000000'],
          sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
          availableSizes: ['S', 'M', 'L', 'XL'],
          colorSizeMapping: {
            '#ff69b4': ['S', 'M', 'L'],
            '#007bff': ['M', 'L', 'XL'],
            '#000000': ['L', 'XL', '2XL']
          }
        },
        {
          id: 3,
          name: 'Áo khoác nam dài tay',
          image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&h=600&fit=crop',
          hoverImage: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=500&h=600&fit=crop',
          price: 599000,
          originalPrice: 799000,
          discount: 25,
          promotionalBadge: 'MUA 2 GIẢM THÊM 10%',
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
          id: 4,
          name: 'Váy nữ công sở',
          image: 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500&h=600&fit=crop',
          hoverImage: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
          price: 399000,
          originalPrice: 499000,
          discount: 20,
          promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
          colorOptions: ['#000000', '#ffffff', '#ff69b4'],
          sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
          availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
          colorSizeMapping: {
            '#000000': ['S', 'M', 'L'],
            '#ffffff': ['M', 'L', 'XL'],
            '#ff69b4': ['L', 'XL', '2XL']
          }
        },
        {
          id: 5,
          name: 'Áo sơ mi nam trắng',
          image: 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500&h=600&fit=crop',
          hoverImage: 'https://images.unsplash.com/photo-1621184455862-c163dfb30e0f?w=500&h=600&fit=crop',
          price: 249000,
          originalPrice: 349000,
          discount: 29,
          promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
          colorOptions: ['#ffffff', '#000000', '#007bff'],
          sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
          availableSizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
          colorSizeMapping: {
            '#ffffff': ['S', 'M', 'L', 'XL'],
            '#000000': ['M', 'L', 'XL', '2XL'],
            '#007bff': ['L', 'XL', '2XL', '3XL']
          }
        }
      ]
    }
  } catch (err) {
    // Fallback mock data for development
    console.warn('API error, using mock data for Featured Products:', err.message)
    featuredProducts.value = [
      {
        id: 1,
        name: 'Áo thun nam cao cấp',
        image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500&h=600&fit=crop',
        price: 299000,
        originalPrice: 399000,
        discount: 25,
        promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
        colorOptions: ['#dc3545', '#007bff', '#28a745'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL', '2XL'],
        colorSizeMapping: {
          '#dc3545': ['S', 'M', 'L'],
          '#007bff': ['M', 'L', 'XL', '2XL'],
          '#28a745': ['L', 'XL', '2XL', '3XL']
        }
      },
      {
        id: 2,
        name: 'Quần short nữ thể thao',
        image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=600&fit=crop',
        price: 199000,
        originalPrice: 299000,
        discount: 33,
        promotionalBadge: 'TẶNG 01 TẤT THỂ THAO',
        colorOptions: ['#ff69b4', '#007bff', '#000000'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['S', 'M', 'L', 'XL'],
        colorSizeMapping: {
          '#ff69b4': ['S', 'M', 'L'],
          '#007bff': ['M', 'L', 'XL'],
          '#000000': ['L', 'XL', '2XL']
        }
      },
      {
        id: 3,
        name: 'Áo khoác nam dài tay',
        image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&h=600&fit=crop',
        hoverImage: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=500&h=600&fit=crop',
        price: 599000,
        originalPrice: 799000,
        discount: 25,
        promotionalBadge: 'MUA 2 GIẢM THÊM 10%',
        colorOptions: ['#000000', '#808080', '#8b4513'],
        sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
        availableSizes: ['L', 'XL', '2XL', '3XL'],
        colorSizeMapping: {
          '#000000': ['L', 'XL', '2XL'],
          '#808080': ['XL', '2XL', '3XL'],
          '#8b4513': ['2XL', '3XL']
        }
      }
    ]
  } finally {
    isLoadingFeatured.value = false
  }
}

// Loading state - disabled for better performance
const isLoading = ref(false)
const carouselInterval = ref(null)

// ===== Infinite loop state =====
const currentIndex = ref(1)       // hiển thị phần tử thật đầu tiên (sau cloneLast)
const isAnimating = ref(false)

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
  isAnimating.value = false
  
  // nếu đang ở cloneFirst (index 4), nhảy tức thời về real first (index 1)
  if (currentIndex.value === extendedSlides.value.length - 1) {
    isAnimating.value = false
    currentIndex.value = 1
  }
  // nếu đang ở cloneLast (index 0), nhảy tức thời về real last (index 3)
  if (currentIndex.value === 0) {
    isAnimating.value = false
    currentIndex.value = heroSlides.value.length
  }
}

// No more mock featured products - using API

// Category filters
const categoryFilters = ref([
  { id: 'ao', name: 'ÁO' },
  { id: 'quan', name: 'QUẦN' }
])

const selectedFilter = ref('ao')

// All categories with images
const allCategories = ref([
  // ÁO categories
  {
    id: 1,
    name: 'ÁO THUN',
    slug: 'ao-thun',
    description: 'Áo thun nam cao cấp',
    filter: 'ao',
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 2,
    name: 'ÁO SƠ MI',
    slug: 'ao-so-mi',
    description: 'Áo sơ mi nam công sở',
    filter: 'ao',
    image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 3,
    name: 'ÁO KHOÁC',
    slug: 'ao-khoac',
    description: 'Áo khoác nam thời trang',
    filter: 'ao',
    image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 4,
    name: 'ÁO POLO',
    slug: 'ao-polo',
    description: 'Áo polo nam cao cấp',
    filter: 'ao',
    image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  // QUẦN categories
  {
    id: 5,
    name: 'QUẦN ÂU',
    slug: 'quan-au',
    description: 'Quần âu nam công sở',
    filter: 'quan',
    image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 6,
    name: 'QUẦN JEAN',
    slug: 'quan-jean',
    description: 'Quần jean nam thời trang',
    filter: 'quan',
    image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 7,
    name: 'QUẦN SHORT',
    slug: 'quan-short',
    description: 'Quần short nam thể thao',
    filter: 'quan',
    image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 8,
    name: 'ÁO HOODIE',
    slug: 'ao-hoodie',
    description: 'Áo hoodie nam thời trang',
    filter: 'ao',
    image: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 9,
    name: 'QUẦN JOGGER',
    slug: 'quan-jogger',
    description: 'Quần jogger nam thể thao',
    filter: 'quan',
    image: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
  }
])

// Computed filtered categories
const filteredCategories = computed(() => {
  return allCategories.value.filter(category => category.filter === selectedFilter.value)
})

// Display 5 categories starting from currentCategoryIndex
const displayCategories = computed(() => {
  if (filteredCategories.value.length === 0) return []
  
  const result = []
  for (let i = 0; i < 5; i++) {
    const index = (currentCategoryIndex.value + i) % filteredCategories.value.length
    result.push(filteredCategories.value[index])
  }
  return result
})

// Methods
const selectFilter = (filterId) => {
  selectedFilter.value = filterId
  // Reset category index when filter changes
  currentCategoryIndex.value = 0
}

const goToCategory = (slug) => {
  router.push(`/category/${slug}`)
}

// Category navigation
const categoriesGrid = ref(null)
const currentCategoryIndex = ref(0)

const scrollCategories = (direction) => {
  // For fixed layout, we need to cycle through categories
  // This will be handled by changing the displayed categories
  if (direction === 'next') {
    // Move to next set of categories
    currentCategoryIndex.value = (currentCategoryIndex.value + 1) % filteredCategories.value.length
  } else {
    // Move to previous set of categories
    currentCategoryIndex.value = (currentCategoryIndex.value - 1 + filteredCategories.value.length) % filteredCategories.value.length
  }
}

// Featured products navigation
const featuredProductsGrid = ref(null)
const currentFeaturedIndex = ref(0)

const scrollFeatured = (direction) => {
  // For fixed layout, we need to cycle through products
  // This will be handled by changing the displayed products
  if (direction === 'next') {
    // Move to next set of products
    currentFeaturedIndex.value = (currentFeaturedIndex.value + 1) % featuredProducts.value.length
  } else {
    // Move to previous set of products
    currentFeaturedIndex.value = (currentFeaturedIndex.value - 1 + featuredProducts.value.length) % featuredProducts.value.length
  }
}

// Display 5 featured products starting from currentFeaturedIndex
const displayFeaturedProducts = computed(() => {
  if (featuredProducts.value.length === 0) return []
  
  const result = []
  for (let i = 0; i < 5; i++) {
    const index = (currentFeaturedIndex.value + i) % featuredProducts.value.length
    result.push(featuredProducts.value[index])
  }
  return result
})

const handleFeaturedScroll = () => {
  // Disable scroll handling for fixed layout
  // Cards are displayed in fixed positions with space-between
  return
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Carousel methods
const goToSlide = (index) => {
  if (isAnimating.value) return
  isAnimating.value = true
  currentIndex.value = index + 1 // +1 vì trước nó có cloneLast
}

const nextSlide = () => {
  if (isAnimating.value) return
  isAnimating.value = true
  currentIndex.value += 1
}

const previousSlide = () => {
  if (isAnimating.value) return
  isAnimating.value = true
  currentIndex.value -= 1
}

// Auto-advance carousel
const startCarousel = () => {
  if (carouselInterval.value) {
    clearInterval(carouselInterval.value)
  }
  carouselInterval.value = setInterval(() => {
    if (!isAnimating.value) {
      nextSlide()
    }
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

onMounted(async () => {
  // Fetch featured products from API
  await fetchFeaturedProducts()
  
  // Start carousel auto-advance immediately
  startCarousel()

  // Pause carousel on hover
  const carouselElement = document.querySelector('.custom-carousel')
  if (carouselElement) {
    carouselElement.addEventListener('mouseenter', stopCarousel)
    carouselElement.addEventListener('mouseleave', startCarousel)
  }

  // Initialize Featured Products index
  nextTick(() => {
    if (featuredProducts.value.length > 0) {
      currentFeaturedIndex.value = 0
    }
  })

  // Skip scroll animations for better performance
  // const elements = document.querySelectorAll('.animate-on-scroll')
  // elements.forEach((el) => observer.observe(el))
})

onUnmounted(() => {
  // Clean up carousel interval
  stopCarousel()
})
</script>

<style scoped>
/* Prevent horizontal scroll and remove extra spacing */
.home {
  overflow-x: hidden;
  width: 100%;
  max-width: 100vw;
  margin: 0;
  padding: 0;
}

/* Global reset for hero section */
html, body {
  margin: 0;
  padding: 0;
}

/* Ensure no padding-top for body on hero page */
body {
  padding-top: 0 !important;
}

.hero, .hero * {
  scroll-margin-top: 0;
}

/* Hero content styling */
.hero-content-wrapper {
  margin-top: 0 !important;
  padding-top: 0 !important;
}

.hero-text, .hero-image {
  padding-top: 0 !important;
  margin-top: 0 !important;
}

.hero-main-image {
  display: block;
  border-radius: 24px;
}

/* Section Header - Layout: title center, button bottom right */
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
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

/* Hero Carousel Section */
.hero-carousel-section {
  position: relative;
  overflow: hidden;
  height: 100vh;
  padding-top: 0;
  margin-top: 0;
  width: 100%;
}

.carousel-inner {
  display: flex;
  height: 100vh;
  width: 500%; /* 5 slides * 100% */
  will-change: transform;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.carousel-slide {
  flex: 0 0 20%; /* 5 slides * 20% = 100% */
  width: 20%;
  min-width: 20%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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
  margin: 0;
  padding: 0;
}


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
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem;
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
  .hero-content-wrapper {
    flex-direction: column !important;
    text-align: center !important;
    padding: 1rem !important;
  }
  
  .hero-text {
    padding-right: 0 !important;
    margin-bottom: 2rem;
  }
  
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
  .hero-carousel-section {
    padding-top: 80px;
  }
  
  .hero-content-wrapper {
    padding: 0.5rem !important;
  }
  
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
  
  .carousel-control-prev,
  .carousel-control-next {
    width: 40px;
    height: 40px;
  }
  
  .carousel-control-prev {
    left: 0.5rem;
  }

  .carousel-control-next {
    right: 0.5rem;
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

/* Categories Section - only specific styles */

/* Category Filters */
.category-filters {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 3rem;
}

.category-filter-btn {
  padding: 12px 32px;
  border: none;
  background: #f8f9fa;
  color: #6c757d;
  font-weight: 600;
  font-size: 0.875rem;
  border-radius: 50px;
  transition: all 0.3s ease;
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.category-filter-btn.active {
  background: #000;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
}

.category-filter-btn:hover:not(.active) {
  background: #e9ecef;
  color: #000;
  transform: translateY(-1px);
}

.categories-carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  width: 100%;
}

/* Desktop responsive - handled by sections.css */

/* No scrollbar needed - all items fit in one row */

/* Remove any background from categories section */
.container {
  background: transparent !important;
}

/* Force remove all backgrounds */
.home {
  background: transparent !important;
}

.home > div {
  background: transparent !important;
}

/* Category content styling only */
.category-content {
  padding: 1.5rem;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.category-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #212529;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.category-description {
  color: #6c757d;
  font-size: 0.9rem;
  margin: 0;
  font-weight: 500;
}

/* Skeleton loading for categories */


/* Responsive Categories - handled by sections.css */

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

/* Featured Products Section */
.featured-products-section {
  padding: 4rem 0;
  background: #f8f9fa;
  width: 100%;
  position: relative;
  margin: 0;
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

/* Responsive breakpoints for Featured Products */
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

@media (max-width: 992px) {
  .section-title {
    font-size: 2rem;
  }
  
  .section-header {
    margin-bottom: 2rem;
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
  
  .featured-products-section {
    padding: 2rem 0;
  }
}

/* Kaira Inspired Styles */

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
