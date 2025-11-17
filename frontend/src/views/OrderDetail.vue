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
            <span :class="['badge', 'fs-6', order.statusClass]">
              {{ order.statusLabel }}
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
              <div class="card-body order-items">
                <div v-for="item in order.chiTietList" :key="item.id" class="order-item">
                  <div class="row align-items-center">
                    <div class="col-md-2">
                      <img
                        :src="item.hinhAnh || 'https://via.placeholder.com/80'"
                        :alt="item.tenSanPham"
                        class="img-fluid rounded"
                      />
                    </div>
                    <div class="col-md-5">
                      <h6 class="mb-1">{{ item.tenSanPham }}</h6>
                      <p class="text-muted small mb-0" v-if="item.moTaBienThe || buildVariantText(item)">
                        {{ item.moTaBienThe || buildVariantText(item) }}
                      </p>
                    </div>
                    <div class="col-md-2 text-center">
                      <div class="text-muted small">Đơn giá</div>
                      <strong>{{ formatPrice(item.donGia) }}</strong>
                    </div>
                    <div class="col-md-1 text-center">
                      <div class="text-muted small">SL</div>
                      <strong>x{{ item.soLuong }}</strong>
                    </div>
                    <div class="col-md-2 text-end">
                      <div class="text-muted small">Thành tiền</div>
                      <strong>{{ formatPrice(item.lineTotal || item.donGia * item.soLuong) }}</strong>
                    </div>
                  </div>

                  <div v-if="item.daDanhGia" class="order-item__review">
                    <div class="order-item__review-header">
                      <div class="order-item__review-stars">
                        <i
                          v-for="star in 5"
                          :key="star"
                          :class="[
                            'bi',
                            star <= (item.danhGiaSoSao || 0) ? 'bi-star-fill text-warning' : 'bi-star text-muted',
                          ]"
                        ></i>
                      </div>
                      <span class="order-item__review-date text-muted small" v-if="item.danhGiaTaoLuc">
                        {{ formatReviewDate(item.danhGiaTaoLuc) }}
                      </span>
                    </div>
                    <p class="order-item__review-content mb-0" v-if="item.danhGiaNoiDung">
                      {{ item.danhGiaNoiDung }}
                    </p>
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
                  <div class="timeline-item" :class="{ active: isStatusActive('PROCESSING') }">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                      <h6>Đang xử lý</h6>
                      <p class="text-muted small">Đơn hàng đang được xử lý</p>
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
                  <strong>{{ formatPrice(order.phiVanChuyen) }}</strong>
                </div>
                <div
                  v-if="order.giamGiaTong > 0"
                  class="d-flex justify-content-between mb-2 text-success"
                >
                  <span>Giảm giá:</span>
                  <strong>-{{ formatPrice(order.giamGiaTong) }}</strong>
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
import { useRoute } from 'vue-router'
import orderService from '@/services/orderService'
import {
  normalizeOrderStatus,
  getOrderStatusCode,
  ORDER_STATUS_FLOW,
  ORDER_STATUS_CODES,
} from '@/utils/orderStatus'

const REORDER_SNAPSHOT_KEY = 'auro_reorder_variant_labels'
const REORDER_SNAPSHOT_TTL = 1000 * 60 * 30

const route = useRoute()

const order = ref(null)
const loading = ref(true)
const error = ref(null)
const isNewOrder = ref(false)

const canCancelOrder = computed(() => {
  if (!order.value) return false

  const currentStatus = order.value.statusCode || getOrderStatusCode(order.value.rawStatus)

  return [ORDER_STATUS_CODES.PENDING, ORDER_STATUS_CODES.PROCESSING].includes(currentStatus)
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

    const orderData = response?.data || response
    const statusInfo = normalizeOrderStatus(orderData?.trangThai)

    console.log('Order detail response:', response)

    const reorderSnapshots = readReorderSnapshots()

    const mappedItems = (orderData?.chiTietList || []).map((item) => {
      const unitPrice = parseAmount(item.donGia)
      const quantity = parseAmount(item.soLuong || 1)
      const lineTotal = unitPrice * quantity
      const enhancedItem = enhanceOrderItemWithVariantInfo(
        {
          ...item,
          donGia: unitPrice,
          soLuong: quantity,
          lineTotal,
        },
        reorderSnapshots,
      )

      return {
        ...enhancedItem,
        moTaBienThe: enhancedItem.moTaBienThe || createVariantText(enhancedItem),
      }
    })

    const subtotal = mappedItems.reduce((sum, item) => sum + parseAmount(item.lineTotal), 0)
    const shippingFee = parseAmount(orderData?.phiVanChuyen)
    const discount = parseAmount(orderData?.giamGiaTong)
    const total = subtotal - discount + shippingFee

    order.value = {
      ...orderData,
      rawStatus: orderData?.trangThai,
      statusCode: statusInfo.code,
      statusLabel: statusInfo.label,
      statusClass: statusInfo.badgeClass,
      chiTietList: mappedItems,
      tamTinh: subtotal,
      phiVanChuyen: shippingFee,
      giamGiaTong: discount,
      tongThanhToan: total,
    }
    
  } catch (err) {
    console.error('Error fetching order:', err)
    error.value = err.message || 'Không thể tải thông tin đơn hàng'
  } finally {
    loading.value = false
  }
}

