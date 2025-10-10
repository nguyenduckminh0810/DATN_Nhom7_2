<template>
  <!-- Modal Overlay -->
  <div v-if="isOpen" class="variant-modal-overlay" @click="closeModal">
    <div class="variant-modal-container" @click.stop>
      <!-- Close Button -->
      <button class="close-btn" @click="closeModal" aria-label="Đóng">
        <i class="bi bi-x"></i>
      </button>

      <!-- Modal Content -->
      <div class="variant-modal-content">
        <!-- Header -->
        <div class="modal-header">
          <div class="product-preview">
            <img :src="product.image" :alt="product.name" class="product-image" />
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-price">
                <span class="current-price">{{ formatPrice(product.price) }}₫</span>
                <span v-if="product.originalPrice" class="original-price">
                  {{ formatPrice(product.originalPrice) }}₫
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Variant Selection -->
        <div class="variant-selection">
          <!-- Color Selection -->
          <div v-if="product.colorOptions && product.colorOptions.length > 0" class="selection-group">
            <h4 class="selection-title">
              Màu sắc
              <span v-if="selectedColor" class="selected-value">- {{ getColorName(selectedColor) }}</span>
            </h4>
            <div class="color-grid">
              <button
                v-for="color in product.colorOptions"
                :key="color"
                class="color-option"
                :class="{ 
                  'selected': selectedColor === color,
                  'disabled': !isColorAvailable(color)
                }"
                :style="{ backgroundColor: color }"
                :title="getColorName(color)"
                @click="selectColor(color)"
                :disabled="!isColorAvailable(color)"
              >
                <span v-if="selectedColor === color" class="check-icon">✓</span>
              </button>
            </div>
          </div>

          <!-- Size Selection -->
          <div v-if="product.sizes && product.sizes.length > 0" class="selection-group">
            <h4 class="selection-title">
              Kích thước
              <span v-if="selectedSize" class="selected-value">- {{ selectedSize }}</span>
              <span v-if="selectedColor && availableSizesForColor.length > 0" class="stock-info">
                ({{ availableSizesForColor.length }} size khả dụng)
              </span>
            </h4>
            <div class="size-grid">
              <button
                v-for="size in product.sizes"
                :key="size"
                class="size-option"
                :class="{ 
                  'selected': selectedSize === size,
                  'unavailable': !isSizeAvailable(size),
                  'disabled': !isSizeAvailable(size)
                }"
                @click="selectSize(size)"
                :disabled="!isSizeAvailable(size)"
              >
                {{ size }}
                <span v-if="!isSizeAvailable(size)" class="unavailable-icon">✕</span>
              </button>
            </div>
          </div>

          <!-- Quantity Selection -->
          <div class="selection-group">
            <h4 class="selection-title">Số lượng</h4>
            <div class="quantity-controls">
              <button 
                class="qty-btn minus" 
                @click="decreaseQuantity"
                :disabled="quantity <= 1"
              >
                <i class="bi bi-dash"></i>
              </button>
              <input 
                type="number" 
                v-model.number="quantity" 
                class="qty-input"
                :min="1"
                :max="maxQuantity"
                @input="validateQuantity"
              />
              <button 
                class="qty-btn plus" 
                @click="increaseQuantity"
                :disabled="quantity >= maxQuantity"
              >
                <i class="bi bi-plus"></i>
              </button>
            </div>
            <div v-if="maxQuantity > 0" class="stock-info">
              Còn lại {{ maxQuantity }} sản phẩm
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="modal-actions">
          <button 
            class="btn-add-to-cart"
            :class="{ 'disabled': !canAddToCart }"
            :disabled="!canAddToCart"
            @click="handleAddToCart"
          >
            <i class="bi bi-cart3 me-2"></i>
            {{ addToCartText }}
          </button>
        </div>

        <!-- Quick Add Notice -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import useCart from '../../composables/useCart'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    required: true
  },
  preselectedColor: {
    type: String,
    default: null
  },
  preselectedSize: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['close', 'add-to-cart'])

const { addToCartWithValidation, trackAddToCart } = useCart()

// Local state
const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)

// Computed properties
const availableSizesForColor = computed(() => {
  if (!selectedColor.value || !props.product.colorSizeMapping) {
    return props.product.sizes || []
  }
  return props.product.colorSizeMapping[selectedColor.value] || []
})

const availableColorsForSize = computed(() => {
  if (!selectedSize.value || !props.product.colorSizeMapping) {
    return props.product.colorOptions || []
  }
  return props.product.colorOptions.filter(color => 
    props.product.colorSizeMapping[color]?.includes(selectedSize.value)
  )
})

const maxQuantity = computed(() => {
  // Mock stock logic - replace with actual API call
  if (selectedColor.value && selectedSize.value) {
    // Simulate stock based on variant
    const variantKey = `${selectedColor.value}-${selectedSize.value}`
    const stockMap = {
      '#ffffff-S': 5,
      '#ffffff-M': 8,
      '#ffffff-L': 3,
      '#000000-M': 6,
      '#000000-L': 4,
      '#007bff-L': 2,
      '#007bff-XL': 7
    }
    return stockMap[variantKey] || 10
  }
  return props.product.stock || 10
})

const canAddToCart = computed(() => {
  // For products without variants, always allow
  if (!props.product.colorOptions?.length && !props.product.sizes?.length) {
    return quantity.value > 0 && quantity.value <= maxQuantity.value
  }
  
  // For products with variants, require both color and size
  return selectedColor.value && selectedSize.value && quantity.value > 0 && quantity.value <= maxQuantity.value
})

