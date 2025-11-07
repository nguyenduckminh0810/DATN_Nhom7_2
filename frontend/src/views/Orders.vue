<template>
  <div class="orders">
    <div class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">Đơn hàng của tôi</li>
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
                <img
                  :src="
                    user.avatar || 'https://via.placeholder.com/100x100/6c757d/ffffff?text=Avatar'
                  "
                  alt="Avatar"
                  class="rounded-circle"
                  width="100"
                  height="100"
                />
              </div>
              <h5 class="mb-1">{{ user.name }}</h5>
              <p class="text-muted small mb-3">{{ user.email }}</p>
            </div>
          </div>

          <!-- Navigation Menu -->
          <div class="card mt-3">
            <div class="list-group list-group-flush">
              <router-link to="/profile" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-person me-2"></i>Thông tin cá nhân
              </router-link>
              <router-link to="/orders" class="list-group-item list-group-item-action active py-3">
                <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
              </router-link>
              <router-link to="/wishlist" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-heart me-2"></i>Sản phẩm yêu thích
              </router-link>
              <router-link
                to="/profile/addresses"
                class="list-group-item list-group-item-action py-3"
              >
                <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
              </router-link>
              <a href="#" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-credit-card me-2"></i>Phương thức thanh toán
              </a>
              <a href="#" class="list-group-item list-group-item-action py-3">
                <i class="bi bi-bell me-2"></i>Thông báo
              </a>
              <a
                href="#"
                class="list-group-item list-group-item-action text-danger py-3"
                @click.prevent="logout"
              >
                <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
              </a>
            </div>
          </div>
        </div>

        <!-- Orders Content -->
        <div class="col-lg-9">
          <!-- Filter Tabs -->
          <div class="card mb-4">
            <div class="card-body">
              <div class="orders-search mb-3">
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-search"></i></span>
                  <input
                    v-model="searchQuery"
                    type="text"
                    class="form-control"
                    placeholder="Tìm theo mã đơn, trạng thái hoặc sản phẩm"
                    aria-label="Tìm kiếm đơn hàng"
                  />
                  <button
                    v-if="searchQuery"
                    type="button"
                    class="btn btn-outline-secondary"
                    @click="searchQuery = ''"
                  >
                    Xóa
                  </button>
                </div>
              </div>

              <div class="status-tabs">
                <div
                  v-for="(row, rowIndex) in statusRows"
                  :key="rowIndex"
                  class="nav nav-pills gap-2 flex-wrap justify-content-center status-tabs__row"
                >
                  <button
                    v-for="option in row"
                    :key="option.code"
                    class="nav-link status-tabs__button"
                    :class="{ active: activeTab === option.code }"
                    @click="activeTab = option.code"
                  >
                    {{ option.label }} ({{ getStatusCount(option.code) }})
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-warning" role="status">
              <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="mt-3 text-muted">Đang tải đơn hàng...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="alert alert-danger">
            <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
            <button class="btn btn-sm btn-outline-danger ms-3" @click="fetchOrders">
              <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
            </button>
          </div>

          <!-- Orders List -->
          <div v-else-if="filteredOrders.length > 0">
            <div v-for="order in filteredOrders" :key="order.id" class="card mb-3">
              <div class="card-body">
                <!-- Order Header -->
                <div class="row align-items-center mb-3">
                  <div class="col-md-6">
                    <h6 class="mb-1">Đơn hàng #{{ order.orderNumber }}</h6>
                    <small class="text-muted">Đặt ngày {{ formatDate(order.orderDate) }}</small>
                  </div>
                  <div class="col-md-6 text-md-end">
                    <span :class="['badge', order.statusClass]">
                      {{ order.statusLabel || getOrderStatusLabel(order.status) }}
                    </span>
                  </div>
                </div>

                <!-- Order Items -->
                <div class="row">
                  <div class="col-md-8">
                    <div class="order-items">
                      <div
                        v-for="item in order.items"
                        :key="item.id"
                        class="order-item d-flex align-items-center mb-2"
                      >
                        <img
                          :src="item.image"
                          alt="Product"
                          class="me-3"
                          width="60"
                          height="60"
                          style="object-fit: cover"
                        />
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
                          <div class="small fw-bold">
                            {{ formatPrice(item.price * item.quantity) }}
                          </div>
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
                      <div
                        v-if="order.discount > 0"
                        class="d-flex justify-content-between mb-1 text-success"
                      >
                        <small>Giảm giá:</small>
                        <small>-{{ formatPrice(order.discount) }}</small>
                      </div>
                      <hr class="my-2" />
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
                      <button
                        class="btn btn-outline-primary btn-sm"
                        @click="viewOrderDetail(order)"
                      >
                        <i class="bi bi-eye me-1"></i>Xem chi tiết
                      </button>

                      <button
                        v-if="order.status === 'PENDING'"
                        class="btn btn-outline-danger btn-sm"
                        @click="cancelOrder(order)"
                      >
                        <i class="bi bi-x-circle me-1"></i>Hủy đơn hàng
                      </button>

                      <button
                        v-if="['DELIVERED', 'COMPLETED'].includes(order.status)"
                        class="btn btn-outline-success btn-sm"
                        @click="reorder(order)"
                      >
                        <i class="bi bi-arrow-repeat me-1"></i>Mua lại
                      </button>

                      <button
                        v-if="['DELIVERED', 'COMPLETED'].includes(order.status)"
                        class="btn btn-outline-warning btn-sm"
                        @click="rateOrder(order)"
                      >
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import orderService from '@/services/orderService'
import {
  ORDER_STATUS_FOR_CUSTOMER,
  normalizeOrderStatus,
  getOrderStatusLabel,
} from '@/utils/orderStatus'

