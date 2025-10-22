<template>
  <div class="product-detail-page">
    <!-- Breadcrumb Section -->
    <nav aria-label="breadcrumb" class="breadcrumb-section">
      <div class="container">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <router-link to="/" class="breadcrumb-link">
              <i class="bi bi-house-door me-1"></i>Trang chủ
            </router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/category" class="breadcrumb-link">Sản phẩm</router-link>
          </li>
          <li class="breadcrumb-item active">{{ product?.name || 'Loading...' }}</li>
        </ol>
      </div>
    </nav>

      <!-- Loading State -->
    <div v-if="isLoading" class="loading-state">
          <div class="text-center py-5">
            <div class="spinner-border text-primary mb-3" role="status">
              <span class="visually-hidden">Đang tải...</span>
            </div>
            <h5>Đang tải thông tin sản phẩm...</h5>
        </div>
      </div>

      <!-- Product Not Found -->
    <div v-else-if="!product" class="error-state">
          <div class="text-center py-5">
            <i class="bi bi-exclamation-triangle text-danger" style="font-size: 4rem;"></i>
            <h4 class="mt-3 mb-3">Không tìm thấy sản phẩm</h4>
            <p class="text-muted mb-4">Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <router-link to="/category" class="btn btn-primary">
              <i class="bi bi-arrow-left me-2"></i>Quay lại danh sách
            </router-link>
        </div>
      </div>

    <!-- Hero Section (max-width 1320px, centered) -->
    <section v-else class="hero-section">
      <div class="hero-container">
        <div class="hero-grid">
          <!-- Sticky Gallery Column -->
          <div class="gallery-column">
            <div class="gallery-wrapper">
              <div class="product-gallery">
          <ImageGallery
            :images="productImages"
            :initial-index="currentImageIndex"
            :key="`gallery-${currentImageIndex}`"
            :main-image-width="600"
            :main-image-height="600"
            :thumbnail-width="80"
            :thumbnail-height="80"
                  :show-navigation="true"
                  :show-thumbnails="true"
                  :show-counter="true"
                  :show-zoom-button="true"
            @image-change="handleImageChange"
            @lightbox-open="handleLightboxOpen"
            @lightbox-close="handleLightboxClose"
          />
              </div>
            </div>
        </div>

          <!-- Product Summary Column -->
          <div class="summary-column">
            <div class="product-summary">
              <!-- Product Badge -->
              <div v-if="product?.discount" class="product-badge mb-3">
                <span class="badge-modern">{{ product?.discount }}% OFF</span>
              </div>

            <!-- Product Title -->
              <h1 class="product-title">{{ product?.name || 'Đang tải...' }}</h1>

            <!-- Product Rating -->
            <div class="product-rating mb-3">
              <div class="stars">
                  <i v-for="i in 5" :key="i" 
                     :class="['bi', i <= Math.floor(product?.rating) ? 'bi-star-fill' : 'bi-star']"></i>
              </div>
                <span class="rating-text">
                  <strong>{{ product?.rating }}</strong>
                  <span class="rating-count">({{ product?.reviewCount }} đánh giá)</span>
                </span>
            </div>

            <!-- Product Price -->
            <div class="product-price mb-4">
                <div class="price-container">
                  <span class="current-price">{{ formatPrice(currentPrice) }}</span>
                  <span v-if="product?.originalPrice && product?.originalPrice > currentPrice" 
                        class="original-price">{{ formatPrice(product?.originalPrice) }}</span>
              </div>
            </div>


            <!-- Product Description -->
              <div class="product-description-modern mb-4">
                <h6 class="section-title-modern">Mô tả sản phẩm</h6>
                <p class="description-text">{{ product?.description || 'Đang tải mô tả...' }}</p>
            </div>

            <!-- Product Variants -->
            <div class="product-variants mb-4">
                <!-- Color Selection -->
              <div class="variant-group mb-3">
                  <label class="variant-label">Màu sắc</label>
                  <div class="color-options">
                    <button v-for="color in availableColors" :key="color"
                            :class="['color-btn', { 
                              'selected': selectedColor === color,
                              'disabled': selectedSize && !isColorAvailableForSize(color)
                            }]"
                            :style="{ backgroundColor: color }"
                            @click="selectColor(color)"
                            :disabled="selectedSize && !isColorAvailableForSize(color)"
                            :title="getColorName(color)"
                            :aria-pressed="selectedColor === color">
                      <i v-if="selectedColor === color" class="bi bi-check check-icon"></i>
                  </button>
                    <span v-if="selectedColor" class="selected-color-name">{{ getColorName(selectedColor) }}</span>
                </div>
              </div>

                <!-- Size Selection -->
              <div class="variant-group mb-3">
                  <label class="variant-label">Kích thước</label>
                  <div class="size-options">
                    <button v-for="size in availableSizes" :key="size"
                            :class="['size-btn', { 'selected': selectedSize === size, 'disabled': !isSizeAvailable(size) }]"
                            @click="selectSize(size)"
                            :disabled="!isSizeAvailable(size)"
                            :aria-pressed="selectedSize === size">
                      {{ size }}
                  </button>
                </div>
                  <a href="#bang-size" class="size-guide-link">
                    <i class="bi bi-rulers me-1"></i>Hướng dẫn chọn size
                  </a>
              </div>

              <!-- Variant Info Badge (shows when variant is selected) -->
              <div v-if="currentVariant && selectedColor && selectedSize" class="variant-info-badge">
                <div class="variant-badge-content">
                  <i class="bi bi-info-circle me-2"></i>
                  <span class="variant-text">
                    Đã chọn: <strong>{{ getColorName(selectedColor) }}</strong> - <strong>{{ selectedSize }}</strong>
                    <span v-if="currentVariant.price && currentVariant.price !== product?.price" class="variant-price-badge">
                      ({{ formatPrice(currentVariant.price) }})
                    </span>
                  </span>
                </div>
              </div>
            </div>

            <!-- Quantity Selection -->
            <div class="quantity-selection mb-4">
                <label class="variant-label">Số lượng</label>
              <div class="quantity-controls">
                  <button class="qty-btn" @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
                  <input v-model.number="quantity" type="number" class="quantity-input" min="1" :max="maxQuantity">
                  <button class="qty-btn" @click="increaseQuantity" :disabled="quantity >= maxQuantity">+</button>
              </div>
              </div>

              <!-- Stock Information -->
              <div class="stock-info mb-4">
                <div v-if="currentStock > 0" class="stock-available">
                  <i class="bi bi-check-circle-fill me-2"></i>
                  <span class="stock-text">Còn {{ currentStock }} sản phẩm</span>
                </div>
                <div v-else class="stock-unavailable">
                  <i class="bi bi-x-circle-fill me-2"></i>
                  <span class="stock-text">Hết hàng</span>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons mb-4">
                <button class="btn-add-to-cart" 
                  :disabled="!canAddToCart"
                        @click="handleAddToCart">
                  <i class="bi bi-cart-plus me-2"></i>
                  {{ canAddToCart ? 'Thêm vào giỏ hàng' : 'Chọn màu và size' }}
                </button>
                <button class="btn-buy-now" 
                        :disabled="!canAddToCart"
                        @click="handleBuyNow">
                  <i class="bi bi-lightning-fill me-2"></i>
                  Mua ngay
                </button>
            </div>

              <!-- Service Badges -->
              <div class="service-badges">
                <div class="badge-item">
                  <div class="badge-icon">
                    <i class="bi bi-truck"></i>
              </div>
                  <div class="badge-content">
                    <div class="badge-title">Miễn phí vận chuyển</div>
                    <div class="badge-subtitle">Đơn hàng từ 500.000₫</div>
              </div>
              </div>
                <div class="badge-item">
                  <div class="badge-icon">
                    <i class="bi bi-arrow-repeat"></i>
            </div>
                  <div class="badge-content">
                    <div class="badge-title">Đổi trả dễ dàng</div>
                    <div class="badge-subtitle">Trong 30 ngày</div>
          </div>
        </div>
                <div class="badge-item">
                  <div class="badge-icon">
                    <i class="bi bi-shield-check"></i>
      </div>
                  <div class="badge-content">
                    <div class="badge-title">Bảo hành chính hãng</div>
                    <div class="badge-subtitle">1 năm</div>
                </div>
              </div>
                </div>
              </div>
                            </div>
                          </div>
                        </div>

    </section>

    <!-- Anchor Navigation -->
    <nav class="anchor-nav" ref="anchorNav">
      <div class="container">
        <ul class="nav-list">
          <li><a href="#mo-ta" :class="{ active: activeSection === 'mo-ta' }">Mô tả</a></li>
          <li><a href="#chat-lieu" :class="{ active: activeSection === 'chat-lieu' }">Chất liệu</a></li>
          <li><a href="#bang-size" :class="{ active: activeSection === 'bang-size' }">Bảng size</a></li>
          <li><a href="#danh-gia" :class="{ active: activeSection === 'danh-gia' }">Đánh giá</a></li>
          <li v-if="viewedProductsStore.getRecentViewedProducts.length > 0">
            <a href="#da-xem" :class="{ active: activeSection === 'da-xem' }">Sản phẩm đã xem</a>
          </li>
          <li><a href="#lien-quan" :class="{ active: activeSection === 'lien-quan' }">Sản phẩm liên quan</a></li>
        </ul>
                      </div>
    </nav>

    <!-- Full-width Content Sections -->
    <section id="mo-ta" class="content-section">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Mô tả sản phẩm</h2>
                            </div>
        <div class="section-content">
          <p>{{ product?.description || 'Đang tải mô tả...' }}</p>
          <div class="product-details">
            <h3>Chi tiết sản phẩm</h3>
            <ul>
              <li>Chất liệu: {{ product.material || 'Cotton cao cấp' }}</li>
              <li>Xuất xứ: {{ product.origin || 'Việt Nam' }}</li>
              <li>Phong cách: {{ product.style || 'Casual, Business' }}</li>
              <li>Phù hợp: {{ product.suitable || 'Mọi lứa tuổi' }}</li>
            </ul>
                          </div>
                            </div>
                          </div>
    </section>

    <section id="chat-lieu" class="content-section alt-bg">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Chất liệu & Bảo quản</h2>
                            </div>
        <div class="section-content">
          <div class="material-info">
            <h3>Thông tin chất liệu</h3>
            <p>Chất liệu cotton cao cấp 100%, mềm mại và thoáng khí, phù hợp với khí hậu nhiệt đới.</p>
                          </div>
          <div class="care-instructions">
            <h3>Hướng dẫn bảo quản</h3>
            <ul>
              <li>Giặt máy ở nhiệt độ thường</li>
              <li>Không sử dụng chất tẩy</li>
              <li>Phơi khô tự nhiên</li>
              <li>Ủi ở nhiệt độ thấp</li>
            </ul>
                            </div>
                          </div>
                            </div>
    </section>

    <section id="bang-size" class="content-section">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Bảng size</h2>
                          </div>
        <div class="section-content">
          <div class="size-chart">
            <table class="size-table">
              <thead>
                <tr>
                  <th>Size</th>
                  <th>Chiều dài (cm)</th>
                  <th>Chiều rộng ngực (cm)</th>
                  <th>Chiều rộng vai (cm)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="size in sizeChart" :key="size.size">
                  <td>{{ size.size }}</td>
                  <td>{{ size.length }}</td>
                  <td>{{ size.chest }}</td>
                  <td>{{ size.shoulder }}</td>
                </tr>
              </tbody>
            </table>
                        </div>
                      </div>
                    </div>
    </section>

    <section id="danh-gia" class="content-section alt-bg">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Đánh giá sản phẩm</h2>
                  </div>
        <div class="section-content">
          <div class="reviews-summary">
            <div class="rating-overview">
              <div class="rating-score">
                <span class="score">{{ product?.rating }}</span>
                          <div class="stars">
                  <i v-for="i in 5" :key="i" 
                     :class="['bi', i <= Math.floor(product?.rating) ? 'bi-star-fill' : 'bi-star']"></i>
                          </div>
                <p>{{ product?.reviewCount }} đánh giá</p>
                        </div>
                      </div>
                    </div>
          <div class="reviews-list">
            <div class="review-item">
              <div class="review-header">
                <div class="reviewer-info">
                  <span class="reviewer-name">Nguyễn Văn A</span>
                  <div class="review-rating">
                    <i class="bi bi-star-fill"></i>
                    <i class="bi bi-star-fill"></i>
                    <i class="bi bi-star-fill"></i>
                    <i class="bi bi-star-fill"></i>
                    <i class="bi bi-star"></i>
                  </div>
                </div>
                <span class="review-date">15/12/2024</span>
              </div>
              <p class="review-content">Sản phẩm chất lượng tốt, vừa size, giao hàng nhanh.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Sản phẩm đã xem -->
    <section id="da-xem" class="content-section" v-if="viewedProductsStore.getRecentViewedProducts.length > 0">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Sản phẩm đã xem</h2>
          <p class="section-subtitle">Những sản phẩm bạn đã xem gần đây</p>
        </div>
        <div class="section-content">
          <div class="viewed-products">
            <div 
              v-for="viewedProduct in viewedProductsStore.getRecentViewedProducts" 
              :key="viewedProduct.id" 
              class="viewed-product-card"
              @click="router.push(`/product/${viewedProduct.id}`)"
            >
              <div class="product-image">
                <img 
                  :src="viewedProduct.image || 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=250&h=250&fit=crop'" 
                  :alt="viewedProduct.name"
                  loading="lazy"
                >
                <div class="viewed-badge">
                  <i class="bi bi-eye"></i>
                </div>
              </div>
              <div class="product-info">
                <h4>{{ viewedProduct.name }}</h4>
                <div class="price-info">
                  <span class="current-price">{{ formatPrice(viewedProduct.price) }}</span>
                  <span v-if="viewedProduct.originalPrice && viewedProduct.originalPrice > viewedProduct.price" 
                        class="original-price">{{ formatPrice(viewedProduct.originalPrice) }}</span>
                </div>
                <div v-if="viewedProduct.discount" class="discount-badge">
                  -{{ viewedProduct.discount }}%
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="lien-quan" class="content-section">
      <div class="content-wrapper">
        <div class="section-header">
          <h2>Sản phẩm liên quan</h2>
        </div>
        <div class="section-content">
          <div v-if="relatedProducts && relatedProducts.length > 0" class="related-products">
            <div 
              v-for="relatedProduct in relatedProducts" 
              :key="relatedProduct.id" 
              class="related-product-card"
            >
              <div class="product-image">
                <img 
                  :src="relatedProduct.image || 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=250&h=250&fit=crop'" 
                      :alt="relatedProduct.name"
                  loading="lazy"
                >
                    </div>
              <div class="product-info">
                <h4>{{ relatedProduct.name }}</h4>
                <p class="price">{{ formatPrice(relatedProduct.price) }}</p>
                      <router-link 
                        :to="`/product/${relatedProduct.id}`" 
                  class="btn btn-sm btn-outline-primary mt-2"
                      >
                        Xem chi tiết
                      </router-link>
                    </div>
                  </div>
                </div>
          <div v-else class="no-related-products">
            <p class="text-muted">Không có sản phẩm liên quan</p>
              </div>
            </div>
          </div>
    </section>

    <!-- Mobile Sticky Bottom Bar -->
    <div class="mobile-sticky-bar" v-if="isMobile">
      <div class="mobile-price">{{ formatPrice(currentPrice) }}</div>
      <button class="mobile-cta" :disabled="!canAddToCart" @click="handleAddToCart">
        {{ canAddToCart ? 'Thêm vào giỏ' : 'Chọn size' }}
      </button>
        </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useProductStore } from '../stores/product'
