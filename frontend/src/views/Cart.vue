<template>
  <div class="cart">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb modern-breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/" class="breadcrumb-link">
              <i class="bi bi-house me-1"></i>Trang chủ
            </router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            Giỏ hàng
          </li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <div class="cart-header text-center mb-5">
            <h1 class="display-5 fw-bold mb-3">Giỏ hàng của bạn</h1>
            <div class="section-divider"></div>
          </div>
        </div>
      </div>

      <!-- Empty Cart -->
      <div v-if="isEmpty" class="empty-cart-state">
        <div class="empty-cart-content">
          <i class="bi bi-cart-x display-1 text-muted mb-4"></i>
          <h4 class="mb-3">Giỏ hàng trống</h4>
          <p class="text-muted mb-4">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
          <router-link to="/category" class="btn btn-auro-primary btn-lg">
            <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
          </router-link>
        </div>
      </div>

      <!-- Cart Items -->
      <div v-else class="row">
        <!-- Cart Items List -->
        <div class="col-lg-8">
          <div class="card modern-cart-card">
            <div class="card-header modern-cart-header">
              <h5 class="mb-0">
                <i class="ph-shopping-cart me-2"></i>Sản phẩm trong giỏ hàng ({{ itemCount }} sản phẩm)
              </h5>
            </div>
            <div class="card-body p-0">
              <div v-for="item in items" :key="item.itemKey" class="cart-item modern-cart-item">
                <div class="row align-items-center p-4">
                  <!-- Product Image -->
                  <div class="col-md-2">
                    <div class="cart-product-image">
                      <img :src="item.image || 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80'" 
                           alt="Product" class="img-fluid">
                    </div>
                  </div>
                  
                  <!-- Product Info -->
                  <div class="col-md-4">
                    <div class="cart-product-info">
                      <h6 class="mb-2 fw-bold">{{ item.name }}</h6>
                      <div class="product-attributes">
                        <span v-if="item.size" class="attribute-badge">
                          <i class="ph-ruler me-1"></i>Size {{ item.size }}
                        </span>
                        <span v-if="item.color" class="attribute-badge">
                          <i class="ph-palette me-1"></i>
                          <span 
                            class="color-dot me-1" 
                            :style="{ backgroundColor: item.color }"
                          ></span>
                          {{ getColorName(item.color) }}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <!-- Price -->
                  <div class="col-md-2">
                    <div class="cart-price">
                      <span class="price-amount">{{ formatPrice(item.price) }}</span>
                      <small class="price-unit text-muted">đ</small>
                    </div>
                  </div>
                  
                  <!-- Quantity -->
                  <div class="col-md-2">
                    <div class="cart-quantity-controls">
                      <button class="btn btn-outline-secondary quantity-btn" 
                              @click="updateItemQuantity(item.itemKey, item.quantity - 1)">
                        -
                      </button>
                      <input type="number" 
                             class="form-control quantity-input" 
                             v-model.number="item.quantity"
                             @change="updateItemQuantity(item.itemKey, item.quantity)"
                             min="1">
                      <button class="btn btn-outline-secondary quantity-btn" 
                              @click="updateItemQuantity(item.itemKey, item.quantity + 1)">
                        +
                      </button>
                    </div>
                  </div>
                  
                  <!-- Total & Actions -->
                  <div class="col-md-2">
                    <div class="cart-total-actions">
                      <div class="cart-total">
                        <span class="total-amount fw-bold">{{ formatPrice(item.price * item.quantity) }}</span>
                      </div>
                      <button class="btn btn-outline-danger btn-sm remove-btn" 
                              @click="removeItemFromCart(item.itemKey)">
                        <i class="ph-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Continue Shopping -->
          <div class="mt-4">
            <router-link to="/category" class="btn btn-outline-secondary modern-continue-btn">
              <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
            </router-link>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="col-lg-4">
          <div class="card modern-summary-card">
            <div class="card-header modern-summary-header">
              <h5 class="mb-0">
                <i class="bi bi-receipt me-2"></i>Tóm tắt đơn hàng
              </h5>
            </div>
            <div class="card-body">
              <!-- Summary Items -->
              <div class="summary-section">
                <div class="summary-item">
                  <span class="summary-label">Tạm tính:</span>
                  <span class="summary-value">{{ formatPrice(totalPrice) }}</span>
                </div>
                
                <div class="summary-item">
                  <span class="summary-label">Phí vận chuyển:</span>
                  <span class="summary-value">{{ formatPrice(shippingFee) }}</span>
                </div>
                
                <div v-if="discountAmount > 0" class="summary-item discount-item">
                  <span class="summary-label">Giảm giá:</span>
                  <span class="summary-value">-{{ formatPrice(discountAmount) }}</span>
                </div>
                
                <div class="summary-divider"></div>
                
                <div class="summary-total">
                  <span class="total-label">Tổng cộng:</span>
                  <span class="total-value">{{ formatPrice(finalTotal) }}</span>
                </div>
              </div>

              <!-- Voucher Code -->
              <div class="voucher-section">
                <label class="form-label fw-bold">
                  <i class="bi bi-ticket-perforated me-2"></i>Mã giảm giá:
                </label>
                <div class="voucher-input-group">
                  <input v-model="voucherCode" 
                         type="text" 
                         class="form-control modern-voucher-input" 
                         placeholder="Nhập mã giảm giá">
                  <button class="btn btn-auro-primary voucher-apply-btn" 
                          @click="applyVoucher"
                          :disabled="!voucherCode.trim()">
                    <i class="bi bi-check-lg"></i>
                  </button>
                </div>
                <div v-if="voucherMessage" 
                     class="voucher-message mt-2"
                     :class="voucherMessage.type === 'success' ? 'text-success' : 'text-danger'">
                  <i :class="voucherMessage.type === 'success' ? 'bi bi-check-circle' : 'bi bi-exclamation-circle'" class="me-1"></i>
                  {{ voucherMessage.text }}
                </div>
              </div>

              <!-- Checkout Button -->
              <div class="checkout-section">
                <router-link to="/checkout" class="btn btn-auro-primary btn-lg w-100 checkout-btn">
                  <i class="bi bi-credit-card me-2"></i>Thanh toán
                </router-link>
              </div>

              <!-- Security Info -->
              <div class="security-info">
                <div class="security-badges">
                  <div class="security-badge">
                    <i class="bi bi-shield-check"></i>
                    <span>Bảo mật</span>
                  </div>
                  <div class="security-badge">
                    <i class="bi bi-truck"></i>
                    <span>Giao hàng nhanh</span>
                  </div>
                  <div class="security-badge">
                    <i class="bi bi-arrow-clockwise"></i>
                    <span>Đổi trả dễ dàng</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Shipping Info -->
          <div class="card modern-shipping-card mt-3">
            <div class="card-body">
              <h6 class="fw-bold mb-3">
                <i class="bi bi-truck me-2"></i>Thông tin giao hàng
              </h6>
              <div class="shipping-info">
                <div class="shipping-item">
                  <i class="bi bi-check-circle text-success me-2"></i>
                  <span>Miễn phí vận chuyển cho đơn hàng trên 500.000đ</span>
                </div>
                <div class="shipping-item">
                  <i class="bi bi-clock text-warning me-2"></i>
                  <span>Giao hàng trong 2-3 ngày làm việc</span>
                </div>
                <div class="shipping-item">
                  <i class="bi bi-arrow-clockwise text-info me-2"></i>
                  <span>Đổi trả miễn phí trong 30 ngày</span>
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
import { ref, computed } from 'vue'
import useCart from '../composables/useCart'
import authUtils from '../utils/auth'

