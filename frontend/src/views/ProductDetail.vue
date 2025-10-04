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
                <h6 class="mb-2">Kích thước</h6>
                <div class="size-options">
                  <button
                    v-for="size in availableSizes"
                    :key="size"
                    class="btn btn-outline-secondary size-btn"
                    :class="{ active: selectedSize === size }"
                    @click="selectSize(size)"
                  >
                    {{ size }}
                  </button>
                </div>
              </div>

              <!-- Color Selection -->
              <div class="variant-group mb-3">
                <h6 class="mb-2">Màu sắc</h6>
                <div class="color-options">
                  <button
                    v-for="color in availableColors"
                    :key="color"
                    class="btn color-btn"
                    :class="{ active: selectedColor === color }"
                    :style="{ backgroundColor: getColorCode(color) }"
                    :title="color"
                    @click="selectColor(color)"
                  >
                    <i v-if="selectedColor === color" class="ph-check text-white"></i>
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
                  v-model.number="quantity"
                  min="1"
                  :max="product.stock || 10"
                />
                <button class="btn btn-outline-secondary" @click="increaseQuantity" :disabled="quantity >= (product.stock || 10)">
                  <i class="ph-plus"></i>
                </button>
              </div>
              <small class="text-muted">Còn {{ product.stock || 10 }} sản phẩm</small>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons mb-4">
              <div class="d-flex gap-3">
                <button
                  class="btn btn-auro-primary flex-grow-1"
                  @click="addToCart"
                  :disabled="!selectedSize || !selectedColor"
                >
                  <i class="ph-shopping-cart me-2"></i>Thêm vào giỏ hàng
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
          <h4 class="mb-4">Sản phẩm liên quan</h4>
          <div class="row g-4">
            <div class="col-md-6 col-lg-3" v-for="relatedProduct in relatedProducts" :key="relatedProduct.id">
              <div class="card product-card h-100">
                <div class="position-relative product-image-container">
                  <OptimizedProductImage
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
                  <div class="position-absolute top-0 end-0 m-3">
                    <span class="badge modern-discount-badge">-{{ relatedProduct.discount }}%</span>
                  </div>
                  <div class="position-absolute top-0 start-0 m-3">
                    <WishlistButton :product="relatedProduct" variant="icon" size="sm" />
                  </div>
                  <div class="product-overlay">
                    <button class="btn btn-auro-primary btn-sm" @click="addToCart(relatedProduct)">
                      <i class="ph-shopping-cart me-1"></i>Thêm vào giỏ
                    </button>
                  </div>
                </div>
                <div class="card-body d-flex flex-column">
                  <h6 class="card-title fw-bold mb-2">{{ relatedProduct.name }}</h6>
                  <p class="card-text text-muted small flex-grow-1 mb-3">{{ relatedProduct.description }}</p>
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="price h6 mb-0">{{ formatPrice(relatedProduct.price) }}</span>
                    <router-link :to="`/product/${relatedProduct.id}`" class="btn btn-outline-primary btn-sm">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'
import ImageGallery from '../components/ui/ImageGallery.vue'
import OptimizedProductImage from '../components/ui/OptimizedProductImage.vue'
import WishlistButton from '../components/ui/WishlistButton.vue'

const route = useRoute()
const cartStore = useCartStore()

// Local state
const isLoading = ref(true)
const product = ref(null)
const activeTab = ref('description')
const selectedSize = ref('')
const selectedColor = ref('')
const quantity = ref(1)

// Mock data
const mockProduct = {
  id: 1,
  name: 'Áo sơ mi nam cao cấp',
  description: 'Áo sơ mi nam chất liệu cotton 100%, phong cách lịch lãm, phù hợp đi làm, đi chơi.',
  price: 450000,
  originalPrice: 600000,
  discount: 25,
  stock: 10,
  images: [
    'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80',
    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80'
  ]
}

const relatedProducts = ref([
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
  },
  {
    id: 5,
    name: 'Quần jean nam',
    description: 'Quần jean nam phong cách trẻ trung',
    price: 550000,
    originalPrice: 700000,
    discount: 21,
    image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
  }
])

const availableSizes = ref(['S', 'M', 'L', 'XL', 'XXL'])
const availableColors = ref(['Đen', 'Trắng', 'Xám', 'Xanh navy'])

// Computed
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

const getColorCode = (colorName) => {
  const colorMap = {
    'Đen': '#212529',
    'Trắng': '#f8f9fa',
    'Xám': '#6c757d',
    'Xanh navy': '#001f3f',
  }
  return colorMap[colorName] || '#ccc'
}

const selectSize = (size) => {
  selectedSize.value = size
}

const selectColor = (color) => {
  selectedColor.value = color
}

const increaseQuantity = () => {
  if (quantity.value < (product.value?.stock || 10)) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = (productToAdd = product.value) => {
  if (!productToAdd) return
  
  const cartItem = {
    ...productToAdd,
    selectedSize: selectedSize.value,
    selectedColor: selectedColor.value,
    quantity: quantity.value
  }
  
  cartStore.addItem(cartItem)
  
  // Show success toast
  if (window.$toast) {
    window.$toast.success(
      `${productToAdd.name} đã được thêm vào giỏ hàng`,
      'Thêm vào giỏ hàng thành công'
    )
  }
}

const handleImageChange = (data) => {
  console.log('Image changed:', data)
}

const handleLightboxOpen = (data) => {
  console.log('Lightbox opened:', data)
}

const handleLightboxClose = () => {
  console.log('Lightbox closed')
}

// Lifecycle
onMounted(() => {
  // Simulate loading
  setTimeout(() => {
    product.value = mockProduct
    isLoading.value = false
  }, 1000)
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

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 150px;
}

.quantity-input {
  text-align: center;
  border-radius: 8px;
}

.action-buttons .btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
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
}
</style>
