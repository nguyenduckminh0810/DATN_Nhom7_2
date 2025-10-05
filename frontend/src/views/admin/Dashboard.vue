<template>
  <div class="dashboard-content">

      <!-- Store Overview Section -->
      <section class="overview-section">
        <div class="overview-grid">
          <div class="overview-card revenue">
            <div class="card-header">
              <div class="card-icon">
                <i class="ph ph-currency-dollar"></i>
        </div>
              <div class="card-info">
                <h3>Doanh thu hôm nay</h3>
                <p class="card-subtitle">So với hôm qua</p>
      </div>
            </div>
            <div class="card-content">
              <div class="main-value">15,420,000₫</div>
              <div class="trend positive">
                <i class="ph ph-trend-up"></i>
                <span>+12.5%</span>
        </div>
      </div>
    </div>

          <div class="overview-card orders">
            <div class="card-header">
              <div class="card-icon">
                <i class="ph ph-shopping-cart"></i>
          </div>
              <div class="card-info">
                <h3>Đơn hàng mới</h3>
                <p class="card-subtitle">Trong 24h qua</p>
          </div>
        </div>
            <div class="card-content">
              <div class="main-value">89</div>
              <div class="trend positive">
                <i class="ph ph-trend-up"></i>
                <span>+8.3%</span>
      </div>
            </div>
          </div>

          <div class="overview-card customers">
            <div class="card-header">
              <div class="card-icon">
                <i class="ph ph-users"></i>
              </div>
              <div class="card-info">
                <h3>Khách hàng mới</h3>
                <p class="card-subtitle">Đăng ký hôm nay</p>
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">23</div>
              <div class="trend positive">
                <i class="ph ph-trend-up"></i>
                <span>+15.2%</span>
        </div>
      </div>
    </div>

          <div class="overview-card inventory">
            <div class="card-header">
              <div class="card-icon">
                <i class="ph ph-package"></i>
        </div>
              <div class="card-info">
                <h3>Sản phẩm sắp hết</h3>
                <p class="card-subtitle">Cần nhập hàng</p>
      </div>
        </div>
            <div class="card-content">
              <div class="main-value">12</div>
              <div class="trend warning">
                <i class="ph ph-warning"></i>
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
            <button class="action-btn">
              <i class="ph ph-plus-circle"></i>
              <span>Thêm sản phẩm</span>
            </button>
            <button class="action-btn">
              <i class="ph ph-shopping-cart"></i>
              <span>Xem đơn hàng</span>
            </button>
            <button class="action-btn">
              <i class="ph ph-users"></i>
              <span>Quản lý khách hàng</span>
            </button>
            <button class="action-btn">
              <i class="ph ph-chart-bar"></i>
              <span>Báo cáo doanh thu</span>
            </button>
          </div>
            </div>

        <div class="alerts-panel">
          <h3>Cần chú ý</h3>
          <div class="alert-list">
            <div class="alert-item urgent">
              <div class="alert-icon">
                <i class="ph ph-warning"></i>
          </div>
              <div class="alert-content">
                <div class="alert-title">5 đơn hàng chờ xác nhận</div>
                <div class="alert-time">2 phút trước</div>
        </div>
              <button class="alert-action">Xem</button>
      </div>
            <div class="alert-item warning">
              <div class="alert-icon">
                <i class="ph ph-package"></i>
    </div>
              <div class="alert-content">
                <div class="alert-title">12 sản phẩm sắp hết hàng</div>
                <div class="alert-time">1 giờ trước</div>
              </div>
              <button class="alert-action">Xem</button>
            </div>
            <div class="alert-item info">
              <div class="alert-icon">
                <i class="ph ph-clock"></i>
              </div>
              <div class="alert-content">
                <div class="alert-title">8 đơn hàng cần giao</div>
                <div class="alert-time">3 giờ trước</div>
              </div>
              <button class="alert-action">Xem</button>
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
                <h3>Phân tích doanh thu & đơn hàng</h3>
                <div class="chart-metrics-toggle">
                  <label class="metric-toggle">
                    <input type="checkbox" v-model="showRevenue" @change="updateChartData">
                    <span>Doanh thu</span>
            </label>
                  <label class="metric-toggle">
                    <input type="checkbox" v-model="showOrders" @change="updateChartData">
                    <span>Số đơn hàng</span>
                  </label>
                  <label class="metric-toggle">
                    <input type="checkbox" v-model="showProfit" @change="updateChartData">
                    <span>Lợi nhuận</span>
                  </label>
                  <label class="metric-toggle">
                    <input type="checkbox" v-model="showComparison" @change="updateChartData">
                    <span>So sánh kỳ trước</span>
            </label>
                </div>
              </div>
              <div class="chart-controls">
                <div class="control-group">
                  <label class="control-label">Khoảng thời gian:</label>
                  <select class="time-selector" v-model="chartTimeRange" @change="updateChartData">
                    <option value="7days">7 ngày qua</option>
                    <option value="30days">30 ngày qua</option>
                    <option value="3months">3 tháng qua</option>
                    <option value="year">Năm nay</option>
                    <option value="custom">Tùy chỉnh</option>
                  </select>
                </div>
                <div class="control-group">
                  <label class="control-label">Độ phân giải:</label>
                  <select class="granularity-selector" v-model="chartGranularity" @change="updateChartData">
                    <option value="daily">Theo ngày</option>
                    <option value="weekly">Theo tuần</option>
                    <option value="monthly">Theo tháng</option>
                  </select>
                </div>
                <button class="chart-action-btn" @click="exportChartData">
                  <i class="ph ph-download"></i>
                  <span>Xuất dữ liệu</span>
                </button>
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
                  <i class="ph ph-trend-up"></i>
      </div>
                <div class="insight-content">
                  <div class="insight-title">Tăng trưởng doanh thu</div>
                  <div class="insight-value positive">+{{ growthRate }}%</div>
                  <div class="insight-subtitle">So với kỳ trước</div>
    </div>
        </div>
              <div class="insight-item">
                <div class="insight-icon orders">
                  <i class="ph ph-shopping-cart"></i>
                </div>
                <div class="insight-content">
                  <div class="insight-title">Đơn hàng trung bình</div>
                  <div class="insight-value">{{ averageOrderValue }}</div>
                  <div class="insight-subtitle">Giá trị/đơn hàng</div>
                </div>
              </div>
              <div class="insight-item">
                <div class="insight-icon profit">
                  <i class="ph ph-chart-line-up"></i>
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
              <div class="product-item" v-for="product in topProducts" :key="product.id">
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
                <i class="ph ph-clock"></i>
        </div>
              <div class="status-info">
                <div class="status-count">23</div>
                <div class="status-label">Chờ xử lý</div>
              </div>
            </div>
            <div class="status-card processing">
              <div class="status-icon">
                <i class="ph ph-gear"></i>
              </div>
              <div class="status-info">
                <div class="status-count">15</div>
                <div class="status-label">Đang xử lý</div>
              </div>
            </div>
            <div class="status-card shipping">
              <div class="status-icon">
                <i class="ph ph-truck"></i>
              </div>
              <div class="status-info">
                <div class="status-count">8</div>
                <div class="status-label">Đang giao</div>
              </div>
            </div>
            <div class="status-card completed">
              <div class="status-icon">
                <i class="ph ph-check-circle"></i>
              </div>
              <div class="status-info">
                <div class="status-count">156</div>
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
              <div class="order-item" v-for="order in recentOrders" :key="order.id">
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
                  <i class="ph ph-users"></i>
              </div>
                <div class="stat-content">
                  <div class="stat-value">2,456</div>
                  <div class="stat-label">Tổng khách hàng</div>
            </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph ph-user-plus"></i>
            </div>
                <div class="stat-content">
                  <div class="stat-value">89</div>
                  <div class="stat-label">Khách hàng mới</div>
          </div>
        </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph ph-repeat"></i>
      </div>
                <div class="stat-content">
                  <div class="stat-value">65%</div>
                  <div class="stat-label">Tỷ lệ quay lại</div>
    </div>
          </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="ph ph-star"></i>
        </div>
                <div class="stat-content">
                  <div class="stat-value">4.8</div>
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
              <div class="category-item" v-for="category in categoryPerformance" :key="category.id">
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
import { ref, onMounted, computed } from 'vue'
import Chart from '@/components/ui/Chart.vue'