import { useViewedProductsStore } from '../stores/viewedProducts'
import ImageGallery from '../components/common/ImageGallery.vue'
import LazyImage from '../components/common/LazyImage.vue'
import { 
  getColorName, 
  buildColorSizeMapping,
  getAvailableColors,
  getAvailableSizes,
  getVariantStock,
  getTotalStock,
  isVariantAvailable
} from '../utils/colorMapping'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const productStore = useProductStore()
const viewedProductsStore = useViewedProductsStore()

// Local state
const isLoading = ref(true)
const error = ref(null)
const product = ref(null)
const relatedProducts = ref([])
const activeTab = ref('description')
const selectedSize = ref('')
const selectedColor = ref('')
const quantity = ref(1)
const currentImageIndex = ref(0)

// New reactive data for Coolmate layout
const activeSection = ref('mo-ta')
const isMobile = ref(false)
const anchorNav = ref(null)

// API Functions
const fetchProductDetail = async (productId) => {
    isLoading.value = true
    error.value = null
    
  try {
    // Fetch product detail from API
    const { default: sanPhamService } = await import('../services/sanPhamService')
    const productData = await sanPhamService.getDetail(productId)
    
    if (!productData) {
      throw new Error('Product not found')
    }
    
    // Collect all product images
    const productImages = productData.hinhAnhs?.map(img => img.url) || []
    
    // Map variants from detail response and collect variant images
    let variants = []
    if (productData.bienThes && productData.bienThes.length > 0) {
      variants = productData.bienThes.map(v => ({
        id: v.id,
        color: v.colorHex || '#000000',
        colorName: v.mauSac || 'Mặc định',
        size: v.kichThuoc || 'M',
        stock: v.tonKho || 0,
        price: v.gia || productData.gia,
        imageUrl: v.hinhAnh,
        available: (v.tonKho || 0) > 0
      }))
      
      // Add variant images to product images if not already included
      variants.forEach(variant => {
        if (variant.imageUrl && !productImages.includes(variant.imageUrl)) {
          productImages.push(variant.imageUrl)
        }
      })
    }
    
    // Map API response to product object
    product.value = {
      id: productData.id,
      name: productData.ten,
      description: productData.moTa || 'Chưa có mô tả sản phẩm',
      price: productData.gia || 0,
      originalPrice: productData.giaGoc || productData.gia,
      discount: productData.giamGia || 0,
      promotionalBadge: productData.nhanKhuyenMai || null,
      img: productData.anhDaiDien || productImages[0] || '',
      image: productData.anhDaiDien || productImages[0] || '',
      images: productImages,
      category: productData.danhMucTen || '',
      brand: 'AURO',
      rating: productData.danhGia || 4.5,
      reviewCount: productData.soLuongDanhGia || 0,
      inStock: productData.trangThai === 'active',
      tags: productData.tags || [],
      sku: productData.sku || '',
      variants: variants
    }
    
    // Continue with variants processing
    if (variants.length > 0) {
      
      // Extract unique colors and sizes from variants
      const uniqueColors = [...new Set(product.value.variants.map(v => v.color))]
      const uniqueSizes = [...new Set(product.value.variants.map(v => v.size))]
      
      product.value.colors = uniqueColors.map(color => {
        const variant = product.value.variants.find(v => v.color === color)
        return {
          name: variant?.colorName || color,
          value: color,
          available: product.value.variants.some(v => v.color === color && v.available)
        }
      })
      
      product.value.sizes = uniqueSizes.map(size => ({
        name: size,
        available: product.value.variants.some(v => v.size === size && v.available)
      }))
      
      console.log('Loaded variants:', product.value.variants.length)
      console.log('Total images (product + variants):', product.value.images.length)
      console.log('Variant images:', product.value.variants.filter(v => v.imageUrl).map(v => v.imageUrl))
    } else {
      // No variants, use default mock data
      product.value.colors = [
        { name: 'Mặc định', value: '#000000', available: true }
      ]
      product.value.sizes = [
        { name: 'M', available: true }
      ]
    }
    
  } catch (err) {
    console.error('Failed to load product:', err)
    error.value = 'Không thể tải thông tin sản phẩm'
    product.value = null
  } finally {
    isLoading.value = false
  }
  
  // Fetch related products with fallback (async, don't wait)
  if (product.value) {
    fetchRelatedProducts(productId)
  }
  
  // Debug: Log related products
  console.log('Related products after fetch:', relatedProducts.value)
}

