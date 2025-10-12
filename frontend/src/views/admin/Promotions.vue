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

    <!-- Promotions List -->
    <div class="promotions-list">
      <div
        v-for="promotion in filteredPromotions"
        :key="promotion.id"
        class="promotion-card"
        :class="'promotion-' + promotion.status"
      >
        <div class="promotion-header">
          <div class="promotion-info">
            <div class="promotion-type-badge" :class="'type-' + promotion.type">
              <i :class="getTypeIcon(promotion.type)"></i>
              {{ getTypeName(promotion.type) }}
            </div>
            <h5 class="promotion-name">{{ promotion.name }}</h5>
            <p class="promotion-description">{{ promotion.description }}</p>
          </div>
          
          <div class="promotion-value">
            <div class="value-display" :class="'value-' + promotion.type">
              {{ getValueDisplay(promotion) }}
            </div>
            <div class="promotion-status">
              <span :class="['status-badge', 'status-' + promotion.status]">
                {{ getStatusText(promotion.status) }}
              </span>
            </div>
          </div>
        </div>

        <div class="promotion-body">
          <div class="promotion-details">
            <div class="detail-item">
              <i class="bi bi-calendar-range"></i>
              <span>{{ formatDate(promotion.startDate) }} - {{ formatDate(promotion.endDate) }}</span>
            </div>
            
            <div class="detail-item">
              <i class="bi bi-tag"></i>
              <span>Mã: <code>{{ promotion.code }}</code></span>
            </div>
            
            <div v-if="promotion.conditions" class="detail-item">
              <i class="bi bi-filter"></i>
              <span>{{ getConditionsSummary(promotion.conditions) }}</span>
            </div>

            <div class="detail-item">
              <i class="bi bi-bar-chart"></i>
              <span>Đã dùng: {{ promotion.usedCount }}/{{ promotion.usageLimit || '∞' }}</span>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Reactive data
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const activeTab = ref('all')
const showCreateModal = ref(false)
const editingPromotion = ref(null)

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

// Mock promotions data
const promotions = ref([
  {
    id: 1,
    name: 'Flash Sale Tết 2025',
    description: 'Giảm giá mạnh dịp Tết Nguyên Đán',
    type: 'percentage',
    percentValue: 30,
    maxDiscount: 200000,
    code: 'TET2025',
    startDate: new Date('2025-01-25'),
    endDate: new Date('2025-02-15'),
    status: 'active',
    usedCount: 45,
    usageLimit: 100,
    minOrderValue: 500000,
    applyTo: 'all',
    conditions: { minOrder: 500000 }
  },
  {
    id: 2,
    name: 'Freeship toàn quốc',
    description: 'Miễn phí vận chuyển cho đơn hàng từ 300k',
    type: 'freeship',
    code: 'FREESHIP300',
    startDate: new Date('2025-01-01'),
    endDate: new Date('2025-12-31'),
    status: 'active',
    usedCount: 128,
    usageLimit: null,
    minOrderValue: 300000,
    applyTo: 'all',
    conditions: { minOrder: 300000 }
  },
  {
    id: 3,
    name: 'Sale Áo sơ mi 20%',
    description: 'Giảm 20% cho tất cả áo sơ mi',
    type: 'percentage',
    percentValue: 20,
    maxDiscount: 150000,
    code: 'SHIRT20',
    startDate: new Date('2025-02-20'),
    endDate: new Date('2025-02-28'),
    status: 'scheduled',
    usedCount: 0,
    usageLimit: 200,
    minOrderValue: 0,
    applyTo: 'category',
    categories: ['ao-so-mi'],
    conditions: { category: 'áo sơ mi' }
  }
])

// Computed
const activePromotions = computed(() => promotions.value.filter(p => p.status === 'active'))
const scheduledPromotions = computed(() => promotions.value.filter(p => p.status === 'scheduled'))
const expiredPromotions = computed(() => promotions.value.filter(p => p.status === 'expired'))

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

const getValueDisplay = (promotion) => {
  switch (promotion.type) {
    case 'percentage':
      return `-${promotion.percentValue}%`
    case 'fixed':
      return `-${formatCurrency(promotion.fixedValue)}`
    case 'freeship':
      return 'FREE'
    case 'bogo':
      return 'BOGO'
    default:
      return '-'
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
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date)
}

const generateCode = () => {
  const timestamp = Date.now().toString().slice(-6)
  promotionForm.value.code = `AURO-${timestamp}`
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
  alert(`Xem chi tiết: ${promotion.name}\nCode: ${promotion.code}\nĐã dùng: ${promotion.usedCount}`)
}

const editPromotion = (promotion) => {
  editingPromotion.value = promotion
  promotionForm.value = { ...promotion }
  showCreateModal.value = true
}

const duplicatePromotion = (promotion) => {
  const duplicated = {
    ...promotion,
    id: Date.now(),
    name: promotion.name + ' (Copy)',
    code: promotion.code + '-COPY',
    usedCount: 0,
    status: 'scheduled'
  }
  promotions.value.push(duplicated)
}

const deletePromotion = (promotion) => {
  if (confirm(`Xóa khuyến mãi "${promotion.name}"?`)) {
    const index = promotions.value.findIndex(p => p.id === promotion.id)
    if (index > -1) {
      promotions.value.splice(index, 1)
    }
  }
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

const savePromotion = () => {
  if (editingPromotion.value) {
    const index = promotions.value.findIndex(p => p.id === editingPromotion.value.id)
    if (index > -1) {
      promotions.value[index] = {
        ...promotionForm.value,
        id: editingPromotion.value.id,
        usedCount: editingPromotion.value.usedCount
      }
    }
  } else {
    const newPromotion = {
      ...promotionForm.value,
      id: Date.now(),
      status: 'scheduled',
      usedCount: 0,
      startDate: new Date(promotionForm.value.startDate),
      endDate: new Date(promotionForm.value.endDate)
    }
    promotions.value.push(newPromotion)
  }
  
  closeCreateModal()
}
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
</style>