// Reactive state
const showQuickAdd = ref(false)

// Chart enhancement state
const showRevenue = ref(true)
const showOrders = ref(true)
const showProfit = ref(false)
const showComparison = ref(false)
const chartTimeRange = ref('30days')
const chartGranularity = ref('daily')

// Ecommerce Data
const topProducts = ref([
  {
    id: 1,
    name: 'Áo sơ mi nam cao cấp',
    image: 'https://via.placeholder.com/40x40/3b82f6/ffffff?text=AS',
    sales: 156,
    revenue: '12.5M₫'
  },
  {
    id: 2,
    name: 'Quần âu nam công sở',
    image: 'https://via.placeholder.com/40x40/10b981/ffffff?text=QA',
    sales: 134,
    revenue: '8.9M₫'
  },
  {
    id: 3,
    name: 'Áo khoác nam dạ',
    image: 'https://via.placeholder.com/40x40/f59e0b/ffffff?text=AK',
    sales: 89,
    revenue: '15.2M₫'
  },
  {
    id: 4,
    name: 'Vest nam công sở',
    image: 'https://via.placeholder.com/40x40/ef4444/ffffff?text=VS',
    sales: 67,
    revenue: '18.7M₫'
  }
])

const recentOrders = ref([
  {
    id: 'AURO-001',
    customer: 'Nguyễn Văn An',
    time: '5 phút trước',
    amount: '1,250,000₫',
    status: 'pending',
    statusText: 'Chờ xử lý'
  },
  {
    id: 'AURO-002',
    customer: 'Trần Thị Bình',
    time: '15 phút trước',
    amount: '890,000₫',
    status: 'processing',
    statusText: 'Đang xử lý'
  },
  {
    id: 'AURO-003',
    customer: 'Lê Minh Cường',
    time: '1 giờ trước',
    amount: '2,450,000₫',
    status: 'shipping',
    statusText: 'Đang giao'
  },
  {
    id: 'AURO-004',
    customer: 'Phạm Thị Dung',
    time: '2 giờ trước',
    amount: '3,200,000₫',
    status: 'completed',
    statusText: 'Hoàn thành'
  }
])