const addToCartText = computed(() => {
  if (!canAddToCart.value) {
    if (!selectedColor.value && props.product.colorOptions?.length) {
      return 'Chọn màu sắc'
    }
    if (!selectedSize.value && props.product.sizes?.length) {
      return 'Chọn kích thước'
    }
    if (quantity.value <= 0) {
      return 'Chọn số lượng'
    }
    return 'Không thể thêm'
  }
  return 'Thêm vào giỏ hàng'
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

const isColorAvailable = (color) => {
  // Color is available if it has any sizes in stock
  if (!props.product.colorSizeMapping) return true
  const sizes = props.product.colorSizeMapping[color]
  return sizes && sizes.length > 0
}

const isSizeAvailable = (size) => {
  if (!selectedColor.value) return true
  return availableSizesForColor.value.includes(size)
}

const selectColor = (color) => {
  if (!isColorAvailable(color)) return
  
  selectedColor.value = color
  // Reset size if current size is not available for new color
  if (selectedSize.value && !availableSizesForColor.value.includes(selectedSize.value)) {
    selectedSize.value = null
  }
  
}

const selectSize = (size) => {
  if (!isSizeAvailable(size)) return
  
  selectedSize.value = size
  
}

const increaseQuantity = () => {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const validateQuantity = () => {
  if (quantity.value < 1) {
    quantity.value = 1
  } else if (quantity.value > maxQuantity.value) {
    quantity.value = maxQuantity.value
  }
}

const handleAddToCart = () => {
  if (!canAddToCart.value) return
  
  const variantData = {
    variantId: selectedColor.value && selectedSize.value ? 
      `${props.product.id}-${selectedColor.value}-${selectedSize.value}` : null,
    color: selectedColor.value,
    size: selectedSize.value,
    quantity: quantity.value
  }
  
  // Add to cart with validation
  const success = addToCartWithValidation(props.product, quantity.value, variantData)
  
  if (success) {
    // Track analytics
    trackAddToCart(props.product, variantData)
    
    // Emit event
    emit('add-to-cart', variantData)
    
    // Close modal
    closeModal()
  }
}

const closeModal = () => {
  emit('close')
}

// Watch for modal open to reset state
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    // Reset selections
    selectedColor.value = props.preselectedColor || null
    selectedSize.value = props.preselectedSize || null
    quantity.value = 1
    
  }
})

// Watch for quantity changes to validate
watch(quantity, (newQty) => {
  if (newQty > maxQuantity.value) {
    quantity.value = maxQuantity.value
  } else if (newQty < 1) {
    quantity.value = 1
  }
})
</script>

<style scoped>
.variant-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  animation: fadeIn 0.3s ease;
}

.variant-modal-container {
  background: white;
  border-radius: 16px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.1);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: scale(1.1);
}

.variant-modal-content {
  padding: 2rem;
}

.modal-header {
  margin-bottom: 2rem;
}

.product-preview {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #212529;
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.current-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #dc3545;
}

.original-price {
  font-size: 1rem;
  color: #6c757d;
  text-decoration: line-through;
}

.selection-group {
  margin-bottom: 2rem;
}

.selection-title {
  font-size: 1rem;
  font-weight: 600;
  color: #212529;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.selected-value {
  color: #007bff;
  font-weight: 500;
}

.stock-info {
  font-size: 0.875rem;
  color: #6c757d;
  font-weight: normal;
}

.color-grid {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-option {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 3px solid #e9ecef;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-option:hover:not(.disabled) {
  border-color: #007bff;
  transform: scale(1.1);
}

.color-option.selected {
  border-color: #007bff;
  border-width: 3px;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3);
}

.color-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  position: relative;
}

.color-option.disabled::after {
  content: '✕';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #dc3545;
  font-weight: bold;
  font-size: 0.875rem;
}

.check-icon {
  color: white;
  font-weight: bold;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

.size-grid {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.size-option {
  min-width: 44px;
  height: 44px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  position: relative;
}

.size-option:hover:not(.disabled) {
  border-color: #007bff;
  background: #f8f9fa;
  transform: translateY(-2px);
}

.size-option.selected {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.size-option.unavailable {
  opacity: 0.5;
  cursor: not-allowed;
  text-decoration: line-through;
}

.unavailable-icon {
  position: absolute;
  top: -5px;
  right: -5px;
  color: #dc3545;
  font-size: 0.75rem;
  font-weight: bold;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.qty-btn {
  width: 44px;
  height: 44px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 1.125rem;
}

.qty-btn:hover:not(:disabled) {
  border-color: #007bff;
  background: #f8f9fa;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 80px;
  height: 44px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  text-align: center;
  font-weight: 600;
  font-size: 1rem;
}

.qty-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3);
}

.modal-actions {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #e9ecef;
}

.btn-add-to-cart {
  width: 100%;
  height: 48px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-add-to-cart:hover:not(.disabled) {
  background: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.btn-add-to-cart.disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}


/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Mobile Responsive */
@media (max-width: 576px) {
  .variant-modal-container {
    margin: 1rem;
    max-width: none;
  }
  
  .variant-modal-content {
    padding: 1.5rem;
  }
  
  .product-preview {
    flex-direction: column;
    text-align: center;
  }
  
  .product-image {
    width: 100px;
    height: 100px;
  }
  
  .color-grid {
    justify-content: center;
  }
  
  .size-grid {
    justify-content: center;
  }
}

/* Focus trap for accessibility */
.variant-modal-overlay:focus {
  outline: none;
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .color-option {
    border-width: 4px;
  }
  
  .size-option {
    border-width: 3px;
  }
}
</style>
