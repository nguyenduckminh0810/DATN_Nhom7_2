<template>
  <div class="product-detail-page">
    <!-- Breadcrumb -->
    <div class="container-fluid">
      <nav aria-label="breadcrumb" class="breadcrumb-nav">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/" class="breadcrumb-link">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/category" class="breadcrumb-link">Danh mục</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">{{ product?.name || 'Sản phẩm' }}</li>
        </ol>
      </nav>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="container-fluid py-5">
      <div class="row">
        <div class="col-12">
          <div class="loading-container">
            <div class="spinner-border text-primary mb-3" role="status">
              <span class="visually-hidden">Đang tải...</span>
            </div>
            <h5>Đang tải thông tin sản phẩm...</h5>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Not Found -->
    <div v-else-if="!product" class="container-fluid py-5">
      <div class="row">
        <div class="col-12">
          <div class="error-container">
            <i class="ph-warning-circle text-danger"></i>
            <h4 class="mt-3 mb-3">Không tìm thấy sản phẩm</h4>
            <p class="text-muted mb-4">Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
            <router-link to="/category" class="btn btn-primary">
              <i class="ph-arrow-left me-2"></i>Quay lại danh sách
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Product Content -->
    <div v-else class="container-fluid">
      <!-- Product Hero Section -->
      <div class="product-hero">
        <div class="row g-4">
          <!-- Product Images -->
          <div class="col-lg-6">
            <ProductImageGallery
              :images="productImages"
              :product-name="product.name"
              :video-url="product.videoUrl"
              @play-video="handleVideoPlay"
            />
          </div>

          <!-- Product Info -->
          <div class="col-lg-6">
            <ProductInfo
              :product="product"
              v-model:selected-color="selectedColor"
              v-model:selected-size="selectedSize"
              @add-to-cart="handleAddToCart"
            />
          </div>
        </div>
      </div>

      <!-- Product Tabs -->
      <div class="product-tabs-section">
        <div class="tabs-navigation">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            class="tab-button"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            <i :class="tab.icon"></i>
            {{ tab.name }}
          </button>
        </div>

        <div class="tab-content">
          <!-- Description Tab -->
          <div v-if="activeTab === 'description'" class="tab-pane active">
            <ProductDescription :product="product" />
          </div>

          <!-- Reviews Tab -->
          <div v-if="activeTab === 'reviews'" class="tab-pane active">
            <ProductReviews :product="product" />
          </div>

          <!-- Shipping Tab -->
          <div v-if="activeTab === 'shipping'" class="tab-pane active">
            <ProductShipping :product="product" />
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <div class="related-products-section">
        <div class="section-header">
          <h3 class="section-title">
            <i class="ph-heart"></i>
            Sản phẩm liên quan
          </h3>
        </div>
        
        <div class="related-products-grid">
          <div 
            v-for="relatedProduct in relatedProducts" 
            :key="relatedProduct.id"
            class="related-product-card"
          >
            <div class="product-image">
              <LazyImage
                :src="relatedProduct.image"
                :alt="relatedProduct.name"
                :width="300"
                :height="300"
                :quality="85"
                :webp="true"
                :lazy="true"
                container-class="image-container"
                image-class="product-img"
              />
              <div class="product-badges">
                <span v-if="relatedProduct.discount" class="discount-badge">
                  -{{ relatedProduct.discount }}%
                </span>
              </div>
            </div>
            
            <div class="product-info">
              <h4 class="product-name">{{ relatedProduct.name }}</h4>
              <div class="product-price">
                <span class="current-price">{{ formatPrice(relatedProduct.price) }}</span>
                <span v-if="relatedProduct.originalPrice" class="original-price">
                  {{ formatPrice(relatedProduct.originalPrice) }}
                </span>
              </div>
              <router-link 
                :to="`/product/${relatedProduct.id}`" 
                class="view-details-btn"
              >
                Xem chi tiết
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Video Modal -->
    <div v-if="showVideoModal" class="video-modal-overlay" @click="closeVideoModal">
      <div class="video-modal-content" @click.stop>
        <button class="video-modal-close" @click="closeVideoModal">
          <i class="ph-x"></i>
        </button>
        <video v-if="currentVideoUrl" controls autoplay>
          <source :src="currentVideoUrl" type="video/mp4">
        </video>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductStore } from '../stores/product'
