<template>
  <div class="cart">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            Giỏ hàng
          </li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <h1 class="h2 fw-bold mb-4">Giỏ hàng của bạn</h1>
        </div>
      </div>

      <!-- Empty Cart -->
      <div v-if="cartStore.isEmpty" class="text-center py-5">
        <i class="bi bi-cart-x display-1 text-muted"></i>
        <h5 class="mt-3">Giỏ hàng trống</h5>
        <p class="text-muted">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
        <router-link to="/category" class="btn btn-warning btn-lg">
          <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
        </router-link>
      </div>

      <!-- Cart Items -->
      <div v-else class="row">
        <!-- Cart Items List -->
        <div class="col-lg-8">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Sản phẩm trong giỏ hàng ({{ cartStore.itemCount }} sản phẩm)</h5>
            </div>
            <div class="card-body p-0">
              <div v-for="item in cartStore.items" :key="item.id" class="cart-item border-bottom">
                <div class="row align-items-center p-3">
                  <!-- Product Image -->
                  <div class="col-md-2">
                    <img :src="item.image || 'https://via.placeholder.com/80x80'" 
                         alt="Product" class="img-fluid rounded">
                  </div>
                  
                  <!-- Product Info -->
                  <div class="col-md-4">
                    <h6 class="mb-1">{{ item.name }}</h6>
                    <small class="text-muted">
                      <span v-if="item.selectedSize">Size: {{ item.selectedSize }}</span>
                      <span v-if="item.selectedSize && item.selectedColor">, </span>
                      <span v-if="item.selectedColor">Màu: {{ item.selectedColor }}</span>
                    </small>
                  </div>
                  
                  <!-- Price -->
                  <div class="col-md-2">
                    <span class="price">{{ formatPrice(item.price) }}</span>
                  </div>
                  
                  <!-- Quantity -->
                  <div class="col-md-2">
                    <div class="input-group">
                      <button class="btn btn-outline-secondary btn-sm" 
                              @click="updateQuantity(item.id, item.quantity - 1)">
                        -
                      </button>
                      <input type="number" 
                             class="form-control form-control-sm text-center" 
                             v-model.number="item.quantity"
                             @change="updateQuantity(item.id, item.quantity)"
                             min="1">
                      <button class="btn btn-outline-secondary btn-sm" 
                              @click="updateQuantity(item.id, item.quantity + 1)">
                        +
                      </button>
                    </div>
                  </div>
                  
                  <!-- Total & Actions -->
                  <div class="col-md-2">
                    <div class="d-flex justify-content-between align-items-center">
                      <span class="fw-bold">{{ formatPrice(item.price * item.quantity) }}</span>
                      <button class="btn btn-outline-danger btn-sm" 
                              @click="removeItem(item.id)">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Continue Shopping -->
          <div class="mt-3">
            <router-link to="/category" class="btn btn-outline-secondary">
              <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
            </router-link>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="col-lg-4">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Tóm tắt đơn hàng</h5>
            </div>
            <div class="card-body">
              <!-- Summary Items -->
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Tạm tính:</span>
                <span>{{ formatPrice(cartStore.totalPrice) }}</span>
              </div>
              
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Phí vận chuyển:</span>
                <span>{{ formatPrice(shippingFee) }}</span>
              </div>
              
              <div v-if="discountAmount > 0" class="summary-item d-flex justify-content-between mb-2 text-success">
                <span>Giảm giá:</span>
                <span>-{{ formatPrice(discountAmount) }}</span>
              </div>
              
              <hr>
              
              <div class="summary-total d-flex justify-content-between mb-4">
                <span class="fw-bold">Tổng cộng:</span>
                <span class="fw-bold text-warning">{{ formatPrice(finalTotal) }}</span>
              </div>

              <!-- Voucher Code -->
              <div class="mb-3">
                <label class="form-label">Mã giảm giá:</label>
                <div class="input-group">
                  <input v-model="voucherCode" 
                         type="text" 
                         class="form-control" 
                         placeholder="Nhập mã giảm giá">
                  <button class="btn btn-outline-secondary" 
                          @click="applyVoucher"
                          :disabled="!voucherCode.trim()">
                    Áp dụng
                  </button>
                </div>
                <div v-if="voucherMessage" 
                     class="mt-2 small"
                     :class="voucherMessage.type === 'success' ? 'text-success' : 'text-danger'">
                  {{ voucherMessage.text }}
                </div>
              </div>

              <!-- Checkout Button -->
              <router-link to="/checkout" class="btn btn-warning btn-lg w-100 mb-3">
                <i class="bi bi-credit-card me-2"></i>Thanh toán
              </router-link>

              <!-- Security Info -->
              <div class="text-center">
                <small class="text-muted">
                  <i class="bi bi-shield-check me-1"></i>
                  Thanh toán an toàn và bảo mật
                </small>
              </div>
            </div>
          </div>

          <!-- Shipping Info -->
          <div class="card mt-3">
            <div class="card-body">
              <h6 class="fw-bold mb-3">
                <i class="bi bi-truck me-2"></i>Thông tin giao hàng
              </h6>
              <div class="shipping-info">
                <div class="d-flex align-items-center mb-2">
                  <i class="bi bi-check-circle text-success me-2"></i>
                  <small>Miễn phí vận chuyển cho đơn hàng trên 500.000đ</small>
                </div>
                <div class="d-flex align-items-center mb-2">
                  <i class="bi bi-clock text-warning me-2"></i>
                  <small>Giao hàng trong 2-3 ngày làm việc</small>
                </div>
                <div class="d-flex align-items-center">
                  <i class="bi bi-arrow-clockwise text-info me-2"></i>
                  <small>Đổi trả miễn phí trong 30 ngày</small>
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
import { useCartStore } from '../stores/cart'

