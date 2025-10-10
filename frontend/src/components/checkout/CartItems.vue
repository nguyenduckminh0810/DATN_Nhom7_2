<template>
  <div class="cart-items-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <div class="cart-header-layout">
          <div class="cart-header-left">
            <div class="d-flex align-items-center">
              <h5 class="mb-0 me-3">
                <i class="bi bi-cart3 me-2"></i>Giỏ hàng
              </h5>
              <div class="form-check me-3">
                <input class="form-check-input" 
                       type="checkbox" 
                       id="selectAll"
                       :checked="allItemsSelected"
                       @change="toggleSelectAll">
                <label class="form-check-label text-white" for="selectAll">
                  Chọn tất cả
                </label>
              </div>
              <span class="badge bg-white text-warning">
                {{ items ? items.length : 0 }} sản phẩm
              </span>
            </div>
          </div>
          
          <div class="cart-header-right">
            <button v-if="items && items.length > 0" 
                    class="btn btn-danger btn-sm clear-all-btn"
                    @click="clearAllItems"
                    title="Xóa tất cả sản phẩm">
              <i class="bi bi-trash-fill me-1"></i>Xóa tất cả
            </button>
          </div>
        </div>
      </div>

      <!-- Cart Items List -->
      <div class="section-body p-0">
        <div v-if="!items || items.length === 0" class="empty-cart">
          <div class="text-center py-5">
            <i class="bi bi-cart-x display-4 text-muted mb-3"></i>
            <h6 class="text-muted">Giỏ hàng trống</h6>
            <p class="text-muted small">Hãy thêm sản phẩm vào giỏ hàng</p>
          </div>
        </div>

        <div v-else>
          <div v-for="item in items" :key="item.itemKey" class="cart-item">
            <div class="row align-items-center p-3">
              <!-- Checkbox -->
              <div class="col-1">
                <div class="form-check">
                  <input class="form-check-input" 
                         type="checkbox" 
                         v-model="item.selected"
                         :id="'item-' + item.id">
                </div>
              </div>

              <!-- Product Image -->
              <div class="col-2">
                <div class="cart-product-image">
                  <img :src="item.image" 
                       :alt="item.name"
                       class="img-fluid rounded">
                </div>
              </div>

              <!-- Product Info -->
              <div class="col-5">
                <div class="cart-product-info">
                  <h6 class="mb-1 fw-bold">{{ item.name }}</h6>
                  <div class="product-variant mb-2">
                    <span class="variant-text">{{ item.color || 'N/A' }} / {{ item.size || 'N/A' }}</span>
                  </div>
                  
                  <!-- Variant Controls -->
                  <div class="variant-controls mb-2">
                    <div class="dropdown me-2">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              data-bs-toggle="dropdown">
                        {{ item.color }}
                      </button>
                      <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">Đen</a></li>
                        <li><a class="dropdown-item" href="#">Trắng</a></li>
                        <li><a class="dropdown-item" href="#">Xám</a></li>
                      </ul>
                    </div>
                    
                    <div class="dropdown">
                      <button class="btn btn-outline-secondary btn-sm dropdown-toggle" 
                              type="button" 
                              data-bs-toggle="dropdown">
                        {{ item.size }}
                      </button>
                      <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">S</a></li>
                        <li><a class="dropdown-item" href="#">M</a></li>
                        <li><a class="dropdown-item" href="#">L</a></li>
                        <li><a class="dropdown-item" href="#">XL</a></li>
                      </ul>
                    </div>
                  </div>

                  <!-- Delete Button -->
                  <button class="btn btn-link text-danger p-0 delete-btn" 
                          @click="removeItem(item.itemKey)">
                    <i class="bi bi-trash me-1"></i>Xóa
                  </button>
                </div>
              </div>

              <!-- Quantity -->
              <div class="col-2">
                <div class="quantity-controls">
                  <div class="input-group input-group-sm">
                    <button class="btn btn-outline-secondary" 
                            type="button"
                            @click="decreaseQuantity(item.itemKey)">
                      <i class="bi bi-dash"></i>
                    </button>
                    <input type="number" 
                           class="form-control text-center" 
                           v-model.number="item.quantity"
                           min="1"
                           @change="updateQuantity(item.itemKey, item.quantity)">
                    <button class="btn btn-outline-secondary" 
                            type="button"
                            @click="increaseQuantity(item.itemKey)">
                      <i class="bi bi-plus"></i>
                    </button>
                  </div>
                </div>
              </div>

              <!-- Price -->
              <div class="col-2">
                <div class="cart-price text-end">
                  <div class="current-price fw-bold">
                    {{ formatPrice(item.price * item.quantity) }}
                  </div>
                  <div v-if="item.originalPrice && item.originalPrice > item.price" 
                       class="original-price text-muted small">
                    {{ formatPrice(item.originalPrice * item.quantity) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Continue Shopping -->
    <div class="mt-3">
      <button class="btn btn-outline-secondary w-100 continue-btn">
        <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCart } from '@/composables/useCart'

const { items, updateQuantity, removeItem, clearCart, formatPrice } = useCart()

const increaseQuantity = (itemKey) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (item) {
    updateQuantity(itemKey, item.quantity + 1)
  }
}

