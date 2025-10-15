<template>
  <div class="admin-promotions">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý khuyến mãi & Voucher</h1>
        <p class="page-subtitle">Tạo và quản lý các chương trình khuyến mãi, voucher giảm giá</p>
      </div>
      <div class="header-right">
        <button class="btn btn-primary" @click="showCreateModal = true">
          <i class="bi bi-plus-circle me-2"></i>
          Tạo khuyến mãi mới
        </button>
      </div>
    </div>

    <!-- Promotion Stats -->
    <div class="promotion-stats">
      <div class="row g-3">
        <div class="col-lg-3 col-md-6">
          <div class="stat-card active">
            <div class="stat-icon">
              <i class="bi bi-lightning-charge"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ activePromotions.length }}</div>
              <div class="stat-label">Đang hoạt động</div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card scheduled">
            <div class="stat-icon">
              <i class="bi bi-clock"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ scheduledPromotions.length }}</div>
              <div class="stat-label">Đã lên lịch</div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card used">
            <div class="stat-icon">
              <i class="bi bi-ticket-perforated"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalVoucherUsed }}</div>
              <div class="stat-label">Voucher đã dùng</div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6">
          <div class="stat-card revenue">
            <div class="stat-icon">
              <i class="bi bi-currency-dollar"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatCurrency(totalDiscountGiven) }}</div>
              <div class="stat-label">Tổng giảm giá</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section">
      <div class="filters-row">
        <div class="search-box">
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            class="form-control"
            placeholder="Tìm kiếm khuyến mãi, voucher..."
            v-model="searchQuery"
          />
        </div>
        <select class="form-select" v-model="statusFilter" style="max-width: 200px;">
          <option value="">Tất cả trạng thái</option>
          <option value="active">Đang hoạt động</option>
          <option value="scheduled">Đã lên lịch</option>
          <option value="expired">Đã hết hạn</option>
        </select>
        <select class="form-select" v-model="typeFilter" style="max-width: 200px;">
          <option value="">Tất cả loại</option>
          <option value="percentage">Giảm %</option>
          <option value="fixed">Giảm cố định</option>
          <option value="freeship">Freeship</option>
          <option value="bogo">Buy X Get Y</option>
        </select>
      </div>
    </div>

    <!-- Promotions Tabs -->
    <div class="promotions-tabs">
      <button
        :class="['tab-btn', { active: activeTab === 'all' }]"
        @click="activeTab = 'all'"
      >
        <i class="bi bi-list-ul me-1"></i>
        Tất cả ({{ promotions.length }})
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'active' }]"
        @click="activeTab = 'active'"
      >
        <i class="bi bi-lightning-charge me-1"></i>
        Đang chạy ({{ activePromotions.length }})
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'scheduled' }]"
        @click="activeTab = 'scheduled'"
      >
        <i class="bi bi-clock me-1"></i>
        Sắp diễn ra ({{ scheduledPromotions.length }})
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'expired' }]"
        @click="activeTab = 'expired'"
      >
        <i class="bi bi-archive me-1"></i>
        Đã kết thúc ({{ expiredPromotions.length }})
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner-border text-warning" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
      <p class="mt-2">Đang tải voucher...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <i class="bi bi-exclamation-triangle display-4 text-danger"></i>
      <h5 class="mt-2">Có lỗi xảy ra</h5>
      <p class="text-muted">{{ error }}</p>
      <button class="btn btn-primary" @click="loadVouchers">
        <i class="bi bi-arrow-clockwise me-2"></i>Thử lại
      </button>
    </div>

    <!-- Promotions List -->
    <div v-else class="promotions-list">
      <div
        v-for="promotion in filteredPromotions"
        :key="promotion.id"
        class="promotion-card"
        :class="'promotion-' + getVoucherStatus(promotion)"
      >
        <div class="promotion-header">
          <div class="promotion-info">
            <div class="promotion-type-badge" :class="'type-' + promotion.type">
              <i :class="getTypeIcon(promotion.type)"></i>
              {{ getTypeName(promotion.type) }}
            </div>
            <h5 class="promotion-name">{{ promotion.ma }}</h5>
            <p class="promotion-description">{{ getVoucherDescription(promotion) }}</p>
          </div>
          
          <div class="promotion-value">
            <div class="value-display" :class="'value-' + promotion.type">
              {{ getValueDisplay(promotion) }}
            </div>
            <div class="promotion-status">
              <span :class="['status-badge', 'status-' + getVoucherStatus(promotion)]">
                {{ getStatusText(getVoucherStatus(promotion)) }}
              </span>
            </div>
          </div>
        </div>

        <div class="promotion-body">
          <div class="promotion-details">
            <div class="detail-item">
              <i class="bi bi-calendar-range"></i>
              <span>{{ formatDate(promotion.batDauLuc) }} - {{ formatDate(promotion.ketThucLuc) }}</span>
            </div>
            
            <div class="detail-item">
              <i class="bi bi-tag"></i>
              <span>Mã: <code>{{ promotion.ma }}</code></span>
            </div>
            
            <div v-if="promotion.donToiThieu" class="detail-item">
              <i class="bi bi-filter"></i>
              <span>Đơn tối thiểu: {{ formatCurrency(promotion.donToiThieu) }}</span>
            </div>

            <div class="detail-item">
              <i class="bi bi-bar-chart"></i>
              <span>Giới hạn: {{ promotion.gioiHanSuDung === -1 || promotion.gioiHanSuDung === null ? '∞' : promotion.gioiHanSuDung }}</span>
            </div>
          </div>

          <div class="promotion-actions">
            <button
              v-if="promotion.status === 'scheduled'"
              class="btn btn-sm btn-outline-success"
              @click="activatePromotion(promotion)"
              title="Kích hoạt ngay"
            >
              <i class="bi bi-play"></i>
            </button>
            <button
              v-if="promotion.status === 'active'"
              class="btn btn-sm btn-outline-warning"
              @click="pausePromotion(promotion)"
              title="Tạm dừng"
            >
              <i class="bi bi-pause"></i>
            </button>
            <button 
              v-if="getVoucherStatus(promotion) === 'scheduled'" 
              class="btn btn-sm btn-success" 
              @click="startPromotion(promotion)" 
              title="Chạy ngay">
              <i class="bi bi-play"></i>
            </button>
            <button class="btn btn-sm btn-outline-primary" @click="viewPromotion(promotion)" title="Xem chi tiết">
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-sm btn-outline-info" @click="editPromotion(promotion)" title="Chỉnh sửa">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-sm btn-outline-secondary" @click="duplicatePromotion(promotion)" title="Nhân bản">
              <i class="bi bi-copy"></i>
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="deletePromotion(promotion)" title="Xóa">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>

        <!-- Progress Bar -->
        <div v-if="promotion.usageLimit" class="promotion-progress">
          <div class="progress">
            <div
              class="progress-bar"
              :class="getProgressBarClass(promotion)"
              :style="{ width: (promotion.usedCount / promotion.usageLimit * 100) + '%' }"
            ></div>
          </div>
          <small class="progress-text">{{ Math.round(promotion.usedCount / promotion.usageLimit * 100) }}% đã sử dụng</small>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="filteredPromotions.length === 0" class="empty-state">
        <i class="bi bi-tag display-1 text-muted"></i>
        <h5>Không có khuyến mãi nào</h5>
        <p>Tạo khuyến mãi mới để thu hút khách hàng</p>
        <button class="btn btn-primary" @click="showCreateModal = true">
          <i class="bi bi-plus-circle me-2"></i>
          Tạo khuyến mãi đầu tiên
        </button>
      </div>
    </div>

    <!-- Create/Edit Promotion Modal -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">{{ editingPromotion ? 'Chỉnh sửa khuyến mãi' : 'Tạo khuyến mãi mới' }}</h5>
          <button class="btn-close" @click="closeCreateModal">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="savePromotion">
            <div class="row g-3">
              <!-- Basic Info -->
              <div class="col-12">
                <h6 class="form-section-title">Thông tin cơ bản</h6>
              </div>
              
              <div class="col-md-8">
                <label class="form-label">Tên chương trình *</label>
                <input type="text" class="form-control" v-model="promotionForm.name" required placeholder="VD: Flash Sale Tết 2025">
              </div>

              <div class="col-md-4">
                <label class="form-label">Loại khuyến mãi *</label>
                <select class="form-select" v-model="promotionForm.type" required>
                  <option value="percentage">Giảm giá %</option>
                  <option value="fixed">Giảm giá cố định</option>
                  <option value="freeship">Miễn phí ship</option>
                  <option value="bogo">Mua X tặng Y</option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Mô tả</label>
                <textarea class="form-control" rows="2" v-model="promotionForm.description" placeholder="Mô tả chi tiết về chương trình"></textarea>
              </div>

              <!-- Discount Value -->
              <div class="col-12">
                <h6 class="form-section-title">Giá trị giảm giá</h6>
              </div>

              <div v-if="promotionForm.type === 'percentage'" class="col-md-6">
                <label class="form-label">Phần trăm giảm (%)</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="promotionForm.percentValue" min="1" max="100">
                  <span class="input-group-text">%</span>
                </div>
              </div>

              <div v-if="promotionForm.type === 'percentage'" class="col-md-6">
                <label class="form-label">Giảm tối đa</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="promotionForm.maxDiscount" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>

              <div v-if="promotionForm.type === 'fixed'" class="col-md-6">
                <label class="form-label">Số tiền giảm</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="promotionForm.fixedValue" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>

              <div v-if="promotionForm.type === 'freeship'" class="col-12">
                <div class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>
                  <strong>Miễn phí vận chuyển:</strong> Voucher này sẽ miễn phí hoàn toàn phí vận chuyển cho đơn hàng.
                </div>
              </div>

              <!-- Voucher Code -->
              <div class="col-12">
                <h6 class="form-section-title">Mã voucher</h6>
              </div>

              <div class="col-md-8">
                <label class="form-label">Mã voucher *</label>
                <div class="input-group">
                  <input type="text" class="form-control" v-model="promotionForm.code" required placeholder="AURO-TET-2025">
                  <button type="button" class="btn btn-outline-secondary" @click="generateCode">
                    <i class="bi bi-arrow-clockwise"></i>
                    Tạo tự động
                  </button>
                </div>
              </div>

              <div class="col-md-4">
                <label class="form-label">Số lần sử dụng</label>
                <input type="number" class="form-control" v-model.number="promotionForm.usageLimit" min="0" placeholder="Không giới hạn">
              </div>

              <!-- Time Range -->
              <div class="col-12">
                <h6 class="form-section-title">Thời gian áp dụng</h6>
              </div>

              <div class="col-md-6">
                <label class="form-label">Ngày bắt đầu *</label>
                <input type="datetime-local" class="form-control" v-model="promotionForm.startDate" required>
              </div>

              <div class="col-md-6">
                <label class="form-label">Ngày kết thúc *</label>
                <input type="datetime-local" class="form-control" v-model="promotionForm.endDate" required>
              </div>

              <!-- Conditions -->
              <div class="col-12">
                <h6 class="form-section-title">Điều kiện áp dụng</h6>
              </div>

              <div class="col-md-6">
                <label class="form-label">Đơn hàng tối thiểu</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="promotionForm.minOrderValue" min="0" placeholder="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>

              <div class="col-md-6">
                <label class="form-label">Áp dụng cho</label>
                <select class="form-select" v-model="promotionForm.applyTo">
                  <option value="all">Tất cả sản phẩm</option>
                  <option value="category">Danh mục cụ thể</option>
                  <option value="products">Sản phẩm cụ thể</option>
                </select>
              </div>

              <div v-if="promotionForm.applyTo === 'category'" class="col-12">
                <label class="form-label">Chọn danh mục</label>
                <div class="category-checkboxes">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="ao" v-model="promotionForm.categories" id="catAo">
                    <label class="form-check-label" for="catAo">Áo</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="ao-so-mi" v-model="promotionForm.categories" id="catAoSoMi">
                    <label class="form-check-label" for="catAoSoMi">Áo sơ mi</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="ao-thun" v-model="promotionForm.categories" id="catAoThun">
                    <label class="form-check-label" for="catAoThun">Áo thun</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="quan" v-model="promotionForm.categories" id="catQuan">
                    <label class="form-check-label" for="catQuan">Quần</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="quan-jean" v-model="promotionForm.categories" id="catQuanJean">
                    <label class="form-check-label" for="catQuanJean">Quần jean</label>
                  </div>
                </div>
              </div>

              <!-- Size & Color Filters -->
              <div class="col-md-6">
                <label class="form-label">Áp dụng cho size (để trống = tất cả)</label>
                <input type="text" class="form-control" v-model="promotionForm.sizesFilter" placeholder="VD: M,L,XL hoặc 30,32,34">
                <small class="text-muted">Nhập size cách nhau bằng dấu phẩy</small>
              </div>

              <div class="col-md-6">
                <label class="form-label">Áp dụng cho màu (để trống = tất cả)</label>
                <input type="text" class="form-control" v-model="promotionForm.colorsFilter" placeholder="VD: Đen,Trắng,Xanh">
                <small class="text-muted">Nhập màu cách nhau bằng dấu phẩy</small>
              </div>
            </div>
          </form>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeCreateModal">Hủy</button>
          <button type="button" class="btn btn-primary" @click="savePromotion">
            <i class="bi bi-check me-2"></i>
            {{ editingPromotion ? 'Cập nhật' : 'Tạo khuyến mãi' }}
          </button>
        </div>
      </div>
    </div>

    <!-- View Modal -->
    <div v-if="showViewModal" class="modal-overlay" @click="closeViewModal">
      <div class="modal-content modal-medium" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Chi tiết voucher</h5>
          <button type="button" class="btn-close" @click="closeViewModal">
            <i class="bi bi-x"></i>
          </button>
        </div>
        
        <div class="modal-body" v-if="selectedPromotion">
          <div class="voucher-detail">
            <div class="detail-section">
              <h6 class="section-title">Thông tin cơ bản</h6>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>Mã voucher:</label>
                  <span class="voucher-code">{{ selectedPromotion.ma }}</span>
                </div>
                <div class="detail-item">
                  <label>Loại:</label>
                  <span class="voucher-type">{{ getVoucherTypeName(selectedPromotion.loai) }}</span>
                </div>
                <div class="detail-item">
                  <label>Giá trị:</label>
                  <span class="voucher-value">{{ formatCurrency(selectedPromotion.giaTri) }}</span>
                </div>
                <div class="detail-item">
                  <label>Giảm tối đa:</label>
                  <span class="voucher-max">{{ formatCurrency(selectedPromotion.giamToiDa) }}</span>
                </div>
                <div class="detail-item">
                  <label>Đơn tối thiểu:</label>
                  <span class="voucher-min">{{ formatCurrency(selectedPromotion.donToiThieu) }}</span>
                </div>
                <div class="detail-item">
                  <label>Giới hạn sử dụng:</label>
                  <span class="voucher-limit">{{ selectedPromotion.gioiHanSuDung === -1 ? 'Không giới hạn' : selectedPromotion.gioiHanSuDung }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h6 class="section-title">Thời gian</h6>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>Bắt đầu:</label>
                  <span class="voucher-date">{{ formatDate(selectedPromotion.batDauLuc) }}</span>
                </div>
                <div class="detail-item">
                  <label>Kết thúc:</label>
                  <span class="voucher-date">{{ formatDate(selectedPromotion.ketThucLuc) }}</span>
                </div>
                <div class="detail-item">
                  <label>Trạng thái:</label>
                  <span class="voucher-status" :class="getVoucherStatus(selectedPromotion)">{{ getVoucherStatusText(selectedPromotion) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeViewModal">
            <i class="bi bi-x me-2"></i>Đóng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import apiService from '@/services/api'

// Reactive data
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const activeTab = ref('all')
const showCreateModal = ref(false)
const showViewModal = ref(false)
const editingPromotion = ref(null)
const selectedPromotion = ref(null)

const promotionForm = ref({
  name: '',
  description: '',
  type: 'percentage',
  percentValue: 10,
  maxDiscount: null,
  fixedValue: null,
  code: '',
  startDate: '',
  endDate: '',
  usageLimit: null,
  minOrderValue: 0,
  applyTo: 'all',
  categories: [],
  sizesFilter: '',
  colorsFilter: ''
})

// Voucher data từ API
const promotions = ref([])
const loading = ref(false)
const error = ref(null)

// Computed
const activePromotions = computed(() => promotions.value.filter(p => getVoucherStatus(p) === 'active'))
const scheduledPromotions = computed(() => promotions.value.filter(p => getVoucherStatus(p) === 'scheduled'))
const expiredPromotions = computed(() => promotions.value.filter(p => getVoucherStatus(p) === 'expired'))

const totalVoucherUsed = computed(() => {
  return promotions.value.reduce((sum, p) => sum + p.usedCount, 0)
})

const totalDiscountGiven = computed(() => {
  // Giả sử mỗi lần dùng voucher giảm TB 100k
  return totalVoucherUsed.value * 100000
})

const filteredPromotions = computed(() => {
  let filtered = promotions.value

  // Tab filter
  if (activeTab.value !== 'all') {
    filtered = filtered.filter(p => p.status === activeTab.value)
  }

  // Search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(p =>
      p.name.toLowerCase().includes(query) ||
      p.code.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (statusFilter.value) {
    filtered = filtered.filter(p => p.status === statusFilter.value)
  }

  // Type filter
  if (typeFilter.value) {
    filtered = filtered.filter(p => p.type === typeFilter.value)
  }

  return filtered
})

// Methods
const getTypeIcon = (type) => {
  const icons = {
    percentage: 'bi bi-percent',
    fixed: 'bi bi-cash',
    freeship: 'bi bi-truck',
    bogo: 'bi bi-gift'
  }
  return icons[type] || 'bi bi-tag'
}

const getTypeName = (type) => {
  const names = {
    percentage: 'Giảm %',
    fixed: 'Giảm cố định',
    freeship: 'Freeship',
    bogo: 'Mua X tặng Y'
  }
  return names[type] || type
}

// Helper function để tạo description cho voucher
const getVoucherDescription = (voucher) => {
  if (voucher.loai === 'percent' || voucher.loai === 'PHAN_TRAM') {
    return `Giảm ${voucher.giaTri}% cho đơn từ ${formatCurrency(voucher.donToiThieu || 0)}`
  } else {
    return `Giảm ${formatCurrency(voucher.giaTri)} cho đơn từ ${formatCurrency(voucher.donToiThieu || 0)}`
  }
}

const getValueDisplay = (promotion) => {
  if (promotion.loai === 'percent' || promotion.loai === 'PHAN_TRAM') {
    return `-${promotion.giaTri}%`
  } else {
    return `-${formatCurrency(promotion.giaTri)}`
  }
}

const getStatusText = (status) => {
  const statuses = {
    active: 'Đang chạy',
    scheduled: 'Sắp diễn ra',
    expired: 'Đã kết thúc',
    paused: 'Tạm dừng'
  }
  return statuses[status] || status
}

const getConditionsSummary = (conditions) => {
  const parts = []
  if (conditions.minOrder) {
    parts.push(`Đơn tối thiểu ${formatCurrency(conditions.minOrder)}`)
  }
  if (conditions.category) {
    parts.push(`Danh mục: ${conditions.category}`)
  }
  return parts.join(', ') || 'Không có điều kiện'
}

const getProgressBarClass = (promotion) => {
  const percent = (promotion.usedCount / promotion.usageLimit) * 100
  if (percent >= 90) return 'bg-danger'
  if (percent >= 70) return 'bg-warning'
  return 'bg-success'
}

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0 ₫'
  try {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  } catch (error) {
    console.error('Error formatting currency:', error, 'Input:', amount)
    return '0 ₫'
  }
}

const formatDate = (date) => {
  if (!date) return ''
  try {
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(new Date(date))
  } catch (error) {
    console.error('Error formatting date:', error, 'Input:', date)
    return ''
  }
}

const generateCode = () => {
  const timestamp = Date.now().toString().slice(-6)
  promotionForm.value.code = `AURO-${timestamp}`
}

const formatLocalDateTime = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

const activatePromotion = (promotion) => {
  if (confirm(`Kích hoạt khuyến mãi "${promotion.name}" ngay bây giờ?`)) {
    promotion.status = 'active'
  }
}

const pausePromotion = (promotion) => {
  if (confirm(`Tạm dừng khuyến mãi "${promotion.name}"?`)) {
    promotion.status = 'paused'
  }
}

const viewPromotion = (promotion) => {
  selectedPromotion.value = promotion
  showViewModal.value = true
}

const duplicatePromotion = async (promotion) => {
  try {
    loading.value = true

    const timestamp = Date.now().toString().slice(-6)
    const newCode = `${promotion.ma}-COPY-${timestamp}`

    const voucherData = {
      ma: newCode,
      loai: promotion.loai,
      giaTri: promotion.giaTri,
      giamToiDa: promotion.giamToiDa,
      donToiThieu: promotion.donToiThieu,
      batDauLuc: promotion.batDauLuc,
      ketThucLuc: promotion.ketThucLuc,
      gioiHanSuDung: promotion.gioiHanSuDung
    }

    console.log('Duplicating voucher:', voucherData)

    const response = await apiService.adminVoucher.create(voucherData)

    promotions.value.push(response.data)

    console.log('Voucher duplicated successfully:', response.data)
  } catch (err) {
    error.value = 'Không thể nhân bản voucher: ' + err.message
    console.error('Lỗi khi nhân bản voucher:', err)
  } finally {
    loading.value = false
  }
  }


const startPromotion = async (promotion) => {
  console.log('Starting promotion:', promotion)
  try {
    const voucherCode = promotion.ma || `VOUCHER_${Date.now()}`
    
    const now = new Date()
    const localNow = formatLocalDateTime(now)
    
    let endDate
    if (promotion.ketThucLuc) {
      endDate = promotion.ketThucLuc
    } else {
      const futureDate = new Date(Date.now() + 30*24*60*60*1000)
      endDate = formatLocalDateTime(futureDate)
    }
    
    const voucherData = {
      ma: voucherCode,
      loai: promotion.loai || 'fixed',
      giaTri: promotion.giaTri || 0,
      giamToiDa: promotion.giamToiDa || 0,
      donToiThieu: promotion.donToiThieu || 0,
      batDauLuc: localNow, 
      ketThucLuc: endDate, 
      gioiHanSuDung: promotion.gioiHanSuDung || -1
    }
    
    console.log('Voucher data to update:', voucherData)
    
    await updateVoucher(promotion.id, voucherData)
    console.log('Promotion started successfully')
  } catch (error) {
    console.error('Error starting promotion:', error)
    error.value = 'Không thể kích hoạt voucher: ' + error.message
  }
}

// Helper function để xác định status của voucher
const getVoucherStatus = (voucher) => {
  console.log('Getting voucher status for:', voucher)
  console.log('batDauLuc:', voucher.batDauLuc, 'ketThucLuc:', voucher.ketThucLuc)
  
  const now = new Date()
  const startDate = new Date(voucher.batDauLuc)
  const endDate = new Date(voucher.ketThucLuc)
  
  console.log('now:', now, 'startDate:', startDate, 'endDate:', endDate)
  
  if (now < startDate) {
    console.log('Status: scheduled')
    return 'scheduled'
  }
  if (now > endDate) {
    console.log('Status: expired')
    return 'expired'
  }
  console.log('Status: active')
  return 'active'
}

const getVoucherStatusText = (promotion) => {
  const status = getVoucherStatus(promotion)
  switch (status) {
    case 'active': return 'Đang chạy'
    case 'scheduled': return 'Sắp diễn ra'
    case 'expired': return 'Đã kết thúc'
    default: return 'Không xác định'
  }
}

const getVoucherTypeName = (type) => {
  switch (type) {
    case 'percent': return 'Phần trăm'
    case 'fixed': return 'Số tiền cố định'
    case 'freeship': return 'Miễn phí vận chuyển'
    default: return type
  }
}

// Cập nhật các method để sử dụng API
const deletePromotion = (promotion) => {
  deleteVoucher(promotion.id)
}

const editPromotion = (promotion) => {
  console.log('Editing promotion:', promotion)
  console.log('Promotion ID:', promotion.id, 'Type:', typeof promotion.id)
  editingPromotion.value = promotion
  
  let formType = 'fixed'
  if (promotion.loai === 'percent') formType = 'percentage'
  else if (promotion.loai === 'freeship') formType = 'freeship'
  else if (promotion.loai === 'buy_x_get_y') formType = 'bogo'
  
  promotionForm.value = {
    name: promotion.ma,
    description: '',
    type: formType, 
    percentValue: promotion.loai === 'percent' ? promotion.giaTri : 0,
    maxDiscount: promotion.giamToiDa,
    fixedValue: promotion.loai === 'fixed' ? promotion.giaTri : 0,
    code: promotion.ma,
    startDate: promotion.batDauLuc,
    endDate: promotion.ketThucLuc,
    usageLimit: promotion.gioiHanSuDung === -1 ? null : promotion.gioiHanSuDung,
    minOrderValue: promotion.donToiThieu || 0,
    applyTo: 'all',
    categories: [],
    sizesFilter: '',
    colorsFilter: ''
  }
  showCreateModal.value = true
}

const closeCreateModal = () => {
  showCreateModal.value = false
  editingPromotion.value = null
  promotionForm.value = {
    name: '',
    description: '',
    type: 'percentage',
    percentValue: 10,
    maxDiscount: null,
    fixedValue: null,
    code: '',
    startDate: '',
    endDate: '',
    usageLimit: null,
    minOrderValue: 0,
    applyTo: 'all',
    categories: [],
    sizesFilter: '',
    colorsFilter: ''
  }
}

const closeViewModal = () => {
  showViewModal.value = false
  selectedPromotion.value = null
}

// Load vouchers từ API
const loadVouchers = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await apiService.adminVoucher.getAll()
    console.log('Loaded vouchers from API:', response.data)
    promotions.value = response.data || []
    console.log('Promotions array after load:', promotions.value)
  } catch (err) {
    error.value = 'Không thể tải danh sách voucher'
    console.error('Lỗi khi tải voucher:', err)
  } finally {
    loading.value = false
  }
}