import { useToast } from '../composables/useToast'

// Components
import ProductImageGallery from '../components/product/ProductImageGallery.vue'
import ProductInfo from '../components/product/ProductInfo.vue'
import ProductDescription from '../components/product/ProductDescription.vue'
import ProductReviews from '../components/product/ProductReviews.vue'
import ProductShipping from '../components/product/ProductShipping.vue'
import LazyImage from '../components/common/LazyImage.vue'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const { showToast } = useToast()

// Reactive data
const isLoading = ref(true)
const product = ref(null)
const relatedProducts = ref([])
const selectedColor = ref('')
const selectedSize = ref('')
const activeTab = ref('description')
const showVideoModal = ref(false)
const currentVideoUrl = ref('')

// Tabs configuration
const tabs = [
  { id: 'description', name: 'Mô tả sản phẩm', icon: 'ph-info' },
  { id: 'reviews', name: 'Đánh giá', icon: 'ph-star' },
  { id: 'shipping', name: 'Giao hàng', icon: 'ph-truck' }
]

// Computed properties
const productImages = computed(() => {
  if (!product.value) return []
  const images = [product.value.image]
  if (product.value.hoverImage) images.push(product.value.hoverImage)
  if (product.value.images) images.push(...product.value.images)
  return images.filter(Boolean)
})

// Methods
const fetchProductDetail = async (productId) => {
  try {
    isLoading.value = true
    
    // Try API first
    const result = await productStore.fetchProductById(productId)
    
    if (result.success && result.data?.product) {
      product.value = result.data.product
      await fetchRelatedProducts(productId)
    } else {
      // Fallback mock data
      await loadMockData(productId)
    }
  } catch (err) {
    console.warn('API error, using mock data:', err.message)
    await loadMockData(productId)
  } finally {
    isLoading.value = false
  }
}

const loadMockData = async (productId) => {
  // Mock product data
  product.value = {
    id: productId,
    name: 'Áo thun nam cao cấp AURO',
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&h=600&fit=crop',
    hoverImage: 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=600&h=600&fit=crop',
    price: 299000,
    originalPrice: 399000,
    discount: 25,
    rating: 4.8,
    reviewCount: 128,
    material: 'Cotton 100%',
    origin: 'Việt Nam',
    warranty: '60 ngày',
    sku: `AURO-${productId}`,
    colorOptions: ['#000000', '#ffffff', '#ff0000', '#007bff'],
    sizes: ['S', 'M', 'L', 'XL', '2XL', '3XL'],
    availableSizes: ['S', 'M', 'L', 'XL'],
    colorSizeMapping: {
      '#000000': ['S', 'M', 'L', 'XL'],
      '#ffffff': ['M', 'L', 'XL', '2XL'],
      '#ff0000': ['L', 'XL', '2XL', '3XL'],
      '#007bff': ['S', 'M', 'L', 'XL', '2XL']
    },
    features: [
      {
        title: 'Chất liệu cao cấp',
        description: 'Cotton 100% mềm mại, thoáng khí, không gây kích ứng da',
        icon: 'ph-leaf'
      },
      {
        title: 'Thiết kế hiện đại',
        description: 'Kiểu dáng trẻ trung, phù hợp với nhiều phong cách thời trang',
        icon: 'ph-palette'
      },
      {
        title: 'Bền đẹp lâu dài',
        description: 'Công nghệ giặt tẩy tiên tiến, giữ màu sắc và form dáng bền lâu',
        icon: 'ph-shield-check'
      }
    ]
  }
  
  await fetchRelatedProducts(productId)
}

