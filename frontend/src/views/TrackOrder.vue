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
                  ref="orderCodeInput"
                  v-model="orderCode"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': orderCode.trim() && !isOrderCodeValid }"
                  placeholder="VD: DH-2025-0001"
                  @keyup.enter="submitForm"
                />
                <div v-if="orderCode.trim() && !isOrderCodeValid" class="invalid-feedback">
                  Mã đơn hàng không hợp lệ.
                </div>
              </div>

              <label class="form-label fw-semibold">Số điện thoại</label>
              <div class="input-group mb-3">
                <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                <input
                  v-model="phone"
                  type="tel"
                  inputmode="numeric"
                  class="form-control"
                  :class="{ 'is-invalid': phone.trim() && !isPhoneValid }"
                  placeholder="VD: 0901 234 567"
                  @keyup.enter="submitForm"
                />
                <div v-if="phone.trim() && !isPhoneValid" class="invalid-feedback">
                  Số điện thoại không hợp lệ (9-11 chữ số).
                </div>
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

              <p class="small mt-3 mb-0" style="color: #856404">
                <i class="bi bi-info-circle me-1"></i>
                Khuyến khích nhập cả hai trường để đảm bảo tính bảo mật và độ chính xác.
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

            <div
              v-for="order in normalizedOrders"
              :key="order.id"
              class="card shadow-sm mb-4 order-card"
            >
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

                <!-- Order Timeline -->
                <div v-if="order.trangThai !== 'CANCELLED'" class="order-timeline mb-4">
                  <div class="timeline-steps">
                    <div
                      v-for="(step, index) in getTimelineSteps()"
                      :key="index"
                      class="timeline-step"
                      :class="getStepClass(order.trangThai, step.status)"
                    >
                      <div class="timeline-icon">
                        <i :class="step.icon"></i>
                      </div>
                      <div class="timeline-label">{{ step.label }}</div>
                    </div>
                  </div>
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
                        <strong class="text-success"
                          >-{{ formatCurrency(order.giamGiaTong) }}</strong
                        >
                      </li>
                      <li class="total">
                        <span>Thành tiền</span>
                        <strong>{{ formatCurrency(order.tongThanhToan ?? order.tamTinh) }}</strong>
                      </li>
                    </ul>
                  </div>
                  <div class="col-md-6">
                    <h6 class="text-uppercase fw-semibold small text-muted">
                      Thanh toán & giao hàng
                    </h6>
                    <p class="mb-2">
                      <strong>Phương thức:</strong>
                      {{
                        order.paymentMethod ? order.paymentMethod.toUpperCase() : 'Chưa cập nhật'
                      }}
                    </p>
                    <p class="mb-2">
                      <strong>Trạng thái thanh toán:</strong>
                      <span class="text-capitalize">{{ order.paymentStatus || 'pending' }}</span>
                    </p>
                    <p class="mb-2">
                      <strong>Địa chỉ giao:</strong>
                      <span class="d-block">{{ order.diaChiGiaoSnapshot || 'Chưa cập nhật' }}</span>
                    </p>
                    <p v-if="order.trackingNumber" class="mb-0">
                      <strong>Mã vận đơn:</strong>
                      <span class="badge bg-light text-dark">{{ order.trackingNumber }}</span>
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
import { computed, ref, onMounted } from 'vue'
import apiService from '../services/api'

const orderCode = ref('')
const phone = ref('')
const isLoading = ref(false)
const error = ref('')
const orders = ref([])
const orderCodeInput = ref(null)

const fallbackImage = 'https://via.placeholder.com/80x80/EEE/555?text=AURO'

const statusMap = {
  PENDING: { label: 'Chờ xác nhận', class: 'bg-warning text-dark', order: 1 },
  CONFIRMED: { label: 'Đã xác nhận', class: 'bg-info text-dark', order: 2 },
  SHIPPING: { label: 'Đang giao', class: 'bg-primary', order: 3 },
  COMPLETED: { label: 'Hoàn tất', class: 'bg-success', order: 4 },
  CANCELLED: { label: 'Đã hủy', class: 'bg-secondary', order: -1 },
}

// Validation for phone number (9-11 digits)
const isPhoneValid = computed(() => {
  if (!phone.value.trim()) return true // Cho phép bỏ trống
  const phoneRegex = /^\d{9,11}$/
  return phoneRegex.test(phone.value.trim().replace(/\s/g, ''))
})

// Validation for order code format (optional, can be customized)
const isOrderCodeValid = computed(() => {
  if (!orderCode.value.trim()) return true // Cho phép bỏ trống
  // Accept format like: DH-2025-0001, DH-8D41A918, or any alphanumeric with optional hyphens
  const codeRegex = /^(DH-)?[A-Z0-9-]+$/i
  return codeRegex.test(orderCode.value.trim())
})