// Tạo voucher mới
const createVoucher = async () => {
  try {
    loading.value = true
    
    // Validation: Kiểm tra giá trị voucher
    let giaTri
    if (promotionForm.value.type === 'percentage') {
      giaTri = promotionForm.value.percentValue
    } else if (promotionForm.value.type === 'fixed') {
      giaTri = promotionForm.value.fixedValue
    } else if (promotionForm.value.type === 'freeship') {
      giaTri = 0 // Freeship có giá trị 0
    }
    
    if (promotionForm.value.type !== 'freeship' && (!giaTri || giaTri <= 0)) {
      error.value = 'Giá trị voucher không được để trống hoặc bằng 0'
      return
    }
    
    const voucherData = {
      ma: promotionForm.value.code,
      loai: promotionForm.value.type === 'percentage' ? 'percent' : 
            promotionForm.value.type === 'freeship' ? 'freeship' : 'fixed',
      giaTri: giaTri,
      giamToiDa: promotionForm.value.maxDiscount || 0,
      donToiThieu: promotionForm.value.minOrderValue || 0,
      batDauLuc: promotionForm.value.startDate,
      ketThucLuc: promotionForm.value.endDate,
      gioiHanSuDung: promotionForm.value.usageLimit === null ? -1 : promotionForm.value.usageLimit
    }
    
    const response = await apiService.adminVoucher.create(voucherData)
    promotions.value.push(response.data)
    closeCreateModal()
  } catch (err) {
    error.value = 'Không thể tạo voucher'
    console.error('Lỗi khi tạo voucher:', err)
  } finally {
    loading.value = false
  }
}