const decreaseQuantity = (itemKey) => {
  const item = items.value.find(item => item.itemKey === itemKey)
  if (item && item.quantity > 1) {
    updateQuantity(itemKey, item.quantity - 1)
  }
}

const clearAllItems = () => {
  if (window.confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
    clearCart()
    if (window.$toast) {
      window.$toast.success('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
    }
  }
}

// Logic cho checkbox "Chọn tất cả"
const allItemsSelected = computed(() => {
  if (!items.value || items.value.length === 0) return false
  return items.value.every(item => item.selected !== false)
})

const toggleSelectAll = () => {
  if (!items.value || items.value.length === 0) return
  
  const shouldSelectAll = !allItemsSelected.value
  items.value.forEach(item => {
    item.selected = shouldSelectAll
  })
  
  if (window.$toast) {
    const message = shouldSelectAll ? 'Đã chọn tất cả sản phẩm' : 'Đã bỏ chọn tất cả sản phẩm'
    window.$toast.success(message)
  }
}
</script>

<style scoped>
.cart-items-section {
  margin-bottom: 2rem;
}

.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.section-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border: none;
  position: relative;
  overflow: hidden;
}

.section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.section-header h5 i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.cart-header-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.cart-header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.cart-header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 1rem;
}

.badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* Select All Checkbox Styling */
.form-check-input {
  background-color: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  width: 18px;
  height: 18px;
}

.form-check-input:checked {
  background-color: #28a745;
  border-color: #28a745;
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.2rem rgba(255, 255, 255, 0.25);
}

.form-check-label {
  font-size: 0.9rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  user-select: none;
}

.cart-item {
  border-bottom: 1px solid #f0f0f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-product-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
}

.cart-product-info h6 {
  font-size: 0.95rem;
  color: #333;
}

.variant-text {
  font-size: 0.8rem;
  color: #666;
  background: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.variant-controls .dropdown {
  display: inline-block;
}

.delete-btn {
  font-size: 0.8rem;
  text-decoration: none;
}

.delete-btn:hover {
  color: #dc3545 !important;
  text-decoration: underline;
}

.quantity-controls .input-group {
  width: 100px;
}

.quantity-controls .form-control {
  border-left: none;
  border-right: none;
}

.current-price {
  color: #333;
  font-size: 0.95rem;
}

.original-price {
  text-decoration: line-through;
}

.continue-btn {
  border-radius: 8px;
  padding: 0.75rem;
  font-weight: 500;
}

.clear-all-btn {
  border-radius: 8px;
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.8);
  color: white;
  background: rgba(220, 53, 69, 0.9);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.clear-all-btn:hover {
  background: rgba(220, 53, 69, 1);
  border-color: white;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
}

.clear-all-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(220, 53, 69, 0.3);
}

.clear-all-btn i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

@media (max-width: 768px) {
  .cart-header-layout {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .cart-header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .cart-header-right {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
  
  .clear-all-btn {
    font-size: 0.8rem;
    padding: 0.3rem 0.6rem;
  }
  
  .badge {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }
  
  .form-check-label {
    font-size: 0.8rem;
  }
  
  .form-check-input {
    width: 16px;
    height: 16px;
  }
  
  .cart-item .row > div {
    margin-bottom: 0.5rem;
  }
  
  .cart-item .col-1,
  .cart-item .col-2,
  .cart-item .col-5,
  .cart-item .col-2 {
    flex: 0 0 100%;
    max-width: 100%;
  }
  
  .cart-item .col-1,
  .cart-item .col-2 {
    flex: 0 0 auto;
    max-width: none;
  }
}
</style>
