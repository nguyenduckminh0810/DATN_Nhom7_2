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
          <h1 class="orders-title h2 fw-bold mb-4">Đơn hàng của tôi</h1>
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
              <router-link
                to="/profile/addresses"
                class="list-group-item list-group-item-action py-3"
              >
                <i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng
              </router-link>
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
                        <div class="flex-grow-1 me-3">
                          <h6 class="mb-1 small">{{ item.name }}</h6>
                          <small class="text-muted" v-if="item.kichCoTen || item.mauSacTen">
                            <span v-if="item.kichCoTen">Size: {{ item.kichCoTen }}</span>
                            <span v-if="item.kichCoTen && item.mauSacTen">, </span>
                            <span v-if="item.mauSacTen">Màu: {{ item.mauSacTen }}</span>
                          </small>
                        </div>
                        <div class="order-item__price text-end me-3">
                          <small class="text-muted d-block">Đơn giá</small>
                          <span class="fw-semibold">{{ formatPrice(item.price) }}</span>
                        </div>
                        <div class="order-item__total text-end">
                          <small class="text-muted d-block">Số lượng: x{{ item.quantity }}</small>
                          <div class="small fw-bold text-nowrap">
                            {{ formatPrice(item.subtotal) }}
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
                        <strong class="text-warning">
                          {{ formatPrice(computeOrderTotal(order)) }}
                        </strong>
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
                        v-if="['DELIVERED', 'COMPLETED', 'CANCELLED'].includes(order.status)"
                        class="btn btn-outline-success btn-sm d-flex align-items-center"
                        :disabled="isOrderReordering(order.id)"
                        @click="reorderOrder(order)"
                      >
                        <span
                          v-if="isOrderReordering(order.id)"
                          class="spinner-border spinner-border-sm me-1"
                          role="status"
                          aria-hidden="true"
                        ></span>
                        <i v-else class="bi bi-arrow-repeat me-1"></i>
                        {{ isOrderReordering(order.id) ? 'Đang thêm...' : 'Mua lại' }}
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

  <div v-if="ratingModalVisible" class="rating-modal">
    <div class="rating-modal__overlay" @click="closeRatingModal"></div>
    <div class="rating-modal__content">
      <div class="rating-modal__header">
        <h5 class="mb-0">Đánh giá đơn hàng #{{ ratingTargetOrder?.orderNumber }}</h5>
        <button type="button" class="btn-close" @click="closeRatingModal"></button>
      </div>

      <div class="rating-modal__body">
        <div class="mb-3">
          <label class="form-label">Chọn sản phẩm</label>
          <select
            class="form-select"
            :value="ratingForm.chiTietId || ''"
            @change="selectRatingItem(Number($event.target.value))"
          >
            <option value="" disabled>-- Chọn sản phẩm --</option>
            <option
              v-for="option in ratingItemOptions"
              :key="option.id"
              :value="option.id"
            >
              {{ option.name }}
              <span v-if="option.hasReview"> (Đã đánh giá)</span>
            </option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Số sao</label>
          <div class="rating-stars" @mouseleave="setHoverRating(0)">
            <button
              v-for="star in 5"
              :key="star"
              type="button"
              class="rating-star"
              :class="{ active: star <= (hoverRating || ratingForm.rating) }"
              @click="setRatingValue(star)"
              @mouseenter="setHoverRating(star)"
            >
              ★
            </button>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Nhận xét</label>
          <textarea
            v-model="ratingForm.comment"
            rows="4"
            class="form-control"
            placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm"
          ></textarea>
        </div>

        <div v-if="selectedRatingItem?.hasReview" class="alert alert-info">
          <i class="bi bi-info-circle me-2"></i>
          Sản phẩm này đã được đánh giá trước đó. Bạn có thể cập nhật lại đánh giá.
        </div>
      </div>

      <div class="rating-modal__footer">
        <button type="button" class="btn btn-outline-secondary" @click="closeRatingModal">
          Hủy
        </button>
        <button
          type="button"
          class="btn btn-warning"
          :disabled="ratingSubmitting"
          @click="submitRating"
        >
          <span v-if="ratingSubmitting" class="spinner-border spinner-border-sm me-2"></span>
          Gửi đánh giá
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import orderService from '@/services/orderService'
import { useReorder } from '@/composables/useReorder'
import {
  ORDER_STATUS_FOR_CUSTOMER,
  normalizeOrderStatus,
  getOrderStatusLabel,
} from '@/utils/orderStatus'