const fetchRelatedProducts = async (productId) => {
  try {
    const result = await productStore.fetchRelatedProducts(productId)
    
    if (result.success && result.data?.products) {
      relatedProducts.value = result.data.products
    } else {
      // Fallback: use empty array instead of mock data to avoid loading unnecessary data
      console.warn('Related products API not available')
      relatedProducts.value = []
    }
  } catch (error) {
    // Silently handle error and show no related products
    console.warn('Error fetching related products:', error.message)
    relatedProducts.value = []
  }
}

// No more mock data - using API

// Computed: Dynamic color-size mapping from variants (for compatibility)
const colorSizeMapping = computed(() => {
  if (!product.value?.variants) return {}
  return buildColorSizeMapping(product.value.variants)
})

// Computed: Available sizes for selected color (for compatibility)
const availableSizesForColor = computed(() => {
  if (!selectedColor.value || !colorSizeMapping.value) {
    return availableSizes.value
  }
  return colorSizeMapping.value[selectedColor.value] || []
})

// Computed: Available colors for selected size (for compatibility)
const availableColorsForSize = computed(() => {
  if (!selectedSize.value || !colorSizeMapping.value) {
    return availableColors.value
  }
  return availableColors.value.filter(color => 
    colorSizeMapping.value[color]?.includes(selectedSize.value)
  )
})

// Computed: Current selected variant (reusable)
const currentVariant = computed(() => {
  if (!selectedColor.value || !selectedSize.value || !product.value?.variants) {
    return null
  }
  
  return product.value.variants.find(
    v => v.color === selectedColor.value && v.size === selectedSize.value
  )
})

// Computed: Current variant stock (for compatibility)
const currentVariantStock = computed(() => {
  if (!selectedColor.value || !selectedSize.value || !product.value?.variants) {
    return getTotalStock(product.value?.variants || [])
  }
  return getVariantStock(product.value.variants, selectedColor.value, selectedSize.value)
})

// Computed: Current variant price (use variant price if available, otherwise product price)
const currentPrice = computed(() => {
  if (!currentVariant.value) {
    return product.value?.price || 0
  }
  
  // Use variant price if available, otherwise fall back to product price
  return currentVariant.value.price || product.value?.price || 0
})

// Computed: Total stock (for compatibility)
const totalStock = computed(() => {
  return getTotalStock(product.value?.variants || [])
})

// Computed
const displayQuantity = computed(() => {
  return quantity.value || 1
})

