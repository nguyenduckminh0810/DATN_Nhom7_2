<template>
  <div class="orders">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            Đơn hàng của tôi
          </li>
        </ol>
      </nav>

      <div class="row">
        <div class="col-12">
          <h1 class="h2 fw-bold mb-4">Đơn hàng của tôi</h1>
        </div>
      </div>

      <div class="row">
        <!-- Orders Sidebar -->
        <div class="col-lg-3 mb-4">
          <div class="card">
            <div class="card-body text-center">
              <div class="profile-avatar mb-3">
                <img :src="user.avatar || 'https://via.placeholder.com/100x100/6c757d/ffffff?text=Avatar'" 
                     alt="Avatar" class="rounded-circle" width="100" height="100">
              </div>
              <h5 class="mb-1">{{ user.name }}</h5>
              <p class="text-muted small mb-3">{{ user.email }}</p>
            </div>
          </div>

          <!-- Navigation Menu -->
          <div class="card mt-3">
            <div class="list-group list-group-flush">
              <a href="#" class="list-group-item list-group-item-action">
                <i class="bi bi-person me-2"></i>Thông tin cá nhân
              </a>
              <router-link to="/orders" class="list-group-item list-group-item-action active">
                <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
              </router-link>
              <a href="#" class="list-group-item list-group-item-action">
                <i class="bi bi-heart me-2"></i>Sản phẩm yêu thích
              </a>
              <a href="#" class="list-group-item list-group-item-action">
                <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
              </a>
              <a href="#" class="list-group-item list-group-item-action">
                <i class="bi bi-credit-card me-2"></i>Phương thức thanh toán
              </a>
              <a href="#" class="list-group-item list-group-item-action">
                <i class="bi bi-bell me-2"></i>Thông báo
              </a>
            </div>
          </div>
        </div>

        <!-- Orders Content -->
        <div class="col-lg-9">
          <!-- Filter Tabs -->
          <div class="card mb-4">
            <div class="card-body">
              <ul class="nav nav-pills nav-fill" role="tablist">
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'all' }"
                          @click="activeTab = 'all'">
                    Tất cả ({{ orders.length }})
                  </button>
                </li>
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'pending' }"
                          @click="activeTab = 'pending'">
                    Chờ xử lý ({{ getOrdersByStatus('pending').length }})
                  </button>
                </li>
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'processing' }"
                          @click="activeTab = 'processing'">
                    Đang xử lý ({{ getOrdersByStatus('processing').length }})
                  </button>
                </li>
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'shipped' }"
                          @click="activeTab = 'shipped'">
                    Đang giao ({{ getOrdersByStatus('shipped').length }})
                  </button>
                </li>
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'delivered' }"
                          @click="activeTab = 'delivered'">
                    Đã giao ({{ getOrdersByStatus('delivered').length }})
                  </button>
                </li>
                <li class="nav-item" role="presentation">
                  <button class="nav-link" 
                          :class="{ active: activeTab === 'cancelled' }"
                          @click="activeTab = 'cancelled'">
                    Đã hủy ({{ getOrdersByStatus('cancelled').length }})
                  </button>
                </li>
              </ul>
            </div>
          </div>

          <!-- Orders List -->
          <div v-if="filteredOrders.length > 0">
            <div v-for="order in filteredOrders" :key="order.id" class="card mb-3">
              <div class="card-body">
                <!-- Order Header -->
                <div class="row align-items-center mb-3">
                  <div class="col-md-6">
                    <h6 class="mb-1">Đơn hàng #{{ order.orderNumber }}</h6>
                    <small class="text-muted">Đặt ngày {{ formatDate(order.orderDate) }}</small>
                  </div>
                  <div class="col-md-6 text-md-end">
                    <span :class="getStatusClass(order.status)" class="badge">
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>
                </div>

                <!-- Order Items -->
                <div class="row">
                  <div class="col-md-8">
                    <div class="order-items">
                      <div v-for="item in order.items" :key="item.id" class="order-item d-flex align-items-center mb-2">
                        <img :src="item.image" 
                             alt="Product" 
                             class="me-3" 
                             width="60" 
                             height="60" 
                             style="object-fit: cover;">
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
                  
                  <!-- Order Summary -->
                  <div class="col-md-4">
                    <div class="order-summary">
                      <div class="d-flex justify-content-between mb-1">
                        <small>Tạm tính:</small>
                        <small>{{ formatPrice(order.subtotal) }}</small>
                      </div>
                      <div class="d-flex justify-content-between mb-1">
                        <small>Phí vận chuyển:</small>
                        <small>{{ formatPrice(order.shippingFee) }}</small>
                      </div>
                      <div v-if="order.discount > 0" class="d-flex justify-content-between mb-1 text-success">
                        <small>Giảm giá:</small>
                        <small>-{{ formatPrice(order.discount) }}</small>
                      </div>
                      <hr class="my-2">
                      <div class="d-flex justify-content-between">
                        <strong>Tổng cộng:</strong>
                        <strong class="text-warning">{{ formatPrice(order.total) }}</strong>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Order Actions -->
                <div class="row mt-3">
                  <div class="col-12">
                    <div class="d-flex gap-2 flex-wrap">
                      <button class="btn btn-outline-primary btn-sm" @click="viewOrderDetail(order)">
                        <i class="bi bi-eye me-1"></i>Xem chi tiết
                      </button>
                      
                      <button v-if="order.status === 'pending'" 
                              class="btn btn-outline-danger btn-sm" 
                              @click="cancelOrder(order)">
                        <i class="bi bi-x-circle me-1"></i>Hủy đơn hàng
                      </button>
                      
                      <button v-if="order.status === 'delivered'" 
                              class="btn btn-outline-success btn-sm" 
                              @click="reorder(order)">
                        <i class="bi bi-arrow-repeat me-1"></i>Mua lại
                      </button>
                      
                      <button v-if="order.status === 'delivered'" 
                              class="btn btn-outline-warning btn-sm" 
                              @click="rateOrder(order)">
                        <i class="bi bi-star me-1"></i>Đánh giá
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="text-center py-5">
            <i class="bi bi-bag display-1 text-muted"></i>
            <h5 class="mt-3">Chưa có đơn hàng nào</h5>
            <p class="text-muted">Bạn chưa có đơn hàng nào trong trạng thái này</p>
            <router-link to="/category" class="btn btn-warning">
              <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// Reactive data
