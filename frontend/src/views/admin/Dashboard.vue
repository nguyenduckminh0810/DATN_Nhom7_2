<template>
  <div class="admin-dashboard">
    <!-- Top Navigation Bar -->
    <div class="dashboard-navbar">
      <div class="navbar-left">
        <div class="search-bar">
          <i class="ph ph-magnifying-glass"></i>
          <input type="text" placeholder="Tìm kiếm..." class="search-input">
        </div>
      </div>
      <div class="navbar-right">
        <button class="nav-icon">
          <i class="ph ph-gear"></i>
        </button>
        <button class="nav-icon">
          <i class="ph ph-shopping-cart"></i>
          <span class="notification-badge">1</span>
        </button>
        <button class="nav-icon">
          <i class="ph ph-grid-four"></i>
        </button>
        <button class="nav-icon">
          <i class="ph ph-bell"></i>
        </button>
        <div class="user-avatar">
          <img src="https://via.placeholder.com/40x40/3b82f6/ffffff?text=J" alt="User" class="avatar-img">
        </div>
      </div>
    </div>

    <!-- Welcome Section -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">Chào buổi chiều, Admin!</h1>
        <p class="welcome-subtitle">Đây là tình hình hoạt động của cửa hàng AURO hôm nay</p>
        <div class="today-metrics">
          <div class="metric-item">
            <span class="metric-label">Lượt truy cập hôm nay</span>
            <span class="metric-value">1,247</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">Doanh thu hôm nay</span>
            <span class="metric-value">15,420,000₫</span>
          </div>
        </div>
      </div>
      <div class="welcome-illustration">
        <div class="store-illustration">
          <div class="store-building"></div>
          <div class="store-tree"></div>
        </div>
      </div>
    </div>

    <!-- Actionable Alerts -->
    <div class="alerts-section">
      <div class="alert-item">
        <div class="alert-content">
          <span class="alert-bullet">•</span>
          <span class="alert-text">3 sản phẩm chưa được đăng lên Facebook</span>
        </div>
        <button class="alert-action">Xem sản phẩm ></button>
      </div>
      <div class="alert-item">
        <div class="alert-content">
          <span class="alert-bullet">•</span>
          <span class="alert-text">5 đơn hàng cần xác nhận thanh toán</span>
        </div>
        <button class="alert-action">Xem thanh toán ></button>
      </div>
      <div class="alert-item">
        <div class="alert-content">
          <span class="alert-bullet">•</span>
          <span class="alert-text">12 đơn hàng cần xử lý giao hàng</span>
        </div>
        <button class="alert-action">Xem đơn hàng ></button>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="kpi-section">
      <div class="kpi-grid">
        <div class="kpi-card" v-for="kpi in kpiData" :key="kpi.id">
          <div class="kpi-header">
            <h3 class="kpi-title">{{ kpi.title }}</h3>
          </div>
          <div class="kpi-content">
            <div class="kpi-current">{{ kpi.current }}</div>
            <div class="kpi-previous">Previous: {{ kpi.previous }}</div>
            <div class="kpi-change" :class="kpi.changeType">
              <i :class="kpi.changeIcon"></i>
              {{ kpi.change }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Total Sales Chart -->
    <div class="chart-section">
      <div class="chart-card large">
        <div class="chart-header">
          <h3 class="chart-title">Tổng doanh thu</h3>
          <div class="chart-controls">
            <label class="chart-checkbox">
              <input type="checkbox" checked>
              <span class="checkmark"></span>
              Tháng trước: 125,420,000₫
            </label>
            <label class="chart-checkbox">
              <input type="checkbox" checked>
              <span class="checkmark"></span>
              Năm trước: 98,750,000₫
            </label>
          </div>
        </div>
        <div class="chart-content">
          <Chart
            type="line"
            :data="totalSalesChartData"
            :options="totalSalesChartOptions"
            :height="300"
          />
        </div>
      </div>
    </div>

    <!-- Mini Charts Section -->
    <div class="mini-charts-section">
      <div class="mini-chart-card">
        <div class="mini-chart-header">
          <h4 class="mini-chart-title">Doanh thu tuần</h4>
        </div>
        <div class="mini-chart-content">
          <div class="mini-chart-value">47.2M₫</div>
          <div class="mini-chart-change positive">+3.5%</div>
          <div class="mini-chart-visual">
            <Chart
              type="bar"
              :data="weeklySalesData"
              :options="miniChartOptions"
              :height="60"
            />
          </div>
        </div>
      </div>

      <div class="mini-chart-card">
        <div class="mini-chart-header">
          <h4 class="mini-chart-title">Thị phần sản phẩm</h4>
        </div>
        <div class="mini-chart-content">
          <div class="mini-chart-value">34.6%</div>
          <div class="mini-chart-change positive">▲ 3.5%</div>
          <div class="mini-chart-target">Mục tiêu: 55%</div>
          <div class="mini-chart-visual">
            <Chart
              type="doughnut"
              :data="productShareData"
              :options="miniDoughnutOptions"
              :height="60"
            />
          </div>
        </div>
      </div>

      <div class="mini-chart-card">
        <div class="mini-chart-header">
          <h4 class="mini-chart-title">Thị trường</h4>
        </div>
        <div class="mini-chart-content">
          <div class="mini-chart-value">26M₫</div>
          <div class="mini-chart-visual">
            <Chart
              type="doughnut"
              :data="marketShareData"
              :options="marketShareOptions"
              :height="60"
            />
          </div>
        </div>
      </div>

      <div class="mini-chart-card">
        <div class="mini-chart-header">
          <h4 class="mini-chart-title">Tổng đơn hàng</h4>
        </div>
        <div class="mini-chart-content">
          <div class="mini-chart-value">1,247</div>
          <div class="mini-chart-change positive">▲ 13.6%</div>
          <div class="mini-chart-visual">
            <Chart
              type="line"
              :data="totalOrderData"
              :options="miniChartOptions"
              :height="60"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Shopping Cart Metrics -->
    <div class="cart-metrics-section">
      <div class="cart-metrics-card">
        <div class="cart-metrics-header">
          <h4 class="cart-metrics-title">Chỉ số giỏ hàng</h4>
        </div>
        <div class="cart-metrics-content">
          <div class="cart-metric-item" v-for="metric in cartMetrics" :key="metric.id">
            <div class="cart-metric-info">
              <div class="cart-metric-label">{{ metric.label }}</div>
              <div class="cart-metric-change" :class="metric.changeType">
                {{ metric.change }}
              </div>
              <div class="cart-metric-count">{{ metric.count }}</div>
            </div>
            <div class="cart-metric-progress">
              <div class="progress-bar" :class="metric.progressClass">
                <div class="progress-fill" :style="{ width: metric.progress + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Purchases Table -->
    <div class="recent-purchases-section">
      <div class="recent-purchases-card">
        <div class="recent-purchases-header">
          <h4 class="recent-purchases-title">Giao dịch gần đây</h4>
          <div class="recent-purchases-actions">
            <button class="btn btn-primary btn-sm">+ Mới</button>
            <button class="btn btn-outline-secondary btn-sm">▼ Lọc</button>
            <button class="btn btn-outline-secondary btn-sm">Xuất</button>
          </div>
        </div>
        <div class="recent-purchases-content">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th><input type="checkbox" class="form-check-input"></th>
                  <th>Khách hàng</th>
                  <th>Email</th>
                  <th>Sản phẩm</th>
                  <th>Thanh toán</th>
                  <th>Số tiền</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="purchase in recentPurchases" :key="purchase.id">
                  <td><input type="checkbox" class="form-check-input"></td>
                  <td>
                    <div class="customer-info">
                      <div class="customer-avatar">
                        <img :src="purchase.avatar" :alt="purchase.customer" class="avatar-img">
                      </div>
                      <span class="customer-name">{{ purchase.customer }}</span>
                    </div>
                  </td>
                  <td>{{ purchase.email }}</td>
                  <td>{{ purchase.product }}</td>
                  <td>
                    <span class="payment-status" :class="purchase.paymentClass">
                      {{ purchase.paymentStatus }}
                    </span>
                  </td>
                  <td>{{ purchase.amount }}</td>
                  <td>
                    <button class="btn btn-sm btn-outline-secondary">
                      <i class="ph ph-dots-three"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="table-pagination">
            <div class="pagination-info">1 đến 5 của 5</div>
            <div class="pagination-controls">
              <button class="btn btn-sm btn-outline-secondary">Trước</button>
              <button class="btn btn-sm btn-outline-secondary">Tiếp</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import Chart from '@/components/ui/Chart.vue'

// Reactive data
const kpiData = ref([
  {
    id: 1,
    title: 'Đơn hàng',
    current: '1,247',
    previous: '1,156',
    change: '7.9%',
    changeType: 'positive',
    changeIcon: 'ph ph-trend-up'
  },
  {
    id: 2,
    title: 'Sản phẩm bán',
    current: '2,856',
    previous: '2,634',
    change: '8.4%',
    changeType: 'positive',
    changeIcon: 'ph ph-trend-up'
  },
  {
    id: 3,
    title: 'Hoàn trả',
    current: '2,450,000₫',
    previous: '2,890,000₫',
    change: '-15.2%',
    changeType: 'negative',
    changeIcon: 'ph ph-trend-down'
  },
  {
    id: 4,
    title: 'Doanh thu gộp',
    current: '125,420,000₫',
    previous: '118,750,000₫',
    change: '5.6%',
    changeType: 'positive',
    changeIcon: 'ph ph-trend-up'
  },
  {
    id: 5,
    title: 'Vận chuyển',
    current: '8,750,000₫',
    previous: '9,200,000₫',
    change: '-4.9%',
    changeType: 'negative',
    changeIcon: 'ph ph-trend-down'
  },
  {
    id: 6,
    title: 'Đang xử lý',
    current: '23',
    previous: '31',
    change: '-25.8%',
    changeType: 'negative',
    changeIcon: 'ph ph-trend-down'
  }
])

const cartMetrics = ref([
  {
    id: 1,
    label: 'Khởi tạo giỏ hàng',
    change: '▲ 12.5%',
    count: '1,247',
    progress: 78,
    changeType: 'positive',
    progressClass: 'progress-blue'
  },
  {
    id: 2,
    label: 'Tỷ lệ bỏ giỏ',
    change: '▲ 8.3%',
    count: '156 of 1,247',
    progress: 12,
    changeType: 'negative',
    progressClass: 'progress-red'
  },
  {
    id: 3,
    label: 'Tỷ lệ thoát',
    change: '▲ 5.2%',
    count: '89 of 1,247',
    progress: 7,
    changeType: 'negative',
    progressClass: 'progress-red'
  },
  {
    id: 4,
    label: 'Tỷ lệ hoàn thành',
    change: '▼ 3.1%',
    count: '1,002 of 1,247',
    progress: 80,
    changeType: 'negative',
    progressClass: 'progress-red'
  },
  {
    id: 5,
    label: 'Tỷ lệ doanh thu',
    change: '▲ 15.7%',
    count: '1,002 of 1,247',
    progress: 80,
    changeType: 'positive',
    progressClass: 'progress-green'
  }
])

const recentPurchases = ref([
  {
    id: 1,
    customer: 'Nguyễn Văn An',
    email: 'nguyenvanan@gmail.com',
    product: 'Áo sơ mi nam cao cấp AURO',
    paymentStatus: 'Thành công ✓',
    paymentClass: 'success',
    amount: '1,250,000₫',
    avatar: 'https://via.placeholder.com/32x32/3b82f6/ffffff?text=NA'
  },
  {
    id: 2,
    customer: 'Trần Thị Bình',
    email: 'tranthibinh@yahoo.com',
    product: 'Quần âu nam công sở',
    paymentStatus: 'Bị chặn 🚫',
    paymentClass: 'blocked',
    amount: '890,000₫',
    avatar: 'https://via.placeholder.com/32x32/ef4444/ffffff?text=TB'
  },
  {
    id: 3,
    customer: 'Lê Minh Cường',
    email: 'leminhcuong@outlook.com',
    product: 'Áo khoác nam dạ cao cấp',
    paymentStatus: 'Chờ xử lý =',
    paymentClass: 'pending',
    amount: '2,450,000₫',
    avatar: 'https://via.placeholder.com/32x32/f59e0b/ffffff?text=LC'
  },
  {
    id: 4,
    customer: 'Phạm Thị Dung',
    email: 'phamthidung@gmail.com',
    product: 'Vest nam công sở',
    paymentStatus: 'Thành công ✓',
    paymentClass: 'success',
    amount: '3,200,000₫',
    avatar: 'https://via.placeholder.com/32x32/10b981/ffffff?text=PD'
  },
  {
    id: 5,
    customer: 'Hoàng Văn Em',
    email: 'hoangvanem@hotmail.com',
    product: 'Áo polo nam thể thao',
    paymentStatus: 'Thành công ✓',
    paymentClass: 'success',
    amount: '650,000₫',
    avatar: 'https://via.placeholder.com/32x32/8b5cf6/ffffff?text=HE'
  }
])

// Chart data
const totalSalesChartData = computed(() => ({
  labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN', 'T2', 'T3', 'T4', 'T5'],
  datasets: [
    {
      label: 'Tháng trước',
      data: [125, 142, 138, 161, 155, 168, 172, 165, 158, 163, 159],
      borderColor: '#3b82f6',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      borderWidth: 2,
      fill: false,
      tension: 0.4
    },
    {
      label: 'Năm trước',
      data: [98, 112, 108, 121, 115, 128, 132, 125, 118, 123, 119],
      borderColor: '#f59e0b',
      backgroundColor: 'rgba(245, 158, 11, 0.1)',
      borderWidth: 2,
      fill: false,
      tension: 0.4
    }
  ]
}))

const totalSalesChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      max: 150,
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

const weeklySalesData = computed(() => ({
  labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
  datasets: [{
    data: [12, 19, 15, 25, 22, 18, 16],
    backgroundColor: 'rgba(59, 130, 246, 0.8)',
    borderColor: '#3b82f6',
    borderWidth: 1,
    borderRadius: 4
  }]
}))

const productShareData = computed(() => ({
  labels: ['Hiện tại', 'Còn lại'],
  datasets: [{
    data: [34.6, 65.4],
    backgroundColor: ['#3b82f6', 'rgba(59, 130, 246, 0.2)'],
    borderWidth: 0
  }]
}))

const marketShareData = computed(() => ({
  labels: ['Áo sơ mi', 'Quần âu', 'Áo khoác'],
  datasets: [{
    data: [40, 35, 25],
    backgroundColor: ['#3b82f6', '#f59e0b', '#ef4444'],
    borderWidth: 0
  }]
}))

const totalOrderData = computed(() => ({
  labels: ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4'],
  datasets: [{
    data: [312, 419, 315, 425],
    borderColor: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    borderWidth: 2,
    fill: true,
    tension: 0.4,
    pointRadius: 0
  }]
}))

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

const miniDoughnutOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  cutout: '70%'
}))

const marketShareOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'bottom',
      labels: {
        usePointStyle: true,
        padding: 10,
        font: {
          size: 10
        }
      }
    }
  },
  cutout: '60%'
}))

onMounted(() => {
  // Initialize dashboard
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
  background: #f8fafc;
  min-height: 100vh;
}

/* Top Navigation Bar */
.dashboard-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.navbar-left {
  flex: 1;
}

.search-bar {
  position: relative;
  max-width: 400px;
}

.search-bar i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  font-size: 16px;
}

.search-input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: #f9fafb;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-icon {
  position: relative;
  width: 40px;
  height: 40px;
  border: none;
  background: #f8fafc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
}

.nav-icon:hover {
  background: #e2e8f0;
  color: #374151;
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e2e8f0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Welcome Section */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin: 0;
}

.welcome-content {
  flex: 1;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: white;
}

.welcome-subtitle {
  font-size: 1.1rem;
  margin: 0 0 2rem 0;
  opacity: 0.9;
}

.today-metrics {
  display: flex;
  gap: 2rem;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.metric-label {
  font-size: 0.875rem;
  opacity: 0.8;
}

.metric-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.welcome-illustration {
  flex-shrink: 0;
}

.store-illustration {
  position: relative;
  width: 200px;
  height: 120px;
}

.store-building {
  width: 120px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  position: absolute;
  bottom: 0;
  left: 20px;
}

.store-tree {
  width: 40px;
  height: 60px;
  background: rgba(34, 197, 94, 0.3);
  border-radius: 20px 20px 0 0;
  position: absolute;
  bottom: 0;
  right: 20px;
}

/* Alerts Section */
.alerts-section {
  padding: 1.5rem 2rem;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f1f5f9;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.alert-bullet {
  color: #3b82f6;
  font-weight: bold;
}

.alert-text {
  color: #374151;
  font-size: 0.875rem;
}

.alert-action {
  background: none;
  border: none;
  color: #3b82f6;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.alert-action:hover {
  background: #f0f9ff;
}

/* KPI Section */
.kpi-section {
  padding: 2rem;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.kpi-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.kpi-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #64748b;
  margin: 0 0 1rem 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.kpi-current {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.kpi-previous {
  font-size: 0.75rem;
  color: #9ca3af;
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
  color: #059669;
}

.kpi-change.negative {
  color: #dc2626;
}

/* Chart Section */
.chart-section {
  padding: 0 2rem 2rem 2rem;
}

.chart-card.large {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.chart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.chart-controls {
  display: flex;
  gap: 1rem;
}

.chart-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #374151;
  cursor: pointer;
}

.chart-checkbox input[type="checkbox"] {
  margin: 0;
}

/* Mini Charts Section */
.mini-charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  padding: 0 2rem 2rem 2rem;
}

.mini-chart-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.mini-chart-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #64748b;
  margin: 0 0 1rem 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mini-chart-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mini-chart-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
}

.mini-chart-change {
  font-size: 0.875rem;
  font-weight: 600;
}

.mini-chart-change.positive {
  color: #059669;
}

.mini-chart-change.negative {
  color: #dc2626;
}

.mini-chart-target {
  font-size: 0.75rem;
  color: #9ca3af;
}

.mini-chart-visual {
  height: 60px;
  margin-top: 0.5rem;
}

/* Cart Metrics Section */
.cart-metrics-section {
  padding: 0 2rem 2rem 2rem;
}

.cart-metrics-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.cart-metrics-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1.5rem 0;
}

.cart-metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #f1f5f9;
}

