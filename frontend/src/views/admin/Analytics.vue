<template>
  <div class="analytics-container">
    <!-- Advanced Header -->
    <div class="analytics-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">Thống kê & Báo cáo</h1>
          <p class="page-subtitle">Phân tích dữ liệu kinh doanh và hiệu suất toàn diện</p>
        </div>
        <div class="header-right">
          <div class="quick-actions">
            <div class="date-range-selector">
              <select v-model="selectedDateRange" @change="applyDateRange">
                <option value="today">Hôm nay</option>
                <option value="yesterday">Hôm qua</option>
                <option value="7days">7 ngày qua</option>
                <option value="30days">30 ngày qua</option>
                <option value="90days">90 ngày qua</option>
                <option value="thisMonth">Tháng này</option>
                <option value="lastMonth">Tháng trước</option>
                <option value="thisYear">Năm nay</option>
                <option value="custom">Tùy chỉnh</option>
              </select>
            </div>
            <button class="btn btn-primary btn-sm" @click="refreshData">
              <i class="bi bi-arrow-clockwise me-1"></i>Làm mới
            </button>
          </div>
        </div>
      </div>

      <!-- Real-time Status -->
      <div class="realtime-status">
        <div class="realtime-indicator">
          <i class="bi bi-circle-fill"></i>
          <span>Đang cập nhật thời gian thực</span>
        </div>
        <div class="last-updated">Cập nhật lần cuối: {{ formatTime(lastUpdated) }}</div>
      </div>
    </div>

    <!-- Key Performance Indicators -->
    <div class="kpi-section">
      <div class="row g-4">
        <div class="col-lg-3 col-md-6">
          <div class="kpi-card revenue">
            <div class="kpi-icon">
              <i class="bi bi-currency-dollar"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value" v-if="!isLoadingKpis">{{ formatCurrency(totalRevenue) }}</div>
              <div class="kpi-value loading-placeholder" v-else>Đang tải...</div>
              <div class="kpi-label">Tổng doanh thu</div>
              <div class="kpi-change" :class="revenueChange.type" v-if="!isLoadingKpis">
                <i :class="revenueChange.icon"></i>
                {{ revenueChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="bi bi-graph-up"></i>
            </div>
          </div>
        </div>

        <div class="col-lg-3 col-md-6">
          <div class="kpi-card orders">
            <div class="kpi-icon">
              <i class="bi bi-bag"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value" v-if="!isLoadingKpis">
                {{ totalOrders.toLocaleString('vi-VN') }}
              </div>
              <div class="kpi-value loading-placeholder" v-else>Đang tải...</div>
              <div class="kpi-label">Tổng đơn hàng</div>
              <div class="kpi-change" :class="ordersChange.type" v-if="!isLoadingKpis">
                <i :class="ordersChange.icon"></i>
                {{ ordersChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="bi bi-graph-up"></i>
            </div>
          </div>
        </div>

        <div class="col-lg-3 col-md-6">
          <div class="kpi-card customers">
            <div class="kpi-icon">
              <i class="bi bi-people"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value" v-if="!isLoadingKpis">
                {{ todayCustomers.toLocaleString('vi-VN') }}
              </div>
              <div class="kpi-value loading-placeholder" v-else>Đang tải...</div>
              <div class="kpi-label">Khách hàng mới</div>
              <div class="kpi-change" :class="customersChange.type" v-if="!isLoadingKpis">
                <i :class="customersChange.icon"></i>
                {{ customersChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="bi bi-graph-up"></i>
            </div>
          </div>
        </div>

        <div class="col-lg-3 col-md-6">
          <div class="kpi-card products">
            <div class="kpi-icon">
              <i class="bi bi-box"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value" v-if="!isLoadingKpis">
                {{ productsSold.toLocaleString('vi-VN') }}
              </div>
              <div class="kpi-value loading-placeholder" v-else>Đang tải...</div>
              <div class="kpi-label">Sản phẩm bán ra</div>
              <div class="kpi-change" :class="productsChange.type" v-if="!isLoadingKpis">
                <i :class="productsChange.icon"></i>
                {{ productsChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="bi bi-graph-up"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Real-time Metrics Section -->
    <div class="realtime-section">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="metric-card-small">
            <div class="metric-header">
              <span class="metric-label">Hôm nay</span>
              <div class="live-indicator">
                <i class="bi bi-circle-fill"></i>
                <span>LIVE</span>
              </div>
            </div>
            <div class="metric-value">{{ formatCurrency(todayRevenue) }}</div>
            <div class="metric-subtitle">Doanh thu</div>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="metric-card-small">
            <div class="metric-header">
              <span class="metric-label">Hôm nay</span>
            </div>
            <div class="metric-value">{{ todayOrders }}</div>
            <div class="metric-subtitle">Đơn hàng</div>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="metric-card-small">
            <div class="metric-header">
              <span class="metric-label">Hôm nay</span>
            </div>
            <div class="metric-value">{{ todayCustomers }}</div>
            <div class="metric-subtitle">Khách hàng mới</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Analytics Details Section -->
    <div class="details-section">
      <div class="row g-4">
        <!-- Top Products Section -->
        <div class="col-lg-6">
          <div class="stat-card">
            <div class="stat-header">
              <h5 class="stat-title">Sản phẩm bán chạy</h5>
              <button class="btn btn-sm btn-outline-primary" @click="viewTopProducts">
                Xem tất cả
              </button>
            </div>
            <div class="stat-content">
              <div v-if="isLoadingTopProducts" class="loading-state">
                <i class="bi bi-arrow-repeat rotate"></i>
                <span>Đang tải...</span>
              </div>
              <div v-else-if="topProducts.length === 0" class="empty-state">
                <i class="bi bi-box"></i>
                <span>Chưa có dữ liệu sản phẩm</span>
              </div>
              <div v-else class="top-products-list">
                <div v-for="(product, index) in topProducts" :key="product.id" class="product-item">
                  <div class="product-rank">{{ index + 1 }}</div>
                  <div class="product-image">
                    <img :src="product.image || '/placeholder.png'" :alt="product.name" />
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-category">{{ product.category || 'Chưa phân loại' }}</div>
                  </div>
                  <div class="product-stats">
                    <div class="product-sales">{{ product.sales }} bán</div>
                    <div class="product-revenue">{{ formatCurrency(product.revenue) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Status Distribution -->
        <div class="col-lg-6">
          <div class="stat-card">
            <div class="stat-header">
              <h5 class="stat-title">Phân bố trạng thái đơn hàng</h5>
            </div>
            <div class="stat-content">
              <div v-if="orderStatusCounts" class="status-grid">
                <div class="status-item pending">
                  <div class="status-count">{{ orderStatusCounts.pending || 0 }}</div>
                  <div class="status-label">Chờ xử lý</div>
                </div>
                <div class="status-item shipping">
                  <div class="status-count">{{ orderStatusCounts.shipping || 0 }}</div>
                  <div class="status-label">Đang giao</div>
                </div>
                <div class="status-item completed">
                  <div class="status-count">{{ orderStatusCounts.completed || 0 }}</div>
                  <div class="status-label">Hoàn thành</div>
                </div>
                <div class="status-item cancelled">
                  <div class="status-count">{{ orderStatusCounts.cancelled || 0 }}</div>
                  <div class="status-label">Đã hủy</div>
                </div>
              </div>
              <div v-else class="empty-state">
                <i class="bi bi-pie-chart"></i>
                <span>Đang tải trạng thái đơn hàng...</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Additional Analytics Section -->
    <div class="additional-analytics">
      <div class="row g-4">
        <!-- Revenue Trends -->
        <div class="col-lg-3">
          <div class="metric-card-small trend-revenue">
            <div class="metric-header">
              <span class="metric-label">Xu hướng doanh thu</span>
              <i class="bi bi-graph-up-arrow trend-icon"></i>
            </div>
            <div class="metric-value">
              {{ revenueGrowth >= 0 ? '+' : '' }}{{ revenueGrowth.toFixed(1) }}%
            </div>
            <div class="metric-subtitle">So với kỳ trước</div>
          </div>
        </div>

        <!-- Order Performance -->
        <div class="col-lg-3">
          <div class="metric-card-small trend-orders">
            <div class="metric-header">
              <span class="metric-label">Hiệu suất đơn hàng</span>
              <i class="bi bi-cart-check trend-icon"></i>
            </div>
            <div class="metric-value">
              {{
                Math.round(
                  (totalOrders / (totalOrders + (orderStatusCounts?.cancelled || 0))) * 100 || 0,
                )
              }}%
            </div>
            <div class="metric-subtitle">Tỷ lệ thành công</div>
          </div>
        </div>

        <!-- Average Order Value -->
        <div class="col-lg-3">
          <div class="metric-card-small trend-average">
            <div class="metric-header">
              <span class="metric-label">Giá trị đơn hàng TB</span>
              <i class="bi bi-calculator trend-icon"></i>
            </div>
            <div class="metric-value">
              {{ formatCurrency(totalOrders > 0 ? totalRevenue / totalOrders : 0) }}
            </div>
            <div class="metric-subtitle">Trung bình mỗi đơn</div>
          </div>
        </div>

        <!-- Customer Growth -->
        <div class="col-lg-3">
          <div class="metric-card-small trend-customers">
            <div class="metric-header">
              <span class="metric-label">Tăng trưởng KH</span>
              <i class="bi bi-people-fill trend-icon"></i>
            </div>
            <div class="metric-value">
              {{ customersGrowth >= 0 ? '+' : '' }}{{ customersGrowth.toFixed(1) }}%
            </div>
            <div class="metric-subtitle">Khách hàng mới</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import thongKeService from '@/services/thongKeService'

// Reactive data
const selectedDateRange = ref('30days')
const customStartDate = ref('')
const customEndDate = ref('')

// Computed date range for API calls
const getDateRange = () => {
  const now = new Date()
  // Set endDate to tomorrow to include full today
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  let startDate = ''
  let endDate = tomorrow.toISOString().split('T')[0] // Tomorrow in YYYY-MM-DD format to include full today

  if (selectedDateRange.value === 'custom' && customStartDate.value && customEndDate.value) {
    startDate = customStartDate.value
    // For custom dates, also extend endDate to include full day
    const customEnd = new Date(customEndDate.value)
    customEnd.setDate(customEnd.getDate() + 1)
    endDate = customEnd.toISOString().split('T')[0]
  } else {
    switch (selectedDateRange.value) {
      case 'today':
        startDate = now.toISOString().split('T')[0]
        break
      case 'yesterday': {
        const yesterday = new Date(now)
        yesterday.setDate(yesterday.getDate() - 1)
        startDate = yesterday.toISOString().split('T')[0]
        endDate = now.toISOString().split('T')[0] // Only yesterday
        break
      }
      case '7days': {
        const sevenDaysAgo = new Date(now)
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
        startDate = sevenDaysAgo.toISOString().split('T')[0]
        break
      }
      case '30days': {
        const thirtyDaysAgo = new Date(now)
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
        startDate = thirtyDaysAgo.toISOString().split('T')[0]
        break
      }
      case '90days': {
        const ninetyDaysAgo = new Date(now)
        ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90)
        startDate = ninetyDaysAgo.toISOString().split('T')[0]
        break
      }
      case 'thisMonth':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
        break
      case 'lastMonth': {
        const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
        startDate = lastMonth.toISOString().split('T')[0]
        const lastDayOfLastMonth = new Date(now.getFullYear(), now.getMonth(), 0)
        lastDayOfLastMonth.setDate(lastDayOfLastMonth.getDate() + 1)
        endDate = lastDayOfLastMonth.toISOString().split('T')[0]
        break
      }
      case 'thisYear':
        startDate = new Date(now.getFullYear(), 0, 1).toISOString().split('T')[0]
        break
      default: {
        // Default to 30 days
        const defaultStart = new Date(now)
        defaultStart.setDate(defaultStart.getDate() - 30)
        startDate = defaultStart.toISOString().split('T')[0]
      }
    }
  }

  console.log('Date range calculated:', {
    startDate,
    endDate,
    selectedRange: selectedDateRange.value,
  })
  return { startDate, endDate }
}

// Loading states
const isLoadingKpis = ref(false)
const isLoadingHourlySales = ref(false)
const isLoadingBusinessInsights = ref(false)
const isLoadingTopProducts = ref(false)

// KPI Data - from API
const totalRevenue = ref(0)
const totalOrders = ref(0)
const totalCustomers = ref(0)
const totalProducts = ref(0)
const revenueGrowth = ref(0)
const ordersGrowth = ref(0)
const customersGrowth = ref(0)
const productsGrowth = ref(0)

// Real-time metrics - from API
const todayRevenue = ref(0)
const todayOrders = ref(0)
const todayCustomers = ref(0)
const productsSold = ref(0)
const lastUpdate = ref('')
const lastUpdated = ref(new Date())

// Chart data - from API
const hourlySales = ref([])
const topProducts = ref([])
const orderStatusCounts = ref(null)

// Computed properties for change indicators
const revenueChange = computed(() => ({
  type: revenueGrowth.value >= 0 ? 'positive' : 'negative',
  icon: revenueGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(revenueGrowth.value).toFixed(1),
}))

const ordersChange = computed(() => ({
  type: ordersGrowth.value >= 0 ? 'positive' : 'negative',
  icon: ordersGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(ordersGrowth.value).toFixed(1),
}))

const customersChange = computed(() => ({
  type: customersGrowth.value >= 0 ? 'positive' : 'negative',
  icon: customersGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(customersGrowth.value).toFixed(1),
}))

const productsChange = computed(() => ({
  type: productsGrowth.value >= 0 ? 'positive' : 'negative',
  icon: productsGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(productsGrowth.value).toFixed(1),
}))

// Set up real-time updates every 30 seconds
let realTimeInterval = null

// ==================== LOAD DATA METHODS ====================

const loadKpis = async () => {
  try {
    isLoadingKpis.value = true
    const { startDate, endDate } = getDateRange()
    const response = await thongKeService.getAnalyticsKpis({ startDate, endDate })
    const data = response?.data ?? response

    totalRevenue.value = Number(data.totalRevenue || 0)
    totalOrders.value = Number(data.totalOrders || 0)
    totalCustomers.value = Number(data.totalCustomers || 0)
    totalProducts.value = Number(data.totalProducts || 0)

    revenueGrowth.value = Number(data.revenueGrowth || 0)
    ordersGrowth.value = Number(data.ordersGrowth || 0)
    customersGrowth.value = Number(data.customersGrowth || 0)
    productsGrowth.value = Number(data.productsGrowth || 0)

    console.log('KPIs loaded:', data)
  } catch (error) {
    console.error('Error loading KPIs:', error)
    // Set default values if API fails
    totalRevenue.value = 0
    totalOrders.value = 0
    totalCustomers.value = 0
    totalProducts.value = 0
    revenueGrowth.value = 0
    ordersGrowth.value = 0
    customersGrowth.value = 0
    productsGrowth.value = 0
  } finally {
    isLoadingKpis.value = false
  }
}

const loadHourlySales = async () => {
  try {
    isLoadingHourlySales.value = true
    const response = await thongKeService.getHourlySales()
    const data = response?.data ?? response
    hourlySales.value = data || []

    // Note: Today's metrics are now loaded from getBusinessInsights()
    // Only use hourly sales for chart data

    lastUpdate.value = new Date().toLocaleString('vi-VN')
    lastUpdated.value = new Date()
  } catch (error) {
    console.error('Error loading hourly sales:', error)
  } finally {
    isLoadingHourlySales.value = false
  }
}

const loadBusinessInsights = async () => {
  try {
    isLoadingBusinessInsights.value = true
    const { startDate, endDate } = getDateRange()
    console.log('Loading business insights with date range:', { startDate, endDate })

    const response = await thongKeService.getBusinessInsights({
      startDate,
      endDate,
    })
    const data = response?.data ?? response

    console.log('Business insights API response:', data)
    console.log('Products sold from API:', data.productsSold)

    // Load today's metrics from business insights
    todayRevenue.value = Number(data.todayRevenue || 0)
    todayOrders.value = Number(data.todayOrders || 0)
    todayCustomers.value = Number(data.todayCustomers || 0)
    productsSold.value = Number(data.productsSold || 0)

    console.log('After assignment - productsSold.value:', productsSold.value)
    console.log('Business insights loaded:', data)
  } catch (error) {
    console.error('Error loading business insights:', error)
    // Set default values if API fails
    todayRevenue.value = 0
    todayOrders.value = 0
    todayCustomers.value = 0
    productsSold.value = 0
  } finally {
    isLoadingBusinessInsights.value = false
  }
}

const loadTopProducts = async () => {
  try {
    isLoadingTopProducts.value = true
    const { startDate, endDate } = getDateRange()
    // Calculate rangeDays from date range
    const start = new Date(startDate)
    const end = new Date(endDate)
    const rangeDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) || 30
    const response = await thongKeService.getTopProducts({ limit: 5, rangeDays })
    const data = response?.data ?? response

    // Sort by sales (quantity sold) in descending order
    topProducts.value = (data || [])
      .map((product) => ({
        id: product.id,
        name: product.name,
        category: product.category || 'N/A',
        sales: product.sales || 0,
        revenue: product.revenue || 0,
        image: product.image || 'https://via.placeholder.com/40x40/3b82f6/ffffff?text=SP',
      }))
      .sort((a, b) => (b.sales || 0) - (a.sales || 0)) // Sort by sales descending

    console.log('Top products loaded:', topProducts.value)
  } catch (error) {
    console.error('Error loading top products:', error)
    // Set default empty array if API fails
    topProducts.value = []
  } finally {
    isLoadingTopProducts.value = false
  }
}

const loadOrderStatusCounts = async () => {
  try {
    const response = await thongKeService.getOrderStatusCounts()
    orderStatusCounts.value = response?.data ?? response
    console.log('Order status counts loaded:', orderStatusCounts.value)
  } catch (error) {
    console.error('Error loading order status counts:', error)
    // Set default values if API fails
    orderStatusCounts.value = null
  }
}

onMounted(async () => {
  // Load all data
  await Promise.all([
    loadKpis(),
    loadHourlySales(),
    loadBusinessInsights(),
    loadTopProducts(),
    loadOrderStatusCounts(),
  ])

  // Start real-time updates (refresh every 5 minutes)
  realTimeInterval = setInterval(async () => {
    await loadHourlySales()
  }, 300000) // 5 minutes
})

onUnmounted(() => {
  if (realTimeInterval) {
    clearInterval(realTimeInterval)
  }
})

// Format helper functions
const formatCurrency = (value) => {
  if (!value || value === 0) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value)
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

// Methods

const applyDateRange = async () => {
  // If custom is selected, just reload data
  if (selectedDateRange.value === 'custom') {
    return
  }

  // Reload all data with new date range
  await refreshData()
}

const refreshData = async () => {
  lastUpdated.value = new Date()
  // Reload all data
  await Promise.all([
    loadKpis(),
    loadHourlySales(),
    loadBusinessInsights(),
    loadTopProducts(),
    loadOrderStatusCounts(),
  ])
}

const getMetricLabel = (metric) => {
  const labels = {
    revenue: 'Doanh thu',
    orders: 'Đơn hàng',
    customers: 'Khách hàng',
    profit: 'Lợi nhuận',
  }
  return labels[metric] || 'Doanh thu'
}

const viewTopProducts = () => {
  // Navigate to all top products
}

const formatMetricValue = (value, metric) => {
  switch (metric) {
    case 'revenue':
    case 'profit':
      return formatCurrency(value)
    case 'orders':
    case 'customers':
      return value.toLocaleString('vi-VN')
    default:
      return value
  }
}

// Lifecycle
onMounted(() => {
  // Initialize advanced analytics
})

// Expose functions and variables to template
defineExpose({
  formatTime,
  formatCurrency,
  formatMetricValue,
  getMetricLabel,
})
</script>

<style scoped>
.analytics-container {
  width: 100%;
  padding: 0;
}

/* Header */
.analytics-header {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  margin-bottom: 1.5rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: #000000;
  margin: 0;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, #000000 0%, #000000 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: #64748b;
  margin: 0.5rem 0 0 0;
  font-size: 0.95rem;
  font-weight: 400;
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.date-range-selector select {
  padding: 0.5rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 0.875rem;
}

.realtime-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
}

.status-indicator.active {
  color: #10b981;
}

/* Real-time Status Styles */
.realtime-status {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem 0;
}

.realtime-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #10b981;
  font-size: 0.875rem;
  font-weight: 500;
}

.realtime-indicator i {
  color: #10b981;
  animation: pulse-green 2s ease-in-out infinite;
}

@keyframes pulse-green {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.9);
  }
}

.last-updated {
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 400;
}

/* Advanced Filters */
.advanced-filters-panel {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  margin-bottom: 1.5rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-section h6 {
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 1rem;
  font-size: 0.95rem;
  letter-spacing: -0.01em;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.filter-group label {
  display: block;
  font-size: 0.875rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

/* KPI Cards */
.kpi-section {
  margin-bottom: 2rem;
}

.kpi-card {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transition: all 0.4s ease;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 20px 60px rgba(0, 0, 0, 0.08);
  border-color: rgba(226, 232, 240, 0.8);
}

.kpi-card.revenue::before {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.kpi-card.orders::before {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.kpi-card.customers::before {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.kpi-card.products::before {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.kpi-icon {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  opacity: 0.12;
  transition: all 0.3s ease;
}

.kpi-card:hover .kpi-icon {
  opacity: 0.18;
  transform: scale(1.05);
}

.kpi-card.revenue .kpi-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.kpi-card.orders .kpi-icon {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.kpi-card.customers .kpi-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.kpi-card.products .kpi-icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.kpi-value {
  font-size: 2.25rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
  line-height: 1;
}

.loading-placeholder {
  color: #94a3b8 !important;
  font-weight: 500 !important;
  font-size: 1rem !important;
  opacity: 0.8;
}

.kpi-label {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.kpi-change {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.875rem;
  font-weight: 600;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.kpi-change.positive {
  color: #059669;
  background: rgba(16, 185, 129, 0.1);
}

.kpi-change.negative {
  color: #dc2626;
  background: rgba(239, 68, 68, 0.1);
}

.kpi-trend {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  color: #28a745;
  font-size: 1.5rem;
}

/* Insights Section */
.insights-section {
  margin-bottom: 2rem;
}

.insight-card {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  height: 100%;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.insight-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 20px 60px rgba(0, 0, 0, 0.08);
}

.insight-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.insight-header h6 {
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  font-size: 1rem;
  letter-spacing: -0.01em;
}

.insight-header i {
  color: #94a3b8;
  font-size: 1.5rem;
  transition: all 0.3s ease;
}

.insight-card:hover .insight-header i {
  color: #6366f1;
  transform: scale(1.1);
}

.insight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.2s ease;
}

.insight-item:last-child {
  border-bottom: none;
}

.insight-item:hover {
  padding-left: 0.5rem;
  border-left: 3px solid #6366f1;
  border-bottom-color: #6366f1;
}

.insight-label {
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
}

.insight-value {
  font-weight: 700;
  color: #0f172a;
  font-size: 1rem;
}

/* Charts Section */
.charts-section {
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  height: 100%;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 20px 60px rgba(0, 0, 0, 0.08);
}

.main-chart {
  min-height: 450px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.chart-title-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.chart-title {
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  font-size: 1.125rem;
  letter-spacing: -0.01em;
}

.growth-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(5, 150, 105, 0.08) 100%);
  color: #059669;
  padding: 0.5rem 1rem;
  border-radius: 24px;
  font-size: 0.875rem;
  font-weight: 600;
  border: 1px solid rgba(16, 185, 129, 0.15);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.08);
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.metric-toggles {
  display: flex;
  gap: 0.5rem;
}

.metric-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1rem;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.metric-toggle:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.metric-toggle.active {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
  transform: translateY(-1px);
}

.metric-toggle.active i {
  filter: drop-shadow(0 0 4px rgba(255, 255, 255, 0.5));
}

.chart-actions {
  display: flex;
  gap: 0.5rem;
}

.chart-content {
  position: relative;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-container-small {
  height: 200px;
  position: relative;
}

.chart-insights {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

/* Comparison Mode Styles */
.comparison-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comparison-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  border-radius: 24px;
  font-size: 0.875rem;
  font-weight: 600;
  border: 1px solid;
  box-shadow: 0 2px 8px;
}

.comparison-badge.positive {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(5, 150, 105, 0.08) 100%);
  color: #059669;
  border-color: rgba(16, 185, 129, 0.15);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.08);
}

.comparison-badge.negative {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(220, 38, 38, 0.08) 100%);
  color: #dc2626;
  border-color: rgba(239, 68, 68, 0.15);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.08);
}

.comparison-dropdown {
  min-width: 200px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
  padding: 0.625rem 1rem;
  background: white;
  color: #0f172a;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.comparison-dropdown:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.comparison-dropdown:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
  background: white;
}

/* Beautiful Date Picker Styles */
.date-picker-container {
  position: relative;
  display: inline-block;
}

.date-picker-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  color: #0f172a;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 200px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.date-picker-btn:hover {
  border-color: #6366f1;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
  transform: translateY(-1px);
}

.date-picker-btn:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.date-picker-icon {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border-radius: 8px;
  color: white;
  font-size: 1rem;
}

.date-picker-content {
  flex: 1;
  text-align: left;
}

.date-picker-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin-bottom: 0.125rem;
}

.date-picker-value {
  font-size: 0.875rem;
  color: #0f172a;
  font-weight: 600;
}

.date-picker-arrow {
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.date-picker-arrow .rotated {
  transform: rotate(180deg);
}

.date-picker-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 1000;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 8px 16px rgba(0, 0, 0, 0.06);
  margin-top: 0.5rem;
  min-width: 320px;
  overflow: hidden;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.date-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.date-picker-header h6 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #0f172a;
}

.close-btn {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #94a3b8;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #64748b;
}

.date-picker-body {
  padding: 1.25rem;
}

.quick-options {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.quick-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: white;
  color: #374151;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.quick-option:hover {
  border-color: #6366f1;
  background: #f8fafc;
  color: #6366f1;
  transform: translateX(4px);
}

.quick-option i {
  width: 1.25rem;
  height: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.quick-option:hover i {
  color: #6366f1;
}

.date-separator {
  position: relative;
  text-align: center;
  margin: 1rem 0;
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 500;
}

.date-separator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
  z-index: 1;
}

.date-separator span {
  background: white;
  padding: 0 1rem;
  position: relative;
  z-index: 2;
}

.custom-date {
  margin-top: 1rem;
}

.date-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.875rem;
  color: #374151;
  background: white;
  transition: all 0.2s ease;
}

.date-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* Real-time Metrics */
.realtime-metrics {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.metric-card-small {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
}

.metric-card-small:hover {
  transform: translateY(-4px);
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.1),
    0 20px 60px rgba(0, 0, 0, 0.1);
  border-color: #6366f1;
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.metric-label {
  font-size: 0.875rem;
  color: #1e293b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #10b981;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: rgba(16, 185, 129, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
}

.live-indicator i {
  animation: pulse-green 2s ease-in-out infinite;
}

@keyframes pulse-green {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.9);
  }
}

.metric-value {
  font-size: 2.25rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 0.5rem;
  line-height: 1;
  letter-spacing: -0.02em;
}

.metric-subtitle {
  font-size: 0.875rem;
  color: #475569;
  font-weight: 600;
}

/* Top Products */
.top-products-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.875rem;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-item:hover {
  background: white;
  border-color: #6366f1;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.08);
}

.product-rank {
  width: 2rem;
  height: 2rem;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
}

.product-image {
  width: 3rem;
  height: 3rem;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.product-item:hover .product-image {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 700;
  color: #1e293b;
  font-size: 1rem;
  margin-bottom: 0.25rem;
  line-height: 1.2;
}

.product-category {
  font-size: 0.875rem;
  color: #475569;
  font-weight: 600;
}

.product-stats {
  text-align: right;
}

.product-sales {
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.product-revenue {
  font-weight: 800;
  color: #059669;
  font-size: 1rem;
}

/* Stat Card */
.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.1),
    0 20px 60px rgba(0, 0, 0, 0.1);
  border-color: #6366f1;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.stat-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.01em;
}

.stat-content {
  color: #475569;
}

/* Loading and Empty States */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #64748b;
  font-size: 0.875rem;
}

.loading-state i {
  font-size: 1.5rem;
  color: #6366f1;
  margin-bottom: 0.5rem;
}

.rotate {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-state i {
  font-size: 2rem;
  color: #94a3b8;
  margin-bottom: 0.5rem;
}

/* Order Status Grid */
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.status-item {
  padding: 1.25rem;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s ease;
}

.status-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-item.pending {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: 1px solid #f59e0b;
}

.status-item.shipping {
  background: linear-gradient(135deg, #ddd6fe 0%, #c4b5fd 100%);
  border: 1px solid #8b5cf6;
}

.status-item.completed {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border: 1px solid #10b981;
}

.status-item.cancelled {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border: 1px solid #ef4444;
}

.status-count {
  font-size: 1.75rem;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0.25rem;
  line-height: 1;
}

.status-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

/* Trend Cards */
.trend-revenue {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border: 1px solid #10b981;
}

.trend-orders {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border: 1px solid #3b82f6;
}

.trend-customers {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: 1px solid #f59e0b;
}

.trend-average {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border: 1px solid #8b5cf6;
}

.trend-icon {
  font-size: 1.5rem;
  opacity: 0.7;
}

/* Business Insights */
.insights-section {
  margin-bottom: 2rem;
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.insight-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.insight-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.insight-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  color: white;
  flex-shrink: 0;
}

.insight-icon.revenue-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.insight-icon.orders-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.insight-icon.customers-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.insight-icon.products-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.insight-description {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
  line-height: 1.4;
}

/* Additional Analytics */
.additional-analytics {
  margin-bottom: 2rem;
}

/* Details Section */
.details-section {
  margin-bottom: 2rem;
}

/* Performance Metrics */
.performance-section {
  margin-bottom: 2rem;
}

.performance-metrics {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.performance-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.performance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.performance-label {
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
}

.performance-value {
  font-weight: 700;
  color: #0f172a;
  font-size: 1.125rem;
}

.progress {
  height: 10px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 50%, #10b981 100%);
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
  position: relative;
  overflow: hidden;
}

.progress-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 100%
  );
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .quick-actions {
    flex-wrap: wrap;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .chart-controls {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }

  .metric-toggles {
    flex-wrap: wrap;
  }
}
</style>