const activeTab = ref('all')
const user = ref({
  id: 1,
  name: 'Nguyễn Văn A',
  email: 'user@example.com',
  avatar: null
})

// Mock orders data
const orders = ref([
  {
    id: 1,
    orderNumber: 'ORD001',
    orderDate: '2024-01-15',
    status: 'delivered',
    subtotal: 900000,
    shippingFee: 0,
    discount: 100000,
    total: 800000,
    items: [
      {
        id: 1,
        name: 'Áo sơ mi nam cao cấp',
        image: 'https://via.placeholder.com/60x60/6c757d/ffffff?text=Áo',
        price: 450000,
        quantity: 2,
        selectedSize: 'L',
        selectedColor: 'Trắng'
      }
    ]
  },
  {
    id: 2,
    orderNumber: 'ORD002',
    orderDate: '2024-01-20',
    status: 'shipped',
    subtotal: 650000,
    shippingFee: 30000,
    discount: 0,
    total: 680000,
    items: [
      {
        id: 2,
        name: 'Quần âu nam',
        image: 'https://via.placeholder.com/60x60/6c757d/ffffff?text=Quần',
        price: 650000,
        quantity: 1,
        selectedSize: 'M',
        selectedColor: 'Đen'
      }
    ]
  },
  {
    id: 3,
    orderNumber: 'ORD003',
    orderDate: '2024-01-25',
    status: 'pending',
    subtotal: 850000,
    shippingFee: 0,
    discount: 0,
    total: 850000,
    items: [
      {
        id: 3,
        name: 'Áo khoác nam',
        image: 'https://via.placeholder.com/60x60/6c757d/ffffff?text=Áo+khoác',
        price: 850000,
        quantity: 1,
        selectedSize: 'XL',
        selectedColor: 'Xám'
      }
    ]
  }
])

// Computed
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value
  }
  return getOrdersByStatus(activeTab.value)
})

// Methods
const getOrdersByStatus = (status) => {
  return orders.value.filter(order => order.status === status)
}

const getStatusClass = (status) => {
  const statusClasses = {
    pending: 'bg-warning',
    processing: 'bg-info',
    shipped: 'bg-primary',
    delivered: 'bg-success',
    cancelled: 'bg-danger'
  }
  return statusClasses[status] || 'bg-secondary'
}

const getStatusText = (status) => {
  const statusTexts = {
    pending: 'Chờ xử lý',
    processing: 'Đang xử lý',
    shipped: 'Đang giao',
    delivered: 'Đã giao',
    cancelled: 'Đã hủy'
  }
  return statusTexts[status] || 'Không xác định'
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const viewOrderDetail = (order) => {
  // TODO: Implement order detail modal or page
  alert(`Xem chi tiết đơn hàng #${order.orderNumber}`)
}

const cancelOrder = (order) => {
  if (confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${order.orderNumber}?`)) {
    order.status = 'cancelled'
    alert('Đơn hàng đã được hủy')
  }
}

const reorder = (order) => {
  // TODO: Implement reorder functionality
  alert(`Mua lại đơn hàng #${order.orderNumber}`)
}

const rateOrder = (order) => {
  // TODO: Implement rating functionality
  alert(`Đánh giá đơn hàng #${order.orderNumber}`)
}

// Lifecycle
onMounted(() => {
  // Load user data
  const storedUser = localStorage.getItem('auro_user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
  
  // TODO: Load orders from API
})
</script>

<style scoped>
.profile-avatar img {
  object-fit: cover;
}

.list-group-item-action.active {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

.list-group-item-action:hover {
  background-color: #f8f9fa;
}

.nav-pills .nav-link {
  border-radius: 0.5rem;
  color: #6c757d;
  font-weight: 500;
}

.nav-pills .nav-link.active {
  background-color: #ffc107;
  color: #212529;
}

.order-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.order-item:last-child {
  border-bottom: none;
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

.btn-sm {
  font-size: 0.8rem;
}
</style>