// Cập nhật voucher
const updateVoucher = async (id, voucherData) => {
  try {
    loading.value = true
    
    if (!voucherData.ma || voucherData.ma.trim() === '') {
      throw new Error('Mã voucher không được để trống')
    }
    if (!voucherData.batDauLuc) {
      throw new Error('Ngày bắt đầu không được để trống')
    }
    if (!voucherData.ketThucLuc) {
      throw new Error('Ngày kết thúc không được để trống')
    }
    
    console.log('Voucher data to update:', voucherData)
    const response = await apiService.adminVoucher.update(id, voucherData)
    const index = promotions.value.findIndex(p => p.id === id)
    if (index > -1) {
      promotions.value[index] = response.data
    }
    closeCreateModal()
  } catch (err) {
    error.value = 'Không thể cập nhật voucher: ' + err.message
    console.error('Lỗi khi cập nhật voucher:', err)
  } finally {
    loading.value = false
  }
}

// Xóa voucher
const deleteVoucher = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa voucher này?')) {
    try {
      loading.value = true
      await apiService.adminVoucher.delete(id)
      promotions.value = promotions.value.filter(p => p.id !== id)
    } catch (err) {
      error.value = 'Không thể xóa voucher'
      console.error('Lỗi khi xóa voucher:', err)
    } finally {
      loading.value = false
    }
  }
}

