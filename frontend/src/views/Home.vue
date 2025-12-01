<template>
  <div class="home">
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
            transition: isAnimating ? 'transform 0.8s cubic-bezier(0.4, 0.0, 0.2, 1)' : 'none',
          }"
          @transitionend="onTransitionEnd"
        >
          <div
            v-for="(slide, index) in extendedSlides"
            :key="'ext-' + index"
            class="carousel-slide"
          >
            <div
              class="hero-slide"
              :style="{
                backgroundImage: `linear-gradient(135deg, rgba(0,0,0,0.3), rgba(0,0,0,0.1)), url(${slide.backgroundImage})`,
              }"
              style="margin-top: 150px;"
            >
              <div class="hero-content-wrapper">
                <div class="hero-text">
                  <div class="badge-new">
                    {{ slide.badge }}
                  </div>
                  <h1 class="hero-title">
                    {{ slide.title }}
                  </h1>
                  <p class="hero-description">
                    {{ slide.description }}
                  </p>
                  <div class="hero-promo" v-if="slide.promo">
                    <span class="promo-text">{{ slide.promo }}</span>
                    <div class="promo-item">
                      <i class="bi bi-plus-circle"></i>
                      <img :src="slide.promoImage" :alt="slide.promoItem" class="promo-image" />
                    </div>
                  </div>
                  <router-link :to="slide.ctaLink" class="btn-hero-cta">
                    {{ slide.ctaText }}
                    <i class="bi bi-arrow-right"></i>
                  </router-link>
                </div>
                <div class="hero-image">
                  <img :src="slide.mainImage" :alt="slide.title" class="hero-main-image" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <button class="carousel-control-prev" type="button" @click="previousSlide">
          <i class="bi bi-caret-left"></i>
        </button>
        <button class="carousel-control-next" type="button" @click="nextSlide">
          <i class="bi bi-caret-right"></i>
        </button>
      </div>
    </section>

    <BestSellers />
    <NewArrivals />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import BestSellers from '../components/home/BestSellers.vue'
import NewArrivals from '../components/home/NewArrivals.vue'
import SkeletonLoader from '../components/common/SkeletonLoader.vue'
import categoryService from '../services/categoryService'

defineOptions({ name: 'HomeView' })

const router = useRouter()
const carouselInterval = ref(null)
const currentIndex = ref(1)
const isAnimating = ref(false)

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
    ctaLink: '/san-pham?sort=createdAt',
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
    ctaLink: '/san-pham?sort=createdAt',
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
    ctaLink: '/san-pham?sort=createdAt',
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

const visibleIndex = computed(() => {
  if (currentIndex.value === 0) return heroSlides.value.length - 1
  if (currentIndex.value === extendedSlides.value.length - 1) return 0
  return currentIndex.value - 1
})

const onTransitionEnd = () => {
  isAnimating.value = false
  if (currentIndex.value === extendedSlides.value.length - 1) currentIndex.value = 1
  if (currentIndex.value === 0) currentIndex.value = heroSlides.value.length
}

// State cho categories
const categoryFilters = ref([])
const selectedFilter = ref(null)
const allCategories = ref([])
const isLoadingCategories = ref(false)

const mapCategoryFromBE = (beCategory) => {
  const tenLower = (beCategory.ten || '').toLowerCase()
  const slugLower = (beCategory.slug || '').toLowerCase()

  let filter = null
  if (tenLower.includes('áo') || slugLower.startsWith('ao') || slugLower.includes('ao-')) {
    filter = 'ao'
  } else if (
    tenLower.includes('quần') ||
    slugLower.startsWith('quan') ||
    slugLower.includes('quan-')
  ) {
    filter = 'quan'
  }

  const description = `${beCategory.ten} nam cao cấp`

  const defaultImage =
    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

  return {
    id: beCategory.id,
    name: beCategory.ten,
    slug: beCategory.slug,
    description: description,
    filter: filter,
    image: defaultImage,
    idCha: beCategory.idCha,
    thuTu: beCategory.thuTu,
    hoatDong: beCategory.hoatDong,
    productCount: beCategory.productCount || 0,
  }
}