const REORDER_SNAPSHOT_KEY = 'auro_reorder_variant_labels'
const REORDER_SNAPSHOT_TTL = 1000 * 60 * 30

defineOptions({
  name: 'OrdersView',
})

const router = useRouter()
const userStore = useUserStore()
const { reorderOrder, isOrderReordering } = useReorder()

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

const ratingModalVisible = ref(false)
const ratingSubmitting = ref(false)
const ratingTargetOrder = ref(null)
const ratingItemOptions = ref([])
const ratingForm = reactive({
  orderId: null,
  chiTietId: null,
  rating: 0,
  comment: '',
})
const hoverRating = ref(0)

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

const computeOrderTotal = (order) => {
  if (!order) {
    return 0
  }
  return calculateOrderTotal(order.subtotal, order.shippingFee, order.discount)
}

const normalizeSnapshotKeyPart = (value) => {
  if (value == null) {
    return ''
  }
  return value.toString().trim().toLowerCase()
}

const buildSnapshotProductKey = (productId, color, size) => {
  if (!productId) {
    return null
  }
  return `${productId}|${normalizeSnapshotKeyPart(color)}|${normalizeSnapshotKeyPart(size)}`
}

const readReorderSnapshots = () => {
  try {
    const raw = localStorage.getItem(REORDER_SNAPSHOT_KEY)
    if (!raw) {
      return null
    }

    const payload = JSON.parse(raw)
    if (!payload || typeof payload !== 'object') {
      localStorage.removeItem(REORDER_SNAPSHOT_KEY)
      return null
    }

    const createdAt = payload.createdAt || payload.timestamp
    if (createdAt && Date.now() - createdAt > REORDER_SNAPSHOT_TTL) {
      localStorage.removeItem(REORDER_SNAPSHOT_KEY)
      return null
    }

    return {
      variants: payload.variants || {},
      products: payload.products || {},
    }
  } catch (error) {
    console.error('❌ [ORDERS] Không thể đọc snapshot reorder:', error)
    localStorage.removeItem(REORDER_SNAPSHOT_KEY)
    return null
  }
}

const applyReorderSnapshotsToOrderItems = (items, snapshots) => {
  if (!Array.isArray(items) || items.length === 0 || !snapshots) {
    return
  }

  const { variants = {}, products = {} } = snapshots

  items.forEach((item) => {
    if (!item) {
      return
    }

    const variantId = item.variantId || item.bienTheId || item.chiTietId || null
    const productId = item.sanPhamId || item.productId || null
    const color = item.mauSacTen || item.color || item.mauSac || null
    const size = item.kichCoTen || item.size || item.kichCo || null

    let snapshot = null

    if (variantId && variants[variantId]) {
      snapshot = variants[variantId]
    } else {
      const productKey = buildSnapshotProductKey(productId, color, size)
      if (productKey && products[productKey]) {
        snapshot = products[productKey]
      }
    }

    if (!snapshot) {
      return
    }

    if (snapshot.displayName) {
      item.name = snapshot.displayName
    }

    if (snapshot.color && !item.mauSacTen) {
      item.mauSacTen = snapshot.color
    }

    if (snapshot.size && !item.kichCoTen) {
      item.kichCoTen = snapshot.size
    }
  })
}

const getStatusCount = (statusCode) => {
  if (statusCode === 'ALL') {
    return orders.value.length
  }
  return getOrdersByStatus(statusCode).length
}

const selectedRatingItem = computed(() => {
  return ratingItemOptions.value.find((item) => item.id === ratingForm.chiTietId) || null
})