const savePromotion = async () => {
  try {
    loading.value = true
    error.value = null
    
    if (!promotionForm.value.code || !promotionForm.value.startDate || !promotionForm.value.endDate) {
      error.value = 'Vui lòng điền đầy đủ thông tin bắt buộc'
      loading.value = false
      return
    }
    
    let giaTri
    if (promotionForm.value.type === 'percentage') {
      giaTri = promotionForm.value.percentValue
    } else if (promotionForm.value.type === 'fixed') {
      giaTri = promotionForm.value.fixedValue
    } else if (promotionForm.value.type === 'freeship') {
      giaTri = 0
    } else if (promotionForm.value.type === 'bogo') {
      giaTri = 0
    }
    
    if (!promotionForm.value.type.includes('freeship') && !promotionForm.value.type.includes('bogo') && (!giaTri || giaTri <= 0)) {
      error.value = 'Giá trị voucher không được để trống hoặc bằng 0'
      loading.value = false
      return
    }
    
    const voucherData = {
      ma: promotionForm.value.code,
      loai: promotionForm.value.type === 'percentage' ? 'percent' : 
            promotionForm.value.type === 'freeship' ? 'freeship' :
            promotionForm.value.type === 'bogo' ? 'buy_x_get_y' : 'fixed',
      giaTri: giaTri,
      giamToiDa: promotionForm.value.maxDiscount || 0,
      donToiThieu: promotionForm.value.minOrderValue || 0,
      batDauLuc: promotionForm.value.startDate,
      ketThucLuc: promotionForm.value.endDate,
      gioiHanSuDung: promotionForm.value.usageLimit === null ? -1 : promotionForm.value.usageLimit
    }
    
    console.log('Voucher data to save:', voucherData)
     
    if (editingPromotion.value) {
      await updateVoucher(editingPromotion.value.id, voucherData)
    } else {
      await createVoucher()
    }
  } catch (err) {
    error.value = 'Không thể lưu voucher: ' + (err.message || 'Lỗi không xác định')
    console.error('Lỗi khi lưu voucher:', err)
  } finally {
    loading.value = false
  }
}