defineOptions({
  name: 'OrdersView',
})

const router = useRouter()
const userStore = useUserStore()

// Reactive data
const statusTabs = ORDER_STATUS_FOR_CUSTOMER.slice().sort(
  (a, b) => a.sortOrder - b.sortOrder,
)

const STATUS_PER_ROW = 4

const statusOptions = computed(() => [
  {
    code: 'ALL',
    label: 'Tất cả',
    isAll: true,
  },
  ...statusTabs.map((status) => ({
    code: status.code,
    label: status.label,
    isAll: false,
  })),
])

const statusRows = computed(() => {
  const rows = []
  const options = statusOptions.value
  for (let i = 0; i < options.length; i += STATUS_PER_ROW) {
    rows.push(options.slice(i, i + STATUS_PER_ROW))
  }
  return rows
})

const activeTab = ref('ALL')
const orders = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')

const user = computed(() => ({
  id: userStore.user?.id || null,
  name: userStore.user?.name || userStore.user?.hoTen || 'Người dùng',
  email: userStore.user?.email || '',
  avatar: userStore.user?.avatar || null,
}))

// Computed
const filteredOrders = computed(() => {
  const baseOrders =
    activeTab.value === 'ALL' ? orders.value : getOrdersByStatus(activeTab.value)

  const query = searchQuery.value.trim().toLowerCase()
  if (!query) {
    return baseOrders
  }

  return baseOrders.filter((order) => {
    const searchableFields = [
      order.orderNumber,
      order.statusLabel || order.rawStatus || '',
      order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : '',
      order.items?.map((item) => item.name || '').join(' ') || '',
    ]

    return searchableFields.some((field) =>
      (field || '').toString().toLowerCase().includes(query),
    )
  })
})

// Methods
const getOrdersByStatus = (status) => {
  return orders.value.filter((order) => order.status === status)
}

const parseAmount = (value) => {
  if (value == null) {
    return 0
  }

  const numberValue = Number(value)
  return Number.isNaN(numberValue) ? 0 : numberValue
}

const calculateOrderTotal = (subtotal, shipping, discount) => {
  const total = parseAmount(subtotal) - parseAmount(discount) + parseAmount(shipping)
  return total > 0 ? total : 0
}