const categoryPerformance = ref([
  {
    id: 1,
    name: 'Áo sơ mi',
    sales: 456,
    revenue: '25.4M₫',
    change: '+12.5%',
    trend: 'positive',
    trendIcon: 'ph ph-trend-up'
  },
  {
    id: 2,
    name: 'Quần âu',
    sales: 389,
    revenue: '18.9M₫',
    change: '+8.3%',
    trend: 'positive',
    trendIcon: 'ph ph-trend-up'
  },
  {
    id: 3,
    name: 'Áo khoác',
    sales: 234,
    revenue: '22.1M₫',
    change: '+15.2%',
    trend: 'positive',
    trendIcon: 'ph ph-trend-up'
  },
  {
    id: 4,
    name: 'Vest',
    sales: 167,
    revenue: '28.7M₫',
    change: '-3.1%',
    trend: 'negative',
    trendIcon: 'ph ph-trend-down'
  }
])


// Enhanced chart data
const enhancedChartData = computed(() => {
  const labels = generateLabels()
  const datasets = []
  
  if (showRevenue.value) {
    datasets.push({
      label: 'Doanh thu (triệu VNĐ)',
      data: getRevenueData(),
      borderColor: '#3b82f6',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      borderWidth: 3,
      fill: showRevenue.value && !showOrders.value,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#3b82f6',
      yAxisID: 'y'
    })
  }
  
  if (showOrders.value) {
    datasets.push({
      label: 'Số đơn hàng',
      data: getOrdersData(),
      borderColor: '#10b981',
      backgroundColor: 'rgba(16, 185, 129, 0.1)',
      borderWidth: 3,
      fill: showOrders.value && !showRevenue.value,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#10b981',
      yAxisID: 'y1'
    })
  }
  
  if (showProfit.value) {
    datasets.push({
      label: 'Lợi nhuận (triệu VNĐ)',
      data: getProfitData(),
      borderColor: '#f59e0b',
      backgroundColor: 'rgba(245, 158, 11, 0.1)',
      borderWidth: 3,
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#f59e0b',
      yAxisID: 'y'
    })
  }
  
  if (showComparison.value) {
    datasets.push({
      label: 'Doanh thu kỳ trước (triệu VNĐ)',
      data: getComparisonData(),
      borderColor: '#6b7280',
      backgroundColor: 'rgba(107, 114, 128, 0.1)',
      borderWidth: 2,
      fill: false,
      tension: 0.4,
      pointRadius: 3,
      pointBackgroundColor: '#6b7280',
      borderDash: [5, 5],
      yAxisID: 'y'
    })
  }
  
  return {
    labels,
    datasets
  }
})

