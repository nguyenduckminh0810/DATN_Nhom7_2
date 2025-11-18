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
              <i class="bi bi-funnel me-1"></i>Bộ lọc nâng cao
            </button>
            <button class="btn btn-outline-success btn-sm" @click="exportReport">
              <i class="bi bi-download me-1"></i>Xuất báo cáo
            </button>
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
        <div class="last-updated">
          Cập nhật lần cuối: {{ formatTime(lastUpdated) }}
        </div>
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
              <i class="bi bi-currency-dollar"></i>
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
              <div class="kpi-value">{{ totalOrders.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Tổng đơn hàng</div>
              <div class="kpi-change" :class="ordersChange.type">
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
              <div class="kpi-value">{{ totalCustomers.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Khách hàng mới</div>
              <div class="kpi-change" :class="customersChange.type">
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
              <div class="kpi-value">{{ totalProducts.toLocaleString('vi-VN') }}</div>
              <div class="kpi-label">Sản phẩm bán ra</div>
              <div class="kpi-change" :class="productsChange.type">
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

    <!-- Business Insights -->
    <div class="insights-section">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6>Hiệu suất kinh doanh</h6>
              <i class="bi bi-graph-up"></i>
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
              <i class="bi bi-people-three"></i>
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
              <i class="bi bi-box"></i>
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
                  <i class="bi bi-graph-up"></i>
                  +{{ growthRate }}% tăng trưởng
                </div>
                <!-- Comparison Mode Selector -->
                <div class="comparison-selector" v-if="comparisonMode !== 'none' && comparisonPercentage !== null">
                  <span class="comparison-badge" :class="comparisonPercentage >= 0 ? 'positive' : 'negative'">
                    <i :class="comparisonPercentage >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down'"></i>
                    {{ Math.abs(comparisonPercentage) }}%
                  </span>
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
                <!-- Chart actions removed - default to line chart -->
                <div class="chart-actions">
                  <button class="btn btn-outline-primary btn-sm" @click="exportChart">
                    <i class="bi bi-download"></i>
                    Xuất biểu đồ
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
                  <i class="bi bi-circle-fill"></i>
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
              <select v-model="customerAnalyticsType" @change="loadCustomerAnalytics" class="form-control form-control-sm">
                <option value="segments">Phân khúc</option>
                <option value="behavior">Hành vi</option>
                <option value="geography">Địa lý</option>
              </select>
            </div>
            <div class="chart-content">
              <Chart
                type="bar"
                :data="customerAnalyticsChartData"
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Chart from '@/components/admin/Chart.vue'
import thongKeService from '@/services/thongKeService'

// Reactive data
const showAdvancedFilters = ref(false)
const selectedDateRange = ref('30days')
const customStartDate = ref('')
const customEndDate = ref('')
const selectedCategory = ref('')
const selectedRegion = ref('')
const selectedCustomerType = ref('')
const selectedAgeGroup = ref('')

// Computed date range for API calls
const getDateRange = () => {
  const now = new Date()
  let startDate = ''
  let endDate = now.toISOString().split('T')[0] // Today in YYYY-MM-DD format

  if (selectedDateRange.value === 'custom' && customStartDate.value && customEndDate.value) {
    startDate = customStartDate.value
    endDate = customEndDate.value
  } else {
    switch (selectedDateRange.value) {
      case 'today':
        startDate = now.toISOString().split('T')[0]
        break
      case 'yesterday':
        const yesterday = new Date(now)
        yesterday.setDate(yesterday.getDate() - 1)
        startDate = yesterday.toISOString().split('T')[0]
        endDate = yesterday.toISOString().split('T')[0]
        break
      case '7days':
        const sevenDaysAgo = new Date(now)
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
        startDate = sevenDaysAgo.toISOString().split('T')[0]
        break
      case '30days':
        const thirtyDaysAgo = new Date(now)
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
        startDate = thirtyDaysAgo.toISOString().split('T')[0]
        break
      case '90days':
        const ninetyDaysAgo = new Date(now)
        ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90)
        startDate = ninetyDaysAgo.toISOString().split('T')[0]
        break
      case 'thisMonth':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
        break
      case 'lastMonth':
        const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
        startDate = lastMonth.toISOString().split('T')[0]
        endDate = new Date(now.getFullYear(), now.getMonth(), 0).toISOString().split('T')[0]
        break
      case 'thisYear':
        startDate = new Date(now.getFullYear(), 0, 1).toISOString().split('T')[0]
        break
      default:
        // Default to 30 days
        const defaultStart = new Date(now)
        defaultStart.setDate(defaultStart.getDate() - 30)
        startDate = defaultStart.toISOString().split('T')[0]
    }
  }

  return { startDate, endDate }
}

const selectedMetric = ref('revenue')
const chartType = ref('line')
const orderDistributionType = ref('status')
const customerAnalyticsType = ref('segments')

// Comparison mode with date picker
const comparisonMode = ref('yesterday')
const comparisonDate = ref(new Date(Date.now() - 24 * 60 * 60 * 1000))
const showComparisonDatePicker = ref(false)

// Chart refs
const mainChart = ref(null)
const orderDistributionChart = ref(null)
const customerAnalyticsChart = ref(null)

// Loading states
const isLoadingKpis = ref(false)
const isLoadingHourlySales = ref(false)
const isLoadingBusinessInsights = ref(false)
const isLoadingPerformance = ref(false)
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

// Business Insights - from API
const averageOrderValue = ref(0)
const refundRate = ref(0)
const profitMargin = ref(0)
const returningCustomers = ref(0)
const customerLifetimeValue = ref(0)
const averageRetentionDays = ref(0)
const topSellingProduct = ref('')
const topCategory = ref('')
const inventoryTurnover = ref(0)

// Performance Metrics - from API
const cartConversionRate = ref(0)
const cartAbandonmentRate = ref(0)
const customerSatisfaction = ref(0)

// Computed growth rate (use revenue growth as main indicator)
const growthRate = computed(() => Math.abs(revenueGrowth.value).toFixed(1))

// Real-time metrics - from API
const todayRevenue = ref(0)
const todayVisitors = ref(0)
const todayClicks = ref(0)
const todayOrders = ref(0)
const todayProducts = ref(0)
const todayBuyers = ref(0)
const todayCustomers = ref(0)
const conversionRate = ref(0)
const lastUpdate = ref('')
const isRealtimeActive = ref(true)
const lastUpdated = ref(new Date())

// Chart data - from API
const hourlySales = ref([])
const topProducts = ref([])
const orderStatusCounts = ref(null)
const customerAnalyticsData = ref(null)

// Computed properties for change indicators
const revenueChange = computed(() => ({
  type: revenueGrowth.value >= 0 ? 'positive' : 'negative',
  icon: revenueGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(revenueGrowth.value).toFixed(1)
}))

const ordersChange = computed(() => ({
  type: ordersGrowth.value >= 0 ? 'positive' : 'negative',
  icon: ordersGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(ordersGrowth.value).toFixed(1)
}))

const customersChange = computed(() => ({
  type: customersGrowth.value >= 0 ? 'positive' : 'negative',
  icon: customersGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(customersGrowth.value).toFixed(1)
}))

const productsChange = computed(() => ({
  type: productsGrowth.value >= 0 ? 'positive' : 'negative',
  icon: productsGrowth.value >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down',
  value: Math.abs(productsGrowth.value).toFixed(1)
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
  } catch (error) {
    console.error('Error loading KPIs:', error)
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
    const response = await thongKeService.getBusinessInsights({ startDate, endDate })
    const data = response?.data ?? response
    
    averageOrderValue.value = Number(data.averageOrderValue || 0)
    refundRate.value = Number(data.refundRate || 0)
    profitMargin.value = Number(data.profitMargin || 0)
    returningCustomers.value = Number(data.returningCustomersRate || 0)
    customerLifetimeValue.value = Number(data.customerLifetimeValue || 0)
    averageRetentionDays.value = Number(data.averageRetentionDays || 0)
    topSellingProduct.value = data.topSellingProduct || 'N/A'
    topCategory.value = data.topCategory || 'N/A'
    inventoryTurnover.value = Number(data.inventoryTurnover || 0)
    
    // Load today's metrics from business insights
    todayRevenue.value = Number(data.todayRevenue || 0)
    todayOrders.value = Number(data.todayOrders || 0)
    todayCustomers.value = Number(data.todayCustomers || 0)
  } catch (error) {
    console.error('Error loading business insights:', error)
  } finally {
    isLoadingBusinessInsights.value = false
  }
}

const loadPerformanceMetrics = async () => {
  try {
    isLoadingPerformance.value = true
    const { startDate, endDate } = getDateRange()
    const response = await thongKeService.getPerformanceMetrics({ startDate, endDate })
    const data = response?.data ?? response
    
    conversionRate.value = Number(data.conversionRate || 0)
    cartAbandonmentRate.value = Number(data.cartAbandonmentRate || 0)
    customerSatisfaction.value = Number(data.customerSatisfaction || 0)
  } catch (error) {
    console.error('Error loading performance metrics:', error)
  } finally {
    isLoadingPerformance.value = false
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
    topProducts.value = (data || []).map(product => ({
      id: product.id,
      name: product.name,
      category: product.category || 'N/A',
      sales: product.sales,
      revenue: product.revenue,
      image: product.image || 'https://via.placeholder.com/40x40/3b82f6/ffffff?text=SP'
    }))
  } catch (error) {
    console.error('Error loading top products:', error)
  } finally {
    isLoadingTopProducts.value = false
  }
}

const loadOrderStatusCounts = async () => {
  try {
    const response = await thongKeService.getOrderStatusCounts()
    orderStatusCounts.value = response?.data ?? response
  } catch (error) {
    console.error('Error loading order status counts:', error)
  }
}

const loadCustomerAnalytics = async () => {
  try {
    const response = await thongKeService.getCustomerAnalytics({ type: customerAnalyticsType.value })
    customerAnalyticsData.value = response?.data ?? response
    console.log('Customer Analytics Data:', customerAnalyticsData.value)
  } catch (error) {
    console.error('Error loading customer analytics:', error)
    customerAnalyticsData.value = null
  }
}

onMounted(async () => {
  // Load all data
  await Promise.all([
    loadKpis(),
    loadHourlySales(),
    loadBusinessInsights(),
    loadPerformanceMetrics(),
    loadTopProducts(),
    loadOrderStatusCounts(),
    loadCustomerAnalytics()
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

// Generate comparison data based on mode
const getComparisonData = computed(() => {
  if (comparisonMode.value === 'none' || !hourlySales.value || hourlySales.value.length === 0) {
    return null
  }
  
  // Generate comparison data for selected date
  return hourlySales.value.map((hourData, index) => {
    // Calculate days difference from selected date
    const selectedDate = new Date(comparisonDate.value)
    const today = new Date()
    const daysDiff = Math.floor((today - selectedDate) / (1000 * 60 * 60 * 24))
    
    // Generate data for the selected date with realistic patterns
    let baseRevenue = 150000
    
    // Peak hours pattern (similar to current day but with variation)
    if (index >= 9 && index <= 11) baseRevenue = 1200000
    else if (index >= 14 && index <= 16) baseRevenue = 1400000
    else if (index >= 19 && index <= 21) baseRevenue = 1600000
    else if (index >= 22 || index <= 6) baseRevenue = 80000
    
    // Add some variation based on days difference (older data tends to be lower)
    const ageFactor = Math.max(0.5, 1 - (daysDiff * 0.05))
    baseRevenue = Math.floor(baseRevenue * ageFactor)
    
    // Add random variation
    const revenue = baseRevenue + Math.floor(Math.random() * baseRevenue * 0.3)
    const orders = Math.floor(revenue / 120000) + Math.floor(Math.random() * 4)
    const customers = Math.floor(orders * 0.6) + Math.floor(Math.random() * 2)
    
    return {
      hour: index,
      time: hourData.time,
      revenue: revenue,
      orders: orders,
      customers: customers
    }
  })
})

// Calculate comparison percentage
const comparisonPercentage = computed(() => {
  if (comparisonMode.value === 'none' || !hourlySales.value || !getComparisonData.value) {
    return null
  }
  
  const currentTotal = hourlySales.value.reduce((sum, hour) => {
    switch (selectedMetric.value) {
      case 'revenue': return sum + (hour.revenue || 0)
      case 'orders': return sum + (hour.orders || 0)
      case 'customers': return sum + (hour.customers || 0)
      case 'profit': return sum + Math.round((hour.revenue || 0) * 0.28)
      default: return sum + (hour.revenue || 0)
    }
  }, 0)
  
  const comparisonTotal = getComparisonData.value.reduce((sum, hour) => {
    if (!hour) return sum
    switch (selectedMetric.value) {
      case 'revenue': return sum + (hour.revenue || 0)
      case 'orders': return sum + (hour.orders || 0)
      case 'customers': return sum + (hour.customers || 0)
      case 'profit': return sum + Math.round((hour.revenue || 0) * 0.28)
      default: return sum + (hour.revenue || 0)
    }
  }, 0)
  
  if (comparisonTotal === 0) return 0
  
  return (((currentTotal - comparisonTotal) / comparisonTotal) * 100).toFixed(1)
})

// Computed properties
const metrics = computed(() => [
  { value: 'revenue', label: 'Doanh thu', icon: 'bi bi-currency-dollar' },
  { value: 'orders', label: 'Đơn hàng', icon: 'bi bi-bag' },
  { value: 'customers', label: 'Khách hàng', icon: 'bi bi-people' },
  { value: 'profit', label: 'Lợi nhuận', icon: 'bi bi-graph-up' }
])

const trendDirection = computed(() => 'positive')
const trendIcon = computed(() => 'bi bi-graph-up')
const trendText = computed(() => 'Tăng trưởng ổn định')
const peakValue = computed(() => 5200000)
const volatility = computed(() => 12.8)
const averageValue = computed(() => {
  if (!hourlySales.value || hourlySales.value.length === 0) return 0
  const total = hourlySales.value.reduce((sum, hour) => sum + (hour.revenue || 0), 0)
  return total / hourlySales.value.length
})

// Chart data
const mainChartData = computed(() => {
  if (!hourlySales.value || hourlySales.value.length === 0) {
    return {
      labels: [],
      datasets: []
    }
  }
  
  // Use hourly data for 24-hour chart
  const labels = hourlySales.value.map(hour => hour.time)
  const data = hourlySales.value.map(hour => {
    switch (selectedMetric.value) {
      case 'orders': return hour.orders || 0
      case 'customers': return hour.customers || 0
      case 'profit': return Math.round((hour.revenue || 0) * 0.28)
      default: return hour.revenue || 0
    }
  })
  
  const datasets = [{
    label: getMetricLabel(selectedMetric.value),
    data,
    borderColor: '#ef4444', // Red for today
    backgroundColor: 'transparent',
    borderWidth: 2,
    fill: false,
    tension: 0.3,
    pointBackgroundColor: '#ffffff',
    pointBorderColor: '#ef4444',
    pointBorderWidth: 2,
    pointRadius: 0,
    pointHoverRadius: 6,
    pointHoverBorderWidth: 2,
    pointStyle: 'circle'
  }]
  
  // Add comparison dataset if enabled
  if (comparisonMode.value !== 'none' && getComparisonData.value) {
    const comparisonData = getComparisonData.value.map(hour => {
      if (!hour) return 0
      switch (selectedMetric.value) {
        case 'orders': return hour.orders || 0
        case 'customers': return hour.customers || 0
        case 'profit': return Math.round((hour.revenue || 0) * 0.28)
        default: return hour.revenue || 0
      }
    })
    
    datasets.push({
      label: getComparisonLabel(),
      data: comparisonData,
      borderColor: '#0ea5e9', // Blue for comparison (yesterday)
      backgroundColor: 'transparent',
      borderWidth: 2,
      borderDash: [0, 0], // Solid line like in the image
      fill: false,
      tension: 0.3,
      pointBackgroundColor: '#ffffff',
      pointBorderColor: '#0ea5e9',
      pointBorderWidth: 2,
      pointRadius: 0,
      pointHoverRadius: 6,
      pointHoverBorderWidth: 2,
      pointStyle: 'circle'
    })
  }
  
  return { labels, datasets }
})

const mainChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index',
    intersect: false
  },
  plugins: {
    legend: {
      display: comparisonMode.value !== 'none',
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
      enabled: true,
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      titleColor: '#374151',
      bodyColor: '#6b7280',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      cornerRadius: 8,
      padding: 12,
      displayColors: true,
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
        family: "'Inter', sans-serif"
      },
      bodySpacing: 4,
      titleMarginBottom: 8,
      callbacks: {
        title: function(context) {
          return context[0].label
        },
        label: function(context) {
          const label = context.dataset.label || ''
          const value = formatMetricValue(context.parsed.y, selectedMetric.value)
          return label + ': ' + value
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
      border: {
        display: false
      },
      ticks: {
        font: {
          size: 10,
          family: "'Inter', -apple-system, sans-serif",
          weight: '400'
        },
        color: '#9ca3af',
        padding: 6,
        maxRotation: 0,
        autoSkip: false,
        maxTicksLimit: 24,
        stepSize: 1
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        color: 'rgba(156, 163, 175, 0.15)',
        drawBorder: false,
        lineWidth: 1
      },
      border: {
        display: false
      },
      ticks: {
        callback: function(value) {
          return formatMetricValue(value, selectedMetric.value)
        },
        font: {
          size: 10,
          family: "'Inter', -apple-system, sans-serif",
          weight: '400'
        },
        color: '#9ca3af',
        padding: 6,
        maxTicksLimit: 8,
        count: 8
      }
    }
  },
  layout: {
    padding: {
      top: 10,
      right: 10,
      bottom: 10,
      left: 10
    }
  },
  animation: {
    duration: 300,
    easing: 'easeInOut'
  },
  elements: {
    line: {
      tension: 0.3
    },
    point: {
      radius: 0,
      hitRadius: 20,
      hoverRadius: 6,
      hoverBorderWidth: 2
    }
  }
}))

const orderDistributionData = computed(() => {
  if (!orderStatusCounts.value) {
    return {
      labels: [],
      datasets: []
    }
  }
  
  if (orderDistributionType.value === 'status') {
    return {
      labels: ['Chờ xử lý', 'Đang giao', 'Hoàn thành'],
      datasets: [{
        data: [
          orderStatusCounts.value.pending || 0,
          orderStatusCounts.value.shipping || 0,
          orderStatusCounts.value.completed || 0
        ],
        backgroundColor: ['#f59e0b', '#8b5cf6', '#10b981'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    }
  } else if (orderDistributionType.value === 'payment') {
    // Mock data for payment - would need real API
    return {
      labels: ['COD', 'VNPay'],
      datasets: [{
        data: [650, 600],
        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    }
  } else if (orderDistributionType.value === 'shipping') {
    // Mock data for shipping - would need real API
    return {
      labels: ['Giao thành công', 'Đang giao', 'Chờ lấy hàng'],
      datasets: [{
        data: [780, 420, 350],
        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    }
  }
  
  return {
    labels: [],
    datasets: []
  }
})

const orderDistributionOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  cutout: '70%',
  plugins: {
    legend: {
      display: true,
      position: 'bottom',
      labels: {
        usePointStyle: true,
        pointStyle: 'circle',
        padding: 15,
        font: {
          size: 12,
          family: "'Inter', sans-serif",
          weight: '500'
        },
        color: '#1e293b',
        boxWidth: 10,
        boxHeight: 10,
        generateLabels: function(chart) {
          const data = chart.data
          if (data.labels.length && data.datasets.length) {
            const dataset = data.datasets[0]
            const total = dataset.data.reduce((sum, val) => sum + val, 0)
            
            return data.labels.map((label, i) => {
              const value = dataset.data[i]
              const percentage = ((value / total) * 100).toFixed(1)
              
              return {
                text: `${label}: ${value} (${percentage}%)`,
                fillStyle: dataset.backgroundColor[i],
                strokeStyle: dataset.backgroundColor[i],
                lineWidth: 0,
                hidden: false,
                index: i
              }
            })
          }
          return []
        }
      }
    },
    tooltip: {
      enabled: true,
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      titleColor: '#ffffff',
      bodyColor: '#e2e8f0',
      borderColor: '#475569',
      borderWidth: 1,
      cornerRadius: 12,
      padding: 16,
      displayColors: true,
      boxWidth: 12,
      boxHeight: 12,
      titleFont: {
        size: 14,
        weight: 'bold',
        family: "'Inter', sans-serif"
      },
      bodyFont: {
        size: 13,
        family: "'Inter', sans-serif"
      },
      callbacks: {
        title: function(context) {
          return '📊 ' + context[0].label
        },
        label: function(context) {
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((context.parsed / total) * 100).toFixed(1)
          return `Số lượng: ${context.parsed} đơn (${percentage}%)`
        },
        footer: function(context) {
          const total = context[0].dataset.data.reduce((sum, val) => sum + val, 0)
          return ['────────────', `📦 Tổng: ${total} đơn`]
        }
      }
    }
  },
  animation: {
    animateRotate: true,
    animateScale: true,
    duration: 1000,
    easing: 'easeInOutQuart'
  },
  elements: {
    arc: {
      borderWidth: 3,
      borderColor: '#ffffff',
      hoverBorderWidth: 4,
      hoverOffset: 12
    }
  }
}))

const customerAnalyticsChartData = computed(() => {
  if (!customerAnalyticsData.value) {
    return {
      labels: [],
      datasets: []
    }
  }
  
  // Handle different types
  if (customerAnalyticsType.value === 'segments') {
    return {
      labels: ['Khách mới', 'Khách cũ', 'VIP'],
      datasets: [{
        label: 'Số lượng khách hàng',
        data: [
          Number(customerAnalyticsData.value.newCustomers || 0),
          Number(customerAnalyticsData.value.returningCustomers || 0),
          Number(customerAnalyticsData.value.vipCustomers || 0)
        ],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b'],
        borderWidth: 0,
        borderRadius: 4
      }]
    }
  } else if (customerAnalyticsType.value === 'age') {
    return {
      labels: ['18-24', '25-34', '35-44', '45+'],
      datasets: [{
        label: 'Số lượng khách hàng',
        data: [
          Number(customerAnalyticsData.value.age18_24 || 0),
          Number(customerAnalyticsData.value.age25_34 || 0),
          Number(customerAnalyticsData.value.age35_44 || 0),
          Number(customerAnalyticsData.value.age45_plus || 0)
        ],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'],
        borderWidth: 0,
        borderRadius: 4
      }]
    }
  } else if (customerAnalyticsType.value === 'region') {
    return {
      labels: ['Miền Bắc', 'Miền Trung', 'Miền Nam'],
      datasets: [{
        label: 'Số lượng khách hàng',
        data: [
          Number(customerAnalyticsData.value.north || 0),
          Number(customerAnalyticsData.value.central || 0),
          Number(customerAnalyticsData.value.south || 0)
        ],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b'],
        borderWidth: 0,
        borderRadius: 4
      }]
    }
  }
  
  return {
    labels: [],
    datasets: []
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
      enabled: true,
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      titleColor: '#ffffff',
      bodyColor: '#e2e8f0',
      borderColor: '#475569',
      borderWidth: 1,
      cornerRadius: 12,
      padding: 16,
      displayColors: true,
      boxWidth: 12,
      boxHeight: 12,
      titleFont: {
        size: 14,
        weight: 'bold',
        family: "'Inter', sans-serif"
      },
      bodyFont: {
        size: 13,
        family: "'Inter', sans-serif"
      },
      callbacks: {
        title: function(context) {
          return '👥 ' + context[0].label
        },
        label: function(context) {
          const value = context.parsed.y
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((value / total) * 100).toFixed(1)
          
          return [
            `Số lượng: ${value.toLocaleString('vi-VN')} khách hàng`,
            `Tỷ lệ: ${percentage}%`
          ]
        },
        footer: function(context) {
          const total = context[0].dataset.data.reduce((sum, val) => sum + val, 0)
          return ['────────────', `📊 Tổng: ${total.toLocaleString('vi-VN')} khách`]
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      border: {
        display: false
      },
      ticks: {
        font: {
          size: 11,
          family: "'Inter', sans-serif",
          weight: '500'
        },
        color: '#64748b'
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        color: 'rgba(15, 23, 42, 0.08)',
        drawBorder: false
      },
      border: {
        display: false
      },
      ticks: {
        font: {
          size: 12,
          family: "'Inter', sans-serif",
          weight: '500'
        },
        color: '#64748b',
        callback: function(value) {
          return value.toLocaleString('vi-VN')
        }
      }
    }
  },
  animation: {
    duration: 750,
    easing: 'easeInOutQuart'
  },
  elements: {
    bar: {
      borderRadius: 8,
      borderSkipped: false
    }
  },
  barPercentage: 0.7,
  categoryPercentage: 0.8
}))

// Format helper functions
const formatCurrency = (value) => {
  if (!value || value === 0) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatNumber = (value) => {
  if (!value || value === 0) return '0'
  return new Intl.NumberFormat('vi-VN').format(value)
}

// Methods
const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const applyDateRange = async () => {
  // If custom is selected, show advanced filters panel
  if (selectedDateRange.value === 'custom') {
    showAdvancedFilters.value = true
    return
  }
  
  // Reload all data with new date range
  await refreshData()
}

const applyFilters = async () => {
  // If custom date range is selected, ensure dates are set
  if (selectedDateRange.value === 'custom') {
    if (!customStartDate.value || !customEndDate.value) {
      alert('Vui lòng chọn cả ngày bắt đầu và ngày kết thúc')
      return
    }
  }
  
  // Close advanced filters panel
  showAdvancedFilters.value = false
  
  // Reload all data with new filters
  await refreshData()
}

const resetFilters = () => {
  selectedCategory.value = ''
  selectedRegion.value = ''
  selectedCustomerType.value = ''
  selectedAgeGroup.value = ''
}

const exportReport = () => {
  // Export report functionality
}

const refreshData = async () => {
  lastUpdated.value = new Date()
  // Reload all data
  await Promise.all([
    loadKpis(),
    loadHourlySales(),
    loadBusinessInsights(),
    loadPerformanceMetrics(),
    loadTopProducts(),
    loadOrderStatusCounts(),
    loadCustomerAnalytics()
  ])
}

// Toggle function moved to later section

const toggleChartType = () => {
  chartType.value = chartType.value === 'line' ? 'bar' : 'line'
}

// exportChart moved to later section

const viewTopProducts = () => {
  // Navigate to all top products
}

// Real-time is always enabled - no toggle needed

const selectQuickDate = (option) => {
  const today = new Date()
  let targetDate = new Date()
  
  switch (option) {
    case 'yesterday':
      targetDate.setDate(today.getDate() - 1)
      break
    case '2days':
      targetDate.setDate(today.getDate() - 2)
      break
    case 'week':
      targetDate.setDate(today.getDate() - 7)
      break
  }
  
  comparisonDate.value = targetDate
  showComparisonDatePicker.value = false
}

const exportChart = () => {
  // Export chart functionality
  console.log('Exporting chart...')
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
    revenue: `rgba(99, 102, 241, ${alpha})`,      // Indigo - vibrant
    orders: `rgba(245, 158, 11, ${alpha})`,       // Amber
    customers: `rgba(16, 185, 129, ${alpha})`,    // Emerald
    profit: `rgba(239, 68, 68, ${alpha})`         // Red
  }
  return colors[metric] || `rgba(99, 102, 241, ${alpha})`
}

const getComparisonLabel = () => {
  const selectedDate = new Date(comparisonDate.value)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  // Check if it's yesterday
  if (selectedDate.toDateString() === yesterday.toDateString()) {
    return 'Hôm qua'
  }
  
  // Check if it's a specific date
  return `Ngày ${formatDate(selectedDate)}`
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
  getComparisonLabel
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 10px 40px rgba(0, 0, 0, 0.04);
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
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
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
  0%, 100% {
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 10px 40px rgba(0, 0, 0, 0.04);
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 10px 40px rgba(0, 0, 0, 0.04);
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
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 20px 60px rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 10px 40px rgba(0, 0, 0, 0.04);
  height: 100%;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.insight-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 20px 60px rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 10px 40px rgba(0, 0, 0, 0.04);
  height: 100%;
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 20px 60px rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1), 0 8px 16px rgba(0, 0, 0, 0.06);
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
  background: white;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 8px 30px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.metric-card-small:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 16px 50px rgba(0, 0, 0, 0.08);
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
  gap: 0.375rem;
  color: #10b981;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.625rem;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 12px;
}

.live-indicator i {
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.9);
  }
}

.metric-value {
  font-size: 1.75rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 0.375rem;
  letter-spacing: -0.02em;
  line-height: 1;
}

.metric-subtitle {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.025em;
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
  font-weight: 600;
  color: #0f172a;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.product-category {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.product-stats {
  text-align: right;
}

.product-sales {
  font-size: 0.8rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.product-revenue {
  font-weight: 700;
  color: #10b981;
  font-size: 0.95rem;
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
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
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
