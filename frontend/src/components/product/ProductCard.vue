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
          @mouseenter="showQuickAdd = true"
          @mouseleave="handleMouseLeave"
        >
        <!-- Clickable Image for Navigation -->
        <img 
          :src="img" 
          :alt="name"
          class="product-image section-item__image"
          itemprop="image"
          loading="lazy"
          @click="navigateToDetail"
        />
        <!-- Hover Image -->
        <img 
          v-if="hoverImg"
          :src="hoverImg" 
          :alt="name"
          class="product-image-hover"
          @click="navigateToDetail"
        />
        <div v-if="discount" class="discount-badge">
          -{{ discount }}%
        </div>
        <!-- Promotional Badge -->
        <div v-if="promotionalBadge" class="promotional-badge">
          {{ promotionalBadge }}
        </div>
        
        <!-- Quick Add Overlay -->
        <div 
          class="quick-add-overlay" 
          :class="{ 'show': showQuickAdd }"
          @click="openVariantModal"
          @mouseenter="handleOverlayMouseEnter"
          @mouseleave="handleOverlayMouseLeave"
        >
          <div class="quick-add-content">
            <i class="ph-plus-circle"></i>
            <span>Thêm nhanh</span>
          </div>
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
      :is-quick-add-mode="isQuickAddMode"
      @close="closeVariantModal"
      @add-to-cart="handleAddToCartSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import useCart from '../../composables/useCart'
import VariantModal from './VariantModal.vue'

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
const isQuickAddMode = ref(false)
const showQuickAdd = ref(false)

