<template>
  <div class="track-order-page">
    <div class="container py-5">
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/">Trang chủ</router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">Theo dõi đơn hàng</li>
        </ol>
      </nav>

      <div class="row g-4">
        <div class="col-lg-4">
          <div class="card shadow-sm h-100">
            <div class="card-body">
              <h1 class="h4 fw-bold mb-2">Tra cứu đơn hàng</h1>
              <p class="text-muted small mb-4">
                Nhập mã đơn hàng <strong>hoặc</strong> số điện thoại đã sử dụng khi đặt hàng.
              </p>

              <label class="form-label fw-semibold">Mã đơn hàng</label>
              <div class="input-group mb-3">
                <span class="input-group-text"><i class="bi bi-hash"></i></span>
                <input
                  v-model="orderCode"
                  type="text"
                  class="form-control"
                  placeholder="VD: DH-2025-0001"
                />
              </div>

              <label class="form-label fw-semibold">Số điện thoại</label>
              <div class="input-group mb-3">
                <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                <input
                  v-model="phone"
                  type="tel"
                  class="form-control"
                  placeholder="VD: 0901 234 567"
                />
              </div>

              <div class="d-flex align-items-center gap-2">
                <button
                  class="btn btn-warning flex-grow-1"
                  :disabled="!canSubmit || isLoading"
                  @click="submitForm"
                >
                  <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
                  Tra cứu
                </button>
                <button
                  v-if="orderCode || phone"
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="resetForm"
                >
                  Xóa
                </button>
              </div>

              <p class="text-muted small mt-3 mb-0">
                * Khuyến khích nhập cả hai trường để đảm bảo tính bảo mật và độ chính xác.
              </p>
            </div>
          </div>
        </div>

        <div class="col-lg-8">
          <div v-if="error" class="alert alert-danger d-flex align-items-center gap-2" role="alert">
            <i class="bi bi-exclamation-triangle"></i>
            <span>{{ error }}</span>
          </div>

          <div v-if="isLoading" class="card shadow-sm text-center py-5">
            <div class="spinner-border text-warning mb-3" role="status">
              <span class="visually-hidden">Đang tra cứu...</span>
            </div>
            <p class="text-muted mb-0">Đang kiểm tra thông tin đơn hàng...</p>
          </div>

          <div v-else-if="hasResults">
            <p class="text-muted mb-3">
              Tìm thấy <strong>{{ normalizedOrders.length }}</strong>
              đơn hàng phù hợp.
            </p>

            <div v-for="order in normalizedOrders" :key="order.id" class="card shadow-sm mb-4 order-card">
              <div class="card-body">
                <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                  <div>
                    <h5 class="mb-1">Đơn hàng #{{ order.soDonHang }}</h5>
                    <small class="text-muted">Đặt lúc {{ formatDateTime(order.taoLuc) }}</small>
                  </div>
                  <span class="badge px-3 py-2" :class="statusMeta(order.trangThai).class">
                    {{ statusMeta(order.trangThai).label }}
                  </span>
                </div>

                <div class="row mb-3 gy-3">
                  <div class="col-md-6">
                    <h6 class="text-uppercase fw-semibold small text-muted">Tổng quan</h6>
                    <ul class="list-unstyled mb-0 order-summary">
                      <li>
                        <span>Tạm tính</span>
                        <strong>{{ formatCurrency(order.tamTinh) }}</strong>
                      </li>
                      <li>
                        <span>Phí vận chuyển</span>
                        <strong>{{ formatCurrency(order.phiVanChuyen) }}</strong>
                      </li>
                      <li v-if="order.giamGiaTong && Number(order.giamGiaTong) > 0">
                        <span>Giảm giá</span>
                        <strong class="text-success">-{{ formatCurrency(order.giamGiaTong) }}</strong>
                      </li>
                      <li class="total">
                        <span>Thành tiền</span>
                        <strong>{{ formatCurrency(order.tongThanhToan ?? order.tamTinh) }}</strong>
                      </li>
                    </ul>
                  </div>
                  <div class="col-md-6">
                    <h6 class="text-uppercase fw-semibold small text-muted">Thanh toán & giao hàng</h6>
                    <p class="mb-2">
                      <strong>Phương thức:</strong>
                      {{ order.paymentMethod ? order.paymentMethod.toUpperCase() : 'Chưa cập nhật' }}
                    </p>
                    <p class="mb-2">
                      <strong>Trạng thái thanh toán:</strong>
                      <span class="text-capitalize">{{ order.paymentStatus || 'pending' }}</span>
                    </p>
                    <p class="mb-0">
                      <strong>Địa chỉ giao:</strong>
                      <span class="d-block">{{ order.diaChiGiaoSnapshot || 'Chưa cập nhật' }}</span>
                    </p>
                  </div>
                </div>

                <div>
                  <h6 class="text-uppercase fw-semibold small text-muted mb-3">Sản phẩm</h6>
                  <div v-if="order.chiTietList.length === 0" class="alert alert-light mb-0">
                    Không tìm thấy chi tiết sản phẩm.
                  </div>
                  <div
                    v-for="item in order.chiTietList"
                    :key="item.id"
                    class="order-item d-flex align-items-center mb-3 pb-3"
                  >
                    <img
                      :src="item.hinhAnh || fallbackImage"
                      alt="Product"
                      class="rounded me-3 order-item__thumb"
                    />
                    <div class="flex-grow-1">
                      <h6 class="mb-1">{{ item.tenSanPham }}</h6>
                      <small class="text-muted">
                        <span v-if="item.kichCoTen">Size: {{ item.kichCoTen }}</span>
                        <span v-if="item.kichCoTen && item.mauSacTen"> · </span>
                        <span v-if="item.mauSacTen">Màu: {{ item.mauSacTen }}</span>
                      </small>
                    </div>
                    <div class="text-end me-4">
                      <small class="text-muted d-block">Đơn giá</small>
                      <strong>{{ formatCurrency(item.donGia) }}</strong>
                    </div>
                    <div class="text-end">
                      <small class="text-muted d-block">Số lượng</small>
                      <strong>x{{ item.soLuong }}</strong>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="card shadow-sm empty-state">
            <div class="card-body text-center py-5">
              <i class="bi bi-search display-5 text-warning mb-3"></i>
              <h5 class="fw-semibold mb-2">Nhập thông tin để tra cứu đơn hàng</h5>
              <p class="text-muted mb-0">
                Bạn có thể dùng mã đơn hàng hoặc số điện thoại đã đặt hàng để xem tình trạng đơn.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import apiService from '../services/api'