// Store image-variant mapping separately
const imageVariantMap = computed(() => {
  if (!product.value || !product.value.variants) return new Map()
  
  const map = new Map()
  
  product.value.variants.forEach(variant => {
    if (variant.imageUrl) {
      if (!map.has(variant.imageUrl)) {
        map.set(variant.imageUrl, [])
      }
      map.get(variant.imageUrl).push({
        color: variant.color,
        colorName: variant.colorName,
        size: variant.size,
        variantId: variant.id,
        price: variant.price,
        stock: variant.stock
      })
    }
  })
  
  return map
})

const productImages = computed(() => {
  if (!product.value) return []
  
  // Return images in the format ImageGallery expects: { src, alt }
  const images = product.value.images.map((src, index) => ({
    src,
    alt: `${product.value.name} - Hình ${index + 1}`
  }))
  
  console.log('Product Images:', images)
  console.log('Image-Variant Map:', imageVariantMap.value)
  
  return images
})

// Computed: Active image index based on selected variant
const activeImageIndex = computed(() => {
  if (!currentVariant.value || !currentVariant.value.imageUrl || !product.value?.images) {
    return 0
  }
  
  // Find the index of the image URL in the product images array
  const index = product.value.images.findIndex(imgUrl => imgUrl === currentVariant.value.imageUrl)
  return index >= 0 ? index : 0
})

// New computed properties for Coolmate layout
const sizeChart = computed(() => {
  return [
    { size: 'S', length: 68, chest: 92, shoulder: 44 },
    { size: 'M', length: 70, chest: 96, shoulder: 46 },
    { size: 'L', length: 72, chest: 100, shoulder: 48 },
    { size: 'XL', length: 74, chest: 104, shoulder: 50 },
    { size: '2XL', length: 76, chest: 108, shoulder: 52 }
  ]
})

const currentStock = computed(() => {
  return currentVariantStock.value
})

const maxQuantity = computed(() => {
  return Math.min(currentVariantStock.value, 10)
})

const canAddToCart = computed(() => {
  return selectedColor.value && selectedSize.value && currentVariantStock.value > 0
})

const availableColors = computed(() => {
  if (!product.value?.variants) return ['Đen', 'Trắng', 'Xám', 'Xanh navy']
  return getAvailableColors(product.value.variants)
})

const availableSizes = computed(() => {
  if (!product.value?.variants) return ['S', 'M', 'L', 'XL', '2XL']
  return getAvailableSizes(product.value.variants)
})

const isSizeAvailable = (size) => {
  if (!selectedColor.value) {
    return availableSizes.value.includes(size)
  }
  return availableSizesForColor.value.includes(size)
}

const isColorAvailable = (color) => {
  if (!selectedSize.value) {
    return availableColors.value.includes(color)
  }
  return availableColorsForSize.value.includes(color)
}

const isColorAvailableForSize = (color) => {
  if (!selectedSize.value) {
    return true // Nếu chưa chọn size thì tất cả màu đều available
  }
  return availableColorsForSize.value.includes(color)
}

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// New methods for Coolmate layout
const selectColor = (color) => {
  if (!isColorAvailable(color)) {
    return
  }
  
  selectedColor.value = color
  
  // Reset size if current size is not available for new color
  if (selectedSize.value && !availableSizesForColor.value.includes(selectedSize.value)) {
    selectedSize.value = ''
  }
  
  // Update product image if variant has image
  updateVariantImage()
  
  // Reset quantity
  if (quantity.value > currentVariantStock.value) {
    quantity.value = Math.min(1, currentVariantStock.value)
  }
  
  updateURL()
}

const selectSize = (size) => {
  if (!isSizeAvailable(size)) {
    return
  }
  
  selectedSize.value = size
  
  // Reset color if current color is not available for new size
  if (selectedColor.value && !availableColorsForSize.value.includes(selectedColor.value)) {
    selectedColor.value = ''
  }
  
  // Update product image if variant has image
  updateVariantImage()
  
  // Reset quantity if exceeds new stock
  if (quantity.value > currentVariantStock.value) {
    quantity.value = Math.min(1, currentVariantStock.value)
  }
  
  updateURL()
}

const updateVariantImage = () => {
  if (!currentVariant.value) {
    console.log('No current variant selected')
    return
  }
  
  // Update main image if variant has image
  if (currentVariant.value.imageUrl) {
    // Update current image index to highlight the correct thumbnail
    const newIndex = activeImageIndex.value
    currentImageIndex.value = newIndex
    
    // Set as main image
    product.value.image = currentVariant.value.imageUrl
    product.value.img = currentVariant.value.imageUrl
    
    console.log('Updated variant image:', {
      variantImageUrl: currentVariant.value.imageUrl,
      newIndex: newIndex,
      color: currentVariant.value.colorName,
      size: currentVariant.value.size
    })
  } else {
    console.log('Current variant has no image URL')
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const increaseQuantity = () => {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
  }
}

const handleAddToCart = () => {
  if (!canAddToCart.value) return
  
  const item = {
    id: product.value.id,
    name: product.value.name,
    price: product.value.price,
    image: product.value.images[0],
    color: selectedColor.value,
    size: selectedSize.value,
    quantity: quantity.value
  }
  
  cartStore.addItem(item)
  // Show success message
}

const handleBuyNow = () => {
  if (!canAddToCart.value) return
  handleAddToCart()
  router.push('/cart')
}

const updateURL = () => {
  const params = new URLSearchParams()
  if (selectedColor.value) params.set('color', selectedColor.value)
  if (selectedSize.value) params.set('size', selectedSize.value)
  
  const newUrl = `${route.path}${params.toString() ? '?' + params.toString() : ''}`
  window.history.replaceState({}, '', newUrl)
}

const setupScrollSpy = () => {
  const sections = ['mo-ta', 'chat-lieu', 'bang-size', 'danh-gia', 'lien-quan']
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        activeSection.value = entry.target.id
      }
    })
  }, {
    threshold: 0.3,
    rootMargin: '-100px 0px -50% 0px'
  })
  
  sections.forEach(id => {
    const element = document.getElementById(id)
    if (element) {
      observer.observe(element)
    }
  })
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 992
}

const handleResize = () => {
  checkMobile()
}