const fetchRelatedProducts = async (productId) => {
  try {
    const result = await productStore.fetchRelatedProducts(productId)
    
    if (result.success && result.data?.products) {
      relatedProducts.value = result.data.products
    } else {
      // Fallback mock data
      relatedProducts.value = [
        {
          id: 2,
          name: 'Quần jogger nữ cao cấp',
          image: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=300&h=300&fit=crop',
          price: 199000,
          originalPrice: 299000,
          discount: 33
        },
        {
          id: 3,
          name: 'Áo khoác da nam biker',
          image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=300&h=300&fit=crop',
          price: 599000,
          originalPrice: 799000,
          discount: 25
        },
        {
          id: 4,
          name: 'Váy đầm nữ dự tiệc',
          image: 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=300&h=300&fit=crop',
          price: 399000,
          originalPrice: 499000,
          discount: 20
        },
        {
          id: 5,
          name: 'Áo sơ mi nam công sở',
          image: 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=300&h=300&fit=crop',
          price: 249000,
          originalPrice: 349000,
          discount: 29
        }
      ]
    }
  } catch (err) {
    console.warn('Error fetching related products:', err.message)
  }
}

const handleVideoPlay = (videoUrl) => {
  currentVideoUrl.value = videoUrl
  showVideoModal.value = true
}

const closeVideoModal = () => {
  showVideoModal.value = false
  currentVideoUrl.value = ''
}

const handleAddToCart = (cartItem) => {
  showToast('Đã thêm vào giỏ hàng thành công!', 'success')
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Lifecycle
onMounted(() => {
  const productId = route.params.id
  if (productId) {
    fetchProductDetail(productId)
  } else {
    isLoading.value = false
    router.push('/category')
  }
})

// Watch for route changes
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchProductDetail(newId)
  }
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.breadcrumb-nav {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 1rem 0;
}

.breadcrumb {
  margin: 0;
  background: none;
  padding: 0;
}

.breadcrumb-link {
  color: #007bff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

.breadcrumb-item.active {
  color: #6c757d;
}

.loading-container, .error-container {
  text-align: center;
  padding: 4rem 2rem;
}

.error-container i {
  font-size: 4rem;
}

.product-hero {
  background: white;
  border-radius: 12px;
  margin: 2rem 0;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.product-tabs-section {
  background: white;
  border-radius: 12px;
  margin: 2rem 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.tabs-navigation {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  padding: 0 2rem;
}

.tab-button {
  background: none;
  border: none;
  padding: 1rem 1.5rem;
  font-weight: 600;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-button:hover {
  color: #007bff;
  background: #f8f9fa;
}

.tab-button.active {
  color: #007bff;
  border-bottom-color: #007bff;
  background: #f8f9fa;
}

.tab-content {
  padding: 2rem;
}

.tab-pane {
  display: none;
}

.tab-pane.active {
  display: block;
}

.related-products-section {
  background: white;
  border-radius: 12px;
  margin: 2rem 0;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.section-header {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
}

.section-title i {
  color: #dc3545;
}

.related-products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.related-product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.related-product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.product-image {
  position: relative;
  overflow: hidden;
}

.image-container {
  width: 100%;
  height: 250px;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.related-product-card:hover .product-img {
  transform: scale(1.05);
}

.product-badges {
  position: absolute;
  top: 1rem;
  right: 1rem;
}

.discount-badge {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.product-info {
  padding: 1.5rem;
}

.product-name {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.4;
  height: 2.8rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 1rem;
}

.current-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #dc3545;
  margin-right: 0.5rem;
}

.original-price {
  font-size: 1rem;
  color: #6c757d;
  text-decoration: line-through;
}

.view-details-btn {
  display: block;
  width: 100%;
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  text-decoration: none;
  text-align: center;
  padding: 0.75rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
  color: white;
}

/* Video Modal */
.video-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.video-modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.video-modal-close {
  position: absolute;
  top: -3rem;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  z-index: 10000;
}

video {
  width: 100%;
  height: auto;
  border-radius: 8px;
}

/* Responsive */
@media (max-width: 768px) {
  .product-hero {
    margin: 1rem 0;
    padding: 1rem;
  }
  
  .product-tabs-section, .related-products-section {
    margin: 1rem 0;
    padding: 1rem;
  }
  
  .tabs-navigation {
    padding: 0 1rem;
    overflow-x: auto;
  }
  
  .tab-button {
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
    white-space: nowrap;
  }
  
  .tab-content {
    padding: 1rem;
  }
  
  .related-products-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }
  
  .section-title {
    font-size: 1.25rem;
  }
}
</style>