// Computed insights
const growthRate = computed(() => {
  const current = getCurrentPeriodRevenue()
  const previous = getPreviousPeriodRevenue()
  return ((current - previous) / previous * 100).toFixed(1)
})

const averageOrderValue = computed(() => {
  const revenue = getCurrentPeriodRevenue()
  const orders = getCurrentPeriodOrders()
  return (revenue / orders).toFixed(0) + 'k₫'
})

const profitMargin = computed(() => {
  const revenue = getCurrentPeriodRevenue()
  const profit = getCurrentPeriodProfit()
  return ((profit / revenue) * 100).toFixed(1)
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
      position: 'bottom',
      labels: {
        usePointStyle: true,
        padding: 20,
        font: {
          size: 12
        }
      }
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      titleColor: '#fff',
      bodyColor: '#fff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      displayColors: true,
      callbacks: {
        label: function(context) {
          let label = context.dataset.label || ''
          if (label) {
            label += ': '
          }
          if (context.dataset.label?.includes('Doanh thu') || context.dataset.label?.includes('Lợi nhuận')) {
            label += context.parsed.y + ' triệu VNĐ'
          } else if (context.dataset.label?.includes('đơn hàng')) {
            label += context.parsed.y + ' đơn'
          } else {
            label += context.parsed.y
          }
          return label
        }
      }
    }
  },
  scales: {
    y: {
      type: 'linear',
      display: true,
      position: 'left',
      beginAtZero: true,
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false
      },
      ticks: {
        color: '#64748b',
        font: {
          size: 11
        },
        callback: function(value) {
          return value + 'M₫'
        }
      }
    },
    y1: {
      type: 'linear',
      display: showOrders.value,
      position: 'right',
      beginAtZero: true,
      grid: {
        drawOnChartArea: false,
      },
      ticks: {
        color: '#64748b',
        font: {
          size: 11
        },
        callback: function(value) {
          return value + ' đơn'
        }
      }
    },
    x: {
      grid: {
        color: 'rgba(148, 163, 184, 0.1)',
        drawBorder: false
      },
      ticks: {
        color: '#64748b',
        font: {
          size: 11
        }
      }
    }
  }
}))

// Mini chart options for ecommerce
const miniChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    x: {
      display: false
    },
    y: {
      display: false
    }
  }
}))