// Load data khi component mount
onMounted(() => {
  loadVouchers()
})

// const formatDateForAPI = (date) => {
//   if (!date) return null
//   const d = new Date(date)
//   return d.toISOString()
// }

// const generateVoucherCode = (prefix = 'VOUCHER') => {
//   const timestamp = Date.now()
//   const random = Math.floor(Math.random() * 1000)
//   return `${prefix}_${timestamp}_${random}`
// }
</script>

<style scoped>
.admin-promotions {
  width: 100%;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
}

/* Stats */
.promotion-stats {
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border-left: 4px solid #10b981;
}

.stat-card.scheduled {
  border-left: 4px solid #6366f1;
}

.stat-card.used {
  border-left: 4px solid #f59e0b;
}

.stat-card.revenue {
  border-left: 4px solid #ef4444;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-card.active .stat-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-card.scheduled .stat-icon {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.stat-card.used .stat-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.stat-card.revenue .stat-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
  margin-top: 0.25rem;
}

/* Filters */
.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #e2e8f0;
}

.filters-row {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.search-box input {
  padding-left: 2.5rem;
}

/* Tabs */
.promotions-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  background: white;
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1.5rem;
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  background: #f8fafb;
  border-color: #6366f1;
  color: #6366f1;
}

.tab-btn.active {
  background: #eef2ff;
  border-color: #6366f1;
  color: #6366f1;
  font-weight: 600;
}