// Watch for modal state changes
watch(showVariantModal, (newValue) => {
  if (newValue) {
    // Hide overlay when modal opens
    showQuickAdd.value = false
  }
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

const openVariantModal = () => {
  if (!isInStock.value) return
  
  // Close any other modals first
  document.querySelectorAll('.variant-modal-overlay').forEach(modal => {
    modal.style.display = 'none'
  })
  
  isQuickAddMode.value = false
  showVariantModal.value = true
}

const closeVariantModal = () => {
  showVariantModal.value = false
  isQuickAddMode.value = false
}

const handleAddToCart = () => {
  if (!isInStock.value) return
  
  if (hasVariants.value) {
    // Open variant modal for products with variants
    isQuickAddMode.value = false
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

const handleMouseLeave = () => {
  // Force hide when leaving image container
  showQuickAdd.value = false
}

const handleCardMouseLeave = () => {
  // Force hide when leaving entire card
  showQuickAdd.value = false
  // Also close variant modal if open
  if (showVariantModal.value) {
    showVariantModal.value = false
  }
}

// Prevent hover image conflict
const handleOverlayMouseEnter = () => {
  // Only show if no modal is open
  if (!showVariantModal.value) {
    showQuickAdd.value = true
  }
}

const handleOverlayMouseLeave = () => {
  showQuickAdd.value = false
}

// Force hide overlay on mount and unmount
onMounted(() => {
  showQuickAdd.value = false
  
  // Add global event listener to hide overlay when clicking outside
  document.addEventListener('click', handleGlobalClick)
  document.addEventListener('mouseleave', handleGlobalMouseLeave)
})

onUnmounted(() => {
  showQuickAdd.value = false
  showVariantModal.value = false
  
  // Remove global event listeners
  document.removeEventListener('click', handleGlobalClick)
  document.removeEventListener('mouseleave', handleGlobalMouseLeave)
})

// Global event handlers
const handleGlobalClick = (event) => {
  // Hide overlay if clicking outside the card
  const card = document.querySelector(`[data-product-id="${props.id}"]`)
  if (card && !card.contains(event.target)) {
    showQuickAdd.value = false
  }
}

const handleGlobalMouseLeave = (event) => {
  // Hide overlay when mouse leaves viewport
  if (event.target === document.documentElement) {
    showQuickAdd.value = false
    showVariantModal.value = false
  }
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

/* Disable hover image when quick add is shown */
.product-image-container:hover .product-image {
  opacity: 0;
}

.product-image-container:hover .product-image-hover {
  opacity: 1;
}

/* Override hover image when quick add overlay is visible */
.quick-add-overlay.show ~ .product-image {
  opacity: 1 !important;
}

.quick-add-overlay.show ~ .product-image-hover {
  opacity: 0 !important;
}

.discount-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #dc3545;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.promotional-badge {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  background: #007bff;
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  max-width: 80%;
  line-height: 1.2;
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

/* Quick Add Overlay */
.quick-add-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 0 0 12px 12px;
  padding: 1rem;
  opacity: 0;
  transform: translateY(100%);
  transition: opacity 0.2s ease, transform 0.2s ease;
  cursor: pointer;
  z-index: 15;
  pointer-events: none;
  will-change: transform, opacity;
  contain: layout style paint; /* Contain the overlay */
  max-height: 100%; /* Ensure it doesn't overflow */
  max-width: 100%;
  box-sizing: border-box;
}

/* Force hide overlay when not in show state */
.quick-add-overlay:not(.show) {
  opacity: 0 !important;
  transform: translateY(100%) !important;
  pointer-events: none !important;
  visibility: hidden !important;
  display: none !important; /* Completely hide */
}

/* Only show when explicitly shown */
.quick-add-overlay.show {
  opacity: 1 !important;
  transform: translateY(0) !important;
  pointer-events: auto !important;
  visibility: visible !important;
  display: block !important; /* Ensure it's visible */
}

/* Ensure smooth transitions */
.quick-add-overlay {
  transition: opacity 0.15s ease, transform 0.15s ease, visibility 0.15s ease !important;
}

/* Hide overlay when variant modal is open */
.variant-modal-overlay ~ .quick-add-overlay {
  display: none !important;
  opacity: 0 !important;
  visibility: hidden !important;
}

.quick-add-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
}

.quick-add-content i {
  font-size: 1.25rem;
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

/* Responsive breakpoints for product cards */
@media (max-width: 1400px) {
  .product-card {
    flex: 0 0 calc((100vw - 100px) / 4); /* 4 sản phẩm */
    max-width: 350px; /* Tăng từ 300px */
  }
}

@media (max-width: 1024px) {
  .product-card {
    flex: 0 0 calc((100vw - 80px) / 3); /* 3 sản phẩm */
    max-width: 320px; /* Tăng từ 280px */
  }
}

@media (max-width: 768px) {
  .product-card {
    flex: 0 0 calc((100vw - 60px) / 2); /* 2 sản phẩm */
    max-width: 280px;
    min-height: 550px; /* Giảm từ 600px */
  }
  
  .product-image-container {
    height: 450px; /* Tăng từ 400px để ảnh lớn hơn */
  }
  
  .product-image {
    height: 450px; /* Tăng từ 400px */
  }
  
  .product-info-section {
    padding: 0.75rem; /* Giảm padding cho mobile */
  }
  
  .product-name {
    font-size: 0.875rem; /* Giảm cho mobile */
    min-height: 1.75rem; /* Giảm cho mobile */
  }
  
  .price-current {
    font-size: 1.125rem; /* Giảm cho mobile */
  }
  
  .price-old {
    font-size: 0.875rem; /* Giảm cho mobile */
  }
}

@media (max-width: 576px) {
  .product-card {
    flex: 0 0 calc((100vw - 40px) / 2);
    max-width: 220px;
    min-height: 450px; /* Giảm từ 500px */
  }
  
  .product-image-container {
    height: 350px; /* Giữ nguyên để ảnh lớn */
  }
  
  .product-image {
    height: 350px; /* Giữ nguyên */
  }
  
  .product-info-section {
    padding: 0.5rem; /* Giảm padding cho mobile nhỏ */
  }
  
  .product-name {
    font-size: 0.875rem; /* Giảm cho mobile nhỏ */
    min-height: 1.5rem; /* Giảm cho mobile nhỏ */
  }
  
  .price-current {
    font-size: 1rem; /* Giảm cho mobile nhỏ */
  }
  
  .price-old {
    font-size: 0.75rem; /* Giảm cho mobile nhỏ */
  }
}
</style>