const fetchCategories = async () => {
  isLoadingCategories.value = true
  try {
    const response = await categoryService.getAll()

    if (response.success && response.data) {
      const mappedCategories = response.data.map(mapCategoryFromBE)

      const activeCategories = mappedCategories.filter((cat) => cat.hoatDong === 1)

      const parentCategories = activeCategories.filter((cat) => !cat.idCha)

      if (parentCategories.length > 0) {
        categoryFilters.value = parentCategories.map((cat) => ({
          id: cat.id,
          name: cat.name,
        }))

        if (categoryFilters.value.length > 0) {
          selectedFilter.value = categoryFilters.value[0].id
        }
      } else {
        const uniqueFilters = [
          ...new Set(activeCategories.map((cat) => cat.filter).filter(Boolean)),
        ]
        if (uniqueFilters.length > 0) {
          categoryFilters.value = uniqueFilters.map((filter) => {
            const filterNames = {
              ao: 'ÁO',
              quan: 'QUẦN',
            }
            return {
              id: filter,
              name: filterNames[filter] || filter.toUpperCase(),
            }
          })
          selectedFilter.value = categoryFilters.value[0].id
        }
      }

      allCategories.value = activeCategories.filter((cat) => cat.idCha !== null)

      allCategories.value.sort((a, b) => {
        if (a.thuTu && b.thuTu) return a.thuTu - b.thuTu
        return 0
      })
    }
  } catch (error) {
    console.error('Error fetching categories:', error)
    categoryFilters.value = [
      { id: 'ao', name: 'ÁO' },
      { id: 'quan', name: 'QUẦN' },
    ]
    selectedFilter.value = 'ao'
  } finally {
    isLoadingCategories.value = false
  }
}

const filteredCategories = computed(() => {
  if (!selectedFilter.value) return []
  return allCategories.value.filter((c) => c.idCha === selectedFilter.value)
})

const currentCategoryIndex = ref(0)
const displayCategories = computed(() => {
  const list = filteredCategories.value
  if (list.length <= 5) return list
  const result = []
  for (let i = 0; i < 5; i++) {
    const index = (currentCategoryIndex.value + i) % list.length
    result.push(list[index])
  }
  return result
})

const selectFilter = (filterId) => {
  selectedFilter.value = filterId
  currentCategoryIndex.value = 0
}
const goToCategory = (slug) => {
  router.push(`/category/${slug}`)
}
const categoriesGrid = ref(null)
const scrollCategories = (direction) => {
  currentCategoryIndex.value =
    direction === 'next'
      ? (currentCategoryIndex.value + 1) % filteredCategories.value.length
      : (currentCategoryIndex.value - 1 + filteredCategories.value.length) %
        filteredCategories.value.length
}

