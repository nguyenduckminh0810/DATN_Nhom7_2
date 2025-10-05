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
            <button class="btn btn-outline-primary btn-sm" @click="toggleAdvancedFilters">
              <i class="ph-funnel me-1"></i>Bộ lọc nâng cao
            </button>
            <button class="btn btn-outline-success btn-sm" @click="exportReport">
              <i class="ph-download me-1"></i>Xuất báo cáo
            </button>
            <button class="btn btn-primary btn-sm" @click="refreshData">
              <i class="ph-arrow-clockwise me-1"></i>Làm mới
            </button>
          </div>
        </div>
      </div>
      
      <!-- Real-time Status -->
      <div class="realtime-status">
        <div class="status-indicator" :class="{ active: isRealtimeActive }">
          <i class="ph-circle-fill"></i>
          <span>{{ isRealtimeActive ? 'Đang cập nhật thời gian thực' : 'Cập nhật thủ công' }}</span>
        </div>
        <div class="last-updated">
          Cập nhật lần cuối: {{ formatTime(lastUpdated) }}
        </div>
        <button class="btn btn-sm btn-outline-secondary" @click="toggleRealtime">
          <i :class="isRealtimeActive ? 'ph-pause' : 'ph-play'"></i>
          {{ isRealtimeActive ? 'Tạm dừng' : 'Bật thời gian thực' }}
        </button>
      </div>
    </div>

    <!-- Advanced Filters Panel -->
    <div v-if="showAdvancedFilters" class="advanced-filters-panel">
      <div class="filters-content">
        <div class="filter-section">
          <h6>Thời gian</h6>
          <div class="filter-row">
            <div class="filter-group">
              <label>Từ ngày</label>
              <input type="date" v-model="customStartDate" class="form-control">
            </div>
            <div class="filter-group">
              <label>Đến ngày</label>
              <input type="date" v-model="customEndDate" class="form-control">
            </div>
          </div>
        </div>
        
        <div class="filter-section">
          <h6>Phân khúc</h6>
          <div class="filter-row">
            <div class="filter-group">
              <label>Danh mục sản phẩm</label>
              <select v-model="selectedCategory" class="form-control">
                <option value="">Tất cả danh mục</option>
                <option value="ao-thun">Áo thun</option>
                <option value="ao-so-mi">Áo sơ mi</option>
                <option value="quan-jean">Quần jean</option>
                <option value="quan-short">Quần short</option>
              </select>
            </div>
            <div class="filter-group">
              <label>Khu vực</label>
              <select v-model="selectedRegion" class="form-control">
                <option value="">Tất cả khu vực</option>
                <option value="hanoi">Hà Nội</option>
                <option value="hcm">TP. Hồ Chí Minh</option>
                <option value="danang">Đà Nẵng</option>
                <option value="other">Khác</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="filter-section">
          <h6>Khách hàng</h6>
          <div class="filter-row">
            <div class="filter-group">
              <label>Loại khách hàng</label>
              <select v-model="selectedCustomerType" class="form-control">
                <option value="">Tất cả</option>
                <option value="new">Khách hàng mới</option>
                <option value="returning">Khách hàng cũ</option>
                <option value="vip">Khách hàng VIP</option>
              </select>
            </div>
            <div class="filter-group">
              <label>Độ tuổi</label>
              <select v-model="selectedAgeGroup" class="form-control">
                <option value="">Tất cả độ tuổi</option>
                <option value="18-25">18-25 tuổi</option>
                <option value="26-35">26-35 tuổi</option>
                <option value="36-45">36-45 tuổi</option>
                <option value="46+">46+ tuổi</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="filter-actions">
          <button class="btn btn-primary" @click="applyFilters">Áp dụng bộ lọc</button>
          <button class="btn btn-outline-secondary" @click="resetFilters">Đặt lại</button>
        </div>
      </div>
    </div>

    <!-- Key Performance Indicators -->
    <div class="kpi-section">
      <div class="row g-4">
        <div class="col-lg-3 col-md-6">
          <div class="kpi-card revenue">
            <div class="kpi-icon">
              <i class="ph-currency-dollar"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ formatCurrency(totalRevenue) }}</div>
              <div class="kpi-label">Tổng doanh thu</div>
              <div class="kpi-change" :class="revenueChange.type">
                <i :class="revenueChange.icon"></i>
                {{ revenueChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="ph-trend-up"></i>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3 col-md-6">
          <div class="kpi-card orders">
            <div class="kpi-icon">
              <i class="ph-shopping-bag"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ totalOrders.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Tổng đơn hàng</div>
              <div class="kpi-change" :class="ordersChange.type">
                <i :class="ordersChange.icon"></i>
                {{ ordersChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="ph-trend-up"></i>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3 col-md-6">
          <div class="kpi-card customers">
            <div class="kpi-icon">
              <i class="ph-users"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ totalCustomers.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Khách hàng mới</div>
              <div class="kpi-change" :class="customersChange.type">
                <i :class="customersChange.icon"></i>
                {{ customersChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="ph-trend-up"></i>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3 col-md-6">
          <div class="kpi-card products">
            <div class="kpi-icon">
              <i class="ph-package"></i>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ totalProducts.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Sản phẩm bán ra</div>
              <div class="kpi-change" :class="productsChange.type">
                <i :class="productsChange.icon"></i>
                {{ productsChange.value }}% so với kỳ trước
              </div>
            </div>
            <div class="kpi-trend">
              <i class="ph-trend-up"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Business Insights -->
    <div class="insights-section">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6>Hiệu suất kinh doanh</h6>
              <i class="ph-chart-line"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Giá trị đơn hàng trung bình</span>
                <span class="insight-value">{{ formatCurrency(averageOrderValue) }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Tỷ lệ hoàn trả</span>
                <span class="insight-value">{{ refundRate }}%</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Biên lợi nhuận</span>
                <span class="insight-value">{{ profitMargin }}%</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6>Xu hướng khách hàng</h6>
              <i class="ph-users-three"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Khách hàng quay lại</span>
                <span class="insight-value">{{ returningCustomers }}%</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Giá trị khách hàng trọn đời</span>
                <span class="insight-value">{{ formatCurrency(customerLifetimeValue) }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Thời gian giữ chân TB</span>
                <span class="insight-value">{{ averageRetentionDays }} ngày</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6>Hiệu quả sản phẩm</h6>
              <i class="ph-package"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Sản phẩm bán chạy nhất</span>
                <span class="insight-value">{{ topSellingProduct }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Danh mục hàng đầu</span>
                <span class="insight-value">{{ topCategory }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Vòng quay tồn kho</span>
                <span class="insight-value">{{ inventoryTurnover }}x</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <div class="row g-4">
        <!-- Main Revenue Chart -->
        <div class="col-lg-8">
          <div class="chart-card main-chart">
            <div class="chart-header">
              <div class="chart-title-section">
                <h5 class="chart-title">Doanh thu & Hiệu suất</h5>
                <div class="growth-badge">
                  <i class="ph-trend-up"></i>
                  +{{ growthRate }}% tăng trưởng
                </div>
              </div>
              <div class="chart-controls">
                <div class="metric-toggles">
                  <button 
                    v-for="metric in metrics" 
                    :key="metric.value"
                    :class="['metric-toggle', { active: selectedMetric === metric.value }]"
                    @click="selectedMetric = metric.value"
                  >
                    <i :class="metric.icon"></i>
                    {{ metric.label }}
                  </button>
                </div>
                <div class="chart-actions">
                  <button class="btn btn-outline-secondary btn-sm" @click="toggleChartType">
                    <i :class="chartType === 'line' ? 'ph-bar-chart' : 'ph-line-chart'"></i>
                    {{ chartType === 'line' ? 'Cột' : 'Đường' }}
                  </button>
                  <button class="btn btn-outline-primary btn-sm" @click="exportChart">
                    <i class="ph-download"></i>
                  </button>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <Chart
                :type="chartType"
                :data="mainChartData"
                :options="mainChartOptions"
                :height="300"
              />
              <div class="chart-insights">
                <div class="insight-item">
                  <span class="insight-label">Xu hướng:</span>
                  <span :class="['insight-value', trendDirection]">
                    <i :class="trendIcon"></i>
                    {{ trendText }}
                  </span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Điểm cao nhất:</span>
                  <span class="insight-value">{{ formatCurrency(peakValue) }}</span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Biến động:</span>
                  <span class="insight-value">{{ volatility }}%</span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Trung bình:</span>
                  <span class="insight-value">{{ formatCurrency(Math.round(averageValue)) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Real-time Metrics -->
        <div class="col-lg-4">
          <div class="realtime-metrics">
            <div class="metric-card-small">
              <div class="metric-header">
                <span class="metric-label">Hôm nay</span>
                <div class="live-indicator">
                  <i class="ph-circle-fill"></i>
                  <span>LIVE</span>
                </div>
              </div>
              <div class="metric-value">{{ formatCurrency(todayRevenue) }}</div>
              <div class="metric-subtitle">Doanh thu</div>
            </div>
            
            <div class="metric-card-small">
              <div class="metric-header">
                <span class="metric-label">Hôm nay</span>
              </div>
              <div class="metric-value">{{ todayOrders }}</div>
              <div class="metric-subtitle">Đơn hàng</div>
            </div>
            
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
    </div>

    <!-- Secondary Charts -->
    <div class="secondary-charts-section">
      <div class="row g-4">
        <!-- Top Products -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Sản phẩm bán chạy</h6>
              <button class="btn btn-sm btn-outline-primary" @click="viewTopProducts">
                Xem tất cả
              </button>
            </div>
            <div class="chart-content">
              <div class="top-products-list">
                <div v-for="(product, index) in topProducts" :key="product.id" class="product-item">
                  <div class="product-rank">{{ index + 1 }}</div>
                  <div class="product-image">
                    <img :src="product.image" :alt="product.name">
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-category">{{ product.category }}</div>
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
        
        <!-- Order Distribution -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Phân bố đơn hàng</h6>
              <select v-model="orderDistributionType" class="form-control form-control-sm">
                <option value="status">Theo trạng thái</option>
                <option value="payment">Theo thanh toán</option>
                <option value="shipping">Theo vận chuyển</option>
              </select>
            </div>
            <div class="chart-content">
              <Chart
                type="doughnut"
                :data="orderDistributionData"
                :options="orderDistributionOptions"
                :height="200"
              />
            </div>
          </div>
        </div>
        
        <!-- Customer Analytics -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Phân tích khách hàng</h6>
              <select v-model="customerAnalyticsType" class="form-control form-control-sm">
                <option value="segments">Phân khúc</option>
                <option value="behavior">Hành vi</option>
                <option value="geography">Địa lý</option>
              </select>
            </div>
            <div class="chart-content">
              <Chart
                type="bar"
                :data="customerAnalyticsData"
                :options="customerAnalyticsOptions"
                :height="200"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Performance Metrics -->
    <div class="performance-section">
      <div class="row g-4">
        <div class="col-12">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Chỉ số hiệu suất</h6>
            </div>
            <div class="chart-content">
              <div class="performance-metrics">
                <div class="performance-item">
                  <div class="performance-header">
                    <span class="performance-label">Tỷ lệ chuyển đổi</span>
                    <span class="performance-value">{{ conversionRate }}%</span>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: conversionRate + '%' }"></div>
                  </div>
                </div>
                
                <div class="performance-item">
                  <div class="performance-header">
                    <span class="performance-label">Tỷ lệ bỏ giỏ hàng</span>
                    <span class="performance-value">{{ cartAbandonmentRate }}%</span>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: cartAbandonmentRate + '%' }"></div>
                  </div>
                </div>
                
                <div class="performance-item">
                  <div class="performance-header">
                    <span class="performance-label">Mức độ hài lòng khách hàng</span>
                    <span class="performance-value">{{ customerSatisfaction }}%</span>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: customerSatisfaction + '%' }"></div>
                  </div>
                </div>
                
                <div class="performance-item">
                  <div class="performance-header">
                    <span class="performance-label">Vòng quay tồn kho</span>
                    <span class="performance-value">{{ inventoryTurnover }}x</span>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: (inventoryTurnover / 10 * 100) + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import Chart from '@/components/ui/Chart.vue'

// Reactive data
const showAdvancedFilters = ref(false)
const selectedDateRange = ref('30days')
const customStartDate = ref('')
const customEndDate = ref('')
const selectedCategory = ref('')
const selectedRegion = ref('')
const selectedCustomerType = ref('')
const selectedAgeGroup = ref('')

const isRealtimeActive = ref(false)
const lastUpdated = ref(new Date())

const selectedMetric = ref('revenue')
const chartType = ref('line')
const orderDistributionType = ref('status')
const customerAnalyticsType = ref('segments')

// Chart refs
const mainChart = ref(null)
const orderDistributionChart = ref(null)
const customerAnalyticsChart = ref(null)

// Mock data
const totalRevenue = ref(125000000)
const totalOrders = ref(1250)
const totalCustomers = ref(450)
const totalProducts = ref(3200)

const todayRevenue = ref(8500000)
const todayOrders = ref(45)
const todayCustomers = ref(18)

const averageOrderValue = ref(100000)
const refundRate = ref(2.5)
const profitMargin = ref(28.5)
const returningCustomers = ref(35)
const customerLifetimeValue = ref(850000)
const averageRetentionDays = ref(120)
const topSellingProduct = ref('Áo thun nam cao cấp')
const topCategory = ref('Áo thun')
const inventoryTurnover = ref(4.2)
const conversionRate = ref(3.8)
const cartAbandonmentRate = ref(68.5)
const customerSatisfaction = ref(92.3)

const growthRate = ref(12.5)

// Chart data
const dailySales = ref([
  { date: new Date('2024-01-15'), revenue: 3500000, orders: 35, customers: 12 },
  { date: new Date('2024-01-16'), revenue: 4200000, orders: 42, customers: 15 },
  { date: new Date('2024-01-17'), revenue: 3700000, orders: 37, customers: 13 },
  { date: new Date('2024-01-18'), revenue: 4700000, orders: 47, customers: 18 },
  { date: new Date('2024-01-19'), revenue: 5200000, orders: 52, customers: 20 },
  { date: new Date('2024-01-20'), revenue: 4800000, orders: 48, customers: 19 },
  { date: new Date('2024-01-21'), revenue: 4500000, orders: 45, customers: 17 }
])

const topProducts = ref([
  { id: 1, name: 'Áo thun nam cao cấp', category: 'Áo thun', sales: 125, revenue: 12500000, image: '/images/products/ao-thun-1.jpg' },
  { id: 2, name: 'Quần jean slim fit', category: 'Quần jean', sales: 98, revenue: 9800000, image: '/images/products/quan-jean-1.jpg' },
  { id: 3, name: 'Áo sơ mi trắng', category: 'Áo sơ mi', sales: 87, revenue: 8700000, image: '/images/products/ao-so-mi-1.jpg' },
  { id: 4, name: 'Quần short kaki', category: 'Quần short', sales: 76, revenue: 7600000, image: '/images/products/quan-short-1.jpg' },
  { id: 5, name: 'Áo hoodie nam', category: 'Áo hoodie', sales: 65, revenue: 6500000, image: '/images/products/ao-hoodie-1.jpg' }
])

// Computed properties
const revenueChange = computed(() => ({
  type: 'positive',
  icon: 'ph-trend-up',
  value: '+12.5'
}))

const ordersChange = computed(() => ({
  type: 'positive', 
  icon: 'ph-trend-up',
  value: '+8.3'
}))

const customersChange = computed(() => ({
  type: 'positive',
  icon: 'ph-trend-up', 
  value: '+15.2'
}))

const productsChange = computed(() => ({
  type: 'positive',
  icon: 'ph-trend-up',
  value: '+6.7'
}))

const metrics = computed(() => [
  { value: 'revenue', label: 'Doanh thu', icon: 'ph-currency-dollar' },
  { value: 'orders', label: 'Đơn hàng', icon: 'ph-shopping-bag' },
  { value: 'customers', label: 'Khách hàng', icon: 'ph-users' },
  { value: 'profit', label: 'Lợi nhuận', icon: 'ph-chart-line' }
])

const trendDirection = computed(() => 'positive')
const trendIcon = computed(() => 'ph-trend-up')
const trendText = computed(() => 'Tăng trưởng ổn định')
const peakValue = computed(() => 5200000)
const volatility = computed(() => 12.8)
const averageValue = computed(() => {
  if (!dailySales.value || dailySales.value.length === 0) return 0
  const total = dailySales.value.reduce((sum, day) => sum + (day.revenue || 0), 0)
  return total / dailySales.value.length
})

// Chart data
const mainChartData = computed(() => {
  if (!dailySales.value || dailySales.value.length === 0) {
    return {
      labels: [],
      datasets: []
    }
  }
  
  const labels = dailySales.value.map(day => formatDate(day.date))
  const data = dailySales.value.map(day => {
    switch (selectedMetric.value) {
      case 'revenue': return day.revenue || 0
      case 'orders': return day.orders || 0
      case 'customers': return day.customers || 0
      case 'profit': return Math.round((day.revenue || 0) * 0.28)
      default: return day.revenue || 0
    }
  })
  
  return {
    labels,
    datasets: [{
      label: getMetricLabel(selectedMetric.value),
      data,
      borderColor: getMetricColor(selectedMetric.value),
      backgroundColor: getMetricColor(selectedMetric.value, 0.1),
      borderWidth: 3,
      fill: true,
      tension: 0.4,
      pointBackgroundColor: '#ffffff',
      pointBorderColor: getMetricColor(selectedMetric.value),
      pointBorderWidth: 3,
      pointRadius: 6,
      pointHoverRadius: 8
    }]
  }
})

const mainChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      titleColor: '#ffffff',
      bodyColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          return getMetricLabel(selectedMetric.value) + ': ' + formatMetricValue(context.parsed.y, selectedMetric.value)
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: true,
        color: 'rgba(0, 0, 0, 0.05)'
      },
      ticks: {
        font: {
          size: 11
        },
        color: '#6b7280'
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        color: 'rgba(0, 0, 0, 0.05)'
      },
      ticks: {
        callback: function(value) {
          return formatMetricValue(value, selectedMetric.value)
        },
        font: {
          size: 11
        },
        color: '#6b7280'
      }
    }
  }
}))

const orderDistributionData = computed(() => {
  const baseData = {
    status: {
      labels: ['Đã giao', 'Đang giao', 'Đang xử lý', 'Chờ xử lý'],
      data: [850, 320, 180, 95],
      colors: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444']
    },
    payment: {
      labels: ['Đã thanh toán', 'Chưa thanh toán', 'Hoàn tiền'],
      data: [1200, 350, 50],
      colors: ['#10b981', '#f59e0b', '#ef4444']
    },
    shipping: {
      labels: ['Giao thành công', 'Đang giao', 'Chờ lấy hàng'],
      data: [780, 420, 350],
      colors: ['#10b981', '#3b82f6', '#f59e0b']
    }
  }
  
  const currentData = baseData[orderDistributionType.value] || baseData.status
  
  return {
    labels: currentData.labels,
    datasets: [{
      data: currentData.data,
      backgroundColor: currentData.colors,
      borderWidth: 2,
      borderColor: '#ffffff'
    }]
  }
})

const orderDistributionOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        padding: 15,
        font: {
          size: 11
        }
      }
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      titleColor: '#ffffff',
      bodyColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((context.parsed / total) * 100).toFixed(1)
          return context.label + ': ' + context.parsed + ' đơn (' + percentage + '%)'
        }
      }
    }
  }
}))