watch(
  () => ratingForm.chiTietId,
  (newValue) => {
    if (!newValue) {
      ratingForm.rating = 0
      ratingForm.comment = ''
      hoverRating.value = 0
      return
    }
    const item = ratingItemOptions.value.find((option) => option.id === newValue)
    if (item) {
      ratingForm.rating = item.reviewRating || 0
      ratingForm.comment = item.reviewComment || ''
      hoverRating.value = 0
    }
  },
)

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

const prepareRatingOptions = (order) => {
  ratingItemOptions.value = order.items.map((item) => ({
    id: item.id,
    name: [item.name, [item.mauSacTen, item.kichCoTen].filter(Boolean).join(' - ')].filter(Boolean).join(' | '),
    hasReview: !!item.hasReview,
    reviewRating: item.reviewRating || 0,
    reviewComment: item.reviewComment || '',
    reviewCreatedAt: item.reviewCreatedAt || null,
  }))
}

const openRatingModal = (order) => {
  if (!order) {
    return
  }
  prepareRatingOptions(order)
  ratingTargetOrder.value = order
  ratingForm.orderId = order.id
  const defaultItem = ratingItemOptions.value.find((item) => !item.hasReview) || ratingItemOptions.value[0]

  if (!defaultItem) {
    alert('Tất cả sản phẩm trong đơn này đã được đánh giá.')
    return
  }

  ratingForm.chiTietId = defaultItem.id
  ratingForm.rating = defaultItem.reviewRating || 0
  ratingForm.comment = defaultItem.reviewComment || ''
  hoverRating.value = 0
  ratingModalVisible.value = true
}

const closeRatingModal = () => {
  ratingModalVisible.value = false
  ratingTargetOrder.value = null
  ratingItemOptions.value = []
  ratingForm.orderId = null
  ratingForm.chiTietId = null
  ratingForm.rating = 0
  ratingForm.comment = ''
  hoverRating.value = 0
}

const setRatingValue = (value) => {
  ratingForm.rating = value
  hoverRating.value = 0
}

const selectRatingItem = (itemId) => {
  if (!itemId) {
    return
  }
  ratingForm.chiTietId = itemId
}

const setHoverRating = (value) => {
  hoverRating.value = value
}

const applyRatingResult = (orderId, itemId, payload) => {
  const targetOrder = orders.value.find((order) => order.id === orderId)
  if (!targetOrder) {
    return
  }

  const itemIndex = targetOrder.items.findIndex((item) => item.id === itemId)
  if (itemIndex === -1) {
    return
  }

  const updatedItem = {
    ...targetOrder.items[itemIndex],
    hasReview: true,
    reviewRating: payload.danhGiaSoSao ?? ratingForm.rating,
    reviewComment: payload.danhGiaNoiDung ?? ratingForm.comment,
    reviewCreatedAt: payload.danhGiaTaoLuc || new Date().toISOString(),
  }

  targetOrder.items.splice(itemIndex, 1, updatedItem)

  const optionIndex = ratingItemOptions.value.findIndex((option) => option.id === itemId)
  if (optionIndex !== -1) {
    ratingItemOptions.value.splice(optionIndex, 1, {
      ...ratingItemOptions.value[optionIndex],
      hasReview: true,
      reviewRating: updatedItem.reviewRating,
      reviewComment: updatedItem.reviewComment,
      reviewCreatedAt: updatedItem.reviewCreatedAt,
    })
  }
}

const submitRating = async () => {
  if (!ratingForm.orderId || !ratingForm.chiTietId) {
    alert('Vui lòng chọn sản phẩm để đánh giá.')
    return
  }

  if (!ratingForm.rating) {
    alert('Vui lòng chọn số sao đánh giá.')
    return
  }

  try {
    ratingSubmitting.value = true
    const response = await orderService.submitReview(ratingForm.orderId, {
      chiTietId: ratingForm.chiTietId,
      soSao: ratingForm.rating,
      noiDung: ratingForm.comment,
    })

    const payload = response?.data ?? response ?? {}
    applyRatingResult(ratingForm.orderId, ratingForm.chiTietId, payload)

    alert('Đã gửi đánh giá!')

    const remainingUnreviewed = ratingItemOptions.value.find((item) => !item.hasReview)
    if (remainingUnreviewed) {
      ratingForm.chiTietId = remainingUnreviewed.id
    } else {
      closeRatingModal()
    }
  } catch (err) {
    console.error('Error rating order:', err)
    alert('Lỗi khi gửi đánh giá: ' + (err.response?.data?.message || err.message))
  } finally {
    ratingSubmitting.value = false
  }
}

