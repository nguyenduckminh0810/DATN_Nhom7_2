<template>
  <div class="dashboard-content">
    <!-- Dashboard Header -->
    <div class="dashboard-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">Tổng Quan Hoạt Động</h1>
          <p class="page-subtitle">Theo dõi hiệu suất kinh doanh của bạn</p>
        </div>
        <div class="header-right">
          <div class="realtime-info">
            <div class="live-indicator">
              <i class="bi bi-circle-fill"></i>
              <span>Cập nhật realtime</span>
            </div>
            <div class="last-updated">
              {{ new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Store Overview Section -->
    <section class="overview-section">
      <div class="overview-grid">
        <div class="overview-card revenue">
          <div class="card-header">
            <div class="card-icon">
              <i class="ph bi bi-currency-dollar"></i>
            </div>
            <div class="card-info">
              <div class="info-title">
                <h3>Doanh thu hôm nay</h3>
                <div class="tooltip-wrapper">
                  <i class="bi bi-info-circle info-icon"></i>
                  <div class="tooltip-content">
                    Tổng doanh thu từ các đơn hàng có trạng thái "Hoàn tất" trong ngày hôm nay (từ
                    00:00 đến hiện tại)
                  </div>
                </div>
              </div>
              <p class="card-subtitle">Đơn hàng hoàn tất hôm nay</p>
            </div>
          </div>
          <div class="card-content">
            <div class="value-section">
              <div class="main-value">
                {{ summary?.revenueToday ? summary.revenueToday.toLocaleString('vi-VN') : '0' }}₫
              </div>
              <p v-if="!summary?.revenueToday || summary?.revenueToday === 0" class="zero-hint">
                Chưa có đơn hàng hoàn tất hôm nay
              </p>
            </div>
            <div
              v-if="revenueGrowth !== null && summary?.revenueToday > 0"
              :class="['trend', revenueGrowth >= 0 ? 'positive' : 'negative']"
            >
              <i :class="revenueGrowth >= 0 ? 'bi bi-graph-up' : 'bi bi-graph-down'"></i>
              <span>{{ revenueGrowth >= 0 ? '+' : '' }}{{ revenueGrowth }}%</span>
            </div>
          </div>
        </div>

        <div class="overview-card orders">
          <div class="card-header">
            <div class="card-icon">
              <i class="ph bi bi-cart3"></i>
            </div>
            <div class="card-info">
              <div class="info-title">
                <h3>Đơn hàng mới</h3>
                <div class="tooltip-wrapper">
                  <i class="bi bi-info-circle info-icon"></i>
                  <div class="tooltip-content">
                    Số đơn hàng mới được tạo trong 24 giờ qua (tất cả trạng thái)
                  </div>
                </div>
              </div>
              <p class="card-subtitle">Trong 24h qua</p>
            </div>
          </div>
          <div class="card-content">
            <div class="main-value">{{ summary?.newOrders24h ?? 0 }}</div>
            <div
              v-if="ordersGrowth !== null"
              :class="['trend', ordersGrowth >= 0 ? 'positive' : 'negative']"
            >
              <i :class="ordersGrowth >= 0 ? 'bi bi-graph-up' : 'bi bi-graph-down'"></i>
              <span>{{ ordersGrowth >= 0 ? '+' : '' }}{{ ordersGrowth }}%</span>
            </div>
          </div>
        </div>

        <div class="overview-card customers">
          <div class="card-header">
            <div class="card-icon">
              <i class="ph bi bi-people"></i>
            </div>
            <div class="card-info">
              <div class="info-title">
                <h3>Khách hàng mới</h3>
                <div class="tooltip-wrapper">
                  <i class="bi bi-info-circle info-icon"></i>
                  <div class="tooltip-content">
                    Số khách hàng mới đăng ký tài khoản trong ngày hôm nay
                  </div>
                </div>
              </div>
              <p class="card-subtitle">Đăng ký hôm nay</p>
            </div>
          </div>
          <div class="card-content">
            <div class="main-value">{{ summary?.newCustomersToday ?? 0 }}</div>
            <div
              v-if="customersGrowth !== null"
              :class="['trend', customersGrowth >= 0 ? 'positive' : 'negative']"
            >
              <i :class="customersGrowth >= 0 ? 'bi bi-graph-up' : 'bi bi-graph-down'"></i>
              <span>{{ customersGrowth >= 0 ? '+' : '' }}{{ customersGrowth }}%</span>
            </div>
          </div>
        </div>

        <div class="overview-card inventory">
          <div class="card-header">
            <div class="card-icon">
              <i class="ph bi bi-box"></i>
            </div>
            <div class="card-info">
              <div class="info-title">
                <h3>Sản phẩm sắp hết</h3>
                <div class="tooltip-wrapper">
                  <i class="bi bi-info-circle info-icon"></i>
                  <div class="tooltip-content">
                    Số sản phẩm có ít nhất 1 biến thể với số lượng tồn kho ≤ 10 (cần nhập thêm hàng)
                  </div>
                </div>
              </div>
              <p class="card-subtitle">Cần nhập hàng</p>
            </div>
          </div>
          <div class="card-content">
            <div class="main-value">{{ summary?.lowStockCount ?? 0 }}</div>
            <div class="trend warning">
              <i class="ph bi bi-exclamation-triangle"></i>
              <span>Cần nhập</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Content Grid -->
    <section class="main-content-section">
      <div class="content-grid">
        <!-- Left Column: Alerts -->
        <div class="left-column">
          <div class="section-card alerts-card">
            <div class="section-header">
              <h3 class="section-title">
                <i class="bi bi-bell"></i>
                Cần chú ý
              </h3>
            </div>
            <div class="alert-list">
              <div class="alert-item urgent">
                <div class="alert-icon">
                  <i class="bi bi-exclamation-triangle-fill"></i>
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alerts?.pendingOrders ?? 0 }} đơn chờ xác nhận</div>
                  <div class="alert-subtitle">Cần xử lý ngay</div>
                </div>
                <button class="alert-btn" @click="$router.push('/admin/orders?status=pending')">
                  <i class="bi bi-arrow-right"></i>
                </button>
              </div>
              <div class="alert-item warning">
                <div class="alert-icon">
                  <i class="bi bi-box-seam"></i>
                </div>
                <div class="alert-content">
                  <div class="alert-title">
                    {{ alerts?.lowStockProducts ?? 0 }} sản phẩm sắp hết
                  </div>
                  <div class="alert-subtitle">Cần nhập hàng</div>
                </div>
                <button class="alert-btn" @click="$router.push('/admin/inventory')">
                  <i class="bi bi-arrow-right"></i>
                </button>
              </div>
              <div class="alert-item info">
                <div class="alert-icon">
                  <i class="bi bi-truck"></i>
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alerts?.needShipping ?? 0 }} đơn cần giao</div>
                  <div class="alert-subtitle">Đang vận chuyển</div>
                </div>
                <button class="alert-btn" @click="$router.push('/admin/orders?status=shipping')">
                  <i class="bi bi-arrow-right"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Order Status -->
        <div class="right-column">
          <div class="section-card status-card">
            <div class="section-header">
              <h3 class="section-title">
                <i class="bi bi-bar-chart"></i>
                Trạng thái đơn hàng
              </h3>
            </div>
            <div class="status-grid">
              <div class="status-item pending">
                <div class="status-icon">
                  <i class="bi bi-clock-history"></i>
                </div>
                <div class="status-info">
                  <div class="status-count">{{ orderStatusCounts?.pending ?? 0 }}</div>
                  <div class="status-label">Chờ xử lý</div>
                </div>
              </div>
              <div class="status-item shipping">
                <div class="status-icon">
                  <i class="bi bi-truck"></i>
                </div>
                <div class="status-info">
                  <div class="status-count">{{ orderStatusCounts?.shipping ?? 0 }}</div>
                  <div class="status-label">Đang giao</div>
                </div>
              </div>
              <div class="status-item completed">
                <div class="status-icon">
                  <i class="bi bi-check-circle-fill"></i>
                </div>
                <div class="status-info">
                  <div class="status-count">{{ orderStatusCounts?.completed ?? 0 }}</div>
                  <div class="status-label">Hoàn thành</div>
                </div>
              </div>
              <div class="status-item cancelled">
                <div class="status-icon">
                  <i class="bi bi-x-circle-fill"></i>
                </div>
                <div class="status-info">
                  <div class="status-count">{{ orderStatusCounts?.cancelled ?? 0 }}</div>
                  <div class="status-label">Đã hủy</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Recent Orders Section -->
    <section class="recent-orders-section">
      <div class="section-card">
        <div class="section-header">
          <h3 class="section-title">
            <i class="bi bi-receipt"></i>
            Đơn hàng gần đây
          </h3>
          <button class="view-all-link" @click="$router.push('/admin/orders')">
            Xem tất cả
            <i class="bi bi-arrow-right"></i>
          </button>
        </div>
        <div class="orders-table">
          <div v-if="isLoadingRecentOrders" class="loading-state">
            <i class="bi bi-arrow-repeat spin"></i>
            <span>Đang tải...</span>
          </div>
          <div v-else-if="recentOrders.length === 0" class="empty-state">
            <i class="bi bi-inbox"></i>
            <span>Chưa có đơn hàng nào</span>
          </div>
          <div v-else class="orders-list">
            <div class="order-row" v-for="order in recentOrders" :key="order.id">
              <div class="order-col order-id">
                <span class="label-mobile">Mã đơn:</span>
                <span class="value">#{{ order.id }}</span>
              </div>
              <div class="order-col customer">
                <span class="label-mobile">Khách hàng:</span>
                <span class="value customer-name">{{ order.customer }}</span>
              </div>
              <div class="order-col products">
                <span class="label-mobile">Sản phẩm:</span>
                <span class="value product-count">
                  <i class="bi bi-box-seam"></i>
                  {{ order.productCount }} SP
                </span>
              </div>
              <div class="order-col payment">
                <span class="label-mobile">Thanh toán:</span>
                <span class="value payment-method" :class="order.paymentMethodClass">
                  <i :class="order.paymentIcon"></i>
                  {{ order.paymentMethodText }}
                </span>
              </div>
              <div class="order-col time">
                <span class="label-mobile">Thời gian:</span>
                <span class="value time-text">{{ order.timeFormatted }}</span>
              </div>
              <div class="order-col amount">
                <span class="label-mobile">Giá trị:</span>
                <span class="value amount-text">{{ order.amount }}</span>
              </div>
              <div class="order-col status">
                <span class="status-badge" :class="order.status">
                  {{ order.statusText }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import thongKeService from '@/services/thongKeService'

// Reactive state
const summary = ref(null)
const alerts = ref(null)
const isLoadingSummary = ref(false)
const isLoadingAlerts = ref(false)

// Real data from API
const recentOrders = ref([])
const orderStatusCounts = ref(null)
const isLoadingRecentOrders = ref(false)
const isLoadingOrderStatusCounts = ref(false)

// Growth rates for overview cards - from API
const revenueGrowth = computed(() => {
  if (!summary.value || summary.value.revenueGrowth === undefined) return null
  return Number(summary.value.revenueGrowth)
})

const ordersGrowth = computed(() => {
  if (!summary.value || summary.value.ordersGrowth === undefined) return null
  return Number(summary.value.ordersGrowth)
})

const customersGrowth = computed(() => {
  if (!summary.value || summary.value.customersGrowth === undefined) return null
  return Number(summary.value.customersGrowth)
})

// Methods

// Load recent orders from API
const loadRecentOrders = async () => {
  try {
    isLoadingRecentOrders.value = true
    console.log('📋 Loading recent orders...')
    const response = await thongKeService.getRecentOrders({ limit: 4 })
    console.log('📋 Recent orders response:', response)
    const data = response?.data ?? response
    recentOrders.value = (data || []).map((order) => ({
      id: order.orderCode || order.id,
      customer: order.customer,
      time: order.time,
      timeFormatted: formatDateTime(order.time),
      amount: formatCurrency(order.amount),
      status: mapOrderStatus(order.status),
      statusText: getStatusDisplayName(order.status),
      productCount: order.productCount || 0,
      paymentMethod: order.paymentMethod || 'COD',
      paymentMethodText: getPaymentMethodText(order.paymentMethod),
      paymentMethodClass: getPaymentMethodClass(order.paymentMethod),
      paymentIcon: getPaymentIcon(order.paymentMethod),
      shippingCity: order.shippingCity || '',
    }))
    console.log('✅ Recent orders loaded:', recentOrders.value)
  } catch (error) {
    console.error('❌ Error loading recent orders:', error)
    console.error('❌ Error details:', error.response?.data || error.message)
    recentOrders.value = []
  } finally {
    isLoadingRecentOrders.value = false
  }
}

// Load order status counts from API
const loadOrderStatusCounts = async () => {
  try {
    isLoadingOrderStatusCounts.value = true
    console.log('📊 Loading order status counts...')
    const response = await thongKeService.getOrderStatusCounts()
    console.log('📊 Order status counts response:', response)
    orderStatusCounts.value = response?.data ?? response
    console.log('✅ Order status counts loaded:', orderStatusCounts.value)
  } catch (error) {
    console.error('❌ Error loading order status counts:', error)
    console.error('❌ Error details:', error.response?.data || error.message)
    orderStatusCounts.value = null
  } finally {
    isLoadingOrderStatusCounts.value = false
  }
}

// Helper methods
const formatCurrency = (amount) => {
  if (!amount) return '0₫'
  const num = Number(amount)
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M₫'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(0) + 'k₫'
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(num)
}

const mapOrderStatus = (status) => {
  // Map English status from API to CSS class
  const statusMap = {
    PENDING: 'pending',
    SHIPPING: 'shipping',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    // Fallback for Vietnamese (backward compatibility)
    'Chờ xác nhận': 'pending',
    'Đang giao': 'shipping',
    'Hoàn tất': 'completed',
    'Đã hủy': 'cancelled',
  }
  return statusMap[status] || 'pending'
}

// Get Vietnamese display name for status
const getStatusDisplayName = (status) => {
  const displayMap = {
    PENDING: 'Chờ xác nhận',
    SHIPPING: 'Đang giao',
    COMPLETED: 'Hoàn tất',
    CANCELLED: 'Đã hủy',
  }
  return displayMap[status] || status
}

// Format date time for display
const formatDateTime = (timeString) => {
  if (!timeString) return ''
  try {
    const date = new Date(timeString)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    // If today, show relative time
    if (diffMins < 60) {
      return diffMins <= 0 ? 'Vừa xong' : `${diffMins} phút trước`
    } else if (diffHours < 24) {
      return `${diffHours} giờ trước`
    } else if (diffDays < 7) {
      return `${diffDays} ngày trước`
    }

    // Otherwise show formatted date
    const day = date.getDate()
    const month = date.getMonth() + 1
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${day} Th${month}, ${hours}:${minutes}`
  } catch {
    return timeString
  }
}

// Get payment method display text
const getPaymentMethodText = (method) => {
  const methodMap = {
    COD: 'COD',
    VNPAY: 'VNPay',
    BANKING: 'Banking',
  }
  return methodMap[method] || method || 'COD'
}

// Get payment method CSS class
const getPaymentMethodClass = (method) => {
  const classMap = {
    COD: 'payment-cod',
    VNPAY: 'payment-vnpay',
    BANKING: 'payment-banking',
  }
  return classMap[method] || 'payment-cod'
}

// Get payment icon
const getPaymentIcon = (method) => {
  const iconMap = {
    COD: 'bi bi-cash-coin',
    VNPAY: 'bi bi-credit-card',
    BANKING: 'bi bi-bank',
  }
  return iconMap[method] || 'bi bi-cash-coin'
}

// Initialize dashboard on mount
onMounted(async () => {
  console.log('🚀 Dashboard mounting...')
  // Load all data in parallel
  await Promise.all([
    (async () => {
      try {
        isLoadingSummary.value = true
        const s = await thongKeService.getSummary({ lowStockThreshold: 10 })
        summary.value = s?.data ?? s
        console.log('✅ Summary loaded:', summary.value)
        console.log('📊 Revenue Today:', summary.value?.revenueToday)
        console.log('📊 New Orders 24h:', summary.value?.newOrders24h)
        console.log('📊 New Customers Today:', summary.value?.newCustomersToday)
        console.log('📊 Low Stock Count:', summary.value?.lowStockCount)
      } catch (error) {
        console.error('❌ Error loading summary:', error)
      } finally {
        isLoadingSummary.value = false
      }
    })(),
    (async () => {
      try {
        isLoadingAlerts.value = true
        const a = await thongKeService.getAlerts({ lowStockThreshold: 10 })
        alerts.value = a?.data ?? a
      } catch (error) {
        console.error('Error loading alerts:', error)
      } finally {
        isLoadingAlerts.value = false
      }
    })(),
    loadRecentOrders(),
    loadOrderStatusCounts(),
  ])
})
</script>

<style scoped>
/* Dashboard Content Styles */
.dashboard-content {
  padding: 0;
  background: #f8fafc;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
}

/* Dashboard Header */
.dashboard-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.page-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  font-size: 1rem;
  color: #6b7280;
  margin: 0;
}

.realtime-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #10b981;
  font-size: 0.875rem;
  font-weight: 500;
}

.live-indicator i {
  font-size: 0.5rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.last-updated {
  font-size: 0.75rem;
  color: #6b7280;
}

/* Overview Section */
.overview-section {
  margin-bottom: 2rem;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.overview-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.overview-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.card-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.card-info {
  flex: 1;
}

.info-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-title h3 {
  margin: 0;
}

.tooltip-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.info-icon {
  font-size: 0.875rem;
  color: #9ca3af;
  cursor: help;
  transition: color 0.2s;
}

.info-icon:hover {
  color: #6366f1;
}

.tooltip-content {
  position: absolute;
  bottom: calc(100% + 0.5rem);
  left: 50%;
  transform: translateX(-50%);
  background: #1f2937;
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.75rem;
  line-height: 1.4;
  white-space: normal;
  width: 250px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s;
  z-index: 1000;
  pointer-events: none;
}

.tooltip-content::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #1f2937;
}

.tooltip-wrapper:hover .tooltip-content {
  opacity: 1;
  visibility: visible;
}

.overview-card.revenue .card-icon {
  background: #dcfce7;
  color: #16a34a;
}

.overview-card.orders .card-icon {
  background: #dbeafe;
  color: #2563eb;
}

.overview-card.customers .card-icon {
  background: #fef3c7;
  color: #d97706;
}

.overview-card.inventory .card-icon {
  background: #fee2e2;
  color: #dc2626;
}

.card-info h3 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.25rem 0;
}

.card-subtitle {
  font-size: 0.75rem;
  color: #9ca3af;
  margin: 0;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.value-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.main-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
}

.zero-hint {
  font-size: 0.75rem;
  color: #9ca3af;
  margin: 0;
  font-style: italic;
}

.trend {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-weight: 600;
}

.trend.positive {
  color: #16a34a;
}

.trend.negative {
  color: #dc2626;
}

.trend.warning {
  color: #d97706;
}

/* Main Content Section */
.main-content-section {
  margin-bottom: 2rem;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

/* Section Card */
.section-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  height: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f3f4f6;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title i {
  color: #6366f1;
}

/* Alerts */
.alert-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;
  border-radius: 10px;
  background: #f9fafb;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.alert-item:hover {
  background: #f3f4f6;
  transform: translateX(2px);
}

.alert-item.urgent {
  border-left-color: #ef4444;
  background: linear-gradient(to right, #fef2f2, #ffffff);
}

.alert-item.warning {
  border-left-color: #f59e0b;
  background: linear-gradient(to right, #fffbeb, #ffffff);
}

.alert-item.info {
  border-left-color: #3b82f6;
  background: linear-gradient(to right, #eff6ff, #ffffff);
}

.alert-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  flex-shrink: 0;
}

.alert-item.urgent .alert-icon {
  background: #fee2e2;
  color: #dc2626;
}

.alert-item.warning .alert-icon {
  background: #fef3c7;
  color: #f59e0b;
}

.alert-item.info .alert-icon {
  background: #dbeafe;
  color: #3b82f6;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.alert-subtitle {
  font-size: 0.8125rem;
  color: #6b7280;
  margin: 0;
}

.alert-btn {
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  width: 2rem;
  height: 2rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
<<<<<<< HEAD
  font-size: 1rem;
}

.insight-icon.revenue {
  background: #dbeafe;
  color: #2563eb;
}

.insight-icon.orders {
  background: #dcfce7;
  color: #16a34a;
}

.insight-icon.profit {
  background: #fef3c7;
  color: #d97706;
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-size: 0.75rem;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 0.25rem;
}

.insight-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.insight-value.positive {
  color: #16a34a;
}

.insight-value.negative {
  color: #dc2626;
}

.insight-subtitle {
  font-size: 0.75rem;
  color: #9ca3af;
}

.chart-content {
  position: relative;
  min-height: 350px;
}

.chart-loading,
.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 350px;
  padding: 2rem;
}

.chart-loading .spinner-border {
  width: 3rem;
  height: 3rem;
}

.chart-empty i {
  opacity: 0.5;
}

.top-products-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.top-products-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.view-all-btn {
  background: none;
  border: none;
  color: #6366f1;
  font-size: 0.875rem;
  font-weight: 500;
=======
>>>>>>> master
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.alert-btn:hover {
  background: #6366f1;
  color: #ffffff;
}

/* Order Status */
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.875rem;
}

.status-item {
  background: #f9fafb;
  border-radius: 10px;
  padding: 1.125rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.status-item:hover {
  background: #ffffff;
  border-color: currentColor;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.status-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.status-item.pending .status-icon {
  background: #fef3c7;
  color: #d97706;
}

.status-item.pending:hover {
  border-color: #fbbf24;
}

.status-item.shipping .status-icon {
  background: #dbeafe;
  color: #3b82f6;
}

.status-item.shipping:hover {
  border-color: #60a5fa;
}

.status-item.completed .status-icon {
  background: #dcfce7;
  color: #16a34a;
}

.status-item.completed:hover {
  border-color: #4ade80;
}

.status-item.cancelled .status-icon {
  background: #fee2e2;
  color: #dc2626;
}

.status-item.cancelled:hover {
  border-color: #f87171;
}

.status-info {
  flex: 1;
}

.status-count {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 0.375rem;
}

.status-label {
  font-size: 0.8125rem;
  color: #6b7280;
  font-weight: 500;
}

/* Recent Orders Section */
.recent-orders-section {
  margin-bottom: 2rem;
}

.view-all-link {
  background: none;
  border: none;
  color: #6366f1;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.375rem;
  transition: all 0.2s;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
}

.view-all-link:hover {
  background: #f3f4f6;
  color: #4f46e5;
}

.view-all-link i {
  font-size: 0.75rem;
}

.orders-table {
  background: #ffffff;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.order-row {
  display: grid;
  grid-template-columns: 100px 1.5fr 100px 130px 140px 110px 120px;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #f3f4f6;
  transition: all 0.2s;
}

.order-row:last-child {
  border-bottom: none;
}

.order-row:hover {
  background: #f9fafb;
}

.order-col {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.label-mobile {
  display: none;
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.order-col .value {
  font-size: 0.875rem;
  color: #1f2937;
}

.order-col.order-id .value {
  font-weight: 600;
  color: #6366f1;
}

.order-col.customer .customer-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-col.products .product-count {
  color: #6b7280;
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.order-col.products i {
  font-size: 0.875rem;
}

.order-col.payment .payment-method {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

.payment-method.payment-cod {
  background: #f3f4f6;
  color: #4b5563;
}

.payment-method.payment-vnpay {
  background: #dbeafe;
  color: #2563eb;
}

.payment-method.payment-banking {
  background: #e0e7ff;
  color: #6366f1;
}

.order-col.time .time-text {
  color: #6b7280;
  font-size: 0.8125rem;
}

.order-col.amount .amount-text {
  font-weight: 600;
  color: #16a34a;
}

.status-badge {
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.shipping {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.completed {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #dc2626;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.section-controls {
  display: flex;
  gap: 0.5rem;
}

.time-filter {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #6b7280;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.time-filter:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.time-filter.active {
  background: #6366f1;
  border-color: #6366f1;
  color: #ffffff;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.kpi-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.kpi-card.positive {
  border-left: 4px solid #10b981;
}

.kpi-card.negative {
  border-left: 4px solid #ef4444;
}

.kpi-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

.kpi-card.positive .kpi-icon {
  background: #ecfdf5;
  color: #10b981;
}

.kpi-card.negative .kpi-icon {
  background: #fef2f2;
  color: #ef4444;
}

.kpi-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 0.5rem 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.kpi-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.kpi-change {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-weight: 600;
}

.kpi-change.positive {
  color: #10b981;
}

.kpi-change.negative {
  color: #ef4444;
}

.kpi-trend {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 4rem;
  height: 2.5rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
    gap: 1.25rem;
  }

  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .status-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .status-grid {
    grid-template-columns: 1fr;
  }

  .order-row {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .order-col .label-mobile {
    display: block;
  }
}

@media (max-width: 480px) {
  .dashboard-content {
    padding: 0;
  }

  .section-card {
    padding: 1.25rem;
  }

  .kpi-card {
    padding: 1.25rem;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Loading and Empty States */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.loading-state .spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #9ca3af;
  font-size: 0.875rem;
  font-style: italic;
}
</style>