const customerAnalyticsData = computed(() => {
  const baseData = {
    segments: {
      labels: ['VIP', 'Thường xuyên', 'Mới', 'Tiềm năng'],
      data: [85, 240, 320, 180],
      colors: ['#f59e0b', '#10b981', '#3b82f6', '#8b5cf6']
    },
    behavior: {
      labels: ['Mua thường xuyên', 'Mua theo mùa', 'Mua một lần', 'Không hoạt động'],
      data: [200, 350, 180, 95],
      colors: ['#10b981', '#3b82f6', '#f59e0b', '#6b7280']
    },
    geography: {
      labels: ['Hà Nội', 'TP.HCM', 'Đà Nẵng', 'Khác'],
      data: [280, 420, 150, 125],
      colors: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6']
    }
  }
  
  const currentData = baseData[customerAnalyticsType.value] || baseData.segments
  
  return {
    labels: currentData.labels,
    datasets: [{
      label: 'Số lượng khách hàng',
      data: currentData.data,
      backgroundColor: currentData.colors,
      borderWidth: 0,
      borderRadius: 4
    }]
  }
})

const customerAnalyticsOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      titleColor: '#ffffff',
      bodyColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          return context.label + ': ' + context.parsed.y + ' khách hàng'
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      ticks: {
        font: {
          size: 10
        },
        color: '#6b7280'
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        color: 'rgba(0, 0, 0, 0.05)'
      },
      ticks: {
        font: {
          size: 10
        },
        color: '#6b7280'
      }
    }
  }
}))