const parseAmount = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : 0
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
    console.error('❌ [ORDER DETAIL] Không thể đọc snapshot reorder:', error)
    localStorage.removeItem(REORDER_SNAPSHOT_KEY)
    return null
  }
}

const enhanceOrderItemWithVariantInfo = (item, snapshots) => {
  if (!item) {
    return item
  }

  const variantId = item.bienTheId || item.variantId || item.id || null
  const productId = item.sanPhamId || item.productId || null
  const initialColor = item.mauSacTen || item.color || item.mauSac || ''
  const initialSize = item.kichCoTen || item.size || item.kichCo || ''

  let snapshot = null
  if (snapshots) {
    const { variants = {}, products = {} } = snapshots

    if (variantId && variants[variantId]) {
      snapshot = variants[variantId]
    } else {
      const productKey = buildSnapshotProductKey(productId, initialColor, initialSize)
      if (productKey && products[productKey]) {
        snapshot = products[productKey]
      }
    }
  }

  const color = snapshot?.color || initialColor
  const size = snapshot?.size || initialSize
  const baseName = item.tenSanPham || snapshot?.displayName || 'Sản phẩm'
  let name = baseName

  if (snapshot?.displayName) {
    name = snapshot.displayName
  } else {
    const variantLabel = [color, size].filter(Boolean).join(' - ')
    if (variantLabel) {
      name = `${baseName} | ${variantLabel}`
    }
  }

  return {
    ...item,
    tenSanPham: name,
    mauSacTen: color,
    kichCoTen: size,
  }
}

const createVariantText = (item) => {
  const variantParts = [item.mauSacTen, item.kichCoTen].filter(Boolean)
  if (variantParts.length === 0) {
    return ''
  }
  return variantParts.join(' • ')
}

const buildVariantText = (item) => {
  return item?.moTaBienThe || createVariantText(item)
}

const formatReviewDate = (dateString) => {
  return formatDate(dateString)
}

const timelineStatuses = ORDER_STATUS_FLOW.filter((code) => code !== 'COMPLETED')

const isStatusActive = (statusCode) => {
  if (!order.value) return false

  const currentStatus = order.value.statusCode || getOrderStatusCode(order.value.rawStatus)
  const normalizedStatus = currentStatus === ORDER_STATUS_CODES.COMPLETED ? ORDER_STATUS_CODES.DELIVERED : currentStatus

  if (normalizedStatus === ORDER_STATUS_CODES.CANCELLED) {
    return statusCode === 'PENDING'
  }

  const currentIndex = timelineStatuses.indexOf(normalizedStatus)
  const checkIndex = timelineStatuses.indexOf(statusCode)

  if (currentIndex === -1 || checkIndex === -1) {
    return false
  }

  return checkIndex <= currentIndex
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'COD': 'Thanh toán khi nhận hàng',
    'VNPAY': 'VNPay',
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
  padding: 6.5rem 0 2rem;
}

@media (max-width: 768px) {
  .order-detail {
    padding: 5rem 0 1.5rem;
  }
}

@media (max-width: 576px) {
  .order-detail {
    padding: 4.5rem 0 1.5rem;
  }
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

.order-item .text-muted.small {
  display: block;
}

.order-item__review {
  margin-top: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  background-color: #f9fafb;
}

.order-item__review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.order-item__review-stars .bi {
  font-size: 1rem;
}

.order-item__review-content {
  font-size: 0.9rem;
  color: #495057;
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
