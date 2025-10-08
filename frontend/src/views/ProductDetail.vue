<template>
  <div class="product-detail">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="py-3 bg-light">
      <div class="container">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <router-link to="/" class="text-decoration-none">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/category" class="text-decoration-none">Sản phẩm</router-link>
          </li>
          <li class="breadcrumb-item active">{{ product.name }}</li>
        </ol>
      </div>
    </nav>

    <div class="container py-5">
      <!-- Loading State -->
      <div v-if="isLoading" class="row">
        <div class="col-12">
          <div class="text-center py-5">
            <div class="spinner-border text-primary mb-3" role="status">
              <span class="visually-hidden">Đang tải...</span>
            </div>
            <h5>Đang tải thông tin sản phẩm...</h5>
          </div>
        </div>
      </div>

      <!-- Product Not Found -->
      <div v-else-if="!product" class="row">
        <div class="col-12">
          <div class="text-center py-5">
            <i class="ph-warning text-danger" style="font-size: 4rem;"></i>
            <h4 class="mt-3 mb-3">Không tìm thấy sản phẩm</h4>
            <p class="text-muted mb-4">Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
            <router-link to="/category" class="btn btn-auro-primary">
              <i class="ph-arrow-left me-2"></i>Quay lại danh sách
            </router-link>
          </div>
        </div>
      </div>

      <!-- Product Content -->
      <div v-else class="row g-5">
        <!-- Product Images -->
        <div class="col-lg-6">
          <ImageGallery
            :images="productImages"
            :initial-index="0"
            :main-image-width="600"
            :main-image-height="600"
            :thumbnail-width="80"
            :thumbnail-height="80"
            @image-change="handleImageChange"
            @lightbox-open="handleLightboxOpen"
            @lightbox-close="handleLightboxClose"
          />
        </div>

        <!-- Product Info -->
        <div class="col-lg-6">
          <div class="product-info">
            <!-- Product Title -->
            <h1 class="product-title mb-3">{{ product.name }}</h1>

            <!-- Product Rating -->
            <div class="product-rating mb-3">
              <div class="stars">
                <i class="ph-star-fill text-warning"></i>
                <i class="ph-star-fill text-warning"></i>
                <i class="ph-star-fill text-warning"></i>
                <i class="ph-star-fill text-warning"></i>
                <i class="ph-star text-warning"></i>
              </div>
              <span class="rating-text ms-2">4.5 (128 đánh giá)</span>
            </div>

            <!-- Product Price -->
            <div class="product-price mb-4">
              <div class="current-price">
                <span class="price-amount">{{ formatPrice(product.price) }}</span>
                <span v-if="product.originalPrice" class="original-price ms-2">
                  {{ formatPrice(product.originalPrice) }}
                </span>
              </div>
              <div v-if="product.discount" class="discount-badge">
                <span class="badge bg-danger">-{{ product.discount }}%</span>
              </div>
            </div>

            <!-- Product Description -->
            <div class="product-description mb-4">
              <h6 class="mb-2">Mô tả sản phẩm</h6>
              <p class="text-muted">{{ product.description }}</p>
            </div>

            <!-- Product Variants -->
            <div class="product-variants mb-4">
              <!-- Size Selection -->
              <div class="variant-group mb-3">
                <h6 class="mb-2">
                  Kích thước
                  <span v-if="selectedSize" class="text-primary ms-2">- {{ selectedSize }}</span>
                  <span v-if="selectedColor" class="text-muted ms-2" style="font-size: 0.875rem;">
                    ({{ availableSizesForColor.length }} size khả dụng)
                  </span>
                </h6>
                <div class="size-options">
                  <button
                    v-for="size in availableSizes"
                    :key="size"
                    class="btn btn-outline-secondary size-btn"
                    :class="{ 
                      active: selectedSize === size,
                      disabled: !isSizeAvailable(size)
                    }"
                    :disabled="!isSizeAvailable(size)"
                    @click="selectSize(size)"
                  >
                    {{ size }}
                    
                  </button>
                </div>
              </div>

              <!-- Color Selection -->
              <div class="variant-group mb-3">
                <h6 class="mb-2">
                  Màu sắc
                  <span v-if="selectedColor" class="text-primary ms-2">- {{ getColorName(selectedColor) }}</span>
                </h6>
                <div class="color-options">
                  <button
                    v-for="color in availableColors"
                    :key="color"
                    class="btn color-btn"
                    :class="{ 
                      active: selectedColor === color,
                      disabled: !isColorAvailable(color)
                    }"
                    :style="{ backgroundColor: getColorCode(color) }"
                    :title="getColorName(color)"
                    :disabled="!isColorAvailable(color)"
                    @click="selectColor(color)"
                  >
                    <i v-if="selectedColor === color" class="ph-check" 
                       :style="{ color: color === '#ffffff' ? '#000' : '#fff' }"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- Quantity Selection -->
            <div class="quantity-selection mb-4">
              <h6 class="mb-2">Số lượng</h6>
              <div class="quantity-controls">
                <button class="btn btn-outline-secondary" @click="decreaseQuantity" :disabled="quantity <= 1">
                  <i class="ph-minus"></i>
                </button>
                <input
                  type="number"
                  class="form-control quantity-input"
                  :value="displayQuantity"
                  @input="updateQuantity"
                  min="1"
                  :max="currentVariantStock"
                  placeholder="1"
                />
                <button class="btn btn-outline-secondary" @click="increaseQuantity" :disabled="quantity >= currentVariantStock">
                  <i class="ph-plus"></i>
                </button>
              </div>
              <small class="text-muted" v-if="selectedColor && selectedSize">
                <span v-if="currentVariantStock > 0">
                  Còn {{ currentVariantStock }} sản phẩm ({{ getColorName(selectedColor) }} - {{ selectedSize }})
                </span>
                <span v-else class="text-danger">
                  <i class="ph-warning me-1"></i>Hết hàng
                </span>
              </small>
              <small class="text-muted" v-else-if="totalStock > 0">
                Tổng còn {{ totalStock }} sản phẩm (chọn màu và size để xem chi tiết)
              </small>
              <small class="text-danger" v-else>
                <i class="ph-warning me-1"></i>Sản phẩm tạm hết hàng
              </small>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons mb-4">
              <div class="d-flex gap-3">
                <button
                  class="btn btn-auro-primary flex-grow-1"
                  @click="addToCart"
                  :disabled="!canAddToCart"
                  :class="{ 'btn-disabled': !canAddToCart }"
                >
                  <i class="ph-shopping-cart me-2"></i>
                  <span v-if="!selectedColor && !selectedSize">Chọn màu và size</span>
                  <span v-else-if="!selectedColor">Chọn màu sắc</span>
                  <span v-else-if="!selectedSize">Chọn kích thước</span>
                  <span v-else-if="currentVariantStock <= 0">Hết hàng</span>
                  <span v-else>Thêm vào giỏ hàng</span>
                </button>
                <WishlistButton
                  :product="product"
                  variant="button"
                  size="lg"
                  :show-text="true"
                />
              </div>
            </div>

            <!-- Product Features -->
            <div class="product-features">
              <div class="feature-item">
                <i class="ph-truck text-primary me-2"></i>
                <span>Miễn phí vận chuyển cho đơn hàng từ 500.000đ</span>
              </div>
              <div class="feature-item">
                <i class="ph-arrow-clockwise text-success me-2"></i>
                <span>Đổi trả trong 30 ngày</span>
              </div>
              <div class="feature-item">
                <i class="ph-shield-check text-info me-2"></i>
                <span>Bảo hành chính hãng</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Tabs -->
      <div class="row mt-5">
        <div class="col-12">
          <div class="product-tabs">
            <ul class="nav nav-tabs" role="tablist">
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link active"
                  :class="{ active: activeTab === 'description' }"
                  @click="activeTab = 'description'"
                  type="button"
                >
                  Mô tả chi tiết
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === 'specifications' }"
                  @click="activeTab = 'specifications'"
                  type="button"
                >
                  Thông số kỹ thuật
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === 'reviews' }"
                  @click="activeTab = 'reviews'"
                  type="button"
                >
                  Đánh giá (128)
                </button>
              </li>
            </ul>
            <div class="tab-content">
              <div v-if="activeTab === 'description'" class="tab-pane active">
                <div class="p-4">
                  <h5>Mô tả chi tiết</h5>
                  <p>{{ product.description }}</p>
                  <p>Áo sơ mi nam cao cấp với thiết kế tinh tế, phù hợp cho mọi dịp từ công sở đến dạo phố. Chất liệu cotton 100% mềm mại, thoáng mát, thấm hút mồ hôi tốt.</p>
                  <ul>
                    <li>Chất liệu: Cotton 100%</li>
                    <li>Kiểu dáng: Regular fit</li>
                    <li>Màu sắc: Đa dạng</li>
                    <li>Phù hợp: Công sở, dạo phố</li>
                  </ul>
                </div>
              </div>
              <div v-if="activeTab === 'specifications'" class="tab-pane active">
                <div class="p-4">
                  <h5>Thông số kỹ thuật</h5>
                  <table class="table table-striped">
                    <tbody>
                      <tr>
                        <td>Chất liệu</td>
                        <td>Cotton 100%</td>
                      </tr>
                      <tr>
                        <td>Kiểu dáng</td>
                        <td>Regular fit</td>
                      </tr>
                      <tr>
                        <td>Màu sắc</td>
                        <td>Đa dạng</td>
                      </tr>
                      <tr>
                        <td>Xuất xứ</td>
                        <td>Việt Nam</td>
                      </tr>
                      <tr>
                        <td>Bảo hành</td>
                        <td>6 tháng</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div v-if="activeTab === 'reviews'" class="tab-pane active">
                <div class="p-4">
                  <h5>Đánh giá sản phẩm</h5>
                  <div class="reviews-summary mb-4">
                    <div class="row">
                      <div class="col-md-6">
                        <div class="rating-overview">
                          <div class="average-rating">
                            <span class="rating-number">4.5</span>
                            <div class="stars">
                              <i class="ph-star-fill text-warning"></i>
                              <i class="ph-star-fill text-warning"></i>
                              <i class="ph-star-fill text-warning"></i>
                              <i class="ph-star-fill text-warning"></i>
                              <i class="ph-star text-warning"></i>
                            </div>
                            <p class="text-muted">Dựa trên 128 đánh giá</p>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="rating-breakdown">
                          <div class="rating-bar">
                            <span>5 sao</span>
                            <div class="progress">
                              <div class="progress-bar" style="width: 70%"></div>
                            </div>
                            <span>70%</span>
                          </div>
                          <div class="rating-bar">
                            <span>4 sao</span>
                            <div class="progress">
                              <div class="progress-bar" style="width: 20%"></div>
                            </div>
                            <span>20%</span>
                          </div>
                          <div class="rating-bar">
                            <span>3 sao</span>
                            <div class="progress">
                              <div class="progress-bar" style="width: 5%"></div>
                            </div>
                            <span>5%</span>
                          </div>
                          <div class="rating-bar">
                            <span>2 sao</span>
                            <div class="progress">
                              <div class="progress-bar" style="width: 3%"></div>
                            </div>
                            <span>3%</span>
                          </div>
                          <div class="rating-bar">
                            <span>1 sao</span>
                            <div class="progress">
                              <div class="progress-bar" style="width: 2%"></div>
                            </div>
                            <span>2%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="reviews-list">
                    <div class="review-item">
                      <div class="review-header">
                        <div class="reviewer-info">
                          <strong>Nguyễn Văn A</strong>
                          <div class="stars">
                            <i class="ph-star-fill text-warning"></i>
                            <i class="ph-star-fill text-warning"></i>
                            <i class="ph-star-fill text-warning"></i>
                            <i class="ph-star-fill text-warning"></i>
                            <i class="ph-star-fill text-warning"></i>
                          </div>
                        </div>
                        <small class="text-muted">2 ngày trước</small>
                      </div>
                      <p class="review-content">Sản phẩm rất tốt, chất lượng cao, giao hàng nhanh. Tôi rất hài lòng!</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <div class="row mt-5">
        <div class="col-12">
          <div class="related-products-section">
            <h4 class="section-title mb-4">
              <i class="ph-heart me-2"></i>
              Sản phẩm liên quan
            </h4>
            <div class="row g-4">
              <div class="col-md-6 col-lg-3" v-for="relatedProduct in relatedProducts" :key="relatedProduct.id">
                <div class="card product-card h-100 shadow-sm">
                  <div class="position-relative product-image-container">
                    <LazyImage
                      :src="relatedProduct.image"
                      :alt="relatedProduct.name"
                      :width="500"
                      :height="500"
                      :quality="85"
                      :webp="true"
                      :lazy="true"
                      container-class="product-image"
                      image-class="card-img-top"
                      show-zoom-overlay="true"
                    />
                    <div class="position-absolute top-0 end-0 m-2">
                      <span class="badge discount-badge">-{{ relatedProduct.discount }}%</span>
                    </div>
                    <div class="position-absolute top-0 start-0 m-2">
                      <WishlistButton :product="relatedProduct" variant="icon" size="sm" />
                    </div>
                    <div class="product-overlay">
                      <button class="btn btn-primary btn-sm" @click="addToCart(relatedProduct)">
                        <i class="ph-shopping-cart me-1"></i>Thêm vào giỏ
                      </button>
                    </div>
                  </div>
                  <div class="card-body d-flex flex-column">
                    <h6 class="card-title fw-bold mb-3 text-truncate">{{ relatedProduct.name }}</h6>
                    
                    <!-- Price Section -->
                    <div class="price-section mb-3">
                      <div class="price-current fw-bold text-primary">{{ formatPrice(relatedProduct.price) }}</div>
                      <div class="price-original text-muted small text-decoration-line-through">
                        {{ formatPrice(relatedProduct.originalPrice) }}
                      </div>
                    </div>
                    
                    <!-- Action Button -->
                    <div class="card-actions mt-auto">
                      <router-link 
                        :to="`/product/${relatedProduct.id}`" 
                        class="btn btn-primary btn-sm w-100 card-action-btn"
                      >
                        <i class="ph-eye me-1"></i>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useProductStore } from '../stores/product'
