<template>
  <div 
    class="product-card section-item" 
    itemscope 
    itemtype="https://schema.org/Product"
    :data-product-id="props.id"
    @mouseleave="handleCardMouseLeave"
  >
    <!-- Image Section - Separate and larger -->
    <div class="product-image-section">
      <div class="image-wrapper">
        <div 
          class="product-image-container"
        >
        <!-- Clickable Image for Navigation -->
        <LazyImage 
          :src="img" 
          :alt="name"
          :aspect-ratio="1"
          class="product-image section-item__image"
          itemprop="image"
          @click="navigateToDetail"
          @load="handleImageLoad"
        />
        <!-- Hover Image -->
        <LazyImage 
          v-if="hoverImg"
          :src="hoverImg" 
          :alt="name"
          :aspect-ratio="1"
          class="product-image-hover"
          @click="navigateToDetail"
        />
        <!-- Wishlist Button -->
        <button class="wishlist-button" @click="toggleWishlist" :class="{ active: isInWishlist }">
          <i class="ph-heart-fill" v-if="isInWishlist"></i>
          <i class="ph-heart" v-else></i>
        </button>
        
        <div v-if="discount" class="discount-badge">
          -{{ discount }}%
        </div>
        <!-- Promotional Badge -->
        <div v-if="promotionalBadge" class="promotional-badge">
          {{ promotionalBadge }}
        </div>
        
        </div>
      </div>
    </div>
    
    <!-- Product Info Section - Separate and smaller -->
    <div class="product-info-section">
      <!-- Clickable Product Name for Navigation -->
      <h3 class="product-name" itemprop="name" @click="navigateToDetail">{{ name }}</h3>
      <div class="product-price">
        <span class="price-current" itemprop="offers" itemscope itemtype="https://schema.org/Offer">
          <span itemprop="price" :content="priceNow">{{ formatPrice(priceNow) }}</span>
          <span itemprop="priceCurrency" content="VND">₫</span>
        </span>
        <span v-if="priceOld" class="price-old">{{ formatPrice(priceOld) }}₫</span>
        <span v-if="discount" class="discount-text">-{{ discount }}%</span>
      </div>
      
      <!-- Color Options -->
      <div v-if="colorOptions && colorOptions.length > 0" class="color-options">
        <div 
          v-for="color in colorOptions" 
          :key="color"
          class="color-swatch"
          :class="{ 'selected': selectedColor === color }"
          :style="{ backgroundColor: color }"
          @click="selectColorForPreview(color)"
          :title="getColorName(color)"
        ></div>
      </div>
      
      <!-- Action Buttons -->
      <div class="product-actions">
        <button 
          class="btn-detail"
          @click="navigateToDetail"
        >
          <i class="ph-eye me-1"></i>
          Xem chi tiết
        </button>
        <button 
          class="btn-add-to-cart"
          @click="handleAddToCart"
          :disabled="!hasVariants && !isInStock"
        >
          <i class="ph-shopping-cart me-1"></i>
          {{ addToCartText }}
        </button>
      </div>
    </div>

    <!-- Variant Modal -->
    <VariantModal
      :is-open="showVariantModal"
      :product="productData"
      @close="closeVariantModal"
      @add-to-cart="handleAddToCartSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import useCart from '../../composables/useCart'
import { useProductStore } from '../../stores/product'
import { useCartStore } from '../../stores/cart'
import VariantModal from './VariantModal.vue'
import LazyImage from '../common/LazyImage.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  name: {
    type: String,
    required: true
  },
  img: {
    type: String,
    required: true
  },
  hoverImg: {
    type: String,
    default: null
  },
  priceNow: {
    type: Number,
    required: true
  },
  priceOld: {
    type: Number,
    default: null
  },
  discount: {
    type: Number,
    default: null
  },
  promotionalBadge: {
    type: String,
    default: null
  },
  colorOptions: {
    type: Array,
    default: () => []
  },
  sizes: {
    type: Array,
    default: () => ['S', 'M', 'L', 'XL', '2XL', '3XL']
  },
  availableSizes: {
    type: Array,
    default: () => ['S', 'M', 'L', 'XL', '2XL', '3XL']
  },
  colorSizeMapping: {
    type: Object,
    default: () => ({})
  },
  stock: {
    type: Number,
    default: 10
  }
})

const router = useRouter()
const { addToCartWithValidation, trackAddToCart } = useCart()