const cartStore = useCartStore()

// Reactive data
const voucherCode = ref('')
const voucherMessage = ref(null)
const discountAmount = ref(0)

// Computed
const shippingFee = computed(() => {
  return cartStore.totalPrice >= 500000 ? 0 : 30000
})

const finalTotal = computed(() => {
  return cartStore.totalPrice + shippingFee.value - discountAmount.value
})

// Methods
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const updateQuantity = (itemId, newQuantity) => {
  if (newQuantity <= 0) {
    removeItem(itemId)
  } else {
    cartStore.updateQuantity(itemId, newQuantity)
  }
}

const removeItem = (itemId) => {
  cartStore.removeItem(itemId)
}

const applyVoucher = () => {
  // Mock voucher validation
  const validVouchers = {
    'WELCOME10': { discount: 0.1, type: 'percent' },
    'SAVE50K': { discount: 50000, type: 'fixed' },
    'NEWUSER': { discount: 0.15, type: 'percent' }
  }

  const voucher = validVouchers[voucherCode.value.toUpperCase()]
  
  if (voucher) {
    if (voucher.type === 'percent') {
      discountAmount.value = cartStore.totalPrice * voucher.discount
    } else {
      discountAmount.value = voucher.discount
    }
    
    voucherMessage.value = {
      type: 'success',
      text: `Áp dụng thành công mã giảm giá ${voucherCode.value}`
    }
  } else {
    discountAmount.value = 0
    voucherMessage.value = {
      type: 'error',
      text: 'Mã giảm giá không hợp lệ'
    }
  }
}
</script>

<style scoped>
.cart-item:last-child {
  border-bottom: none !important;
}

.price {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffc107;
}

.summary-item {
  font-size: 0.9rem;
}

.summary-total {
  font-size: 1.1rem;
}

.shipping-info small {
  font-size: 0.8rem;
}

.breadcrumb {
  background: none;
  padding: 0;
}

.breadcrumb-item a {
  text-decoration: none;
  color: #6c757d;
}

.breadcrumb-item a:hover {
  color: #ffc107;
}

.input-group .btn {
  border-color: #dee2e6;
}

.input-group .btn:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
}
</style>