// Methods
const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const applyDateRange = () => {
  console.log('Applying date range:', selectedDateRange.value)
}

const applyFilters = () => {
  console.log('Applying filters...')
}

const resetFilters = () => {
  selectedCategory.value = ''
  selectedRegion.value = ''
  selectedCustomerType.value = ''
  selectedAgeGroup.value = ''
}

const exportReport = () => {
  console.log('Exporting report...')
}

const refreshData = () => {
  lastUpdated.value = new Date()
  console.log('Refreshing data...')
}

const toggleRealtime = () => {
  isRealtimeActive.value = !isRealtimeActive.value
}

const toggleChartType = () => {
  chartType.value = chartType.value === 'line' ? 'bar' : 'line'
}

const exportChart = () => {
  console.log('Exporting chart...')
}

const viewTopProducts = () => {
  console.log('Viewing all top products...')
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

const formatTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}

const getMetricLabel = (metric) => {
  const labels = {
    revenue: 'Doanh thu',
    orders: 'Đơn hàng', 
    customers: 'Khách hàng',
    profit: 'Lợi nhuận'
  }
  return labels[metric] || 'Doanh thu'
}

const getMetricColor = (metric, alpha = 1) => {
  const colors = {
    revenue: `rgba(59, 130, 246, ${alpha})`,
    orders: `rgba(245, 158, 11, ${alpha})`,
    customers: `rgba(16, 185, 129, ${alpha})`,
    profit: `rgba(239, 68, 68, ${alpha})`
  }
  return colors[metric] || `rgba(59, 130, 246, ${alpha})`
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
  console.log('Advanced Analytics loaded')
})
</script>