// Methods
const updateChartData = () => {
  // Force chart re-render when data changes
  console.log('Chart data updated')
}

const exportChartData = () => {
  // Export chart data functionality
  console.log('Exporting chart data...')
}

// Data generation methods
const generateLabels = () => {
  const range = chartTimeRange.value
  const granularity = chartGranularity.value
  
  if (range === '7days') {
    return Array.from({length: 7}, (_, i) => {
      const date = new Date()
      date.setDate(date.getDate() - (6 - i))
      return granularity === 'daily' ? 
        `${date.getDate()}/${date.getMonth() + 1}` : 
        `T${i + 1}`
    })
  } else if (range === '30days') {
    return Array.from({length: 30}, (_, i) => {
      const date = new Date()
      date.setDate(date.getDate() - (29 - i))
      return granularity === 'daily' ? 
        `${date.getDate()}/${date.getMonth() + 1}` : 
        `T${i + 1}`
    })
  } else if (range === '3months') {
    return ['T1', 'T2', 'T3']
  } else if (range === 'year') {
    return ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12']
  }
  
  return ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12']
}

const getRevenueData = () => {
  const range = chartTimeRange.value
  if (range === '7days') {
    return [15, 18, 12, 22, 19, 25, 28]
  } else if (range === '30days') {
    return Array.from({length: 30}, (_, i) => Math.floor(Math.random() * 20) + 10)
  } else if (range === '3months') {
    return [125, 142, 138]
  }
  return [125, 142, 138, 161, 155, 168, 172, 165, 158, 163, 159, 175]
}

const getOrdersData = () => {
  const range = chartTimeRange.value
  if (range === '7days') {
    return [45, 52, 38, 68, 55, 72, 78]
  } else if (range === '30days') {
    return Array.from({length: 30}, (_, i) => Math.floor(Math.random() * 30) + 20)
  } else if (range === '3months') {
    return [456, 520, 480]
  }
  return [456, 520, 480, 580, 540, 620, 650, 600, 570, 590, 580, 620]
}

const getProfitData = () => {
  const revenue = getRevenueData()
  return revenue.map(val => Math.floor(val * 0.25)) // 25% profit margin
}

const getComparisonData = () => {
  const current = getRevenueData()
  return current.map(val => Math.floor(val * 0.85)) // 85% of current period
}

const getCurrentPeriodRevenue = () => {
  const data = getRevenueData()
  return data.reduce((sum, val) => sum + val, 0)
}

const getCurrentPeriodOrders = () => {
  const data = getOrdersData()
  return data.reduce((sum, val) => sum + val, 0)
}

const getCurrentPeriodProfit = () => {
  const data = getProfitData()
  return data.reduce((sum, val) => sum + val, 0)
}

const getPreviousPeriodRevenue = () => {
  const data = getComparisonData()
  return data.reduce((sum, val) => sum + val, 0)
}

// Methods for ecommerce dashboard
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getOrderStatusColor = (status) => {
  const colors = {
    pending: '#f59e0b',
    processing: '#3b82f6',
    shipping: '#8b5cf6',
    completed: '#10b981'
  }
  return colors[status] || '#6b7280'
}

onMounted(() => {
  // Initialize dashboard
  console.log('Modern Admin Dashboard loaded')
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
  color: #3b82f6;
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
  background: #3b82f6;
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
  background: #2563eb;
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

.chart-title-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.chart-metrics-toggle {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.metric-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  transition: color 0.2s;
}

.metric-toggle:hover {
  color: #1f2937;
}

.metric-toggle input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
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
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
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
  color: #3b82f6;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s;
}

.view-all-btn:hover {
  color: #2563eb;
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
  border-color: #3b82f6;
  color: #3b82f6;
}

.time-filter.active {
  background: #3b82f6;
  border-color: #3b82f6;
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
</style>