const rateOrder = (order) => {
  openRatingModal(order)
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

    const reorderSnapshots = readReorderSnapshots()

    // Map backend data to frontend structure
    orders.value = orderData.map((order) => {
      const statusInfo = normalizeOrderStatus(order.trangThai)

      const items =
        order.chiTietList?.map((item) => {
          const unitPrice = parseAmount(item.donGia)
          const quantity = parseAmount(item.soLuong || 1)
          const lineTotal = unitPrice * quantity
          const baseName = item.tenSanPham || 'Sản phẩm'
          const variantLabel = [item.mauSacTen, item.kichCoTen].filter((part) => !!part).join(' - ')
          const displayName = variantLabel ? `${baseName} | ${variantLabel}` : baseName

          return {
            id: item.id,
            name: displayName,
            image:
              item.hinhAnh ||
              'https://via.placeholder.com/60x60/6c757d/ffffff?text=Product',
            price: unitPrice,
            quantity,
            subtotal: lineTotal,
            bienTheId: item.bienTheId || null,
            sanPhamId: item.sanPhamId || null,
            mauSacId: item.mauSacId || null,
            mauSacTen: item.mauSacTen || '',
            kichCoId: item.kichCoId || null,
            kichCoTen: item.kichCoTen || '',
            hasReview: !!item.daDanhGia,
            reviewRating: item.danhGiaSoSao || 0,
            reviewComment: item.danhGiaNoiDung || '',
            reviewCreatedAt: item.danhGiaTaoLuc || null,
          }
        }) || []

      applyReorderSnapshotsToOrderItems(items, reorderSnapshots)

      const computedSubtotal = items.reduce((sum, item) => sum + parseAmount(item.subtotal), 0)
      const shippingFee = parseAmount(order.phiVanChuyen)
      const discount = parseAmount(order.giamGiaTong)
      const total = calculateOrderTotal(computedSubtotal, shippingFee, discount)

      return {
        id: order.id,
        orderNumber: order.soDonHang || `ORD${order.id}`,
        orderDate: order.taoLuc || order.createdAt,
        status: statusInfo.code,
        statusLabel: statusInfo.label,
        statusClass: statusInfo.badgeClass,
        rawStatus: order.trangThai,
        subtotal: computedSubtotal,
        shippingFee,
        discount,
        total,
        paymentStatus: order.paymentStatus,
        paymentMethod: order.paymentMethod,
        shippingSnapshot: order.diaChiGiaoSnapshot || order.diaChiGiao || '',
        shippingNote: order.ghiChu || '',
        items,
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
.orders {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 6.5rem 0 2rem;
}

.orders-title {
  letter-spacing: 0.02em;
}

.orders .container {
  padding-top: 0;
}

@media (max-width: 768px) {
  .orders {
    padding: 5rem 0 1.5rem;
  }
}

@media (max-width: 576px) {
  .orders {
    padding: 4.5rem 0 1.5rem;
  }
}

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

.rating-modal {
  position: fixed;
  inset: 0;
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rating-modal__overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
}

.rating-modal__content {
  position: relative;
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 12px;
  padding: 1.5rem;
  z-index: 1;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.15);
}

.rating-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.rating-modal__body {
  margin-bottom: 1.5rem;
}

.rating-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.rating-stars {
  display: flex;
  gap: 0.5rem;
}

.rating-star {
  border: none;
  background: transparent;
  font-size: 1.75rem;
  cursor: pointer;
  color: #ced4da;
  transition: color 0.2s ease;
}

.rating-star.active,
.rating-star:hover {
  color: #ffc107;
}

.order-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item__price,
.order-item__total {
  min-width: 120px;
}

.order-item__total .small {
  font-size: 0.9rem;
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

  .order-item__price,
  .order-item__total {
    min-width: auto;
  }
}
</style>
