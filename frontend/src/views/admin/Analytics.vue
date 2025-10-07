<template>
  <div class="admin-analytics">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Thống kê & Báo cáo</h1>
        <p class="page-subtitle">Phân tích dữ liệu kinh doanh và hiệu suất</p>
      </div>
      <div class="header-right">
        <div class="date-range-picker">
          <div class="input-group">
            <input type="date" class="form-control" v-model="startDate">
            <span class="input-group-text">đến</span>
            <input type="date" class="form-control" v-model="endDate">
            <button class="btn btn-auro-primary" @click="updateAnalytics">
              <i class="ph-arrow-clockwise me-1"></i>Cập nhật
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Key Metrics -->
    <div class="metrics-section">
      <div class="row g-4">
        <div class="col-md-3">
          <div class="metric-card">
            <div class="metric-icon bg-primary">
              <i class="ph-currency-dollar"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ formatCurrency(totalRevenue) }}</div>
              <div class="metric-label">Tổng doanh thu</div>
              <div class="metric-change positive">
                <i class="ph-trend-up"></i>
                +12.5% so với tháng trước
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card">
            <div class="metric-icon bg-success">
              <i class="ph-shopping-bag"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalOrders }}</div>
              <div class="metric-label">Tổng đơn hàng</div>
              <div class="metric-change positive">
                <i class="ph-trend-up"></i>
                +8.3% so với tháng trước
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card">
            <div class="metric-icon bg-warning">
              <i class="ph-users"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalCustomers }}</div>
              <div class="metric-label">Khách hàng mới</div>
              <div class="metric-change positive">
                <i class="ph-trend-up"></i>
                +15.2% so với tháng trước
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card">
            <div class="metric-icon bg-info">
              <i class="ph-package"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalProducts }}</div>
              <div class="metric-label">Sản phẩm bán ra</div>
              <div class="metric-change positive">
                <i class="ph-trend-up"></i>
                +6.7% so với tháng trước
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <div class="row g-4">
        <!-- Revenue Chart -->
        <div class="col-lg-8">
          <div class="chart-card">
            <div class="chart-header">
              <h5 class="chart-title">Doanh thu theo thời gian</h5>
              <div class="chart-controls">
                <div class="btn-group" role="group">
                  <button 
                    v-for="period in chartPeriods" 
                    :key="period.value"
                    :class="['btn', 'btn-sm', period.value === selectedPeriod ? 'btn-primary' : 'btn-outline-primary']"
                    @click="selectedPeriod = period.value"
                  >
                    {{ period.label }}
                  </button>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <Chart
                type="line"
                :data="revenueChartData"
                :options="revenueChartOptions"
                :height="300"
              />
            </div>
          </div>
        </div>

        <!-- Top Products -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h5 class="chart-title">Sản phẩm bán chạy</h5>
            </div>
            <div class="chart-content">
              <div class="top-products-list">
                <div v-for="(product, index) in topProducts" :key="product.id" class="product-item">
                  <div class="product-rank">{{ index + 1 }}</div>
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-sales">{{ product.sales }} đã bán</div>
                  </div>
                  <div class="product-revenue">{{ formatCurrency(product.revenue) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4 mt-4">
        <!-- Orders Status Chart -->
        <div class="col-lg-6">
          <div class="chart-card">
            <div class="chart-header">
              <h5 class="chart-title">Trạng thái đơn hàng</h5>
            </div>
            <div class="chart-content">
              <Chart
                type="doughnut"
                :data="orderStatusChartData"
                :options="orderStatusChartOptions"
                :height="300"
              />
            </div>
          </div>
        </div>

        <!-- Customer Growth -->
        <div class="col-lg-6">
          <div class="chart-card">
            <div class="chart-header">
              <h5 class="chart-title">Tăng trưởng khách hàng</h5>
            </div>
            <div class="chart-content">
              <Chart
                type="bar"
                :data="customerGrowthChartData"
                :options="customerGrowthChartOptions"
                :height="300"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reports Section -->
    <div class="reports-section">
      <div class="row g-4">
        <!-- Sales Report -->
        <div class="col-lg-6">
          <div class="report-card">
            <div class="report-header">
              <h5 class="report-title">Báo cáo bán hàng</h5>
              <button class="btn btn-sm btn-outline-primary" @click="exportReport('sales')">
                <i class="ph-download me-1"></i>Xuất báo cáo
              </button>
            </div>
            <div class="report-content">
              <div class="report-summary">
                <div class="summary-item">
                  <span class="summary-label">Doanh thu trung bình/đơn:</span>
                  <span class="summary-value">{{ formatCurrency(avgOrderValue) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Đơn hàng trung bình/ngày:</span>
                  <span class="summary-value">{{ avgOrdersPerDay }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Tỷ lệ chuyển đổi:</span>
                  <span class="summary-value">{{ conversionRate }}%</span>
                </div>
              </div>
              <div class="report-details">
                <h6>Chi tiết theo ngày</h6>
                <div class="table-responsive">
                  <table class="table table-sm">
                    <thead>
                      <tr>
                        <th>Ngày</th>
                        <th>Đơn hàng</th>
                        <th>Doanh thu</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="day in dailySales" :key="day.date">
                        <td>{{ formatDate(day.date) }}</td>
                        <td>{{ day.orders }}</td>
                        <td>{{ formatCurrency(day.revenue) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Customer Report -->
        <div class="col-lg-6">
          <div class="report-card">
            <div class="report-header">
              <h5 class="report-title">Báo cáo khách hàng</h5>
              <button class="btn btn-sm btn-outline-primary" @click="exportReport('customers')">
                <i class="ph-download me-1"></i>Xuất báo cáo
              </button>
            </div>
            <div class="report-content">
              <div class="report-summary">
                <div class="summary-item">
                  <span class="summary-label">Khách hàng VIP:</span>
                  <span class="summary-value">{{ vipCustomers }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Khách hàng thường:</span>
                  <span class="summary-value">{{ regularCustomers }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Tỷ lệ khách hàng mới:</span>
                  <span class="summary-value">{{ newCustomerRate }}%</span>
                </div>
              </div>
              <div class="report-details">
                <h6>Phân tích khách hàng</h6>
                <div class="customer-analysis">
                  <div class="analysis-item">
                    <div class="analysis-label">Chi tiêu trung bình:</div>
                    <div class="analysis-value">{{ formatCurrency(avgCustomerSpending) }}</div>
                  </div>
                  <div class="analysis-item">
                    <div class="analysis-label">Số đơn hàng trung bình:</div>
                    <div class="analysis-value">{{ avgOrdersPerCustomer }}</div>
                  </div>
                  <div class="analysis-item">
                    <div class="analysis-label">Tỷ lệ quay lại mua:</div>
                    <div class="analysis-value">{{ repeatPurchaseRate }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Export Options -->
    <div class="export-section">
      <div class="export-card">
        <div class="export-header">
          <h5 class="export-title">Xuất dữ liệu</h5>
          <p class="export-subtitle">Tải xuống báo cáo chi tiết</p>
        </div>
        <div class="export-content">
          <div class="row g-3">
            <div class="col-md-4">
              <div class="export-option">
                <div class="export-icon">
                  <i class="ph-file-csv"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo CSV</h6>
                  <p>Dữ liệu thô để phân tích</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('csv')">
                  <i class="ph-download me-1"></i>Tải xuống
                </button>
              </div>
            </div>
            <div class="col-md-4">
              <div class="export-option">
                <div class="export-icon">
                  <i class="ph-file-pdf"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo PDF</h6>
                  <p>Báo cáo định dạng đẹp</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('pdf')">
                  <i class="ph-download me-1"></i>Tải xuống
                </button>
              </div>
            </div>
            <div class="col-md-4">
              <div class="export-option">
                <div class="export-icon">
                  <i class="ph-file-excel"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo Excel</h6>
                  <p>Bảng tính với biểu đồ</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('excel')">
                  <i class="ph-download me-1"></i>Tải xuống
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Chart from '@/components/ui/Chart.vue'

// Reactive data
const startDate = ref(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0])
const endDate = ref(new Date().toISOString().split('T')[0])
const selectedPeriod = ref('month')

const chartPeriods = [
  { label: 'Tuần', value: 'week' },
  { label: 'Tháng', value: 'month' },
  { label: 'Quý', value: 'quarter' },
  { label: 'Năm', value: 'year' }
]

// Mock data
const totalRevenue = ref(125000000)
const totalOrders = ref(1250)
const totalCustomers = ref(450)
const totalProducts = ref(3200)

const orderStatus = ref({
  pending: 45,
  processing: 23,
  shipped: 67,
  delivered: 1115
})

const topProducts = ref([
  { id: 1, name: 'Áo sơ mi nam cao cấp', sales: 125, revenue: 56250000 },
  { id: 2, name: 'Quần âu nam', sales: 98, revenue: 63700000 },
  { id: 3, name: 'Áo khoác nam', sales: 76, revenue: 64600000 },
  { id: 4, name: 'Áo thun nam', sales: 145, revenue: 36250000 },
  { id: 5, name: 'Quần jean nam', sales: 89, revenue: 53400000 }
])

const newCustomers = ref(45)
const returningCustomers = ref(405)
const retentionRate = ref(85.2)

const avgOrderValue = ref(100000)
const avgOrdersPerDay = ref(42)
const conversionRate = ref(3.2)

const vipCustomers = ref(25)
const regularCustomers = ref(425)
const newCustomerRate = ref(10.0)

const avgCustomerSpending = ref(277778)
const avgOrdersPerCustomer = ref(2.8)
const repeatPurchaseRate = ref(78.5)

// Chart data
const revenueChartData = computed(() => ({
  labels: dailySales.value.map(day => formatDate(day.date)),
  datasets: [{
    label: 'Doanh thu',
    data: dailySales.value.map(day => day.revenue),
    borderColor: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    borderWidth: 3,
    fill: true,
    tension: 0.4,
    pointBackgroundColor: '#ffffff',
    pointBorderColor: '#3b82f6',
    pointBorderWidth: 3,
    pointRadius: 5,
    pointHoverRadius: 7,
    pointHoverBackgroundColor: '#ffffff',
    pointHoverBorderColor: '#1d4ed8',
    pointHoverBorderWidth: 4
  }]
}))

const revenueChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: function(context) {
          return 'Doanh thu: ' + formatCurrency(context.parsed.y)
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function(value) {
          return formatCurrency(value)
        }
      }
    }
  }
}))

const orderStatusChartData = computed(() => ({
  labels: ['Chờ xử lý', 'Đang xử lý', 'Đã giao', 'Hoàn thành'],
  datasets: [{
    data: [
      orderStatus.value.pending,
      orderStatus.value.processing,
      orderStatus.value.shipped,
      orderStatus.value.delivered
    ],
    backgroundColor: [
      'rgba(245, 158, 11, 0.8)',
      'rgba(59, 130, 246, 0.8)',
      'rgba(16, 185, 129, 0.8)',
      'rgba(34, 197, 94, 0.8)'
    ],
    borderWidth: 3,
    borderColor: '#ffffff',
    hoverBackgroundColor: [
      'rgba(245, 158, 11, 1)',
      'rgba(59, 130, 246, 1)',
      'rgba(16, 185, 129, 1)',
      'rgba(34, 197, 94, 1)'
    ],
    hoverBorderWidth: 4,
    spacing: 2
  }]
}))

const orderStatusChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        padding: 20,
        usePointStyle: true
      }
    },
    tooltip: {
      callbacks: {
        label: function(context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0)
          const percentage = ((context.parsed / total) * 100).toFixed(1)
          return context.label + ': ' + context.parsed + ' (' + percentage + '%)'
        }
      }
    }
  }
}))