const canSubmit = computed(() => {
  const hasInput = Boolean(orderCode.value.trim() || phone.value.trim())
  return hasInput && isPhoneValid.value && isOrderCodeValid.value
})

const hasResults = computed(() => orders.value.length > 0)

// Auto-focus on mount
onMounted(() => {
  orderCodeInput.value?.focus()
})

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

// Timeline steps configuration
const getTimelineSteps = () => [
  { status: 'PENDING', label: 'Chờ xác nhận', icon: 'bi bi-clock-history' },
  { status: 'CONFIRMED', label: 'Đã xác nhận', icon: 'bi bi-check-circle' },
  { status: 'SHIPPING', label: 'Đang giao', icon: 'bi bi-truck' },
  { status: 'COMPLETED', label: 'Hoàn tất', icon: 'bi bi-box-seam' },
]

const getStepClass = (currentStatus, stepStatus) => {
  const current = statusMap[currentStatus]?.order || 0
  const step = statusMap[stepStatus]?.order || 0

  if (current >= step) return 'completed'
  if (current + 1 === step) return 'active'
  return ''
}

const submitForm = async () => {
  if (!canSubmit.value) {
    if (!isPhoneValid.value) {
      error.value = 'Số điện thoại không hợp lệ (9-11 chữ số).'
    } else if (!isOrderCodeValid.value) {
      error.value = 'Mã đơn hàng không hợp lệ.'
    } else {
      error.value = 'Vui lòng nhập mã đơn hàng hoặc số điện thoại.'
    }
    return
  }

  isLoading.value = true
  error.value = ''
  orders.value = []

  try {
    const params = {}
    if (orderCode.value.trim()) params.maDon = orderCode.value.trim()
    if (phone.value.trim()) params.soDienThoai = phone.value.trim().replace(/\s/g, '')

    const response = await apiService.orders.track(params)
    const result = normalizePayload(response)

    orders.value = result

    if (!result.length) {
      error.value = 'Không tìm thấy đơn hàng phù hợp với thông tin bạn cung cấp.'
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
  orderCodeInput.value?.focus()
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

/* Timeline Styles */
.order-timeline {
  padding: 1.5rem 0;
  background: linear-gradient(to bottom, #f9fafb 0%, transparent 100%);
  border-radius: 12px;
}

.timeline-steps {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  padding: 0 2rem;
}

.timeline-steps::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 2rem;
  right: 2rem;
  height: 3px;
  background: #e1e5ea;
  z-index: 0;
}

.timeline-step {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  z-index: 1;
}

.timeline-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 3px solid #e1e5ea;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
  font-size: 1.1rem;
  color: #9ca3af;
}

.timeline-label {
  font-size: 0.75rem;
  text-align: center;
  color: #6b7280;
  font-weight: 500;
  max-width: 80px;
}

/* Completed Step */
.timeline-step.completed .timeline-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-color: #10b981;
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.timeline-step.completed .timeline-label {
  color: #059669;
  font-weight: 600;
}

/* Active Step */
.timeline-step.active .timeline-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-color: #f59e0b;
  color: white;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.4);
  animation: pulse 2s infinite;
}

.timeline-step.active .timeline-label {
  color: #d97706;
  font-weight: 600;
}

@keyframes pulse {
  0%,
  100% {
    box-shadow: 0 2px 8px rgba(245, 158, 11, 0.4);
  }
  50% {
    box-shadow: 0 2px 16px rgba(245, 158, 11, 0.6);
  }
}

/* Progress Line */
.timeline-step.completed + .timeline-step::before {
  content: '';
  position: absolute;
  top: 20px;
  left: -50%;
  width: 100%;
  height: 3px;
  background: linear-gradient(to right, #10b981, #059669);
  z-index: -1;
}

/* Responsive Timeline */
@media (max-width: 768px) {
  .timeline-steps {
    padding: 0 0.5rem;
  }

  .timeline-steps::before {
    left: 0.5rem;
    right: 0.5rem;
  }

  .timeline-icon {
    width: 32px;
    height: 32px;
    font-size: 0.9rem;
  }

  .timeline-label {
    font-size: 0.65rem;
    max-width: 60px;
  }
}

/* Invalid Feedback */
.invalid-feedback {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #dc3545;
}

.form-control.is-invalid {
  border-color: #dc3545;
  padding-right: calc(1.5em + 0.75rem);
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right calc(0.375em + 0.1875rem) center;
  background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
}

.form-control.is-invalid:focus {
  border-color: #dc3545;
  box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25);
}
</style>
