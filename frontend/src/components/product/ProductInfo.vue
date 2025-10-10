<template>
  <div class="product-info">
    <!-- Product Title & Rating -->
    <div class="product-header">
      <h1 class="product-title">{{ product.name }}</h1>
      <div class="product-rating">
        <div class="stars">
          <i v-for="star in 5" :key="star" 
             :class="star <= averageRating ? 'bi bi-star-fill' : 'bi bi-star'"
             class="star-icon"></i>
        </div>
        <span class="rating-text">({{ reviewCount }})</span>
      </div>
      <button class="share-btn" @click="shareProduct">
        <i class="bi bi-share"></i>
        Chia sẻ
      </button>
    </div>

    <!-- Price Section -->
    <div class="price-section">
      <div class="current-price">{{ formatPrice(product.price) }}</div>
      <div v-if="product.originalPrice" class="original-price">
        {{ formatPrice(product.originalPrice) }}
      </div>
      <div v-if="product.discount" class="discount-badge">
        -{{ product.discount }}%
      </div>
    </div>

    <!-- Trust Badges -->
    <div class="trust-badges">
      <div class="trust-item">
        <i class="bi bi-truck"></i>
        <span>Miễn phí vận chuyển</span>
      </div>
      <div class="trust-item">
        <i class="bi bi-arrow-clockwise"></i>
        <span>60 ngày đổi trả</span>
      </div>
      <div class="trust-item">
        <i class="bi bi-headset"></i>
        <span>Hỗ trợ 24/7</span>
      </div>
    </div>

    <!-- Color Selection -->
    <div class="color-selection">
      <h4 class="section-title">Màu sắc:</h4>
      <div class="color-options">
        <div 
          v-for="color in availableColors" 
          :key="color.value"
          class="color-option"
          :class="{ active: selectedColor === color.value }"
          @click="selectColor(color.value)"
        >
          <div class="color-swatch" :style="{ backgroundColor: color.value }"></div>
          <span class="color-name">{{ color.name }}</span>
        </div>
      </div>
    </div>

    <!-- Size Selection -->
    <div class="size-selection">
      <h4 class="section-title">
        Kích thước:
        <button class="size-guide-btn" @click="openSizeGuide">
          <i class="bi bi-rulers"></i>
          Hướng dẫn chọn size
        </button>
      </h4>
      <div class="size-options">
        <button 
          v-for="size in availableSizes" 
          :key="size"
          class="size-option"
          :class="{ 
            active: selectedSize === size,
            unavailable: !isSizeAvailable(size)
          }"
          @click="selectSize(size)"
          :disabled="!isSizeAvailable(size)"
        >
          {{ size }}
        </button>
      </div>
    </div>

    <!-- Quantity & Add to Cart -->
    <div class="quantity-cart-section">
      <div class="quantity-selector">
        <label class="quantity-label">Số lượng:</label>
        <div class="quantity-controls">
          <button class="quantity-btn" @click="decreaseQuantity" :disabled="quantity <= 1">
            <i class="bi bi-dash"></i>
          </button>
          <input 
            v-model="quantity" 
            type="number" 
            min="1" 
            :max="maxStock"
            class="quantity-input"
          />
          <button class="quantity-btn" @click="increaseQuantity" :disabled="quantity >= maxStock">
            <i class="bi bi-plus"></i>
          </button>
        </div>
      </div>
      
      <div class="cart-actions">
        <button 
          class="add-to-cart-btn"
          @click="addToCart"
          :disabled="!selectedSize || !selectedColor"
        >
          <i class="bi bi-cart3"></i>
          Thêm vào giỏ
        </button>
        <button class="buy-now-btn" @click="buyNow">
          Mua ngay
        </button>
      </div>
    </div>

    <!-- Product Features -->
    <div class="product-features">
      <div class="feature-item">
        <i class="bi bi-check-circle"></i>
        <span>Chất liệu cao cấp</span>
      </div>
      <div class="feature-item">
        <i class="bi bi-check-circle"></i>
        <span>Thiết kế hiện đại</span>
      </div>
      <div class="feature-item">
        <i class="bi bi-check-circle"></i>
        <span>Bền đẹp lâu dài</span>
      </div>
    </div>

    <!-- Size Guide Modal -->
    <div v-if="showSizeGuide" class="size-guide-modal" @click="closeSizeGuide">
      <div class="size-guide-content" @click.stop>
        <button class="modal-close" @click="closeSizeGuide">
          <i class="bi bi-x"></i>
        </button>
        <h3>Hướng dẫn chọn size</h3>
        <div class="size-chart">
          <!-- Size chart content -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useCartStore } from '../../stores/cart'
import { useToast } from '../../composables/useToast'

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  selectedColor: {
    type: String,
    default: ''
  },
  selectedSize: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:selectedColor', 'update:selectedSize', 'add-to-cart'])

const cartStore = useCartStore()
const { showToast } = useToast()

const quantity = ref(1)
const showSizeGuide = ref(false)

const averageRating = computed(() => props.product.rating || 4.5)
const reviewCount = computed(() => props.product.reviewCount || 0)

const availableColors = computed(() => {
  return props.product.colorOptions?.map(color => ({
    value: color,
    name: getColorName(color)
  })) || []
})

const availableSizes = computed(() => {
  return props.product.sizes || ['S', 'M', 'L', 'XL', '2XL', '3XL']
})

const maxStock = computed(() => {
  if (!props.selectedColor || !props.selectedSize) return 0
  return props.product.colorSizeMapping?.[props.selectedColor]?.includes(props.selectedSize) ? 10 : 0
})