const { 
  items, 
  itemCount, 
  totalPrice, 
  isEmpty, 
  updateQuantity, 
  removeItem, 
  canApplyVoucher, 
  applyVoucher: applyVoucherToCart 
} = useCart()

// Reactive data
const voucherCode = ref('')
const voucherMessage = ref(null)
const discountAmount = ref(0)

// Computed
const shippingFee = computed(() => {
  return totalPrice.value >= 500000 ? 0 : 30000
})

const finalTotal = computed(() => {
  return totalPrice.value + shippingFee.value - discountAmount.value
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
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

const updateItemQuantity = (itemKey, newQuantity) => {
  if (newQuantity <= 0) {
    removeItemFromCart(itemKey)
  } else {
    updateQuantity(itemKey, newQuantity)
  }
}

const removeItemFromCart = (itemKey) => {
  removeItem(itemKey)
  
  // Show success toast
  if (window.$toast) {
    window.$toast.success(
      'Sản phẩm đã được xóa khỏi giỏ hàng',
      'Xóa thành công'
    )
  }
}

const applyVoucher = () => {
  // Check if user can apply voucher
  if (!canApplyVoucher()) {
    const restriction = authUtils.getVoucherRestrictionMessage()
    voucherMessage.value = {
      type: 'error',
      text: restriction.message
    }
    
    if (window.$toast) {
      window.$toast.warning(restriction.message, restriction.title)
    }
    return
  }

  // Apply voucher using composable
  const success = applyVoucherToCart(voucherCode.value, (code) => {
    // Mock voucher validation
    const validVouchers = {
      'WELCOME10': { discount: 0.1, type: 'percent' },
      'SAVE50K': { discount: 50000, type: 'fixed' },
      'NEWUSER': { discount: 0.15, type: 'percent' }
    }

    const voucher = validVouchers[code.toUpperCase()]
    
    if (voucher) {
      if (voucher.type === 'percent') {
        discountAmount.value = totalPrice.value * voucher.discount
      } else {
        discountAmount.value = voucher.discount
      }
      
      voucherMessage.value = {
        type: 'success',
        text: `Áp dụng thành công mã giảm giá ${code}`
      }
      
      return true
    } else {
      discountAmount.value = 0
      voucherMessage.value = {
        type: 'error',
        text: 'Mã giảm giá không hợp lệ'
      }
      
      if (window.$toast) {
        window.$toast.error(
          'Mã voucher không hợp lệ hoặc đã hết hạn',
          'Lỗi voucher'
        )
      }
      return false
    }
  })

  if (success) {
    if (window.$toast) {
      window.$toast.success(
        `Áp dụng thành công mã giảm giá ${voucherCode.value}`,
        'Áp dụng voucher'
      )
    }
  }
}
</script>

<style scoped>
/* Breadcrumb */
.modern-breadcrumb {
  background: none;
  padding: 0;
  margin: 0;
}

.breadcrumb-link {
  text-decoration: none;
  color: var(--auro-text-light);
  transition: color 0.3s ease;
  font-weight: 500;
}

.breadcrumb-link:hover {
  color: var(--auro-accent);
}

/* Section Divider */
.section-divider {
  width: 80px;
  height: 4px;
  background: var(--auro-gradient-accent);
  margin: 0 auto;
  border-radius: 2px;
}

/* Empty Cart */
.empty-cart-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-cart-content {
  text-align: center;
  padding: 3rem;
  background: var(--auro-card);
  border-radius: 20px;
  border: 1px solid var(--auro-border);
}

/* Cart Cards */
.modern-cart-card {
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  box-shadow: var(--auro-shadow);
}

.modern-cart-header {
  background: var(--auro-gradient);
  color: white;
  border-radius: 20px 20px 0 0;
  border: none;
  padding: 1.5rem;
}

.modern-cart-item {
  border-bottom: 1px solid var(--auro-border);
  transition: background-color 0.3s ease;
}

.modern-cart-item:hover {
  background: rgba(212, 175, 55, 0.05);
}

.modern-cart-item:last-child {
  border-bottom: none;
}

/* Product Image */
.cart-product-image {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cart-product-image img {
  width: 100%;
  height: 80px;
  object-fit: cover;
}

/* Product Info */
.cart-product-info h6 {
  color: var(--auro-text);
  font-weight: 600;
}

.product-attributes {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.attribute-badge {
  background: var(--auro-bg);
  color: var(--auro-text-light);
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  border: 1px solid var(--auro-border);
  display: flex;
  align-items: center;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #e9ecef;
  flex-shrink: 0;
}

/* Price */
.cart-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.price-amount {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--auro-accent);
}

.price-unit {
  font-size: 12px;
}

/* Quantity Controls */
.cart-quantity-controls {
  display: flex;
  align-items: center;
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  overflow: hidden;
}

.quantity-btn {
  border: none;
  background: var(--auro-card);
  color: var(--auro-text);
  padding: 8px 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.quantity-btn:hover {
  background: var(--auro-accent);
  color: var(--auro-dark);
}

.quantity-input {
  border: none;
  text-align: center;
  font-weight: 600;
  background: var(--auro-bg);
  color: var(--auro-text);
  width: 60px;
}

.quantity-input:focus {
  box-shadow: none;
  background: var(--auro-card);
}

/* Total & Actions */
.cart-total-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-total {
  text-align: right;
}

.total-amount {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--auro-accent);
}

.remove-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: #dc3545;
  border-color: #dc3545;
  color: white;
  transform: scale(1.1);
}

/* Continue Shopping */
.modern-continue-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.modern-continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Summary Card */
.modern-summary-card {
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  box-shadow: var(--auro-shadow);
  position: sticky;
  top: 100px;
}

.modern-summary-header {
  background: var(--auro-gradient);
  color: white;
  border-radius: 20px 20px 0 0;
  border: none;
  padding: 1.5rem;
}

.summary-section {
  padding: 1rem 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--auro-border);
}

.summary-item:last-of-type {
  border-bottom: none;
}

.summary-label {
  color: var(--auro-text);
  font-weight: 500;
}

.summary-value {
  color: var(--auro-text-light);
  font-weight: 600;
}

.discount-item {
  color: #28a745 !important;
}

.summary-divider {
  height: 2px;
  background: var(--auro-gradient-accent);
  margin: 1rem 0;
  border-radius: 1px;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  background: var(--auro-bg);
  border-radius: 12px;
  padding: 1rem;
  margin-top: 1rem;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--auro-text);
}