const orderCode = ref('')
const phone = ref('')
const isLoading = ref(false)
const error = ref('')
const orders = ref([])

const fallbackImage =
  'https://via.placeholder.com/80x80/EEE/555?text=AURO'

const statusMap = {
  PENDING: { label: 'Chờ xác nhận', class: 'bg-warning text-dark' },
  CONFIRMED: { label: 'Đã xác nhận', class: 'bg-info text-dark' },
  SHIPPING: { label: 'Đang giao', class: 'bg-primary' },
  COMPLETED: { label: 'Hoàn tất', class: 'bg-success' },
  CANCELLED: { label: 'Đã hủy', class: 'bg-secondary' },
}

const canSubmit = computed(() => Boolean(orderCode.value.trim() || phone.value.trim()))
const hasResults = computed(() => orders.value.length > 0)

const normalizedOrders = computed(() =>
  orders.value.map((order) => ({
    ...order,
    chiTietList: Array.isArray(order.chiTietList) ? order.chiTietList : [],
  })),
)

const statusMeta = (status) => {
  if (!status) return { label: 'Không rõ', class: 'bg-secondary' }
  const key = status.toUpperCase()
  return statusMap[key] || { label: status, class: 'bg-secondary' }
}

const formatCurrency = (value) => {
  const numeric = typeof value === 'number' ? value : Number(value ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(numeric || 0)
}

const formatDateTime = (value) => {
  if (!value) return 'Không rõ thời gian'
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

const normalizePayload = (payload) => {
  if (!payload) return []
  if (Array.isArray(payload.content)) return payload.content
  if (Array.isArray(payload)) return payload
  if (payload.data) return normalizePayload(payload.data)
  return [payload]
}

const submitForm = async () => {
  if (!canSubmit.value) {
    error.value = 'Vui lòng nhập mã đơn hàng hoặc số điện thoại.'
    return
  }

  isLoading.value = true
  error.value = ''
  orders.value = []

  try {
    const params = {}
    if (orderCode.value.trim()) params.maDon = orderCode.value.trim()
    if (phone.value.trim()) params.soDienThoai = phone.value.trim()

    const response = await apiService.orders.track(params)
    const result = normalizePayload(response)

    orders.value = result

    if (!result.length) {
      error.value = 'Không tìm thấy đơn hàng phù hợp thông tin bạn cung cấp.'
    }
  } catch (err) {
    error.value = err?.message || 'Không thể tra cứu đơn hàng. Vui lòng thử lại sau.'
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  orderCode.value = ''
  phone.value = ''
  orders.value = []
  error.value = ''
}
</script>

<style scoped>
.track-order-page {
  background: #f9fafb;
  min-height: calc(100vh - 80px);
}

.order-card {
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 16px;
}

.order-summary li {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.35rem;
  font-size: 0.95rem;
}

.order-summary li.total {
  padding-top: 0.5rem;
  border-top: 1px dashed #e1e5ea;
  margin-top: 0.25rem;
  font-weight: 600;
}

.order-item {
  border-bottom: 1px dashed #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 0;
}

.order-item__thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border: 1px solid #eee;
  background: #fff;
}

.empty-state {
  border: 1px dashed #f0c04a;
  background: #fffef6;
}
</style>