const customerGrowthChartData = computed(() => ({
  labels: ['Khách hàng mới', 'Khách hàng quay lại', 'Tỷ lệ giữ chân (%)'],
  datasets: [{
    label: 'Số lượng',
    data: [newCustomers.value, returningCustomers.value, retentionRate.value],
    backgroundColor: [
      'rgba(34, 197, 94, 0.8)',
      'rgba(59, 130, 246, 0.8)',
      'rgba(245, 158, 11, 0.8)'
    ],
    borderWidth: 2,
    borderColor: '#ffffff',
    borderRadius: 8,
    borderSkipped: false,
    hoverBackgroundColor: [
      'rgba(21, 128, 61, 0.9)',
      'rgba(37, 99, 235, 0.9)',
      'rgba(180, 83, 9, 0.9)'
    ],
    hoverBorderWidth: 3
  }]
}))

const customerGrowthChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: function(context) {
          if (context.label === 'Tỷ lệ giữ chân (%)') {
            return context.label + ': ' + context.parsed.y + '%'
          }
          return context.label + ': ' + context.parsed.y + ' người'
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        stepSize: 1
      }
    }
  }
}))

const dailySales = ref([
  { date: new Date('2024-01-15'), orders: 35, revenue: 3500000 },
  { date: new Date('2024-01-16'), orders: 42, revenue: 4200000 },
  { date: new Date('2024-01-17'), orders: 38, revenue: 3800000 },
  { date: new Date('2024-01-18'), orders: 45, revenue: 4500000 },
  { date: new Date('2024-01-19'), orders: 52, revenue: 5200000 },
  { date: new Date('2024-01-20'), orders: 48, revenue: 4800000 },
  { date: new Date('2024-01-21'), orders: 41, revenue: 4100000 }
])

// Methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(date)
}

const updateAnalytics = () => {
  console.log('Updating analytics for period:', startDate.value, 'to', endDate.value)
  // Implement analytics update logic
}

const exportReport = (type) => {
  console.log('Exporting report:', type)
  // Implement report export logic
}

const exportData = (format) => {
  console.log('Exporting data in format:', format)
  // Implement data export logic
}

// Lifecycle
onMounted(() => {
  console.log('Analytics page loaded')
})
</script>

<style scoped>
.admin-analytics {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
  margin: 0;
}

.date-range-picker .input-group {
  max-width: 400px;
}

/* Metrics Section */
.metrics-section {
  margin-bottom: 2rem;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.metric-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.metric-change {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.metric-change.positive {
  color: #28a745;
}

.metric-change.negative {
  color: #dc3545;
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

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.chart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.chart-content {
  min-height: 300px;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #6c757d;
}

.status-legend {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.growth-stats {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.growth-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.growth-label {
  color: #6c757d;
}

.growth-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Top Products */
.top-products-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.product-rank {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.product-sales {
  font-size: 0.8rem;
  color: #6c757d;
}

.product-revenue {
  font-weight: 600;
  color: #28a745;
}

/* Reports Section */
.reports-section {
  margin-bottom: 2rem;
}

.report-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.report-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.report-summary {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-label {
  color: #6c757d;
  font-weight: 500;
}

.summary-value {
  font-weight: 600;
  color: #2c3e50;
}

.report-details h6 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.table {
  margin-bottom: 0;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: #2c3e50;
  padding: 0.75rem;
}

.table td {
  padding: 0.75rem;
}

.customer-analysis {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.analysis-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.analysis-label {
  color: #6c757d;
  font-weight: 500;
}

.analysis-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Export Section */
.export-section {
  margin-bottom: 2rem;
}

.export-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.export-header {
  text-align: center;
  margin-bottom: 2rem;
}

.export-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.export-subtitle {
  color: #6c757d;
  margin: 0;
}

.export-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.export-option:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.export-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.export-info {
  flex: 1;
}

.export-info h6 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.export-info p {
  font-size: 0.8rem;
  color: #6c757d;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .date-range-picker .input-group {
    max-width: 100%;
  }
  
  .metric-card {
    flex-direction: column;
    text-align: center;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .report-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .export-option {
    flex-direction: column;
    text-align: center;
  }
  
  .summary-item,
  .analysis-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