// Lifecycle hooks
onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', handleResize)
  
  const productId = route.params.id
  await fetchProductDetail(productId)
  
  // Track sản phẩm đã xem
  if (product.value) {
    viewedProductsStore.addViewedProduct(product.value)
  }
  
  // Setup scroll spy after content is loaded
  await nextTick()
  setupScrollSpy()
  
  // Read URL parameters
  const urlParams = new URLSearchParams(window.location.search)
  const color = urlParams.get('color')
  const size = urlParams.get('size')
  
  if (color && availableColors.value.includes(color)) {
    selectedColor.value = color
  }
  if (size && availableSizes.value.includes(size)) {
    selectedSize.value = size
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const getColorCode = (hexColor) => {
  // Color is already in hex format
  return hexColor
}



const updateQuantity = (event) => {
  const newValue = parseInt(event.target.value) || 1
  const maxStock = currentVariantStock.value || 10
  
  // Validate the input
  if (newValue < 1) {
    quantity.value = 1
  } else if (newValue > maxStock) {
    quantity.value = maxStock
  } else {
    quantity.value = newValue
  }
  
  // Force update the input value to ensure it displays correctly
  nextTick(() => {
    if (event.target.value !== quantity.value.toString()) {
      event.target.value = quantity.value
    }
  })
}

const addToCart = (productToAdd = product.value) => {
  if (!productToAdd) {
    console.error('Product data is null or undefined')
    return
  }
  
  // Debug: Log product data
  console.log('Product data for cart:', {
    id: productToAdd.id,
    name: productToAdd.name,
    price: productToAdd.price,
    priceNow: productToAdd.priceNow,
    img: productToAdd.img,
    image: productToAdd.image
  })
  
  // Validate selections
  if (!selectedColor.value || !selectedSize.value) {
    if (window.$toast) {
      window.$toast.error('Vui lòng chọn màu sắc và kích thước', 'Thiếu thông tin')
    }
    return
  }
  
  // Check stock
  if (currentVariantStock.value <= 0) {
    if (window.$toast) {
      window.$toast.error('Sản phẩm này hiện đã hết hàng', 'Hết hàng')
    }
    return
  }
  
  // Check quantity
  if (quantity.value > currentVariantStock.value) {
    if (window.$toast) {
      window.$toast.error(
        `Chỉ còn ${currentVariantStock.value} sản phẩm trong kho`,
        'Vượt quá số lượng'
      )
    }
    return
  }
  
  // Ensure we have valid product data
  const productId = productToAdd.id || 'unknown'
  const productName = productToAdd.name || 'Sản phẩm không tên'
  const productPrice = parseFloat(productToAdd.priceNow || productToAdd.price || 0)
  const productImage = productToAdd.img || productToAdd.image || productToAdd.images?.[0] || ''
  
  const cartItem = {
    id: productId,
    name: productName,
    price: productPrice,
    image: productImage,
    size: selectedSize.value,
    color: selectedColor.value,
    selectedColorName: getColorName(selectedColor.value),
    quantity: quantity.value,
    variantStock: currentVariantStock.value,
    variantId: `${productId}_${selectedColor.value}_${selectedSize.value}`
  }
  
  console.log('Cart item being added:', cartItem)
  
  cartStore.addItem(cartItem)
  
  // Show success toast
  if (window.$toast) {
    window.$toast.success(
      `${productName} (${getColorName(selectedColor.value)} - ${selectedSize.value}) đã được thêm vào giỏ hàng`,
      'Thêm vào giỏ hàng thành công'
    )
  }
  
  // Reset selections
  selectedColor.value = ''
  selectedSize.value = ''
  quantity.value = 1
}

const handleImageChange = (data) => {
  // When user clicks on a thumbnail, automatically select the corresponding variant
  if (!data || !productImages.value || data.index < 0 || data.index >= productImages.value.length) return
  
  const clickedImage = productImages.value[data.index]
  const imageSrc = clickedImage.src
  
  // Check if this image has associated variants
  const variants = imageVariantMap.value.get(imageSrc)
  
  if (variants && variants.length > 0) {
    // Get the first variant associated with this image
    const variantInfo = variants[0]
    
    // Auto-select the color and size
    if (variantInfo.color && availableColors.value.includes(variantInfo.color)) {
      selectedColor.value = variantInfo.color
    }
    
    if (variantInfo.size && availableSizes.value.includes(variantInfo.size)) {
      selectedSize.value = variantInfo.size
    }
    
    console.log('Auto-selected variant from image:', variantInfo)
  } else {
    console.log('No variant associated with this image')
  }
}

const handleLightboxOpen = (data) => {
  // Lightbox opened event - can be used for analytics
}

const handleLightboxClose = () => {
  // Lightbox closed event
}

// Watch quantity changes
watch(quantity, (newQty) => {
  const maxStock = currentVariantStock.value || 10
  if (newQty > maxStock) {
    quantity.value = maxStock
  } else if (newQty < 1) {
    quantity.value = 1
  }
})

// Watch for color/size changes to update stock display
watch([selectedColor, selectedSize], () => {
  // Validate quantity against new stock
  if (currentVariantStock.value > 0 && quantity.value > currentVariantStock.value) {
    quantity.value = currentVariantStock.value
  }
})

// Watch for active image index changes to update current image index
watch(activeImageIndex, (newIndex) => {
  currentImageIndex.value = newIndex
  console.log('Active image index changed to:', newIndex)
})

// Watch route params to fetch product when ID changes
watch(() => route.params.id, (newId) => {
  if (newId) {
    // Reset state
    selectedColor.value = ''
    selectedSize.value = ''
    quantity.value = 1
    
    // Fetch new product
    fetchProductDetail(newId)
  }
}, { immediate: false })

</script>

<style scoped>
.product-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--auro-dark);
}

.product-rating {
  display: flex;
  align-items: center;
}

.stars {
  display: flex;
  gap: 2px;
}

.rating-text {
  color: #6c757d;
  font-size: 0.9rem;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.price-amount {
  font-size: 2rem;
  font-weight: 700;
  color: var(--auro-primary);
}

.original-price {
  font-size: 1.1rem;
  color: #6c757d;
  text-decoration: line-through;
  margin-left: 0.5rem;
  font-weight: 500;
}

.discount-badge .badge {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
}

.size-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.size-btn {
  min-width: 50px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.size-btn.active {
  background-color: var(--auro-primary);
  border-color: var(--auro-primary);
  color: white;
}

.size-btn.disabled,
.size-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  text-decoration: line-through;
  background-color: #f8f9fa;
  position: relative;
}

.unavailable-mark {
  position: absolute;
  top: -5px;
  right: -5px;
  color: #dc3545;
  font-size: 0.75rem;
  font-weight: bold;
}

.color-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.color-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.color-btn.active {
  border-color: var(--auro-primary);
  box-shadow: 0 0 0 3px rgba(var(--auro-primary-rgb), 0.3);
}

.color-btn.disabled,
.color-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  filter: grayscale(60%);
  transform: none !important;
}

.color-btn.disabled:hover,
.color-btn:disabled:hover {
  transform: none !important;
  border-color: #e9ecef !important;
}


.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 150px;
}

.quantity-input {
  text-align: center;
  border-radius: 8px;
  min-width: 60px;
  padding: 0.5rem;
  font-weight: 500;
  border: 1px solid #dee2e6;
}

.quantity-input:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
  outline: none;
}

.action-buttons .btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
}

.btn-disabled,
.btn-auro-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #6c757d !important;
  border-color: #6c757d !important;
}

.product-features {
  border-top: 1px solid #e9ecef;
  padding-top: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.product-tabs .nav-tabs {
  border-bottom: 2px solid #e9ecef;
}

.product-tabs .nav-link {
  border: none;
  border-bottom: 2px solid transparent;
  color: #6c757d;
  font-weight: 500;
  padding: 1rem 1.5rem;
  transition: all 0.3s ease;
}

.product-tabs .nav-link.active {
  color: var(--auro-primary);
  border-bottom-color: var(--auro-primary);
  background: none;
}

.product-tabs .tab-content {
  border: 1px solid #e9ecef;
  border-top: none;
  border-radius: 0 0 8px 8px;
}

.rating-overview {
  text-align: center;
}

.rating-number {
  font-size: 3rem;
  font-weight: 700;
  color: var(--auro-primary);
}

.rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.rating-bar .progress {
  flex: 1;
  height: 8px;
}

.review-item {
  border-bottom: 1px solid #e9ecef;
  padding: 1rem 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.review-content {
  margin: 0;
  color: #6c757d;
}

/* Section Subtitle */
.section-subtitle {
  color: #6c757d;
  font-size: 1rem;
  margin: 0.5rem 0 0 0;
  font-weight: 400;
}

/* Viewed Products */
.viewed-products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
  margin-top: 1rem;
}

.viewed-product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.viewed-product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.viewed-product-card .product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.viewed-product-card .product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.viewed-product-card:hover .product-image img {
  transform: scale(1.05);
}

.viewed-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  backdrop-filter: blur(10px);
}

