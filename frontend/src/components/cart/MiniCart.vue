<template>
  <div v-if="isOpen" class="mini-cart-overlay" @click="closeMiniCart">
    <div class="mini-cart-container" @click.stop>
      <!-- Header -->
      <div class="mini-cart-header">
        <h4 class="mini-cart-title">
          <i class="bi bi-cart3 me-2"></i>
          Giỏ hàng ({{ cartStore.itemCount }})
        </h4>
        <button class="close-btn" @click="closeMiniCart" aria-label="Đóng">
          <i class="bi bi-x"></i>
        </button>
      </div>

      <!-- Cart Items -->
      <div class="mini-cart-body">
        <div v-if="cartStore.isEmpty" class="empty-cart">
          <div class="empty-cart-icon">
            <i class="bi bi-cart-x"></i>
          </div>
          <p class="empty-cart-text">Giỏ hàng trống</p>
          <p class="empty-cart-subtext">Hãy thêm sản phẩm vào giỏ để tiếp tục mua sắm</p>
        </div>

        <div v-else class="cart-items">
          <div 
            v-for="item in cartStore.items" 
            :key="item.itemKey"
            class="cart-item"
          >
            <!-- Product Image -->
            <div class="item-image">
              <img :src="item.image" :alt="item.name" loading="lazy" />
            </div>

            <!-- Product Info -->
            <div class="item-info">
              <h5 class="item-name">{{ item.name }}</h5>
              
              <!-- Variant Info -->
              <div v-if="item.color || item.size" class="item-variant">
                <span v-if="item.color" class="variant-color">
                  <span 
                    class="color-dot" 
                    :style="{ backgroundColor: item.color }"
                  ></span>
                  {{ getColorName(item.color) }}
                </span>
                <span v-if="item.size" class="variant-size">
                  Size {{ item.size }}
                </span>
              </div>

              <!-- Price and Quantity -->
              <div class="item-details">
                <span class="item-price">{{ cartStore.formatPrice(item.price) }}</span>
                <div class="quantity-controls">
                  <button 
                    class="qty-btn minus"
                    @click="updateItemQuantity(item.itemKey, item.quantity - 1)"
                    :disabled="item.quantity <= 1"
                  >
                    <i class="bi bi-dash"></i>
                  </button>
                  <span class="qty-display">{{ item.quantity }}</span>
                  <button 
                    class="qty-btn plus"
                    @click="updateItemQuantity(item.itemKey, item.quantity + 1)"
                  >
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- Remove Button -->
            <button 
              class="remove-btn"
              @click="removeItem(item.itemKey)"
              aria-label="Xóa sản phẩm"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="!cartStore.isEmpty" class="mini-cart-footer">
        <!-- Total -->
        <div class="cart-total">
          <div class="total-row">
            <span>Tạm tính:</span>
            <span class="total-amount">{{ cartStore.formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <div class="total-note">
            <small>Miễn phí vận chuyển cho đơn từ 500.000₫</small>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="cart-actions">
          <button 
            class="btn btn-outline-danger btn-clear-all"
            @click="clearAllItems"
            v-if="items.length > 0"
          >
            <i class="bi bi-trash me-1" ></i>Xóa tất cả
          </button>
          <router-link to="/cart" class="btn btn-primary btn-checkout" @click="closeMiniCart">
  Thanh toán
</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useCartStore } from '../../stores/cart'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const cartStore = useCartStore()

// Computed
const items = computed(() => cartStore.items)

// ✅ Watch isOpen để reload cart khi mở mini cart
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    console.log('🔄 [MINI CART] Opening, reloading cart from backend...')
    try {
      await cartStore.loadCart()
      console.log('✅ [MINI CART] Cart reloaded successfully')
    } catch (error) {
      console.error('❌ [MINI CART] Error reloading cart:', error)
    }
  }
})

// Methods

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

const updateItemQuantity = async (itemKey, newQuantity) => {
  try {
    await cartStore.updateQuantity(itemKey, newQuantity)
  } catch (error) {
    console.error('Error updating quantity:', error)
  }
}

const removeItem = async (itemKey) => {
  try {
    await cartStore.removeItem(itemKey)
  } catch (error) {
    console.error('Error removing item:', error)
  }
}

const closeMiniCart = () => {
  emit('close')
}

const clearAllItems = async () => {
  if (confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?')) {
    try {
      await cartStore.clearCart()
    } catch (error) {
      console.error('Error clearing cart:', error)
    }
  }
}
</script>

<style scoped>
.mini-cart-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1050;
  display: flex;
  justify-content: flex-end;
  padding-top: 80px; /* Account for navbar height */
}

.mini-cart-container {
  background: white;
  width: 100%;
  max-width: 400px;
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
  animation: slideInRight 0.3s ease;
}

.mini-cart-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
}

.mini-cart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #212529;
  margin: 0;
  display: flex;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #212529;
}

.mini-cart-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.empty-cart {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-cart-icon {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1rem;
}

.empty-cart-text {
  font-size: 1.25rem;
  font-weight: 600;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.empty-cart-subtext {
  color: #adb5bd;
  font-size: 0.875rem;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  background: #fff;
  transition: all 0.3s ease;
}

.cart-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-image {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.item-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #212529;
  margin: 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-variant {
  display: flex;
  gap: 0.75rem;
  font-size: 0.75rem;
  color: #6c757d;
}

.variant-color {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #e9ecef;
}

.variant-size {
  font-weight: 500;
}

.item-details {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.item-price {
  font-size: 1rem;
  font-weight: 700;
  color: #dc3545;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 0.25rem;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.75rem;
  color: #6c757d;
}

.qty-btn:hover:not(:disabled) {
  background: #e9ecef;
  color: #212529;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-display {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.remove-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
}

.remove-btn:hover {
  background: #dc3545;
  color: white;
}

.mini-cart-footer {
  border-top: 1px solid #e9ecef;
  padding: 1.5rem;
  background: #f8f9fa;
}

.cart-total {
  margin-bottom: 1.5rem;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.total-amount {
  color: #dc3545;
  font-size: 1.25rem;
}

.total-note {
  text-align: center;
  color: #6c757d;
}

.cart-actions {
  display: flex;
  gap: 0.75rem;
}

.btn {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.btn-outline-primary {
  background: transparent;
  color: #007bff;
  border-color: #007bff;
}

.btn-outline-primary:hover {
  background: #007bff;
  color: white;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-outline-danger {
  background: transparent;
  color: #dc3545;
  border-color: #dc3545;
}

.btn-outline-danger:hover {
  background: #dc3545;
  color: white;
}

.btn-clear-all {
  flex: 1;
}

.btn-checkout {
  flex: 2;
}

/* Animations */
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Mobile Responsive */
@media (max-width: 576px) {
  .mini-cart-overlay {
    padding-top: 70px;
  }
  
  .mini-cart-container {
    height: calc(100vh - 70px);
    max-width: none;
  }
  
  .cart-item {
    padding: 0.75rem;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .item-name {
    font-size: 0.8rem;
  }
  
  .item-price {
    font-size: 0.875rem;
  }
  
  .mini-cart-footer {
    padding: 1rem;
  }
  
  .cart-actions {
    flex-direction: column;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .cart-item {
    border-width: 2px;
  }
  
  .qty-btn {
    border: 1px solid #6c757d;
  }
}
</style>
