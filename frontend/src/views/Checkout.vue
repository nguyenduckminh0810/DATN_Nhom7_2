<template>
  <div class="checkout">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/cart">Giỏ hàng</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            Thanh toán
          </li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <h1 class="h2 fw-bold mb-4">Thanh toán</h1>
        </div>
      </div>

      <!-- Empty Cart Check -->
      <div v-if="cartStore.isEmpty" class="text-center py-5">
        <i class="bi bi-cart-x display-1 text-muted"></i>
        <h5 class="mt-3">Giỏ hàng trống</h5>
        <p class="text-muted">Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán</p>
        <router-link to="/category" class="btn btn-warning btn-lg">
          <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
        </router-link>
      </div>

      <!-- Checkout Form -->
      <div v-else class="row">
        <!-- Checkout Form -->
        <div class="col-lg-8">
          <form @submit.prevent="handleCheckout">
            <!-- Customer Information -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-person me-2"></i>Thông tin khách hàng
                </h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Họ và tên *</label>
                    <input v-model="form.fullName" 
                           type="text" 
                           class="form-control" 
                           required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input v-model="form.phone" 
                           type="tel" 
                           class="form-control" 
                           required>
                  </div>
                  <div class="col-12 mb-3">
                    <label class="form-label">Email</label>
                    <input v-model="form.email" 
                           type="email" 
                           class="form-control">
                  </div>
                </div>
              </div>
            </div>

            <!-- Shipping Address -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
                </h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Tỉnh/Thành phố *</label>
                    <select v-model="form.city" class="form-select" required>
                      <option value="">Chọn tỉnh/thành phố</option>
                      <option value="hcm">TP. Hồ Chí Minh</option>
                      <option value="hn">Hà Nội</option>
                      <option value="dn">Đà Nẵng</option>
                      <option value="hp">Hải Phòng</option>
                    </select>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Quận/Huyện *</label>
                    <select v-model="form.district" class="form-select" required>
                      <option value="">Chọn quận/huyện</option>
                      <option value="q1">Quận 1</option>
                      <option value="q2">Quận 2</option>
                      <option value="q3">Quận 3</option>
                      <option value="q4">Quận 4</option>
                    </select>
                  </div>
                  <div class="col-12 mb-3">
                    <label class="form-label">Địa chỉ chi tiết *</label>
                    <textarea v-model="form.address" 
                              class="form-control" 
                              rows="3" 
                              placeholder="Số nhà, tên đường, phường/xã..."
                              required>
                    </textarea>
                  </div>
                </div>
              </div>
            </div>

            <!-- Payment Method -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-credit-card me-2"></i>Phương thức thanh toán
                </h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <div class="form-check">
                      <input v-model="form.paymentMethod" 
                             class="form-check-input" 
                             type="radio" 
                             value="cod" 
                             id="cod">
                      <label class="form-check-label" for="cod">
                        <i class="bi bi-cash me-2"></i>Thanh toán khi nhận hàng (COD)
                      </label>
                    </div>
                  </div>
                  <div class="col-md-6 mb-3">
                    <div class="form-check">
                      <input v-model="form.paymentMethod" 
                             class="form-check-input" 
                             type="radio" 
                             value="bank" 
                             id="bank">
                      <label class="form-check-label" for="bank">
                        <i class="bi bi-bank me-2"></i>Chuyển khoản ngân hàng
                      </label>
                    </div>
                  </div>
                  <div class="col-md-6 mb-3">
                    <div class="form-check">
                      <input v-model="form.paymentMethod" 
                             class="form-check-input" 
                             type="radio" 
                             value="momo" 
                             id="momo">
                      <label class="form-check-label" for="momo">
                        <i class="bi bi-phone me-2"></i>Ví MoMo
                      </label>
                    </div>
                  </div>
                  <div class="col-md-6 mb-3">
                    <div class="form-check">
                      <input v-model="form.paymentMethod" 
                             class="form-check-input" 
                             type="radio" 
                             value="vnpay" 
                             id="vnpay">
                      <label class="form-check-label" for="vnpay">
                        <i class="bi bi-credit-card me-2"></i>VNPay
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Notes -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-chat-text me-2"></i>Ghi chú đơn hàng
                </h5>
              </div>
              <div class="card-body">
                <textarea v-model="form.notes" 
                          class="form-control" 
                          rows="3" 
                          placeholder="Ghi chú thêm cho đơn hàng (không bắt buộc)">
                </textarea>
              </div>
            </div>

            <!-- Submit Button -->
            <div class="d-flex gap-3">
              <router-link to="/cart" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-2"></i>Quay lại giỏ hàng
              </router-link>
              <button type="submit" 
                      class="btn btn-warning btn-lg flex-grow-1"
                      :disabled="isSubmitting">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-check-circle me-2"></i>
                {{ isSubmitting ? 'Đang xử lý...' : 'Đặt hàng' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Order Summary -->
        <div class="col-lg-4">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Tóm tắt đơn hàng</h5>
            </div>
            <div class="card-body">
              <!-- Order Items -->
              <div class="order-items mb-3">
                <h6 class="fw-bold mb-3">Sản phẩm ({{ cartStore.itemCount }})</h6>
                <div v-for="item in cartStore.items" :key="item.id" class="order-item mb-2">
                  <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                      <h6 class="mb-1 small">{{ item.name }}</h6>
                      <small class="text-muted">
                        <span v-if="item.selectedSize">Size: {{ item.selectedSize }}</span>
                        <span v-if="item.selectedSize && item.selectedColor">, </span>
                        <span v-if="item.selectedColor">Màu: {{ item.selectedColor }}</span>
                      </small>
                    </div>
                    <div class="text-end">
                      <small class="text-muted">x{{ item.quantity }}</small>
                      <div class="small fw-bold">{{ formatPrice(item.price * item.quantity) }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <hr>

              <!-- Summary -->
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Tạm tính:</span>
                <span>{{ formatPrice(cartStore.totalPrice) }}</span>
              </div>
              
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Phí vận chuyển:</span>
                <span>{{ formatPrice(shippingFee) }}</span>
              </div>
              
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Giảm giá:</span>
                <span class="text-success">-{{ formatPrice(discountAmount) }}</span>
              </div>
              
              <hr>
              
              <div class="summary-total d-flex justify-content-between mb-3">
                <span class="fw-bold">Tổng cộng:</span>
                <span class="fw-bold text-warning h5">{{ formatPrice(finalTotal) }}</span>
              </div>

              <!-- Security Info -->
              <div class="text-center">
                <small class="text-muted">
                  <i class="bi bi-shield-check me-1"></i>
                  Thông tin thanh toán được bảo mật
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
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// Reactive data
const isSubmitting = ref(false)
const discountAmount = ref(0)

const form = ref({
  fullName: '',
  phone: '',
  email: '',
  city: '',
  district: '',
  address: '',
  paymentMethod: 'cod',
  notes: ''
})

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

const handleCheckout = async () => {
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Create order data
    const orderData = {
      ...form.value,
      items: cartStore.items,
      subtotal: cartStore.totalPrice,
      shippingFee: shippingFee.value,
      discount: discountAmount.value,
      total: finalTotal.value,
      orderDate: new Date().toISOString()
    }
    
    // TODO: Send order to API
    console.log('Order data:', orderData)
    
    // Clear cart
    cartStore.clearCart()
    
    // Redirect to success page
    router.push({
      name: 'order-success',
      params: { orderId: 'ORD' + Date.now() }
    })
    
  } catch (error) {
    console.error('Checkout error:', error)
    alert('Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.order-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.order-item:last-child {
  border-bottom: none;
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

.form-check-input:checked {
  background-color: #ffc107;
  border-color: #ffc107;
}

.form-check-label {
  cursor: pointer;
}
</style>
