<template>
  <div class="dashboard-content">

      <!-- Store Overview Section -->
      <section class="overview-section">
        <div class="overview-grid">
          <div class="overview-card revenue">
            <div class="card-header">
              <div class="card-icon">
                <i class="ph bi bi-currency-dollar"></i>
        </div>
              <div class="card-info">
                <h3>Tổng doanh thu</h3>
                <p class="card-subtitle">Đơn hàng hoàn tất</p>
      </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ summary?.revenueToday?.toLocaleString('vi-VN') }}₫</div>
              <div v-if="revenueGrowth !== null" :class="['trend', revenueGrowth >= 0 ? 'positive' : 'negative']">
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
                <h3>Đơn hàng mới</h3>
                <p class="card-subtitle">Trong 24h qua</p>
          </div>
        </div>
            <div class="card-content">
              <div class="main-value">{{ summary?.newOrders24h ?? 0 }}</div>
              <div v-if="ordersGrowth !== null" :class="['trend', ordersGrowth >= 0 ? 'positive' : 'negative']">
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
                <h3>Khách hàng mới</h3>
                <p class="card-subtitle">Đăng ký hôm nay</p>
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ summary?.newCustomersToday ?? 0 }}</div>
              <div v-if="customersGrowth !== null" :class="['trend', customersGrowth >= 0 ? 'positive' : 'negative']">
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
                <h3>Sản phẩm sắp hết</h3>
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

      <!-- Quick Actions & Alerts -->
      <section class="actions-alerts-section">
        <div class="quick-actions-panel">
          <h3>Thao tác nhanh</h3>
          <div class="action-buttons">
            <button class="action-btn" @click="$router.push('/admin/products')">
              <i class="ph bi bi-plus-circle"></i>
              <span>Thêm sản phẩm</span>
            </button>
            <button class="action-btn" @click="$router.push('/admin/orders')">
              <i class="ph bi bi-cart3"></i>
              <span>Xem đơn hàng</span>
            </button>
            <button class="action-btn" @click="$router.push('/admin/users')">
              <i class="ph bi bi-people"></i>
              <span>Quản lý khách hàng</span>
            </button>
            <button class="action-btn" @click="$router.push('/admin/analytics')">
              <i class="ph bi bi-bar-chart"></i>
              <span>Báo cáo doanh thu</span>
            </button>
          </div>
            </div>

        <div class="alerts-panel">
          <h3>Cần chú ý</h3>
          <div class="alert-list">
            <div class="alert-item urgent">
              <div class="alert-icon">
                <i class="ph bi bi-exclamation-triangle"></i>
          </div>
              <div class="alert-content">
                <div class="alert-title">{{ alerts?.pendingOrders ?? 0 }} đơn hàng chờ xác nhận</div>
                <div class="alert-time">Cần xử lý</div>
        </div>
              <button class="alert-action" @click="$router.push('/admin/orders?status=pending')">Xem</button>
      </div>
            <div class="alert-item warning">
              <div class="alert-icon">
                <i class="ph bi bi-box"></i>
    </div>
              <div class="alert-content">
                <div class="alert-title">{{ alerts?.lowStockProducts ?? 0 }} sản phẩm sắp hết hàng</div>
                <div class="alert-time">Cần nhập hàng</div>
              </div>
              <button class="alert-action" @click="$router.push('/admin/inventory')">Xem</button>
            </div>
            <div class="alert-item info">
              <div class="alert-icon">
                <i class="ph bi bi-clock"></i>
              </div>
              <div class="alert-content">
                <div class="alert-title">{{ alerts?.needShipping ?? 0 }} đơn hàng cần giao</div>
                <div class="alert-time">Đang vận chuyển</div>
              </div>
              <button class="alert-action" @click="$router.push('/admin/orders?status=shipping')">Xem</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Sales Analytics Section -->
      <section class="analytics-section">
        <div class="analytics-grid">
          <div class="chart-card main-chart">
            <div class="chart-header">
              <div class="chart-title-section">
                <div class="chart-title-row">
                  <h3 class="chart-title">Phân tích doanh thu & đơn hàng</h3>
                  <div class="growth-badge">
                    <i class="bi bi-graph-up"></i>
                    +{{ growthRate }}% tăng trưởng
                  </div>
                </div>
              </div>
              <div class="chart-controls">
                <div class="metric-toggles">
                  <button 
                    :class="['metric-toggle', { active: selectedMetric === 'revenue' }]"
                    @click="selectedMetric = 'revenue'"
                  >
                    <i class="bi bi-currency-dollar"></i>
                    Doanh thu
                  </button>
                  <button 
                    :class="['metric-toggle', { active: selectedMetric === 'orders' }]"
                    @click="selectedMetric = 'orders'"
                  >
                    <i class="bi bi-cart3"></i>
                    Đơn hàng
                  </button>
                  <button 
                    :class="['metric-toggle', { active: selectedMetric === 'customers' }]"
                    @click="selectedMetric = 'customers'"
                  >
                    <i class="bi bi-people"></i>
                    Khách hàng
                  </button>
                </div>
                
                <!-- Beautiful Date Picker for Comparison -->
                <div class="date-picker-container">
                  <button 
                    type="button" 
                    class="date-picker-btn"
                    @click="showComparisonDatePicker = !showComparisonDatePicker"
                  >
                    <div class="date-picker-icon">
                      <i class="bi bi-calendar3"></i>
                    </div>
                    <div class="date-picker-content">
                      <div class="date-picker-label">So sánh với</div>
                      <div class="date-picker-value">{{ formatDate(comparisonDate) }}</div>
                    </div>
                    <div class="date-picker-arrow">
                      <i class="bi bi-chevron-down" :class="{ 'rotated': showComparisonDatePicker }"></i>
                    </div>
                  </button>
                  
                  <div v-if="showComparisonDatePicker" class="date-picker-dropdown" @click.stop>
                    <div class="date-picker-header">
                      <h6>Chọn ngày so sánh</h6>
                      <button type="button" class="close-btn" @click="showComparisonDatePicker = false">
                        <i class="bi bi-x"></i>
                      </button>
                    </div>
                    
                    <div class="date-picker-body">
                      <div class="quick-options">
                        <button 
                          type="button" 
                          class="quick-option"
                          @click="selectQuickDate('yesterday')"
                        >
                          <i class="bi bi-calendar-check"></i>
                          Hôm qua
                        </button>
                        <button 
                          type="button" 
                          class="quick-option"
                          @click="selectQuickDate('2days')"
                        >
                          <i class="bi bi-calendar2"></i>
                          2 ngày trước
                        </button>
                        <button 
                          type="button" 
                          class="quick-option"
                          @click="selectQuickDate('week')"
                        >
                          <i class="bi bi-calendar-week"></i>
                          1 tuần trước
                        </button>
                      </div>
                      
                      <div class="date-separator">
                        <span>Hoặc chọn ngày cụ thể</span>
                      </div>
                      
                      <div class="custom-date">
                        <input 
                          type="date" 
                          v-model="comparisonDate" 
                          class="date-input"
                          :max="new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().split('T')[0]"
                          @change="showComparisonDatePicker = false"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="chart-actions">
                  <button class="btn btn-outline-primary btn-sm" @click="exportChartData">
                    <i class="bi bi-download"></i>
                    Xuất biểu đồ
                  </button>
                </div>
              </div>
            </div>
        <div class="chart-content">
          <Chart
            type="line"
                :data="enhancedChartData"
                :options="enhancedChartOptions"
                :height="350"
          />
        </div>
            <div class="chart-insights">
              <div class="insight-item">
                <div class="insight-icon revenue">
                  <i class="bi bi-graph-up"></i>
      </div>
                <div class="insight-content">
                  <div class="insight-title">Tăng trưởng doanh thu</div>
                  <div class="insight-value positive">+{{ growthRate }}%</div>
                  <div class="insight-subtitle">So với kỳ trước</div>
    </div>
        </div>
              <div class="insight-item">
                <div class="insight-icon orders">
                  <i class="ph bi bi-cart3"></i>
                </div>
                <div class="insight-content">
                  <div class="insight-title">Đơn hàng trung bình</div>
                  <div class="insight-value">{{ averageOrderValue }}</div>
                  <div class="insight-subtitle">Giá trị/đơn hàng</div>
                </div>
              </div>
              <div class="insight-item">
                <div class="insight-icon profit">
                  <i class="bi bi-graph-up"></i>
                </div>
                <div class="insight-content">
                  <div class="insight-title">Tỷ lệ lợi nhuận</div>
                  <div class="insight-value">{{ profitMargin }}%</div>
                  <div class="insight-subtitle">Margin trung bình</div>
                </div>
          </div>
        </div>
      </div>

          <div class="top-products-card">
            <div class="card-header">
              <h3>Sản phẩm bán chạy</h3>
              <button class="view-all-btn">Xem tất cả</button>
        </div>
            <div class="products-list">
              <div v-if="isLoadingTopProducts" class="loading-state">
                <i class="bi bi-arrow-repeat spin"></i> Đang tải...
              </div>
              <div v-else-if="topProducts.length === 0" class="empty-state">
                Chưa có dữ liệu sản phẩm
              </div>
              <div v-else class="product-item" v-for="product in topProducts" :key="product.id">
                <div class="product-image">
                  <img :src="product.image" :alt="product.name">
          </div>
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-sales">{{ product.sales }} đã bán</div>
        </div>
                <div class="product-revenue">{{ product.revenue }}</div>
      </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Order Management Section -->
      <section class="order-management-section">
        <div class="order-grid">
          <div class="order-status-cards">
            <div class="status-card pending">
              <div class="status-icon">
                <i class="ph bi bi-clock"></i>
        </div>
              <div class="status-info">
                <div class="status-count">{{ orderStatusCounts?.pending ?? 0 }}</div>
                <div class="status-label">Chờ xử lý</div>
              </div>
            </div>
            <div class="status-card shipping">
              <div class="status-icon">
                <i class="ph bi bi-truck"></i>
              </div>
              <div class="status-info">
                <div class="status-count">{{ orderStatusCounts?.shipping ?? 0 }}</div>
                <div class="status-label">Đang giao</div>
              </div>
            </div>
            <div class="status-card completed">
              <div class="status-icon">
                <i class="ph bi bi-check-circle"></i>
              </div>
              <div class="status-info">
                <div class="status-count">{{ orderStatusCounts?.completed ?? 0 }}</div>
                <div class="status-label">Hoàn thành</div>
          </div>
        </div>
      </div>

          <div class="recent-orders-card">
            <div class="card-header">
              <h3>Đơn hàng gần đây</h3>
              <button class="view-all-btn">Xem tất cả</button>
        </div>
            <div class="orders-list">
              <div v-if="isLoadingRecentOrders" class="loading-state">
                <i class="bi bi-arrow-repeat spin"></i> Đang tải...
              </div>
              <div v-else-if="recentOrders.length === 0" class="empty-state">
                Chưa có đơn hàng nào
              </div>
              <div v-else class="order-item" v-for="order in recentOrders" :key="order.id">
                <div class="order-info">
                  <div class="order-id">#{{ order.id }}</div>
                  <div class="customer-name">{{ order.customer }}</div>
                  <div class="order-time">{{ order.time }}</div>
          </div>
                <div class="order-amount">{{ order.amount }}</div>
                <div class="order-status" :class="order.status">
                  {{ order.statusText }}
        </div>
      </div>
    </div>
          </div>
        </div>
      </section>

      <!-- Customer Insights Section -->
      <section class="customer-insights-section">
        <div class="insights-grid">
          <div class="customer-stats-card">
            <div class="card-header">
              <h3>Thống kê khách hàng</h3>
        </div>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph bi bi-people"></i>
              </div>
                <div class="stat-content">
                  <div class="stat-value">{{ customerSummary?.totalCustomers ?? 0 }}</div>
                  <div class="stat-label">Tổng khách hàng</div>
            </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph bi bi-person-plus"></i>
            </div>
                <div class="stat-content">
                  <div class="stat-value">{{ customerSummary?.newCustomersToday ?? 0 }}</div>
                  <div class="stat-label">Khách hàng mới</div>
          </div>
        </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph bi bi-arrow-repeat"></i>
      </div>
                <div class="stat-content">
                  <div class="stat-value">{{ customerSummary?.repeatRate ?? 0 }}%</div>
                  <div class="stat-label">Tỷ lệ quay lại</div>
    </div>
          </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph bi bi-star"></i>
        </div>
                <div class="stat-content">
                  <div class="stat-value">{{ customerSummary?.averageRating ?? 0 }}</div>
                  <div class="stat-label">Đánh giá trung bình</div>
                      </div>
                    </div>
          </div>
            </div>

          <div class="category-performance-card">
            <div class="card-header">
              <h3>Hiệu suất danh mục</h3>
          </div>
            <div class="categories-list">
              <div v-if="isLoadingCategoryPerformance" class="loading-state">
                <i class="bi bi-arrow-repeat spin"></i> Đang tải...
              </div>
              <div v-else-if="categoryPerformance.length === 0" class="empty-state">
                Chưa có dữ liệu danh mục
              </div>
              <div v-else class="category-item" v-for="category in categoryPerformance" :key="category.id">
                <div class="category-info">
                  <div class="category-name">{{ category.name }}</div>
                  <div class="category-sales">{{ category.sales }} sản phẩm</div>
        </div>
                <div class="category-revenue">{{ category.revenue }}</div>
                <div class="category-trend" :class="category.trend">
                  <i :class="category.trendIcon"></i>
                  <span>{{ category.change }}</span>
      </div>
    </div>
            </div>
          </div>
        </div>
      </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import Chart from '@/components/admin/Chart.vue'
