<template>
  <div class="order-detail">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/orders">Đơn hàng</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            Chi tiết đơn hàng
          </li>
        </ol>
      </nav>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Đang tải...</span>
        </div>
        <p class="mt-3">Đang tải thông tin đơn hàng...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="alert alert-danger">
        <i class="bi bi-exclamation-triangle me-2"></i>
        {{ error }}
      </div>

      <!-- Order Detail Content -->
      <div v-else-if="order" class="order-content">
        <!-- Success Message for New Order -->
        <div v-if="isNewOrder" class="alert alert-success mb-4">
          <i class="bi bi-check-circle me-2"></i>
          <strong>Đặt hàng thành công!</strong> Đơn hàng của bạn đã được tạo và đang chờ xử lý.
        </div>

        <!-- Order Header -->
        <div class="row mb-4">
          <div class="col-md-8">
            <h1 class="h3 fw-bold mb-2">Đơn hàng #{{ order.soDonHang }}</h1>
            <p class="text-muted mb-0">
              Đặt lúc: {{ formatDate(order.taoLuc) }}
            </p>
          </div>
          <div class="col-md-4 text-md-end">
            <span :class="['badge', 'fs-6', getStatusClass(order.trangThai)]">
              {{ getStatusText(order.trangThai) }}
            </span>
          </div>
        </div>

        <div class="row">
          <!-- Left Column -->
          <div class="col-lg-8">
            <!-- Order Items -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-bag me-2"></i>Sản phẩm đã đặt
                </h5>
              </div>
              <div class="card-body">
                <div v-for="item in order.chiTietList" :key="item.id" class="order-item">
                  <div class="row align-items-center">
                    <div class="col-md-2">
                      <img 
                        :src="item.hinhAnh || 'https://via.placeholder.com/80'" 
                        :alt="item.tenSanPham"
                        class="img-fluid rounded"
                      >
                    </div>
                    <div class="col-md-6">
                      <h6 class="mb-1">{{ item.tenSanPham }}</h6>
                      <p class="text-muted small mb-0">{{ item.moTaBienThe }}</p>
                    </div>
                    <div class="col-md-2 text-center">
                      <span class="text-muted">x{{ item.soLuong }}</span>
                    </div>
                    <div class="col-md-2 text-end">
                      <strong>{{ formatPrice(item.donGia * item.soLuong) }}</strong>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Timeline -->
            <div class="card">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-clock-history me-2"></i>Trạng thái đơn hàng
                </h5>
              </div>
              <div class="card-body">
                <div class="timeline">
                  <div class="timeline-item active">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                      <h6>Đơn hàng đã được tạo</h6>
                      <p class="text-muted small">{{ formatDate(order.taoLuc) }}</p>
                    </div>
                  </div>
                  <div class="timeline-item" :class="{ active: isStatusActive('CONFIRMED') }">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                      <h6>Đã xác nhận</h6>
                      <p class="text-muted small">Đang chờ xác nhận</p>
                    </div>
                  </div>
                  <div class="timeline-item" :class="{ active: isStatusActive('SHIPPING') }">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                      <h6>Đang giao hàng</h6>
                      <p class="text-muted small">Đang chờ giao hàng</p>
                    </div>
                  </div>
                  <div class="timeline-item" :class="{ active: isStatusActive('DELIVERED') }">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                      <h6>Đã giao hàng</h6>
                      <p class="text-muted small">Hoàn tất đơn hàng</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column -->
          <div class="col-lg-4">
            <!-- Shipping Info -->
            <div class="card mb-4">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-geo-alt me-2"></i>Thông tin giao hàng
                </h6>
              </div>
              <div class="card-body">
                <p class="mb-2" v-html="formatAddress(order.diaChiGiaoSnapshot)"></p>
                <p v-if="order.ghiChu" class="text-muted small mb-0">
                  <strong>Ghi chú:</strong> {{ order.ghiChu }}
                </p>
              </div>
            </div>

            <!-- Payment Info -->
            <div class="card mb-4">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-credit-card me-2"></i>Thanh toán
                </h6>
              </div>
              <div class="card-body">
                <p class="mb-2">
                  <strong>Phương thức:</strong> 
                  {{ getPaymentMethodText(order.paymentMethod) }}
                </p>
                <p class="mb-0">
                  <strong>Trạng thái:</strong>
                  <span :class="['badge', 'ms-2', getPaymentStatusClass(order.paymentStatus)]">
                    {{ getPaymentStatusText(order.paymentStatus) }}
                  </span>
                </p>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-receipt me-2"></i>Tổng đơn hàng
                </h6>
              </div>
              <div class="card-body">
                <div class="d-flex justify-content-between mb-2">
                  <span>Tạm tính:</span>
                  <strong>{{ formatPrice(order.tamTinh) }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Phí vận chuyển:</span>
                  <strong>{{ formatPrice(order.tongThanhToan - order.tamTinh) }}</strong>
                </div>
                <hr>
                <div class="d-flex justify-content-between">
                  <strong>Tổng cộng:</strong>
                  <strong class="text-primary fs-5">{{ formatPrice(order.tongThanhToan) }}</strong>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="card mt-4" v-if="canCancelOrder">
              <div class="card-body text-center">
                <button class="btn btn-danger w-100" @click="cancelOrder">
                  <i class="bi bi-x-circle me-2"></i>
                  Hủy đơn hàng
                </button>
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
import { useRoute, useRouter } from 'vue-router'
import orderService from '@/services/orderService'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref(null)
const isNewOrder = ref(false)

const canCancelOrder = computed(() => {
  if (!order.value) return false
  return ['PENDING', 'CONFIRMED'].includes(order.value.trangThai)
})

onMounted(async () => {
  await fetchOrderDetail()
  // Check if this is a newly created order
  isNewOrder.value = route.query.new === 'true'
})

const fetchOrderDetail = async () => {
  try {
    loading.value = true
    error.value = null
    
    const orderId = route.params.id
    const response = await orderService.getOrderById(orderId)
    
    console.log('Order detail response:', response)
    order.value = response
    
  } catch (err) {
    console.error('Error fetching order:', err)
    error.value = err.message || 'Không thể tải thông tin đơn hàng'
  } finally {
    loading.value = false
  }
}

const cancelOrder = async () => {
  if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) return
  
  try {
    await orderService.cancelOrder(order.value.id, 'Khách hàng hủy đơn')
    
    if (window.$toast) {
      window.$toast.success('Đơn hàng đã được hủy')
    }
    
    // Refresh order detail
    await fetchOrderDetail()
  } catch (err) {
    console.error('Error canceling order:', err)
    if (window.$toast) {
      window.$toast.error(err.message || 'Không thể hủy đơn hàng')
    }
  }
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatAddress = (address) => {
  if (!address) return ''
  return address.replace(/\n/g, '<br>')
}

const getStatusClass = (status) => {
  const statusMap = {
    'PENDING': 'bg-warning',
    'CONFIRMED': 'bg-info',
    'SHIPPING': 'bg-primary',
    'DELIVERED': 'bg-success',
    'CANCELLED': 'bg-danger'
  }
  return statusMap[status] || 'bg-secondary'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'SHIPPING': 'Đang giao',
    'DELIVERED': 'Đã giao',
    'CANCELLED': 'Đã hủy'
  }
  return statusMap[status] || status
}