const goToSlide = (index) => {
  if (isAnimating.value) return
  isAnimating.value = true
  currentIndex.value = index + 1
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
const startCarousel = () => {
  if (carouselInterval.value) clearInterval(carouselInterval.value)
  carouselInterval.value = setInterval(() => {
    if (!isAnimating.value) nextSlide()
  }, 5000)
}
const stopCarousel = () => {
  if (carouselInterval.value) {
    clearInterval(carouselInterval.value)
    carouselInterval.value = null
  }
}

onMounted(() => {
  startCarousel()
  fetchCategories()
})
onUnmounted(() => {
  stopCarousel()
})
</script>

<style scoped>
.home {
  overflow-x: hidden;
  width: 100%;
  max-width: 100vw;
  margin: 0;
  padding: 0;
  margin-top: -2rem;
}
html,
body {
  margin: 0;
  padding: 0;
}
body {
  padding-top: 0 !important;
}
.hero,
.hero * {
  scroll-margin-top: 0;
}
.hero-content-wrapper {
  margin-top: 0 !important;
  padding-top: 0 !important;
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 4rem;
  align-items: center;
  position: relative;
  z-index: 2;
  gap: 4rem;
}
.hero-text,
.hero-image {
  flex: 1;
  padding-top: 0 !important;
  margin-top: 0 !important;
}

.hero-text {
  padding-left: 2rem;
  padding-right: 0;
}

.hero-image {
  text-align: center;
}
.hero-main-image {
  display: block;
  border-radius: 24px;
  width: 100%;
  height: 50vh;
  max-height: 600px;
  object-fit: cover;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-hero-cta i {
  font-size: 1.25rem;
}
.section-header {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 1rem;
  width: 100%;
  text-align: center;
}

.categories-section.section-full {
  margin-top: 1rem;
  padding: 2.5rem 0;
}
.section-list {
  display: flex;
  gap: 0.75rem; /* Khoảng cách vừa phải giữa các items */
  justify-content: center;
}

/* Khoảng cách cho categories grid */
.section-list.categories-grid {
  gap: 0.75rem !important; /* Khoảng cách 12px */
  justify-content: center !important;
}

.section-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
  flex: 0 0 auto;
}

/* Điều chỉnh width cho categories items */
.categories-grid .section-item {
  width: 280px;
  max-width: 280px;
}

/* Tránh ảnh hưởng đến product-card trong best-sellers */
.best-sellers-section .section-item {
  width: auto !important;
}
.section-item:hover {
  transform: translateY(-2px);
}
.section-item__image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  display: block;
  border-radius: 20px;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.section-item:hover .section-item__image {
  transform: scale(1.05);
}
.category-content {
  padding: 1rem 0.75rem;
}
.category-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: #1a1a1a;
}
.category-description {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
  line-height: 1.4;
}
.section-nav-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  font-size: 18px;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 300;
}
.section-nav-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.12);
  transform: scale(1.05);
}
.section-nav-btn:active {
  transform: scale(0.95);
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* Hero Carousel Section */
.hero-carousel-section {
  position: relative;
  overflow: hidden;
  height: 70vh;
  min-height: 600px;
  max-height: 800px;
  padding-top: 0;
  margin-top: 0;
  padding: 0;
  width: 100%;
}

.carousel-inner {
  display: flex;
  height: 100%;
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
  height: 100%;
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
  height: 100%;
  overflow: hidden;
}

.hero-slide {
  height: 100%;
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
  padding: 1rem 0;
}

.hero-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.badge-new {
  display: inline-block;
  background: rgba(205, 127, 50, 0.15);
  color: var(--auro-secondary);
  padding: 0.375rem 0.875rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 1.5rem;
  border: 1px solid rgba(205, 127, 50, 0.3);
}

.hero-title {
  font-family: var(--auro-display-font);
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.15;
  margin-bottom: 1.25rem;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
}

.hero-description {
  font-size: 1.05rem;
  line-height: 1.75;
  margin-bottom: 1.5rem;
  color: rgba(255, 255, 255, 0.9);
  max-width: 500px;
}

.hero-promo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
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
  width: 32px;
  height: 3px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  border: none;
  margin: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.carousel-indicators button.active {
  background: rgba(255, 255, 255, 0.95);
  width: 48px;
}

.carousel-control-prev,
.carousel-control-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 50%;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

.carousel-control-prev {
  left: 2rem;
}

.carousel-control-next {
  right: 2rem;
}

.carousel-control-prev:hover,
.carousel-control-next:hover {
  background: rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-50%) scale(1.05);
}

.carousel-control-prev i,
.carousel-control-next i {
  font-size: 1.25rem;
  color: white;
  font-weight: 300;
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
  margin-bottom: 2rem;
}

.category-filter-btn {
  padding: 10px 28px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  color: #666;
  font-weight: 500;
  font-size: 0.875rem;
  border-radius: 999px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.category-filter-btn.active {
  background: rgba(26, 26, 26, 0.95);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.category-filter-btn:hover:not(.active) {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.12);
  color: #1a1a1a;
}

.categories-carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
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

  .prev-btn,
  .next-btn {
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