import thongKeService from '@/services/thongKeService'

// Reactive state
const summary = ref(null)
const alerts = ref(null)
const isLoadingSummary = ref(false)
const isLoadingAlerts = ref(false)

// Chart enhancement state
const selectedMetric = ref('revenue')
const comparisonDate = ref(new Date(Date.now() - 24 * 60 * 60 * 1000))
const showComparisonDatePicker = ref(false)
const chartTimeRange = ref('30days')

// Real data from API
const topProducts = ref([])
const recentOrders = ref([])
const categoryPerformance = ref([])
const chartData = ref(null)
const customerSummary = ref(null)
const orderStatusCounts = ref(null)
const isLoadingChart = ref(false)
const isLoadingTopProducts = ref(false)
const isLoadingRecentOrders = ref(false)
const isLoadingCategoryPerformance = ref(false)
const isLoadingCustomerSummary = ref(false)
const isLoadingOrderStatusCounts = ref(false)


// Enhanced chart data - Analytics style with real API data
const enhancedChartData = computed(() => {
  if (!chartData.value || !chartData.value.labels) {
    return {
      labels: [],
      datasets: []
    }
  }

  const datasets = []
  
  // Main dataset based on selected metric
  if (selectedMetric.value === 'revenue') {
    datasets.push({
      label: 'Doanh thu (triệu VNĐ)',
      data: chartData.value.current || [],
      borderColor: '#ef4444',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      yAxisID: 'y'
    })
    
    datasets.push({
      label: 'Doanh thu kỳ trước (triệu VNĐ)',
      data: chartData.value.previous || [],
      borderColor: '#0ea5e9',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      borderDash: [0, 0],
      yAxisID: 'y'
    })
  } else if (selectedMetric.value === 'orders') {
    datasets.push({
      label: 'Số đơn hàng',
      data: chartData.value.current || [],
      borderColor: '#ef4444',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      yAxisID: 'y'
    })
    
    datasets.push({
      label: 'Số đơn hàng kỳ trước',
      data: chartData.value.previous || [],
      borderColor: '#0ea5e9',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      borderDash: [0, 0],
      yAxisID: 'y'
    })
  } else if (selectedMetric.value === 'customers') {
    // For customers, use orders data as proxy (can be enhanced later)
    datasets.push({
      label: 'Khách hàng mới',
      data: chartData.value.current || [],
      borderColor: '#ef4444',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      yAxisID: 'y'
    })
    
    datasets.push({
      label: 'Khách hàng kỳ trước',
      data: chartData.value.previous || [],
      borderColor: '#0ea5e9',
      backgroundColor: 'transparent',
      borderWidth: 2,
      fill: false,
      tension: 0.3,
      pointRadius: 0,
      pointStyle: 'circle',
      borderDash: [0, 0],
      yAxisID: 'y'
    })
  }
  
  return {
    labels: chartData.value.labels || [],
    datasets
  }
})