<style scoped>
.analytics-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem;
}

/* Header */
.analytics-header {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.page-subtitle {
  color: #6c757d;
  margin: 0.5rem 0 0 0;
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
  color: #28a745;
}

.last-updated {
  color: #6c757d;
  font-size: 0.875rem;
}

/* Advanced Filters */
.advanced-filters-panel {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-section h6 {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
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
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.kpi-card.revenue {
  border-left: 4px solid #28a745;
}

.kpi-card.orders {
  border-left: 4px solid #3b82f6;
}

.kpi-card.customers {
  border-left: 4px solid #f59e0b;
}

.kpi-card.products {
  border-left: 4px solid #ef4444;
}

.kpi-icon {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  opacity: 0.1;
}

.kpi-card.revenue .kpi-icon {
  background: #28a745;
  color: white;
}

.kpi-card.orders .kpi-icon {
  background: #3b82f6;
  color: white;
}

.kpi-card.customers .kpi-icon {
  background: #f59e0b;
  color: white;
}

.kpi-card.products .kpi-icon {
  background: #ef4444;
  color: white;
}

.kpi-value {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.kpi-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.kpi-change {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.kpi-change.positive {
  color: #28a745;
}

.kpi-change.negative {
  color: #ef4444;
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
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.insight-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.insight-header h6 {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.insight-header i {
  color: #6c757d;
  font-size: 1.25rem;
}

.insight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.insight-item:last-child {
  border-bottom: none;
}

.insight-label {
  color: #6c757d;
  font-size: 0.875rem;
}

.insight-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Charts Section */
.charts-section {
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.main-chart {
  min-height: 500px;
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
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.growth-badge {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
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
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e9ecef;
  background: white;
  border-radius: 6px;
  font-size: 0.8rem;
  transition: all 0.2s ease;
}

.metric-toggle:hover {
  background: #f8f9fa;
}

.metric-toggle.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
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

/* Real-time Metrics */
.realtime-metrics {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.metric-card-small {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.metric-label {
  font-size: 0.8rem;
  color: #6c757d;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #28a745;
  font-size: 0.7rem;
  font-weight: 600;
}

.metric-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.metric-subtitle {
  font-size: 0.8rem;
  color: #6c757d;
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
  gap: 0.75rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.product-rank {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
}

.product-image {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 6px;
  overflow: hidden;
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
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.875rem;
}

.product-category {
  font-size: 0.75rem;
  color: #6c757d;
}

.product-stats {
  text-align: right;
}

.product-sales {
  font-size: 0.8rem;
  color: #6c757d;
}

.product-revenue {
  font-weight: 600;
  color: #28a745;
  font-size: 0.875rem;
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
}

.performance-label {
  color: #6c757d;
  font-size: 0.875rem;
}

.performance-value {
  font-weight: 600;
  color: #2c3e50;
}

.progress {
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #28a745);
  border-radius: 4px;
  transition: width 0.3s ease;
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