.total-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--auro-accent);
}

/* Voucher Section */
.voucher-section {
  padding: 1.5rem 0;
  border-top: 1px solid var(--auro-border);
  margin-top: 1rem;
}

.voucher-input-group {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.modern-voucher-input {
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 500;
  background: var(--auro-card);
}

.modern-voucher-input:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
}

.voucher-apply-btn {
  border-radius: 12px;
  padding: 12px 16px;
  min-width: 60px;
}

.voucher-message {
  font-size: 14px;
  font-weight: 500;
}

/* Checkout Section */
.checkout-section {
  padding: 1.5rem 0;
  border-top: 1px solid var(--auro-border);
}

.checkout-btn {
  border-radius: 12px;
  padding: 16px;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.3);
}

/* Security Info */
.security-info {
  padding: 1rem 0;
  border-top: 1px solid var(--auro-border);
}

.security-badges {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.security-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background: var(--auro-bg);
  border-radius: 12px;
  border: 1px solid var(--auro-border);
  flex: 1;
  transition: all 0.3s ease;
}

.security-badge:hover {
  background: var(--auro-card);
  transform: translateY(-2px);
}

.security-badge i {
  font-size: 20px;
  color: var(--auro-accent);
}

.security-badge span {
  font-size: 12px;
  font-weight: 500;
  color: var(--auro-text);
  text-align: center;
}

/* Shipping Card */
.modern-shipping-card {
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  box-shadow: var(--auro-shadow);
}

.shipping-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  font-size: 14px;
  color: var(--auro-text-light);
}

/* Responsive */
@media (max-width: 991.98px) {
  .modern-summary-card {
    position: static;
    margin-top: 2rem;
  }
  
  .security-badges {
    flex-direction: column;
  }
  
  .cart-total-actions {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .remove-btn {
    align-self: flex-end;
  }
}
</style>