.cart-metric-item:last-child {
  border-bottom: none;
}

.cart-metric-info {
  flex: 1;
}

.cart-metric-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.25rem;
}

.cart-metric-change {
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.cart-metric-change.positive {
  color: #059669;
}

.cart-metric-change.negative {
  color: #dc2626;
}

.cart-metric-count {
  font-size: 0.75rem;
  color: #9ca3af;
}

.cart-metric-progress {
  width: 200px;
  margin-left: 1rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-blue .progress-fill {
  background: #3b82f6;
}

.progress-green .progress-fill {
  background: #10b981;
}

.progress-red .progress-fill {
  background: #ef4444;
}

/* Recent Purchases Section */
.recent-purchases-section {
  padding: 0 2rem 2rem 2rem;
}

.recent-purchases-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.recent-purchases-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.recent-purchases-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.recent-purchases-actions {
  display: flex;
  gap: 0.5rem;
}

.recent-purchases-content {
  padding: 1.5rem;
}

.table {
  margin: 0;
}

.table th {
  border-top: none;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  padding: 0.75rem;
}

.table td {
  border-top: 1px solid #f1f5f9;
  padding: 0.75rem;
  vertical-align: middle;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.customer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
}

.customer-name {
  font-weight: 500;
  color: #374151;
}

.payment-status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.payment-status.success {
  background: #dcfce7;
  color: #166534;
}

.payment-status.blocked {
  background: #fee2e2;
  color: #991b1b;
}

.payment-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.table-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  font-size: 0.875rem;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  gap: 0.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard-navbar {
    padding: 1rem;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 2rem;
  }
  
  .today-metrics {
    justify-content: center;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .mini-charts-section {
    grid-template-columns: 1fr;
  }
  
  .chart-controls {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .recent-purchases-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .recent-purchases-actions {
    justify-content: center;
  }
}
</style>