const isStatusActive = (status) => {
  if (!order.value) return false
  
  const statusOrder = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED']
  const currentIndex = statusOrder.indexOf(order.value.trangThai)
  const checkIndex = statusOrder.indexOf(status)
  
  return checkIndex <= currentIndex
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'COD': 'Thanh toán khi nhận hàng',
    'VNPAY': 'VNPay',
    'MOMO': 'MoMo',
    'BANK_TRANSFER': 'Chuyển khoản ngân hàng'
  }
  return methodMap[method] || method
}

const getPaymentStatusClass = (status) => {
  const statusMap = {
    'PENDING': 'bg-warning',
    'PAID': 'bg-success',
    'FAILED': 'bg-danger'
  }
  return statusMap[status] || 'bg-secondary'
}

const getPaymentStatusText = (status) => {
  const statusMap = {
    'PENDING': 'Chờ thanh toán',
    'PAID': 'Đã thanh toán',
    'FAILED': 'Thất bại'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.order-detail {
  min-height: 80vh;
  background-color: #f8f9fa;
}

.card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 1.5rem;
}

.card-header {
  background-color: white;
  border-bottom: 1px solid #e9ecef;
  padding: 1rem 1.5rem;
}

.order-item {
  padding: 1rem 0;
  border-bottom: 1px solid #e9ecef;
}

.order-item:last-child {
  border-bottom: none;
}

.timeline {
  position: relative;
  padding-left: 2rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0.5rem;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e9ecef;
}

.timeline-item {
  position: relative;
  padding-bottom: 2rem;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-marker {
  position: absolute;
  left: -1.65rem;
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  background: #e9ecef;
  border: 2px solid white;
  z-index: 1;
}

.timeline-item.active .timeline-marker {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.timeline-content h6 {
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.timeline-item.active .timeline-content h6 {
  color: #667eea;
}

.badge {
  padding: 0.5rem 1rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .order-detail .col-md-2.text-end {
    text-align: left !important;
    margin-top: 0.5rem;
  }
}
</style>