/* Promotions List */
.promotions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.promotion-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.promotion-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.promotion-card.promotion-active {
  border-left: 4px solid #10b981;
  background: linear-gradient(135deg, #ffffff 0%, #f0fdf4 100%);
}

.promotion-card.promotion-scheduled {
  border-left: 4px solid #6366f1;
}

.promotion-card.promotion-expired {
  opacity: 0.7;
  border-left: 4px solid #94a3b8;
}

.promotion-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.promotion-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.type-percentage {
  background: #eef2ff;
  color: #6366f1;
}

.type-fixed {
  background: #dcfce7;
  color: #16a34a;
}

.type-freeship {
  background: #fef3c7;
  color: #d97706;
}

.type-bogo {
  background: #fce7f3;
  color: #db2777;
}

.promotion-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
}

.promotion-description {
  color: #64748b;
  margin: 0;
  font-size: 0.9rem;
}

.promotion-value {
  text-align: right;
}

.value-display {
  font-size: 2.5rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.value-percentage {
  color: #6366f1;
}

.value-fixed {
  color: #10b981;
}

.value-freeship {
  color: #f59e0b;
}

.value-bogo {
  color: #db2777;
}

.promotion-status {
  margin-top: 0.5rem;
}

.status-badge {
  padding: 0.35rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  color: white;
}

.status-active {
  background: #10b981;
}

.status-scheduled {
  background: #6366f1;
}

.status-expired {
  background: #94a3b8;
}

.status-paused {
  background: #f59e0b;
}

/* Promotion Body */
.promotion-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.promotion-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
  font-size: 0.9rem;
}