// Local state
const showVariantModal = ref(false)
const selectedColor = ref(null)

// Store instances
const productStore = useProductStore()
const cartStore = useCartStore()

// Watch for modal state changes
watch(showVariantModal, (newValue) => {
  // Modal state management
})

// Computed properties
const hasVariants = computed(() => {
  return (props.colorOptions && props.colorOptions.length > 0) || 
         (props.sizes && props.sizes.length > 0)
})

const isInStock = computed(() => {
  return props.stock > 0
})

const productData = computed(() => ({
  id: props.id,
  name: props.name,
  image: props.img,
  price: props.priceNow,
  originalPrice: props.priceOld,
  discount: props.discount,
  colorOptions: props.colorOptions,
  sizes: props.sizes,
  colorSizeMapping: props.colorSizeMapping,
  stock: props.stock
}))

const addToCartText = computed(() => {
  if (!isInStock.value) return 'Hết hàng'
  if (hasVariants.value) return 'Chọn size'
  return 'Thêm vào giỏ'
})

// Wishlist computed
const isInWishlist = computed(() => {
  return productStore.isInWishlist(props.id)
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN').format(price)
}

const getColorName = (color) => {
  const colorNames = {
    '#ffffff': 'Trắng',
    '#000000': 'Đen', 
    '#007bff': 'Xanh dương',
    '#dc3545': 'Đỏ',
    '#28a745': 'Xanh lá',
    '#808080': 'Xám',
    '#8b4513': 'Nâu',
    '#000080': 'Xanh navy',
    '#dc143c': 'Đỏ đậm',
    '#228b22': 'Xanh rừng'
  }
  return colorNames[color] || color
}

const navigateToDetail = () => {
  // Navigate to product detail page
  router.push(`/product/${props.id}`)
}

const selectColorForPreview = (color) => {
  selectedColor.value = color
}

const closeVariantModal = () => {
  showVariantModal.value = false
}

// Add to cart method
const handleAddToCart = () => {
  if (!isInStock.value) return
  
  if (hasVariants.value) {
    // Open variant modal for products with variants
    showVariantModal.value = true
  } else {
    // Direct add to cart for products without variants
    const productData = {
      id: props.id,
      name: props.name,
      price: props.priceNow,
      image: props.img,
      stock: props.stock
    }
    
    const success = addToCartWithValidation(productData, 1)
    if (success) {
      trackAddToCart(productData)
    }
  }
}

const handleAddToCartSuccess = (variantData) => {
  // This is called when variant modal successfully adds to cart
  closeVariantModal()
}


const handleImageLoad = () => {
  // Track image load performance
  // This could be used for analytics or performance monitoring
}

const handleCardMouseLeave = () => {
  // Close variant modal if open
  if (showVariantModal.value) {
    showVariantModal.value = false
  }
}


// Lifecycle hooks
onMounted(() => {
  // Add global event listener to handle clicks outside
  document.addEventListener('click', handleGlobalClick)
  document.addEventListener('mouseleave', handleGlobalMouseLeave)
})

onUnmounted(() => {
  showVariantModal.value = false
  
  // Remove global event listeners
  document.removeEventListener('click', handleGlobalClick)
  document.removeEventListener('mouseleave', handleGlobalMouseLeave)
})

// Global event handlers
const handleGlobalClick = (event) => {
  // Close modal if clicking outside the card
  const card = document.querySelector(`[data-product-id="${props.id}"]`)
  if (card && !card.contains(event.target)) {
    showVariantModal.value = false
  }
}

const handleGlobalMouseLeave = (event) => {
  // Close modal when mouse leaves viewport
  if (event.target === document.documentElement) {
    showVariantModal.value = false
  }
}

// Wishlist methods
const toggleWishlist = (event) => {
  // Prevent event bubbling
  event.preventDefault()
  event.stopPropagation()
  
  const product = {
    id: props.id,
    name: props.name,
    image: props.img,
    price: props.priceNow,
    originalPrice: props.priceOld,
    discount: props.discount
  }
  
  console.log('🎯 Toggle wishlist for product:', product.id, product.name)
  console.log('🎯 Current wishlist items:', productStore.wishlistItems.map(item => item.id))
  
  const success = productStore.toggleWishlist(product)
  
  console.log('🎯 Toggle result:', success)
  console.log('🎯 Wishlist after toggle:', productStore.wishlistItems.map(item => item.id))
  console.log('🎯 Is in wishlist:', productStore.isInWishlist(props.id))
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 0 0 calc((100vw - 120px) / 5); /* 5 sản phẩm chia đều chiều ngang */
  max-width: 400px;
  min-height: 650px; /* Giảm từ 700px vì product-info nhỏ hơn */
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* Image Section - Separate and larger */
.product-image-section {
  flex: 1; /* Chiếm phần lớn không gian */
  display: flex;
  flex-direction: column;
}

/* Image wrapper to contain all overlays */
.image-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
  flex: 1;
  isolation: isolate; /* Create new stacking context */
}

.product-image-container {
  position: relative;
  height: 580px; /* Tăng từ 550px để ảnh lớn hơn */
  overflow: hidden;
  flex: 1; /* Chiếm toàn bộ không gian của section */
  transition: none; /* Prevent conflicts */
}

.product-image {
  width: 100%;
  height: 580px; /* Tăng từ 550px */
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.product-image-hover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 580px;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.product-image-container:hover .product-image {
  opacity: 0;
}

.product-image-container:hover .product-image-hover {
  opacity: 1;
}


.discount-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
  color: white;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  z-index: 10; /* Tăng z-index */
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
  border: 2px solid white;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.promotional-badge {
  position: absolute;
  bottom: 0.75rem;
  left: 0.75rem;
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  max-width: 85%;
  line-height: 1.2;
  z-index: 10; /* Tăng z-index */
  box-shadow: 0 2px 8px rgba(184, 134, 11, 0.3);
  border: 2px solid white;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  transition: all 0.3s ease;
  cursor: pointer;
}

/* Hover effects cho badges */
.product-card:hover .discount-badge {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
}

.product-card:hover .promotional-badge {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(184, 134, 11, 0.4);
  background: linear-gradient(135deg, #DAA520 0%, #FFD700 100%);
}

/* Wishlist Button */
.wishlist-button {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid white;
  background: rgba(255, 255, 255, 0.9);
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.wishlist-button:hover {
  background: rgba(255, 255, 255, 1);
  color: #dc3545;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.wishlist-button.active {
  background: #dc3545;
  color: white;
  border-color: #dc3545;
}

.wishlist-button.active:hover {
  background: #c82333;
  border-color: #c82333;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
}

.wishlist-button i {
  font-size: 18px;
  transition: all 0.3s ease;
}

.wishlist-button:hover i {
  transform: scale(1.1);
}

.wishlist-button.active i {
  animation: heartBeat 0.6s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  25% { transform: scale(1.2); }
  50% { transform: scale(1); }
  75% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* Product Info Section - Separate and smaller */
.product-info-section {
  flex: 0 0 auto; /* Không co giãn, kích thước cố định */
  padding: 1rem; /* Giảm padding */
  display: flex;
  flex-direction: column;
  gap: 0.5rem; /* Giảm gap */
  background: #fff;
  border-top: 1px solid #f0f0f0; /* Đường phân cách */
}

.product-name {
  font-size: 1rem; /* Giảm từ 1.25rem */
  font-weight: 600;
  color: #212529;
  margin-bottom: 0; /* Bỏ margin vì đã có gap */
  line-height: 1.3; /* Giảm line-height */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2rem; /* Giảm từ 2.5rem */
}

.product-price {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* Giảm từ 0.75rem */
  margin-bottom: 0; /* Bỏ margin vì đã có gap */
}

.price-current {
  font-size: 1.25rem; /* Giảm từ 1.75rem */
  font-weight: 700;
  color: #dc3545;
  line-height: 1.2;
}

.price-old {
  font-size: 1rem; /* Giảm từ 1.25rem */
  color: #6c757d;
  text-decoration: line-through;
}

.discount-text {
  font-size: 0.875rem; /* Giảm từ 1rem */
  color: #dc3545;
  font-weight: 600;
}

.color-options {
  display: flex;
  gap: 0.5rem; /* Giảm từ 0.75rem */
  margin-top: 0; /* Bỏ margin vì đã có gap */
}

.color-swatch {
  width: 32px;
  height: 16px;
  border-radius: 8px;
  border: 2px solid #e9ecef;
  cursor: pointer;
  transition: all 0.2s ease;
}

.color-swatch:hover {
  border-color: #3b82f6;
  transform: scale(1.05);
}

.color-swatch.selected {
  border-color: #3b82f6;
  border-width: 3px;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
  transform: scale(1.05);
}


/* Product Actions */
.product-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-detail,
.btn-add-to-cart {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
}

.btn-detail {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #e9ecef;
}

.btn-detail:hover {
  background: #e9ecef;
  color: #495057;
  transform: translateY(-1px);
}

.btn-add-to-cart {
  background: #007bff;
  color: white;
}

.btn-add-to-cart:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
}

.btn-add-to-cart:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Clickable elements */
.product-name {
  cursor: pointer;
  transition: color 0.3s ease;
}

.product-name:hover {
  color: #007bff;
}

.product-image,
.product-image-hover {
  cursor: pointer;
}

/* Color swatch improvements */
.color-swatch {
  position: relative;
}

.color-swatch::after {
  content: attr(title);
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
  z-index: 10;
}

.color-swatch:hover::after {
  opacity: 1;
}

/* Desktop Optimized Product Cards - Compact */
.product-card {
  flex: 0 0 calc((100vw - 120px) / 5); /* 5 sản phẩm trên desktop */
  max-width: 280px;
  min-width: 260px;
  min-height: 480px; /* Giảm từ 680px */
  background: white;
  border-radius: 16px; /* Giảm từ 20px */
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.product-card:hover {
  transform: translateY(-12px) scale(1.03);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.15);
  border-color: rgba(184, 134, 11, 0.2);
}

.product-image-container {
  height: 380px; /* Giảm từ 600px */
  overflow: hidden;
  position: relative;
  z-index: 1; /* Đảm bảo container có z-index */
}

.product-image {
  height: 380px; /* Giảm từ 600px */
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-info-section {
  padding: 1rem; /* Giảm từ 1.5rem */
  background: white;
  border-top: 1px solid #f0f0f0;
}

.product-name {
  font-size: 1rem; /* Giảm từ 1.125rem */
  font-weight: 700;
  color: #212529;
  margin-bottom: 0.5rem; /* Giảm từ 0.75rem */
  line-height: 1.3;
  min-height: 2rem; /* Giảm từ 2.5rem */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  margin-bottom: 0.75rem; /* Giảm từ 1rem */
}

.price-current {
  font-size: 1.25rem; /* Giảm từ 1.5rem */
  font-weight: 800;
  color: #dc3545;
}

.price-old {
  font-size: 1rem; /* Giảm từ 1.125rem */
  color: #6c757d;
  text-decoration: line-through;
  margin-left: 0.5rem; /* Giảm từ 0.75rem */
}

.discount-text {
  font-size: 0.875rem; /* Giảm từ 1rem */
  color: #dc3545;
  font-weight: 700;
  margin-left: 0.25rem; /* Giảm từ 0.5rem */
}

.color-options {
  margin-bottom: 0.75rem; /* Giảm từ 1.25rem */
  gap: 0.5rem; /* Giảm từ 0.75rem */
}

.color-swatch {
  width: 36px;
  height: 18px;
  border-radius: 10px;
  border: 3px solid #e9ecef;
  cursor: pointer;
  transition: all 0.3s ease;
}

.color-swatch:hover {
  border-color: #B8860B;
  transform: scale(1.1);
}

.color-swatch.selected {
  border-color: #B8860B;
  border-width: 4px;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.3);
  transform: scale(1.1);
}

.product-actions {
  gap: 0.5rem; /* Giảm từ 0.75rem */
}

.btn-detail,
.btn-add-to-cart {
  padding: 0.625rem 0.875rem; /* Giảm từ 0.75rem 1rem */
  border-radius: 10px; /* Giảm từ 12px */
  font-size: 0.85rem; /* Giảm từ 0.9rem */
  font-weight: 600; /* Giảm từ 700 */
  transition: all 0.3s ease;
  border: 2px solid;
}

.btn-detail {
  background: #f8f9fa;
  color: #6c757d;
  border-color: #e9ecef;
}

.btn-detail:hover {
  background: #e9ecef;
  color: #495057;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-add-to-cart {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  border-color: #B8860B;
}

.btn-add-to-cart:hover:not(:disabled) {
  background: linear-gradient(135deg, #DAA520 0%, #FFD700 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(184, 134, 11, 0.4);
}

.btn-add-to-cart:disabled {
  background: #6c757d;
  border-color: #6c757d;
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