.viewed-product-card .product-info {
  padding: 1.5rem;
}

.viewed-product-card .product-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.viewed-product-card .price-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.viewed-product-card .current-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #dc3545;
}

.viewed-product-card .original-price {
  font-size: 1rem;
  color: #6c757d;
  text-decoration: line-through;
  font-weight: 500;
}

.viewed-product-card .discount-badge {
  background: #dc3545;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  display: inline-block;
}

/* Related Products Section */
.related-products-section {
  background: #f8f9fa;
  padding: 2rem;
  border-radius: 12px;
  margin-top: 2rem;
}

.section-title {
  color: #2c3e50;
  font-weight: 600;
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 0.5rem;
}

.section-title i {
  color: #dc3545;
}

.product-card {
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.15) !important;
}

/* Enhanced Card Body */
.product-card .card-body {
  padding: 1.5rem;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
}

.product-card .card-title {
  color: #2c3e50;
  font-size: 1rem;
  line-height: 1.4;
  margin-bottom: 1rem;
  min-height: 2.8rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-image-container {
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.product-image-container img {
  transition: transform 0.3s ease;
}

.product-card:hover .product-image-container img {
  transform: scale(1.05);
}

.discount-badge {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  font-weight: 600;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.product-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  padding: 1rem;
  transform: translateY(100%);
  transition: transform 0.3s ease;
}

.product-card:hover .product-overlay {
  transform: translateY(0);
}

/* Enhanced Price Section */
.price-section {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  padding: 0.75rem;
  border: 1px solid #e9ecef;
  text-align: center;
}

.price-current {
  color: #dc3545;
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.price-original {
  font-size: 0.9rem;
  opacity: 0.7;
  margin: 0;
}

/* Enhanced Card Actions */
.card-actions {
  margin-top: 1rem;
}

.card-action-btn {
  background: linear-gradient(135deg, #007bff, #0056b3);
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,123,255,0.3);
}

.card-action-btn:hover {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,123,255,0.4);
}

.card-action-btn i {
  font-size: 1rem;
}

/* Responsive */
@media (max-width: 768px) {
  .product-title {
    font-size: 1.5rem;
  }
  
  .price-amount {
    font-size: 1.5rem;
  }
  
  .action-buttons .d-flex {
    flex-direction: column;
  }
  
  .action-buttons .btn {
    width: 100%;
  }
  
  .related-products-section {
    padding: 1rem;
  }
  
  .section-title {
    font-size: 1.25rem;
  }
  
  .product-card .card-body {
    padding: 1rem;
  }
  
  .price-section {
    padding: 0.5rem;
  }
  
  .price-current {
    font-size: 1.1rem;
  }
  
  .card-action-btn {
    padding: 0.6rem 0.8rem;
    font-size: 0.8rem;
  }
}

/* ===== COOLMATE LAYOUT STYLES ===== */

.product-detail-page {
  width: 100%;
}

/* Breadcrumb Section */
.breadcrumb-section {
  background: #f8f9fa;
  padding: 1rem 0;
  border-bottom: 1px solid #e9ecef;
}

.loading-state,
.error-state {
  width: 100%;
  padding: 2rem 3rem;
  text-align: center;
}

/* Hero Section */
.hero-section {
  padding: 2rem 0;
  background: #fff;
}

.hero-container {
  max-width: 1320px;
  margin: 0 auto;
  padding: 0 2rem;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: start;
}

/* Gallery Column */
.gallery-column {
  position: relative;
}

.gallery-wrapper {
  position: sticky;
  top: 100px; /* Header height + spacing */
  z-index: 10;
}

.product-gallery {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* Summary Column */
.summary-column {
  padding-left: 1rem;
}

.product-summary {
  position: sticky;
  top: 100px;
  z-index: 10;
}

/* ===== PRODUCT SUMMARY STYLES ===== */

/* Product Title */
.product-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.2;
}

/* Product Rating */
.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.stars {
  display: flex;
  gap: 2px;
}

.stars .bi {
  color: #ffc107;
  font-size: 1.1rem;
}

.rating-text {
  font-size: 0.95rem;
  color: #6c757d;
}

.rating-count {
  margin-left: 0.5rem;
}

/* Product Price */
.product-price {
  margin-bottom: 1.5rem;
}

.price-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.current-price {
  font-size: 2rem;
  font-weight: 700;
  color: #dc3545;
}

.original-price {
  font-size: 1.2rem;
  color: #6c757d;
  text-decoration: line-through;
}

/* Product Benefits */
.product-benefits {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.benefit-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #495057;
}

.benefit-item:last-child {
  margin-bottom: 0;
}

/* Product Variants */
.product-variants {
  margin-bottom: 1.5rem;
}

.variant-group {
  margin-bottom: 1rem;
}

.variant-label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.color-options {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.color-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #e9ecef;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-btn:hover {
  border-color: #007bff;
  transform: scale(1.1);
}

.color-btn.selected {
  border-color: #007bff;
  border-width: 3px;
}

.check-icon {
  color: white;
  font-size: 0.8rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.selected-color-name {
  margin-left: 0.5rem;
  font-weight: 500;
  color: #495057;
}

.size-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.size-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
  min-width: 50px;
  text-align: center;
}

.size-btn:hover:not(:disabled) {
  border-color: #007bff;
  color: #007bff;
}

.size-btn.selected {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.size-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.size-guide-link {
  display: inline-block;
  margin-top: 0.5rem;
  color: #007bff;
  text-decoration: none;
  font-size: 0.9rem;
}

.size-guide-link:hover {
  text-decoration: underline;
}

/* Variant Info Badge */
.variant-info-badge {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.variant-badge-content {
  display: flex;
  align-items: center;
  color: white;
  font-size: 0.95rem;
}

.variant-badge-content i {
  font-size: 1.1rem;
  opacity: 0.9;
}

.variant-text {
  flex: 1;
}

.variant-text strong {
  font-weight: 600;
  text-transform: capitalize;
}

.variant-price-badge {
  display: inline-block;
  margin-left: 0.5rem;
  padding: 0.2rem 0.5rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.9rem;
}

/* Quantity Selection */
.quantity-selection {
  margin-bottom: 1rem;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.qty-btn:hover:not(:disabled) {
  border-color: #007bff;
  color: #007bff;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 80px;
  height: 40px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  text-align: center;
  font-weight: 500;
}

.quantity-input:focus {
  outline: none;
  border-color: #007bff;
}

/* Stock Info */
.stock-info {
  margin-bottom: 1rem;
}

.stock-available {
  color: #28a745;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.stock-unavailable {
  color: #dc3545;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.stock-text {
  font-size: 0.95rem;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-add-to-cart,
.btn-buy-now {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-add-to-cart {
  background: #007bff;
  color: white;
}

.btn-add-to-cart:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.btn-buy-now {
  background: #dc3545;
  color: white;
}

.btn-buy-now:hover:not(:disabled) {
  background: #c82333;
  transform: translateY(-1px);
}

.btn-add-to-cart:disabled,
.btn-buy-now:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Service Badges */
.service-badges {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1rem;
}

@media (max-width: 992px) {
  .service-badges {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }
}

.badge-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  min-height: 80px;
}

.badge-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.badge-icon {
  width: 32px;
  height: 32px;
  background: #dc3545;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.badge-icon i {
  color: white;
  font-size: 1rem;
}

.badge-content {
  flex: 1;
  min-width: 0;
}

.badge-title {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
  margin-bottom: 0.15rem;
  line-height: 1.2;
}

.badge-subtitle {
  color: #6c757d;
  font-size: 0.8rem;
  line-height: 1.2;
}


/* ===== ANCHOR NAVIGATION ===== */

.anchor-nav {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-list {
  display: flex;
  gap: 2rem;
  list-style: none;
  margin: 0;
  padding: 0;
  justify-content: center;
}

.nav-list li a {
  color: #6c757d;
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.nav-list li a:hover,
.nav-list li a.active {
  color: #007bff;
  background: #f8f9fa;
}

/* ===== CONTENT SECTIONS ===== */

.content-section {
  padding: 3rem 0;
}

.content-section.alt-bg {
  background: #f8f9fa;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section-header {
  text-align: center;
  margin-bottom: 2rem;
}

.section-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.section-content {
  line-height: 1.6;
  color: #495057;
}

.section-content h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.section-content ul {
  padding-left: 1.5rem;
}

.section-content li {
  margin-bottom: 0.5rem;
}

/* Size Chart */
.size-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.size-table th,
.size-table td {
  padding: 1rem;
  text-align: center;
  border-bottom: 1px solid #e9ecef;
}

.size-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

/* Reviews */
.reviews-summary {
  margin-bottom: 2rem;
}

.rating-overview {
  text-align: center;
}

.rating-score .score {
  font-size: 3rem;
  font-weight: 700;
  color: #2c3e50;
}

.rating-score .stars {
  justify-content: center;
  margin: 0.5rem 0;
}

.rating-score p {
  color: #6c757d;
  margin: 0;
}

.review-item {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.reviewer-name {
  font-weight: 600;
  color: #2c3e50;
}

.review-rating {
  display: flex;
  gap: 2px;
  margin-left: 1rem;
}

.review-rating .bi {
  color: #ffc107;
  font-size: 0.9rem;
}

.review-date {
  color: #6c757d;
  font-size: 0.9rem;
}

.review-content {
  color: #495057;
  line-height: 1.5;
  margin: 0;
}

/* Related Products */
.related-products {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .viewed-products {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
  }
  
  .viewed-product-card .product-info {
    padding: 1rem;
  }
  
  .viewed-product-card .product-info h4 {
    font-size: 1rem;
  }
  
  .viewed-product-card .current-price {
    font-size: 1.1rem;
  }
  
  .related-products {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }
}

.no-related-products {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.related-product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
  display: flex;
  flex-direction: column;
}

.related-product-card:hover {
  transform: translateY(-2px);
}

.related-product-card .product-image {
  aspect-ratio: 1;
  overflow: hidden;
}

.related-product-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-product-card .product-info {
  padding: 1rem;
}

.related-product-card h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.related-product-card .price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #dc3545;
  margin: 0;
}

/* ===== MOBILE STICKY BOTTOM BAR ===== */

.mobile-sticky-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 1rem;
  border-top: 1px solid #e9ecef;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: none;
}

.mobile-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #dc3545;
  margin-bottom: 0.5rem;
}

.mobile-cta {
  width: 100%;
  height: 50px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.mobile-cta:hover:not(:disabled) {
  background: #0056b3;
}

.mobile-cta:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== RESPONSIVE DESIGN ===== */

@media (max-width: 992px) {
  .hero-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .gallery-wrapper,
  .product-summary {
    position: static;
  }
  
  .mobile-sticky-bar {
    display: block;
  }
  
  .nav-list {
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .nav-list li a {
    font-size: 0.9rem;
    padding: 0.4rem 0.8rem;
  }
}

@media (max-width: 768px) {
  .hero-container {
    padding: 0 1rem;
  }
  
  .content-wrapper {
    padding: 0 1rem;
  }
  
  .product-title {
    font-size: 1.5rem;
  }
  
  .current-price {
    font-size: 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .service-badges {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .badge-item {
    padding: 0.75rem;
    gap: 0.6rem;
    min-height: auto;
  }
  
  .badge-icon {
    width: 28px;
    height: 28px;
  }
  
  .badge-icon i {
    font-size: 0.9rem;
  }
  
  .badge-title {
    font-size: 0.85rem;
    margin-bottom: 0.1rem;
  }
  
  .badge-subtitle {
    font-size: 0.75rem;
  }
  
  .size-table {
    font-size: 0.9rem;
  }
  
  .size-table th,
  .size-table td {
    padding: 0.5rem;
  }
}

/* ===== MODERN STYLES ===== */

/* Modern Breadcrumb */
.modern-breadcrumb {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 1rem 0;
  border-bottom: 1px solid #dee2e6;
}

.breadcrumb-link {
  color: #6c757d;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.breadcrumb-link:hover {
  color: #dc3545;
}

.breadcrumb-item.active {
  color: #2c3e50;
  font-weight: 600;
}

/* Modern Product Container */
.modern-product-container {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.08);
  overflow: hidden;
  margin-top: 2rem;
}

.product-image-section {
  padding: 0;
  background: transparent;
  border-radius: 16px;
  overflow: hidden;
  max-height: 500px;
  height: auto;
}

.product-info-card {
  padding: 2.5rem;
  background: #fff;
  position: relative;
}

/* Modern Product Badge */
.product-badge {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  z-index: 10;
}

.badge-modern {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  font-weight: 700;
  font-size: 0.8rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(220,53,69,0.3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Modern Product Title */
.product-title-modern {
  font-size: 2.2rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.3;
  margin-bottom: 1.5rem;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Modern Rating */
.product-rating-modern {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.stars-modern {
  display: flex;
  gap: 2px;
}

.stars-modern i {
  color: #ffc107;
  font-size: 1.1rem;
  filter: drop-shadow(0 1px 2px rgba(255,193,7,0.3));
}

.rating-text-modern {
  font-weight: 700;
  color: #2c3e50;
  font-size: 1.1rem;
}

.rating-count {
  color: #6c757d;
  font-size: 0.9rem;
}

/* Modern Price */
.product-price-modern {
  margin-bottom: 2rem;
}

.price-container {
  display: flex;
  align-items: baseline;
  gap: 1rem;
}

.current-price-modern {
  font-size: 2.5rem;
  font-weight: 800;
  color: #dc3545;
  text-shadow: 0 2px 4px rgba(220,53,69,0.2);
}

.original-price-modern {
  font-size: 1.1rem;
  color: #6c757d;
  text-decoration: line-through;
  opacity: 0.8;
  margin-left: 0.5rem;
  font-weight: 500;
}

/* Modern Description */
.product-description-modern {
  margin-bottom: 2rem;
}

.section-title-modern {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
}

.section-title-modern::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #dc3545, #c82333);
  margin-right: 0.75rem;
  border-radius: 2px;
}

.description-text {
  color: #6c757d;
  line-height: 1.6;
  font-size: 1rem;
}

/* Modern Variants */
.product-variants-modern {
  margin-bottom: 2rem;
}

.variant-group-modern {
  margin-bottom: 1.5rem;
}

.variant-label-modern {
  display: flex;
  align-items: center;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  font-size: 1rem;
}

.variant-label-modern i {
  color: #dc3545;
  font-size: 1.1rem;
}

.selected-value {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  margin-left: auto;
  font-weight: 600;
}

/* Modern Size Options */
.size-options-modern {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.size-btn-modern {
  background: #fff;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 0.75rem 1.25rem;
  font-weight: 600;
  color: #6c757d;
  transition: all 0.3s ease;
  cursor: pointer;
  min-width: 50px;
  text-align: center;
}

.size-btn-modern:hover {
  border-color: #dc3545;
  color: #dc3545;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220,53,69,0.2);
}

.size-btn-modern.active {
  background: linear-gradient(135deg, #dc3545, #c82333);
  border-color: #dc3545;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220,53,69,0.3);
}

.size-btn-modern.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

/* Modern Color Options */
.color-options-modern {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-btn-modern {
  width: 45px;
  height: 45px;
  border: 3px solid #e9ecef;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-btn-modern:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.color-btn-modern.active {
  border-color: #dc3545;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(220,53,69,0.3);
}

.color-btn-modern.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  filter: grayscale(60%);
  transform: none !important;
}

.color-btn-modern.disabled:hover {
  transform: none !important;
  box-shadow: none !important;
}

.check-icon {
  color: white;
  font-size: 1.2rem;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

/* Modern Quantity */
.quantity-selection-modern {
  margin-bottom: 2rem;
}

.quantity-controls-modern {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 0.5rem;
  border: 2px solid #e9ecef;
  width: fit-content;
}

.qty-btn-modern {
  background: #fff;
  border: none;
  border-radius: 8px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6c757d;
}

.qty-btn-modern:hover:not(:disabled) {
  background: #dc3545;
  color: white;
  transform: scale(1.05);
}

.qty-btn-modern:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input-modern {
  background: transparent;
  border: none;
  text-align: center;
  font-weight: 700;
  font-size: 1.1rem;
  color: #2c3e50;
  width: 60px;
  outline: none;
}

.stock-info {
  margin-top: 0.75rem;
  font-size: 0.9rem;
}

.stock-available {
  color: #28a745;
  font-weight: 600;
}

.stock-unavailable {
  color: #dc3545;
  font-weight: 600;
}

.stock-info-text {
  color: #6c757d;
}

/* Modern Action Buttons */
.action-buttons-modern {
  margin-bottom: 2rem;
}

.main-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.btn-add-to-cart-modern {
  flex: 1;
  background: linear-gradient(135deg, #dc3545, #c82333);
  border: none;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(220,53,69,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-add-to-cart-modern:hover:not(:disabled) {
  background: linear-gradient(135deg, #c82333, #a71e2a);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220,53,69,0.4);
}

.btn-add-to-cart-modern:disabled,
.btn-add-to-cart-modern.btn-disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-wishlist-modern {
  background: #fff;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6c757d;
}

.btn-wishlist-modern:hover {
  border-color: #dc3545;
  color: #dc3545;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220,53,69,0.2);
}

.secondary-actions {
  width: 100%;
}

.btn-share-modern {
  width: 100%;
  background: #fff;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 0.75rem 1rem;
  color: #6c757d;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-share-modern:hover {
  border-color: #007bff;
  color: #007bff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
}

/* Modern Product Features */
.product-features-modern {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 2rem;
  border-radius: 16px;
  margin-top: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  background: #fff;
  padding: 1.5rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.feature-icon {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.feature-content h6 {
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

.feature-content p {
  color: #6c757d;
  margin: 0;
  font-size: 0.9rem;
}

/* ===== NUCLEAR OPTION - FIX IMAGE GALLERY LAYOUT ===== */

.product-image-section {
  position: relative !important;
  background: transparent !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08) !important;
  padding: 0 !important;
  margin: 0 !important;
  width: 100% !important;
  height: auto !important;
}

.product-image-section .image-gallery {
  width: 100% !important;
  height: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
  position: relative !important;
}

.product-image-section .main-image-container {
  margin-bottom: 0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  background: #f8f9fa !important;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1) !important;
  width: 100% !important;
  aspect-ratio: 1 / 1 !important;
  max-height: 400px !important;
  position: relative !important;
  padding: 0 !important;
}

.product-image-section .main-image {
  width: 100% !important;
  height: 100% !important;
  max-height: 400px !important;
  padding: 0 !important;
  margin: 0 !important;
  aspect-ratio: 1 / 1 !important;
  display: block !important;
}

.product-image-section .main-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  display: block !important;
  padding: 0 !important;
  margin: 0 !important;
  border: none !important;
  outline: none !important;
}

/* Force Navigation Arrows to Show */
.product-image-section .gallery-nav {
  position: absolute !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  background: rgba(220, 53, 69, 0.9) !important;
  color: white !important;
  border: 2px solid rgba(255,255,255,0.2) !important;
  width: 50px !important;
  height: 50px !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  z-index: 100 !important;
  font-size: 1.2rem !important;
  font-weight: bold !important;
  backdrop-filter: blur(10px) !important;
  visibility: visible !important;
  opacity: 1 !important;
  pointer-events: auto !important;
}

/* Debug: Make sure arrows are visible even when disabled */
.product-image-section .gallery-nav:disabled {
  background: rgba(108, 117, 125, 0.7) !important;
  opacity: 0.6 !important;
  cursor: not-allowed !important;
}

.product-image-section .gallery-nav-prev {
  left: 20px !important;
}

.product-image-section .gallery-nav-next {
  right: 20px !important;
}

.product-image-section .gallery-nav:hover:not(:disabled) {
  background: rgba(220, 53, 69, 1) !important;
  transform: translateY(-50%) scale(1.15) !important;
  box-shadow: 0 4px 15px rgba(220,53,69,0.4) !important;
  border-color: rgba(255,255,255,0.4) !important;
}

.product-image-section .thumbnails-container {
  margin-top: 1rem !important;
  padding: 0 1rem !important;
  max-height: 100px !important;
}

.product-image-section .thumbnail {
  width: 80px !important;
  height: 80px !important;
  overflow: hidden !important;
}

/* Force thumbnails to only show images */
.product-image-section .thumbnail img,
.product-image-section .thumbnail-img {
  display: block !important;
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

/* Hide any text or non-image content in thumbnails */
.product-image-section .thumbnail *:not(img):not(.lazy-image-container) {
  display: none !important;
  visibility: hidden !important;
}

.product-image-section .thumbnail::before,
.product-image-section .thumbnail::after,
.product-image-section .thumbnail-item::before,
.product-image-section .thumbnail-item::after {
  content: none !important;
  display: none !important;
}

/* Remove any extra spacing */
.product-image-section * {
  box-sizing: border-box !important;
}

/* Nuclear option: Remove ALL possible spacing */
.product-image-section .image-gallery,
.product-image-section .image-gallery *,
.product-image-section .main-image-container,
.product-image-section .main-image-container *,
.product-image-section .main-image,
.product-image-section .main-image *,
.product-image-section .main-image-img {
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

/* Force image to fill container completely */
.product-image-section .main-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  object-position: center !important;
  display: block !important;
  position: relative !important;
}

/* Layout Responsive */
@media (max-width: 768px) {
  .product-detail {
    padding: 1rem;
  }
  
  .loading-state,
  .error-state {
    padding: 1rem;
  }
  
  .product-layout {
    flex-direction: column;
    gap: 1rem;
  }
  
  .product-left,
  .product-right {
    flex: 1;
  }
}

/* Modern Responsive */
@media (max-width: 768px) {
  .product-title-modern {
    font-size: 1.8rem;
  }
  
  .current-price-modern {
    font-size: 2rem;
  }
  
  .main-actions {
    flex-direction: column;
  }
  
  .btn-wishlist-modern {
    width: 100%;
    height: 50px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .product-info-card {
    padding: 1.5rem;
  }
  
  .product-image-section {
    padding: 1rem;
  }
  
  .product-image-section .gallery-nav {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .product-image-section .gallery-nav-prev {
    left: 15px;
  }
  
  .product-image-section .gallery-nav-next {
    right: 15px;
  }
  
  .product-image-section .thumbnail {
    width: 60px;
    height: 60px;
  }
  
  .product-image-section .thumbnails-container {
    padding: 0 0.5rem;
  }
}
</style>