import ImageGallery from '../components/common/ImageGallery.vue'
import LazyImage from '../components/common/LazyImage.vue'
import WishlistButton from '../components/product/WishlistButton.vue'
import { 
  getColorName, 
  getColorHex,
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

// Local state
const isLoading = ref(true)
const error = ref(null)
const product = ref(null)
const relatedProducts = ref([])
const activeTab = ref('description')
const selectedSize = ref('')
const selectedColor = ref('')
const quantity = ref(1)

// API Functions
const fetchProductDetail = async (productId) => {
  try {
    isLoading.value = true
    error.value = null
    
    const result = await productStore.fetchProductById(productId)
    
    if (result.success && result.data?.product) {
      product.value = result.data.product
      
      // Fetch related products
      await fetchRelatedProducts(productId)
    } else {
      // Fallback mock data for development when API is not ready
      console.warn('API not available, using mock data for Product Detail')
      product.value = {
        id: productId,
        name: 'Áo thun nam cao cấp',
        description: 'Áo thun nam chất liệu cotton cao cấp, thoáng mát, thấm hút mồ hôi tốt. Thiết kế đơn giản nhưng thanh lịch, phù hợp cho mọi hoạt động hàng ngày.',
        price: 399000,
        priceNow: 299000,
        originalPrice: 399000,
  discount: 25,
        promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
        img: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
        image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
  images: [
          'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
          'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=800&h=800&fit=crop',
          'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&h=800&fit=crop'
        ],
        colors: [
          { name: 'Đỏ', value: '#dc3545', available: true },
          { name: 'Xanh dương', value: '#007bff', available: true },
          { name: 'Xanh lá', value: '#28a745', available: true }
        ],
        sizes: [
          { name: 'S', available: true },
          { name: 'M', available: true },
          { name: 'L', available: true },
          { name: 'XL', available: true },
          { name: '2XL', available: true }
        ],
        variants: [
          { color: '#dc3545', size: 'S', stock: 10 },
          { color: '#dc3545', size: 'M', stock: 15 },
          { color: '#dc3545', size: 'L', stock: 8 },
          { color: '#007bff', size: 'M', stock: 12 },
          { color: '#007bff', size: 'L', stock: 20 },
          { color: '#007bff', size: 'XL', stock: 5 },
          { color: '#28a745', size: 'L', stock: 7 },
          { color: '#28a745', size: 'XL', stock: 18 },
          { color: '#28a745', size: '2XL', stock: 3 }
        ],
        category: 'Áo thun',
        brand: 'AURO',
        rating: 4.5,
        reviewCount: 128,
        inStock: true,
        tags: ['cotton', 'thoáng mát', 'nam']
      }
      
      // Fetch related products with fallback
      await fetchRelatedProducts(productId)
    }
  } catch (err) {
    // Fallback mock data for development
    console.warn('API error, using mock data for Product Detail:', err.message)
    product.value = {
      id: productId,
      name: 'Áo thun nam cao cấp',
      description: 'Áo thun nam chất liệu cotton cao cấp, thoáng mát, thấm hút mồ hôi tốt. Thiết kế đơn giản nhưng thanh lịch, phù hợp cho mọi hoạt động hàng ngày.',
      price: 399000,
      priceNow: 299000,
      originalPrice: 399000,
      discount: 25,
      promotionalBadge: 'MUA 2 GIẢM THÊM 15%',
      img: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
      image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
      images: [
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
        'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=800&h=800&fit=crop',
        'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&h=800&fit=crop'
      ],
      colors: [
        { name: 'Đỏ', value: '#dc3545', available: true },
        { name: 'Xanh dương', value: '#007bff', available: true },
        { name: 'Xanh lá', value: '#28a745', available: true }
      ],
      sizes: [
        { name: 'S', available: true },
        { name: 'M', available: true },
        { name: 'L', available: true },
        { name: 'XL', available: true },
        { name: '2XL', available: true }
      ],
      variants: [
        { color: '#dc3545', size: 'S', stock: 10 },
        { color: '#dc3545', size: 'M', stock: 15 },
        { color: '#dc3545', size: 'L', stock: 8 },
        { color: '#007bff', size: 'M', stock: 12 },
        { color: '#007bff', size: 'L', stock: 20 },
        { color: '#007bff', size: 'XL', stock: 5 },
        { color: '#28a745', size: 'L', stock: 7 },
        { color: '#28a745', size: 'XL', stock: 18 },
        { color: '#28a745', size: '2XL', stock: 3 }
      ],
      category: 'Áo thun',
      brand: 'AURO',
      rating: 4.5,
      reviewCount: 128,
      inStock: true,
      tags: ['cotton', 'thoáng mát', 'nam']
    }
    
    // Fetch related products with fallback
    await fetchRelatedProducts(productId)
  } finally {
    isLoading.value = false
  }
}

const fetchRelatedProducts = async (productId) => {
  try {
    const result = await productStore.fetchRelatedProducts(productId)
    
    if (result.success && result.data?.products) {
      relatedProducts.value = result.data.products
    } else {
      // Fallback mock data for related products
      console.warn('API not available, using mock data for Related Products')
      relatedProducts.value = [
        {
          id: 2,
          name: 'Quần jogger nữ cao cấp',
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
          name: 'Áo khoác da nam biker',
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
          name: 'Váy đầm nữ dự tiệc',
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
          name: 'Áo sơ mi nam công sở',
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
    // Fallback mock data for related products
    console.warn('API error, using mock data for Related Products:', err.message)
    relatedProducts.value = [
      {
        id: 2,
        name: 'Quần jogger nữ cao cấp',
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
        name: 'Áo khoác da nam biker',
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
  }
}

// No more mock data - using API

// Computed: Dynamic color-size mapping from variants
const colorSizeMapping = computed(() => {
  if (!product.value?.variants) return {}
  return buildColorSizeMapping(product.value.variants)
})

// Computed: Available colors (hex codes) with stock
const availableColors = computed(() => {
  if (!product.value?.variants) return []
  return getAvailableColors(product.value.variants)
})

// Computed: Available sizes with stock
const availableSizes = computed(() => {
  if (!product.value?.variants) return []
  return getAvailableSizes(product.value.variants)
})

// Computed: Available sizes for selected color
const availableSizesForColor = computed(() => {
  if (!selectedColor.value || !colorSizeMapping.value) {
    return availableSizes.value
  }
  return colorSizeMapping.value[selectedColor.value] || []
})

// Computed: Available colors for selected size
const availableColorsForSize = computed(() => {
  if (!selectedSize.value || !colorSizeMapping.value) {
    return availableColors.value
  }
  return availableColors.value.filter(color => 
    colorSizeMapping.value[color]?.includes(selectedSize.value)
  )
})

// Computed: Current variant stock
const currentVariantStock = computed(() => {
  if (!selectedColor.value || !selectedSize.value || !product.value?.variants) {
    return getTotalStock(product.value?.variants || [])
  }
  return getVariantStock(product.value.variants, selectedColor.value, selectedSize.value)
})

// Computed: Total stock
const totalStock = computed(() => {
  return getTotalStock(product.value?.variants || [])
})

// Computed: Check if can add to cart
const canAddToCart = computed(() => {
  // Must have color and size selected
  if (!selectedColor.value || !selectedSize.value) return false
  // Must have stock
  if (currentVariantStock.value <= 0) return false
  // Quantity must be valid
  if (quantity.value < 1 || quantity.value > currentVariantStock.value) return false
  return true
})

// Computed
const displayQuantity = computed(() => {
  return quantity.value || 1
})

const productImages = computed(() => {
  if (!product.value) return []
  
  return product.value.images.map((src, index) => ({
    src,
    alt: `${product.value.name} - Hình ${index + 1}`
  }))
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getColorCode = (hexColor) => {
  // Color is already in hex format
  return hexColor
}

const selectSize = (size) => {
  // Check if size is available for current color
  if (selectedColor.value && !availableSizesForColor.value.includes(size)) {
    return // Don't select unavailable size
  }
  selectedSize.value = size
  
  // Reset quantity if exceeds new stock
  if (quantity.value > currentVariantStock.value) {
    quantity.value = Math.min(quantity.value, currentVariantStock.value)
  }
}

const selectColor = (color) => {
  // Check if color is available
  if (!availableColors.value.includes(color)) {
    return // Don't select unavailable color
  }
  
  selectedColor.value = color
  
  // Reset size if current size is not available for new color
  if (selectedSize.value && !availableSizesForColor.value.includes(selectedSize.value)) {
    selectedSize.value = ''
  }
  
  // Reset quantity
  if (quantity.value > currentVariantStock.value) {
    quantity.value = Math.min(1, currentVariantStock.value)
  }
}

// Check if size is available
const isSizeAvailable = (size) => {
  if (!selectedColor.value) {
    // No color selected, check if size exists in any variant
    return availableSizes.value.includes(size)
  }
  // Color selected, check if size is available for that color
  return availableSizesForColor.value.includes(size)
}

// Check if color is available
const isColorAvailable = (color) => {
  if (!selectedSize.value) {
    // No size selected, check if color has any stock
    return availableColors.value.includes(color)
  }
  // Size selected, check if color is available for that size
  return availableColorsForSize.value.includes(color)
}

const increaseQuantity = () => {
  const maxStock = currentVariantStock.value || 10
  if (quantity.value < maxStock) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
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
  // Image changed event - can be used for analytics
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

// Lifecycle
onMounted(() => {
  const productId = route.params.id
  
  if (productId) {
    fetchProductDetail(productId)
  } else {
    error.value = 'ID sản phẩm không hợp lệ'
    isLoading.value = false
    if (window.$toast) {
      window.$toast.error('ID sản phẩm không hợp lệ', 'Lỗi')
    }
  }
})
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
  font-size: 1.2rem;
  color: #6c757d;
  text-decoration: line-through;
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
  position: relative;
}

.color-btn.disabled::after {
  content: '✕';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #dc3545;
  font-weight: bold;
  font-size: 1.2rem;
  text-shadow: 0 0 3px white;
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
</style>