// Computed insights from real data
const growthRate = computed(() => {
  if (!chartData.value || !chartData.value.current || !chartData.value.previous) {
    return '0.0'
  }
  
  const currentTotal = chartData.value.current.reduce((sum, val) => sum + Number(val || 0), 0)
  const previousTotal = chartData.value.previous.reduce((sum, val) => sum + Number(val || 0), 0)
  
  if (previousTotal === 0) return currentTotal > 0 ? '100.0' : '0.0'
  return ((currentTotal - previousTotal) / previousTotal * 100).toFixed(1)
})

const averageOrderValue = computed(() => {
  if (!summary.value || !summary.value.revenueToday || !summary.value.newOrders24h) {
    return '0k₫'
  }
  
  const revenue = Number(summary.value.revenueToday || 0)
  const orders = Number(summary.value.newOrders24h || 1)
  
  if (orders === 0) return '0k₫'
  return (revenue / orders / 1000).toFixed(0) + 'k₫'
})

const profitMargin = computed(() => {
  // Mock profit margin (can be enhanced with real data later)
  return '25.0'
})

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

const enhancedChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index',
    intersect: false,
  },
  plugins: {
    legend: {
      display: true,
      position: 'top',
      align: 'end',
      labels: {
        usePointStyle: true,
        pointStyle: 'circle',
        padding: 15,
        font: {
          size: 10,
          family: "'Inter', sans-serif",
          weight: '400'
        },
        color: '#6b7280',
        boxWidth: 6,
        boxHeight: 6
      }
    },
    tooltip: {
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      titleColor: '#374151',
      bodyColor: '#6b7280',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      padding: 12,
      boxWidth: 8,
      boxHeight: 8,
      boxPadding: 4,
      titleFont: {
        size: 12,
        weight: '600',
        family: "'Inter', sans-serif"
      },
      bodyFont: {
        size: 11,
        weight: '500',
        family: "'Inter', sans-serif"
      },
      bodySpacing: 4,
      titleMarginBottom: 8,
      callbacks: {
        title: function(context) {
          return `${context[0].label}`
        },
        label: function(context) {
          return `${context.dataset.label}: ${context.parsed.y.toLocaleString('vi-VN')}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: true,
        color: 'rgba(156, 163, 175, 0.15)',
        drawBorder: false,
        lineWidth: 1
      },
      ticks: {
        font: {
          size: 11,
          family: "'Inter', sans-serif",
          weight: '400'
        },
        color: '#9ca3af',
        padding: 6,
        autoSkip: false,
        maxTicksLimit: 24,
        stepSize: 1
      }
    },
    y: {
      grid: {
        display: true,
        color: 'rgba(156, 163, 175, 0.15)',
        drawBorder: false,
        lineWidth: 1
      },
      ticks: {
        font: {
          size: 10,
          family: "'Inter', sans-serif",
          weight: '400'
        },
        color: '#9ca3af',
        padding: 6,
        maxTicksLimit: 8,
        count: 8
      }
    }
  },
  animation: {
    duration: 300,
    easing: 'easeInOut'
  },
  elements: {
    point: {
      radius: 0,
      hitRadius: 30,
      hoverRadius: 6,
      hoverBorderWidth: 2
    }
  },
  layout: {
    padding: {
      top: 10,
      right: 10,
      bottom: 10,
      left: 10
    }
  }
}))


// Methods
const formatDate = (date) => {
  const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000)
  const targetDate = new Date(date)
  
  if (targetDate.toDateString() === yesterday.toDateString()) {
    return 'Hôm qua'
  }
  
  return targetDate.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const selectQuickDate = (option) => {
  const today = new Date()
  let targetDate = new Date()
  
  switch (option) {
    case 'yesterday':
      targetDate = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      break
    case '2days':
      targetDate = new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000)
      break
    case 'week':
      targetDate = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
  }
  
  comparisonDate.value = targetDate
  showComparisonDatePicker.value = false
}


const exportChartData = () => {
  // Export chart data functionality
}

// Load chart data from API
const loadChartData = async () => {
  try {
    isLoadingChart.value = true
    const response = await thongKeService.getChart({
      range: chartTimeRange.value,
      metric: selectedMetric.value
    })
    chartData.value = response?.data ?? response
  } catch (error) {
    console.error('Error loading chart data:', error)
    chartData.value = { labels: [], current: [], previous: [] }
  } finally {
    isLoadingChart.value = false
  }
}

// Load top products from API
const loadTopProducts = async () => {
  try {
    isLoadingTopProducts.value = true
    const response = await thongKeService.getTopProducts({ limit: 4, rangeDays: 30 })
    const data = response?.data ?? response
    topProducts.value = (data || []).map(product => ({
      id: product.id,
      name: product.name,
      image: product.image || 'https://via.placeholder.com/40x40/3b82f6/ffffff?text=SP',
      sales: product.sales,
      revenue: formatCurrency(product.revenue)
    }))
  } catch (error) {
    console.error('Error loading top products:', error)
    topProducts.value = []
  } finally {
    isLoadingTopProducts.value = false
  }
}

// Load recent orders from API
const loadRecentOrders = async () => {
  try {
    isLoadingRecentOrders.value = true
    const response = await thongKeService.getRecentOrders({ limit: 4 })
    const data = response?.data ?? response
    recentOrders.value = (data || []).map(order => ({
      id: order.orderCode || order.id,
      customer: order.customer,
      time: order.time,
      amount: formatCurrency(order.amount),
      status: mapOrderStatus(order.status),
      statusText: getStatusDisplayName(order.status)
    }))
  } catch (error) {
    console.error('Error loading recent orders:', error)
    recentOrders.value = []
  } finally {
    isLoadingRecentOrders.value = false
  }
}

// Load category performance from API
const loadCategoryPerformance = async () => {
  try {
    isLoadingCategoryPerformance.value = true
    const response = await thongKeService.getCategoryPerformance({ limit: 4, rangeDays: 30 })
    const data = response?.data ?? response
    categoryPerformance.value = (data || []).map(category => ({
      id: category.id,
      name: category.name,
      sales: category.sales,
      revenue: formatCurrency(category.revenue),
      change: (category.change >= 0 ? '+' : '') + category.change + '%',
      trend: category.trend,
      trendIcon: category.trendIcon
    }))
  } catch (error) {
    console.error('Error loading category performance:', error)
    categoryPerformance.value = []
  } finally {
    isLoadingCategoryPerformance.value = false
  }
}

// Load customer summary from API
const loadCustomerSummary = async () => {
  try {
    isLoadingCustomerSummary.value = true
    const response = await thongKeService.getCustomerSummary()
    customerSummary.value = response?.data ?? response
  } catch (error) {
    console.error('Error loading customer summary:', error)
    customerSummary.value = null
  } finally {
    isLoadingCustomerSummary.value = false
  }
}

// Load order status counts from API
const loadOrderStatusCounts = async () => {
  try {
    isLoadingOrderStatusCounts.value = true
    const response = await thongKeService.getOrderStatusCounts()
    orderStatusCounts.value = response?.data ?? response
  } catch (error) {
    console.error('Error loading order status counts:', error)
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
    currency: 'VND'
  }).format(num)
}

const mapOrderStatus = (status) => {
  // Map English status from API to CSS class
  const statusMap = {
    'PENDING': 'pending',
    'SHIPPING': 'shipping',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled',
    // Fallback for Vietnamese (backward compatibility)
    'Chờ xác nhận': 'pending',
    'Đang giao': 'shipping',
    'Hoàn tất': 'completed',
    'Đã hủy': 'cancelled'
  }
  return statusMap[status] || 'pending'
}

// Get Vietnamese display name for status
const getStatusDisplayName = (status) => {
  const displayMap = {
    'PENDING': 'Chờ xác nhận',
    'SHIPPING': 'Đang giao',
    'COMPLETED': 'Hoàn tất',
    'CANCELLED': 'Đã hủy'
  }
  return displayMap[status] || status
}

// Watch for metric and time range changes
watch([selectedMetric, chartTimeRange], () => {
  loadChartData()
})

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
    loadChartData(),
    loadTopProducts(),
    loadRecentOrders(),
    loadCategoryPerformance(),
    loadCustomerSummary(),
    loadOrderStatusCounts()
  ])
})
</script>

<style scoped>
/* Dashboard Content Styles */
.dashboard-content {
  padding: 0;
  background: #f8fafc;
  color: #1f2937;
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

.main-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
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

/* Actions & Alerts Section */
.actions-alerts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.quick-actions-panel, .alerts-panel {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.quick-actions-panel h3, .alerts-panel h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  color: #374151;
}

.action-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.action-btn i {
  font-size: 1.5rem;
  color: #6366f1;
}

.action-btn span {
  font-size: 0.875rem;
  font-weight: 500;
  text-align: center;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.alert-item:hover {
  background: #f9fafb;
}

.alert-item.urgent {
  border-left: 4px solid #ef4444;
}

.alert-item.warning {
  border-left: 4px solid #f59e0b;
}

.alert-item.info {
  border-left: 4px solid #3b82f6;
}

.alert-item .alert-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.alert-item.urgent .alert-icon {
  background: #fef2f2;
  color: #dc2626;
}

.alert-item.warning .alert-icon {
  background: #fffbeb;
  color: #f59e0b;
}

.alert-item.info .alert-icon {
  background: #eff6ff;
  color: #3b82f6;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.alert-time {
  font-size: 0.75rem;
  color: #9ca3af;
  margin: 0;
}

.alert-action {
  background: #6366f1;
  color: #ffffff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.alert-action:hover {
  background: #4f46e5;
}

/* Analytics Section */
.analytics-section {
  margin-bottom: 2rem;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.chart-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.chart-header {
  margin-bottom: 1.5rem;
}

.chart-title-section {
  margin-bottom: 1rem;
}

.chart-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.chart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.growth-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.growth-badge i {
  font-size: 0.75rem;
}

.metric-toggles {
  display: flex;
  gap: 0.5rem;
  background: #f8fafc;
  padding: 0.25rem;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 1rem;
}

.metric-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.metric-toggle:hover {
  background: #e2e8f0;
  color: #475569;
}

.metric-toggle.active {
  background: white;
  color: #1e293b;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.metric-toggle i {
  font-size: 0.75rem;
}

.chart-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
  justify-content: space-between;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.control-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.time-selector, .granularity-selector {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #ffffff;
  font-size: 0.875rem;
  color: #374151;
  cursor: pointer;
  transition: border-color 0.2s;
}

.time-selector:focus, .granularity-selector:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.chart-action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.chart-action-btn:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.chart-insights {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.insight-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.insight-item:hover {
  background: #f3f4f6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.insight-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  cursor: pointer;
  transition: color 0.2s;
}

.view-all-btn:hover {
  color: #4f46e5;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.product-item:hover {
  background: #f9fafb;
}

.product-image img {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 6px;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.product-sales {
  font-size: 0.75rem;
  color: #6b7280;
}

.product-revenue {
  font-size: 0.875rem;
  font-weight: 600;
  color: #16a34a;
}

/* Order Management Section */
.order-management-section {
  margin-bottom: 2rem;
}

.order-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.order-status-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.status-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  text-align: center;
  transition: all 0.2s;
}

.status-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.status-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  margin: 0 auto 1rem auto;
}

.status-card.pending .status-icon {
  background: #fef3c7;
  color: #d97706;
}

.status-card.processing .status-icon {
  background: #dbeafe;
  color: #2563eb;
}

.status-card.shipping .status-icon {
  background: #e9d5ff;
  color: #8b5cf6;
}

.status-card.completed .status-icon {
  background: #dcfce7;
  color: #16a34a;
}

.status-info {
  text-align: center;
}

.status-count {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.status-label {
  font-size: 0.875rem;
  color: #6b7280;
}

.recent-orders-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.order-item:hover {
  background: #f9fafb;
}

.order-info {
  flex: 1;
}

.order-id {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.customer-name {
  font-size: 0.875rem;
  color: #374151;
  margin-bottom: 0.25rem;
}

.order-time {
  font-size: 0.75rem;
  color: #9ca3af;
}

.order-amount {
  font-size: 0.875rem;
  font-weight: 600;
  color: #16a34a;
}

.order-status {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
}

.order-status.pending {
  background: #fef3c7;
  color: #d97706;
}

.order-status.processing {
  background: #dbeafe;
  color: #2563eb;
}

.order-status.shipping {
  background: #e9d5ff;
  color: #8b5cf6;
}

.order-status.completed {
  background: #dcfce7;
  color: #16a34a;
}

/* Customer Insights Section */
.customer-insights-section {
  margin-bottom: 2rem;
}

.insights-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.customer-stats-card, .category-performance-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.stat-item:hover {
  background: #f9fafb;
}

.stat-item .stat-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  background: #dbeafe;
  color: #2563eb;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
}

.categories-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.category-item:hover {
  background: #f9fafb;
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.category-sales {
  font-size: 0.75rem;
  color: #6b7280;
}

.category-revenue {
  font-size: 0.875rem;
  font-weight: 600;
  color: #16a34a;
  margin-right: 1rem;
}

.category-trend {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.category-trend.positive {
  color: #16a34a;
}

.category-trend.negative {
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
@media (max-width: 1200px) {
  .analytics-grid {
    grid-template-columns: 1fr;
  }
  
  .order-grid {
    grid-template-columns: 1fr;
  }
  
  .insights-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1024px) {
  .actions-alerts-section {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .order-status-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .chart-metrics-toggle {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .chart-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }
  
  .control-group {
    justify-content: space-between;
  }
  
  .chart-insights {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-alerts-section {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    grid-template-columns: 1fr;
  }
  
  .order-status-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .overview-card {
    padding: 1rem;
  }
  
  .quick-actions-panel, .alerts-panel {
    padding: 1rem;
  }
  
  .chart-card, .top-products-card {
    padding: 1rem;
  }
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
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.date-picker-btn:hover {
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.date-picker-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border-radius: 8px;
  font-size: 0.875rem;
}

.date-picker-content {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.date-picker-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.date-picker-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
}

.date-picker-arrow {
  display: flex;
  align-items: center;
  color: #64748b;
  transition: transform 0.3s ease;
}

.date-picker-arrow .rotated {
  transform: rotate(180deg);
}

.date-picker-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  z-index: 1000;
  min-width: 280px;
  animation: slideDown 0.3s ease-out;
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
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid #f1f5f9;
}

.date-picker-header h6 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border: none;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #475569;
}

.date-picker-body {
  padding: 1rem 1.25rem 1.25rem;
}

.quick-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.quick-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.quick-option:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateX(2px);
}

.quick-option i {
  color: #3b82f6;
  font-size: 0.875rem;
}

.date-separator {
  position: relative;
  text-align: center;
  margin: 1rem 0;
}

.date-separator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
}

.date-separator span {
  background: white;
  padding: 0 1rem;
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.custom-date {
  margin-top: 0.5rem;
}

.date-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.875rem;
  color: #1e293b;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.date-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
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