.detail-item i {
  color: #6366f1;
}

.detail-item code {
  background: #f1f5f9;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}

.promotion-actions {
  display: flex;
  gap: 0.5rem;
}

/* Progress */
.promotion-progress {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.progress {
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-text {
  color: #64748b;
  font-size: 0.8rem;
}

/* Empty State */
.empty-state {
  background: white;
  border-radius: 12px;
  padding: 4rem 2rem;
  text-align: center;
  border: 2px dashed #e2e8f0;
}

.empty-state i {
  color: #cbd5e1;
  margin-bottom: 1rem;
}

.empty-state h5 {
  color: #475569;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #94a3b8;
  margin-bottom: 1.5rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.modal-large {
  max-width: 800px;
}

.modal-content.modal-medium {
  max-width: 600px;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-weight: 600;
  color: #1e293b;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #64748b;
  cursor: pointer;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

/* Form */
.form-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #475569;
  margin: 0 0 0.75rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.category-checkboxes {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.75rem;
  padding: 1rem;
  background: #f8fafb;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.form-check-input:checked {
  background-color: #6366f1;
  border-color: #6366f1;
}

/* Loading & Error States */
.loading-state,
.error-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  border: 2px dashed #e2e8f0;
}

.loading-state .spinner-border {
  width: 3rem;
  height: 3rem;
}

.error-state i {
  color: #dc3545;
  margin-bottom: 1rem;
}

.error-state h5 {
  color: #dc3545;
  margin-bottom: 0.5rem;
}

.error-state p {
  color: #6c757d;
  margin-bottom: 1.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-right {
    width: 100%;
  }

  .promotions-tabs {
    flex-direction: column;
  }

  .promotion-header {
    flex-direction: column;
  }

  .promotion-value {
    text-align: left;
    margin-top: 1rem;
  }

  .promotion-details {
    flex-direction: column;
    gap: 0.75rem;
  }

  .promotion-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
    width: 100%;
  }
}

/* Voucher Detail Styles */
.voucher-detail {
  padding: 1rem 0;
}

.detail-section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e5e7eb;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 0.875rem;
}

.voucher-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #059669;
  background: #ecfdf5;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.voucher-type {
  color: #7c3aed;
  font-weight: 500;
}

.voucher-value, .voucher-max, .voucher-min {
  color: #dc2626;
  font-weight: 600;
}

.voucher-limit {
  color: #059669;
  font-weight: 500;
}

.voucher-date {
  color: #374151;
  font-weight: 500;
}

.voucher-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  display: inline-block;
}

.voucher-status.active {
  background: #dcfce7;
  color: #166534;
}

.voucher-status.scheduled {
  background: #fef3c7;
  color: #92400e;
}

.voucher-status.expired {
  background: #fee2e2;
  color: #991b1b;
}
</style>