const selectColor = (color) => {
  emit('update:selectedColor', color)
}

const selectSize = (size) => {
  if (isSizeAvailable(size)) {
    emit('update:selectedSize', size)
  }
}

const isSizeAvailable = (size) => {
  if (!props.selectedColor) return true
  return props.product.colorSizeMapping?.[props.selectedColor]?.includes(size) || false
}

const increaseQuantity = () => {
  if (quantity.value < maxStock.value) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  if (!props.selectedSize || !props.selectedColor) {
    showToast('Vui lòng chọn màu sắc và kích thước', 'warning')
    return
  }

  const cartItem = {
    id: props.product.id,
    name: props.product.name,
    price: props.product.price,
    image: props.product.image,
    color: props.selectedColor,
    size: props.selectedSize,
    quantity: quantity.value,
    variantId: `${props.product.id}_${props.selectedColor}_${props.selectedSize}`
  }

  cartStore.addItem(cartItem)
  showToast('Đã thêm vào giỏ hàng', 'success')
  emit('add-to-cart', cartItem)
}

const buyNow = () => {
  addToCart()
  // Navigate to checkout
}

const shareProduct = () => {
  if (navigator.share) {
    navigator.share({
      title: props.product.name,
      text: `Xem sản phẩm ${props.product.name} tại AURO`,
      url: window.location.href
    })
  } else {
    // Fallback: copy to clipboard
    navigator.clipboard.writeText(window.location.href)
    showToast('Đã copy link sản phẩm', 'success')
  }
}

const openSizeGuide = () => {
  showSizeGuide.value = true
}

const closeSizeGuide = () => {
  showSizeGuide.value = false
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getColorName = (colorCode) => {
  const colorMap = {
    '#000000': 'Đen',
    '#ffffff': 'Trắng',
    '#ff0000': 'Đỏ',
    '#0000ff': 'Xanh dương',
    '#00ff00': 'Xanh lá',
    '#ffff00': 'Vàng',
    '#ff69b4': 'Hồng',
    '#808080': 'Xám',
    '#8b4513': 'Nâu'
  }
  return colorMap[colorCode] || colorCode
}

// Watch for color changes to reset size
watch(() => props.selectedColor, () => {
  emit('update:selectedSize', '')
})
</script>

<style scoped>
.product-info {
  padding: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.product-header {
  margin-bottom: 2rem;
}

.product-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.stars {
  display: flex;
  gap: 0.25rem;
}

.star-icon {
  color: #ffc107;
  font-size: 1.2rem;
}

.rating-text {
  color: #6c757d;
  font-weight: 500;
}

.share-btn {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  color: #495057;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.share-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.price-section {
  margin-bottom: 2rem;
}

.current-price {
  font-size: 2.5rem;
  font-weight: 700;
  color: #dc3545;
  margin-bottom: 0.5rem;
}

.original-price {
  font-size: 1.2rem;
  color: #6c757d;
  text-decoration: line-through;
  margin-bottom: 0.5rem;
}

.discount-badge {
  display: inline-block;
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.trust-badges {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #495057;
}

.trust-item i {
  color: #28a745;
  font-size: 1.1rem;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.size-guide-btn {
  background: none;
  border: none;
  color: #007bff;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.color-selection, .size-selection {
  margin-bottom: 2rem;
}

.color-options, .size-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.color-option.active {
  background: #e3f2fd;
  border: 2px solid #2196f3;
}

.color-swatch {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #dee2e6;
  transition: all 0.3s ease;
}

.color-option.active .color-swatch {
  border-color: #2196f3;
  transform: scale(1.1);
}

.color-name {
  font-size: 0.8rem;
  color: #495057;
  font-weight: 500;
}

.size-option {
  width: 50px;
  height: 50px;
  border: 2px solid #dee2e6;
  border-radius: 8px;
  background: white;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}

.size-option:hover:not(.unavailable) {
  border-color: #007bff;
  background: #e3f2fd;
}

.size-option.active {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.size-option.unavailable {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

.quantity-cart-section {
  margin-bottom: 2rem;
}

.quantity-selector {
  margin-bottom: 1.5rem;
}

.quantity-label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #dee2e6;
  border-radius: 8px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quantity-btn:hover:not(:disabled) {
  border-color: #007bff;
  background: #e3f2fd;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 80px;
  height: 40px;
  border: 2px solid #dee2e6;
  border-radius: 8px;
  text-align: center;
  font-weight: 600;
}

.cart-actions {
  display: flex;
  gap: 1rem;
}

.add-to-cart-btn, .buy-now-btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.add-to-cart-btn {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #0056b3, #004085);
  transform: translateY(-2px);
}

.add-to-cart-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.buy-now-btn {
  background: white;
  color: #007bff;
  border: 2px solid #007bff;
}

.buy-now-btn:hover {
  background: #007bff;
  color: white;
}

.product-features {
  display: flex;
  gap: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #495057;
  font-size: 0.9rem;
}

.feature-item i {
  color: #28a745;
  font-size: 1.1rem;
}

/* Size Guide Modal */
.size-guide-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.size-guide-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 600px;
  width: 100%;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

/* Responsive */
@media (max-width: 768px) {
  .product-info {
    padding: 1rem;
  }
  
  .product-title {
    font-size: 1.5rem;
  }
  
  .current-price {
    font-size: 2rem;
  }
  
  .trust-badges {
    flex-direction: column;
    gap: 1rem;
  }
  
  .cart-actions {
    flex-direction: column;
  }
  
  .product-features {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>