const getStatusCount = (statusCode) => {
  if (statusCode === 'ALL') {
    return orders.value.length
  }
  return getOrdersByStatus(statusCode).length
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const viewOrderDetail = (order) => {
  router.push(`/orders/${order.id}`)
}

const cancelOrder = async (order) => {
  if (!confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${order.orderNumber}?`)) {
    return
  }

  try {
    loading.value = true
    await orderService.cancelOrder(order.id)
    alert('Đơn hàng đã được hủy thành công!')
    // Reload orders
    await fetchOrders()
  } catch (err) {
    console.error('Error canceling order:', err)
    alert('Lỗi khi hủy đơn hàng: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

const reorder = async (order) => {
  try {
    loading.value = true
    // Import cartService if not already
    const { default: cartService } = await import('@/services/cartService')

    // Add items back to cart
    for (const item of order.items) {
      await cartService.addToCart({
        sanPhamId: item.sanPhamId,
        soLuong: item.quantity,
        mauSacId: item.mauSacId,
        kichCoId: item.kichCoId,
      })
    }
    alert('Đã thêm sản phẩm vào giỏ hàng!')
    router.push('/cart')
  } catch (err) {
    console.error('Error reordering:', err)
    alert('Lỗi khi đặt lại đơn hàng: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

const rateOrder = (order) => {
  // TODO: Implement rating modal/page
  alert(`Đánh giá đơn hàng #${order.orderNumber}`)
}

const logout = () => {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    userStore.logout()
    router.push('/login')
  }
}

// Fetch orders from API
const fetchOrders = async () => {
  try {
    loading.value = true
    error.value = null

    console.log('🔄 Fetching orders...')
    console.log('🔑 Current user:', userStore.user)
    console.log('🔑 Token exists:', !!localStorage.getItem('auro_token'))

    const response = await orderService.getMyOrders()
    console.log('📦 Full API response:', response)
    console.log('📦 Response type:', typeof response)
    console.log('📦 Response keys:', response ? Object.keys(response) : 'null')
    console.log('📦 Response.data:', response.data)
    console.log('📦 Response.data type:', typeof response.data)
    console.log('📦 Response.data.content:', response.data?.content)
    console.log('📦 Response.data.totalElements:', response.data?.totalElements)

    // Backend returns paginated data
    let orderData = response.data?.content || response.data || []

    // Check if response is the paginated object itself
    if (response.content && Array.isArray(response.content)) {
      console.log('⚠️ Response is paginated object itself, using response.content')
      orderData = response.content
    }

    console.log('📋 Order data array:', orderData)
    console.log('📋 Order data length:', orderData.length)
    console.log('📋 Is array?:', Array.isArray(orderData))

    // Map backend data to frontend structure
    orders.value = orderData.map((order) => {
      const statusInfo = normalizeOrderStatus(order.trangThai)
      const subtotal = order.tamTinh || 0
      const shippingFee = order.phiVanChuyen || 0
      const discount = order.giamGiaTong || 0
      const total =
        order.tongThanhToan != null
          ? parseAmount(order.tongThanhToan)
          : calculateOrderTotal(subtotal, shippingFee, discount)

      return {
        id: order.id,
        orderNumber: order.soDonHang || `ORD${order.id}`,
        orderDate: order.taoLuc || order.createdAt,
        status: statusInfo.code,
        statusLabel: statusInfo.label,
        statusClass: statusInfo.badgeClass,
        rawStatus: order.trangThai,
        subtotal,
        shippingFee,
        discount,
        total,
        paymentStatus: order.paymentStatus,
        paymentMethod: order.paymentMethod,
        items:
          order.chiTietList?.map((item) => ({
            id: item.id,
            name: item.tenSanPham || 'Sản phẩm',
            image:
              item.hinhAnh ||
              'https://via.placeholder.com/60x60/6c757d/ffffff?text=Product',
            price: item.donGia || 0,
            quantity: item.soLuong || 1,
            subtotal: item.thanhTien || 0,
            selectedSize: '', // Backend không có thông tin này
            selectedColor: '', // Backend không có thông tin này
          })) || [],
      }
    })

    console.log('✅ Mapped orders:', orders.value)
    console.log('📊 First order details:', orders.value[0])
    if (orders.value.length > 0) {
      console.log('💰 Shipping fee:', orders.value[0].shippingFee)
      console.log('🖼️ First item image:', orders.value[0].items[0]?.image)
    }

    // Debug: Check if user actually has no orders
    if (orders.value.length === 0) {
      console.log('⚠️ No orders found for this user')
      console.log('💡 Possible reasons:')
      console.log('   1. No orders placed yet')
      console.log('   2. Orders were placed as GUEST (different khachHangId)')
      console.log('   3. User account not linked to KhachHang properly')
      console.log('')
      console.log('🔍 If you just placed an order via guest-checkout:')
      console.log('   - The order was created with a new GUEST KhachHang record')
      console.log("   - It won't show here because it's not linked to your logged-in account")
      console.log('   - Need to use authenticated checkout endpoint instead')
    }
  } catch (err) {
    console.error('❌ Error fetching orders:', err)
    console.error('Error details:', {
      message: err.message,
      response: err.response,
      status: err.status,
      type: err.type,
    })

    // Show more detailed error message
    if (err.status === 500) {
      error.value =
        'Lỗi server. Vui lòng kiểm tra: \n1. Bạn đã đăng nhập chưa? \n2. Tài khoản có liên kết với khách hàng không?'
    } else {
      error.value = err.message || 'Không thể tải danh sách đơn hàng. Vui lòng thử lại sau.'
    }
    orders.value = []
  } finally {
    loading.value = false
  }
}

// Map backend status to frontend status
// Lifecycle
onMounted(async () => {
  await fetchOrders()
})
</script>

<style scoped>
.profile-avatar img {
  object-fit: cover;
  border: 2px solid #ffc107;
}

.list-group-item-action.active {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000;
  font-weight: 500;
}

.list-group-item-action:hover {
  background-color: #f8f9fa;
}

.nav-pills .nav-link {
  border-radius: 0.5rem;
  color: #6c757d;
  font-weight: 500;
}

.status-tabs__row {
  margin-bottom: 0.5rem;
}

.status-tabs__row:last-child {
  margin-bottom: 0;
}

.status-tabs__button {
  flex: 1 1 calc(25% - 0.5rem);
  min-width: 140px;
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

.orders-search .form-control {
  min-width: 220px;
}

@media (max-width: 576px) {
  .orders-search .input-group {
    flex-wrap: nowrap;
  }

  .orders-search .form-control {
    min-width: 0;
  }

  .status-tabs__button {
    flex: 1 1 calc(50% - 0.5rem);
    min-width: 120px;
  }
}
</style>
