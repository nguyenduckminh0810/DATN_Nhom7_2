<template>
  <div class="admin-analytics">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Thống kê & Báo cáo</h1>
        <p class="page-subtitle">Phân tích dữ liệu kinh doanh và hiệu suất</p>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
            <i class="bi bi-funnel me-1"></i>Bộ lọc nâng cao
            <i :class="showAdvancedFilters ? 'bi bi-caret-up' : 'bi bi-caret-down'" class="ms-1"></i>
          </button>
          <button class="btn btn-outline-success" @click="exportReport">
            <i class="bi bi-download me-1"></i>Xuất báo cáo
          </button>
          <button class="btn btn-outline-secondary" @click="refreshData">
            <i class="bi bi-arrow-clockwise me-1"></i>Làm mới
          </button>
        </div>
      </div>
    </div>

    <!-- Advanced Filters -->
    <div v-if="showAdvancedFilters" class="advanced-filters-section">
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label">Khoảng thời gian</label>
          <div class="input-group">
            <input type="date" class="form-control" v-model="startDate">
            <span class="input-group-text">đến</span>
            <input type="date" class="form-control" v-model="endDate">
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">Tần suất</label>
          <select class="form-select" v-model="chartGranularity">
            <option value="daily">Hàng ngày</option>
            <option value="weekly">Hàng tuần</option>
            <option value="monthly">Hàng tháng</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label">So sánh</label>
          <select class="form-select" v-model="comparisonPeriod">
            <option value="previous">Kỳ trước</option>
            <option value="last-year">Cùng kỳ năm trước</option>
            <option value="none">Không so sánh</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">Loại báo cáo</label>
          <select class="form-select" v-model="reportType">
            <option value="overview">Tổng quan</option>
            <option value="sales">Bán hàng</option>
            <option value="customers">Khách hàng</option>
            <option value="products">Sản phẩm</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label">Thao tác</label>
          <button class="btn btn-primary w-100" @click="applyFilters">
            <i class="bi bi-search me-1"></i>Áp dụng
          </button>
        </div>
      </div>
    </div>

    <!-- Real-time Status -->
    <div class="realtime-status-section">
      <div class="row g-3">
        <div class="col-md-8">
          <div class="realtime-indicator">
            <div class="status-dot" :class="{ active: isRealtimeActive }"></div>
            <span class="status-text">
              {{ isRealtimeActive ? 'Đang cập nhật real-time' : 'Chế độ tĩnh' }}
            </span>
            <button class="btn btn-sm btn-outline-primary ms-2" @click="toggleRealtime">
              {{ isRealtimeActive ? 'Tắt' : 'Bật' }} real-time
            </button>
          </div>
        </div>
        <div class="col-md-4">
          <div class="last-updated">
            <i class="bi bi-clock me-1"></i>
            Cập nhật lần cuối: {{ lastUpdated }}
          </div>
        </div>
      </div>
    </div>

    <!-- Key Metrics -->
    <div class="metrics-section">
      <div class="row g-4">
        <div class="col-md-3">
          <div class="metric-card revenue">
            <div class="metric-icon">
              <i class="bi bi-currency-dollar"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ formatCurrency(totalRevenue) }}</div>
              <div class="metric-label">Tổng doanh thu</div>
              <div class="metric-change" :class="revenueChange.type">
                <i :class="revenueChange.icon"></i>
                {{ revenueChange.value }}% so với {{ comparisonPeriod === 'previous' ? 'kỳ trước' : 'cùng kỳ năm trước' }}
              </div>
              <div class="metric-detail">
                <span class="detail-label">TB/ngày:</span>
                <span class="detail-value">{{ formatCurrency(dailyAverageRevenue) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card orders">
            <div class="metric-icon">
              <i class="bi bi-bag"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalOrders }}</div>
              <div class="metric-label">Tổng đơn hàng</div>
              <div class="metric-change" :class="ordersChange.type">
                <i :class="ordersChange.icon"></i>
                {{ ordersChange.value }}% so với {{ comparisonPeriod === 'previous' ? 'kỳ trước' : 'cùng kỳ năm trước' }}
              </div>
              <div class="metric-detail">
                <span class="detail-label">TB/ngày:</span>
                <span class="detail-value">{{ dailyAverageOrders }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card customers">
            <div class="metric-icon">
              <i class="bi bi-people"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalCustomers }}</div>
              <div class="metric-label">Khách hàng mới</div>
              <div class="metric-change" :class="customersChange.type">
                <i :class="customersChange.icon"></i>
                {{ customersChange.value }}% so với {{ comparisonPeriod === 'previous' ? 'kỳ trước' : 'cùng kỳ năm trước' }}
              </div>
              <div class="metric-detail">
                <span class="detail-label">Tỷ lệ chuyển đổi:</span>
                <span class="detail-value">{{ conversionRate }}%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="metric-card products">
            <div class="metric-icon">
              <i class="bi bi-box"></i>
            </div>
            <div class="metric-content">
              <div class="metric-value">{{ totalProducts }}</div>
              <div class="metric-label">Sản phẩm bán ra</div>
              <div class="metric-change" :class="productsChange.type">
                <i :class="productsChange.icon"></i>
                {{ productsChange.value }}% so với {{ comparisonPeriod === 'previous' ? 'kỳ trước' : 'cùng kỳ năm trước' }}
              </div>
              <div class="metric-detail">
                <span class="detail-label">TB/đơn:</span>
                <span class="detail-value">{{ averageItemsPerOrder }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Additional Insights -->
    <div class="insights-section">
      <div class="row g-4">
        <div class="col-md-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6 class="insight-title">Hiệu suất kinh doanh</h6>
              <i class="bi bi-graph-up insight-icon"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Giá trị đơn hàng TB:</span>
                <span class="insight-value">{{ formatCurrency(averageOrderValue) }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Tỷ lệ hoàn trả:</span>
                <span class="insight-value">{{ refundRate }}%</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Tỷ suất lợi nhuận:</span>
                <span class="insight-value">{{ profitMargin }}%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6 class="insight-title">Xu hướng khách hàng</h6>
              <i class="bi bi-people-three insight-icon"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Khách hàng trở lại:</span>
                <span class="insight-value">{{ returningCustomers }}%</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Lifetime Value:</span>
                <span class="insight-value">{{ formatCurrency(customerLifetimeValue) }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Thời gian giữ chân TB:</span>
                <span class="insight-value">{{ averageRetentionDays }} ngày</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="insight-card">
            <div class="insight-header">
              <h6 class="insight-title">Hiệu quả sản phẩm</h6>
              <i class="bi bi-box insight-icon"></i>
            </div>
            <div class="insight-content">
              <div class="insight-item">
                <span class="insight-label">Sản phẩm bán chạy nhất:</span>
                <span class="insight-value">{{ topSellingProduct }}</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Tỷ lệ tồn kho:</span>
                <span class="insight-value">{{ inventoryTurnover }}%</span>
              </div>
              <div class="insight-item">
                <span class="insight-label">Danh mục phổ biến:</span>
                <span class="insight-value">{{ topCategory }}</span>
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
          <div class="chart-card">
            <div class="chart-header">
              <div class="chart-title-section">
                <h5 class="chart-title">Doanh thu & Hiệu suất</h5>
                <div class="chart-metrics">
                  <span class="metric-badge">
                    <i class="bi bi-graph-up"></i>
                    +{{ revenueGrowth }}% tăng trưởng
                  </span>
                </div>
              </div>
              <div class="chart-controls">
                <div class="chart-tabs">
                  <button 
                    v-for="metric in chartMetrics" 
                    :key="metric.value"
                    :class="['tab-btn', { active: selectedMetric === metric.value }]"
                    @click="selectedMetric = metric.value"
                  >
                    <i :class="metric.icon"></i>
                    {{ metric.label }}
                  </button>
                </div>
                <div class="chart-actions">
                  <button class="btn btn-sm btn-outline-primary" @click="toggleChartType">
                    <i :class="chartType === 'line' ? 'bi bi-graph-up' : 'bi bi-bar-chart'"></i>
                    {{ chartType === 'line' ? 'Cột' : 'Đường' }}
                  </button>
                  <button class="btn btn-sm btn-outline-secondary" @click="exportChart">
                    <i class="bi bi-download"></i>
                  </button>
                </div>
              </div>
            </div>
            <div class="chart-content">
              <Chart
                type="line"
                :data="dynamicChartData"
                :options="dynamicChartOptions"
                :height="350"
              />
              <div class="chart-insights">
                <div class="insight-item">
                  <span class="insight-label">Xu hướng:</span>
                  <span :class="['insight-value', getTrendAnalysis().direction]">
                    <i :class="getTrendAnalysis().direction === 'positive' ? 'bi bi-graph-up' : 'bi bi-graph-down'"></i>
                    {{ getTrendAnalysis().percentage }}% ({{ getTrendAnalysis().strength }})
                  </span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Điểm cao nhất:</span>
                  <span class="insight-value">{{ formatCurrency(getPerformanceInsights().bestDay?.revenue || 0) }}</span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Biến động:</span>
                  <span class="insight-value">{{ getPerformanceInsights().volatility }}%</span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Trung bình:</span>
                  <span class="insight-value">{{ formatCurrency(Math.round(getAverageValue())) }}</span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Tăng trưởng:</span>
                  <span :class="['insight-value', getPerformanceInsights().growthRate >= 0 ? 'positive' : 'negative']">
                    <i :class="getPerformanceInsights().growthRate >= 0 ? 'bi bi-graph-up' : 'bi bi-graph-down'"></i>
                    {{ getPerformanceInsights().growthRate >= 0 ? '+' : '' }}{{ getPerformanceInsights().growthRate }}%
                  </span>
                </div>
                <div class="insight-item">
                  <span class="insight-label">Ngày tốt nhất:</span>
                  <span class="insight-value">{{ getPerformanceInsights().bestDay ? formatDate(getPerformanceInsights().bestDay.date) : 'N/A' }}</span>
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
                <h6 class="metric-title">Hôm nay</h6>
                <span class="live-indicator">
                  <i class="bi bi-heart-pulse"></i>
                  LIVE
                </span>
              </div>
              <div class="metric-stats">
                <div class="stat-row">
                  <span class="stat-label">Doanh thu:</span>
                  <span class="stat-value">{{ formatCurrency(todayRevenue) }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">Đơn hàng:</span>
                  <span class="stat-value">{{ todayOrders }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">Khách mới:</span>
                  <span class="stat-value">{{ todayCustomers }}</span>
                </div>
              </div>
            </div>
            
            <div class="chart-card">
              <div class="chart-header">
                <h6 class="chart-title">Top sản phẩm</h6>
                <button class="btn btn-sm btn-outline-primary" @click="viewTopProducts">
                  Xem tất cả
                </button>
              </div>
              <div class="chart-content">
                <div class="top-products-list">
                  <div v-for="(product, index) in topProducts" :key="product.id" class="product-item">
                    <div class="product-rank">{{ index + 1 }}</div>
                    <div class="product-image">
                      <img :src="product.image" :alt="product.name" class="product-img">
                    </div>
                    <div class="product-info">
                      <div class="product-name">{{ product.name }}</div>
                      <div class="product-category">{{ product.category }}</div>
                      <div class="product-sales">{{ product.sales }} đã bán</div>
                    </div>
                    <div class="product-revenue">{{ formatCurrency(product.revenue) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4 mt-4">
        <!-- Advanced Analytics -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Phân bố đơn hàng</h6>
              <div class="chart-controls">
                <select class="form-select form-select-sm" v-model="orderDistributionType">
                  <option value="status">Theo trạng thái</option>
                  <option value="payment">Theo thanh toán</option>
                  <option value="shipping">Theo vận chuyển</option>
                </select>
              </div>
            </div>
            <div class="chart-content">
              <Chart
                type="doughnut"
                :data="orderDistributionData"
                :options="orderDistributionOptions"
                :height="250"
              />
            </div>
          </div>
        </div>

        <!-- Customer Analytics -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Phân tích khách hàng</h6>
              <div class="chart-controls">
                <select class="form-select form-select-sm" v-model="customerAnalyticsType">
                  <option value="segments">Phân khúc</option>
                  <option value="behavior">Hành vi</option>
                  <option value="geography">Địa lý</option>
                </select>
              </div>
            </div>
            <div class="chart-content">
              <Chart
                type="bar"
                :data="customerAnalyticsData"
                :options="customerAnalyticsOptions"
                :height="250"
              />
            </div>
          </div>
        </div>

        <!-- Performance Metrics -->
        <div class="col-lg-4">
          <div class="chart-card">
            <div class="chart-header">
              <h6 class="chart-title">Hiệu suất kinh doanh</h6>
            </div>
            <div class="chart-content">
              <div class="performance-metrics">
                <div class="performance-item">
                  <div class="performance-label">Conversion Rate</div>
                  <div class="performance-value">{{ conversionRate }}%</div>
                  <div class="performance-progress">
                    <div class="progress-bar" :style="{ width: conversionRate + '%' }"></div>
                  </div>
                </div>
                <div class="performance-item">
                  <div class="performance-label">Cart Abandonment</div>
                  <div class="performance-value">{{ cartAbandonmentRate }}%</div>
                  <div class="performance-progress">
                    <div class="progress-bar" :style="{ width: cartAbandonmentRate + '%' }"></div>
                  </div>
                </div>
                <div class="performance-item">
                  <div class="performance-label">Customer Satisfaction</div>
                  <div class="performance-value">{{ customerSatisfaction }}%</div>
                  <div class="performance-progress">
                    <div class="progress-bar" :style="{ width: customerSatisfaction + '%' }"></div>
                  </div>
                </div>
                <div class="performance-item">
                  <div class="performance-label">Inventory Turnover</div>
                  <div class="performance-value">{{ inventoryTurnover }}%</div>
                  <div class="performance-progress">
                    <div class="progress-bar" :style="{ width: Math.min(inventoryTurnover, 100) + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Detailed Report Section -->
    <div class="detailed-report-section">
      <div class="row g-4">
        <div class="col-12">
          <div class="report-summary-card">
            <div class="report-header">
              <h5 class="report-title">Báo cáo chi tiết</h5>
              <div class="report-actions">
                <button class="btn btn-outline-primary btn-sm" @click="generateDetailedReport">
                  <i class="bi bi-file-text me-1"></i>Tạo báo cáo
                </button>
                <button class="btn btn-outline-success btn-sm" @click="exportReport('detailed')">
                  <i class="bi bi-download me-1"></i>Xuất báo cáo
                </button>
              </div>
            </div>
            <div class="report-content">
              <div class="row g-3">
                <div class="col-md-3">
                  <div class="report-metric">
                    <div class="metric-label">Tổng doanh thu</div>
                    <div class="metric-value">{{ formatCurrency(totalRevenue) }}</div>
                    <div class="metric-change" :class="revenueChange.type">
                      <i :class="revenueChange.icon"></i>
                      {{ revenueChange.value }}% so với kỳ trước
                    </div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="report-metric">
                    <div class="metric-label">Tổng đơn hàng</div>
                    <div class="metric-value">{{ totalOrders.toLocaleString('vi-VN') }}</div>
                    <div class="metric-change" :class="ordersChange.type">
                      <i :class="ordersChange.icon"></i>
                      {{ ordersChange.value }}% so với kỳ trước
                    </div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="report-metric">
                    <div class="metric-label">Khách hàng mới</div>
                    <div class="metric-value">{{ totalCustomers.toLocaleString('vi-VN') }}</div>
                    <div class="metric-change" :class="customersChange.type">
                      <i :class="customersChange.icon"></i>
                      {{ customersChange.value }}% so với kỳ trước
                    </div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="report-metric">
                    <div class="metric-label">Tỷ lệ chuyển đổi</div>
                    <div class="metric-value">{{ conversionRate }}%</div>
                    <div class="metric-change positive">
                      <i class="bi bi-graph-up"></i>
                      +0.8% so với kỳ trước
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="report-insights mt-4">
                <h6 class="insights-title">Insights quan trọng</h6>
                <div class="insights-grid">
                  <div class="insight-card-small">
                    <div class="insight-icon positive">
                      <i class="bi bi-graph-up"></i>
                    </div>
                    <div class="insight-content">
                      <div class="insight-title">Xu hướng tích cực</div>
                      <div class="insight-description">
                        Doanh thu tăng {{ getPerformanceInsights().growthRate }}% trong {{ dailySales.value.length }} ngày qua
                      </div>
                    </div>
                  </div>
                  <div class="insight-card-small">
                    <div class="insight-icon warning">
                      <i class="bi bi-exclamation-triangle"></i>
                    </div>
                    <div class="insight-content">
                      <div class="insight-title">Biến động cao</div>
                      <div class="insight-description">
                        Biến động doanh thu {{ getPerformanceInsights().volatility }}% - cần theo dõi sát sao
                      </div>
                    </div>
                  </div>
                  <div class="insight-card-small">
                    <div class="insight-icon info">
                      <i class="bi bi-lightbulb"></i>
                    </div>
                    <div class="insight-content">
                      <div class="insight-title">Cơ hội tăng trưởng</div>
                      <div class="insight-description">
                        {{ getPerformanceInsights().bestDay ? formatDate(getPerformanceInsights().bestDay.date) : 'N/A' }} là ngày có doanh thu cao nhất
                      </div>
                    </div>
                  </div>
                  <div class="insight-card-small">
                    <div class="insight-icon success">
                      <i class="bi bi-check-circle"></i>
                    </div>
                    <div class="insight-content">
                      <div class="insight-title">Hiệu suất tốt</div>
                      <div class="insight-description">
                        Tỷ lệ chuyển đổi {{ conversionRate }}% vượt mục tiêu 3.5%
                      </div>
                    </div>
                  </div>
                </div>
              </div>
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
                <i class="bi bi-download me-1"></i>Xuất báo cáo
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
                <i class="bi bi-download me-1"></i>Xuất báo cáo
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
                  <i class="bi bi-file-csv"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo CSV</h6>
                  <p>Dữ liệu thô để phân tích</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('csv')">
                  <i class="bi bi-download me-1"></i>Tải xuống
                </button>
              </div>
            </div>
            <div class="col-md-4">
              <div class="export-option">
                <div class="export-icon">
                  <i class="bi bi-file-pdf"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo PDF</h6>
                  <p>Báo cáo định dạng đẹp</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('pdf')">
                  <i class="bi bi-download me-1"></i>Tải xuống
                </button>
              </div>
            </div>
            <div class="col-md-4">
              <div class="export-option">
                <div class="export-icon">
                  <i class="bi bi-file-excel"></i>
                </div>
                <div class="export-info">
                  <h6>Báo cáo Excel</h6>
                  <p>Bảng tính với biểu đồ</p>
                </div>
                <button class="btn btn-outline-primary btn-sm" @click="exportData('excel')">
                  <i class="bi bi-download me-1"></i>Tải xuống
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
import Chart from '@/components/admin/Chart.vue'

// Reactive data
const startDate = ref(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0])
const endDate = ref(new Date().toISOString().split('T')[0])
const selectedPeriod = ref('month')
const showAdvancedFilters = ref(false)
const chartGranularity = ref('daily')
const comparisonPeriod = ref('previous')
const reportType = ref('overview')
const isRealtimeActive = ref(true)
const lastUpdated = ref(new Date().toLocaleTimeString('vi-VN'))
const chartType = ref('line')
const selectedMetric = ref('revenue')
const orderDistributionType = ref('status')
const customerAnalyticsType = ref('segments')

const chartMetrics = [
  { label: 'Doanh thu', value: 'revenue', icon: 'bi bi-currency-dollar' },
  { label: 'Đơn hàng', value: 'orders', icon: 'bi bi-bag' },
  { label: 'Khách hàng', value: 'customers', icon: 'bi bi-people' },
  { label: 'Lợi nhuận', value: 'profit', icon: 'bi bi-graph-up-up' }
]

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
  { 
    id: 1, 
    name: 'Áo sơ mi nam cao cấp', 
    sales: 125, 
    revenue: 56250000,
    image: 'https://via.placeholder.com/40/3498db/ffffff?text=AS',
    category: 'Áo sơ mi'
  },
  { 
    id: 2, 
    name: 'Quần âu nam', 
    sales: 98, 
    revenue: 63700000,
    image: 'https://via.placeholder.com/40/2ecc71/ffffff?text=QA',
    category: 'Quần âu'
  },
  { id: 3, name: 'Áo khoác nam', sales: 76, revenue: 64600000 },
  { id: 4, name: 'Áo thun nam', sales: 145, revenue: 36250000 },
  { id: 5, name: 'Quần jean nam', sales: 89, revenue: 53400000 }
])

const newCustomers = ref(45)
const returningCustomers = ref(405)
const retentionRate = ref(85.2)

const avgOrderValue = ref(100000)
const avgOrdersPerDay = ref(42)

const vipCustomers = ref(25)
const regularCustomers = ref(425)
const newCustomerRate = ref(10.0)

const avgCustomerSpending = ref(277778)
const avgOrdersPerCustomer = ref(2.8)
const repeatPurchaseRate = ref(78.5)

// Additional metrics
const todayRevenue = ref(4500000)
const todayOrders = ref(45)
const todayCustomers = ref(12)
const revenueGrowth = ref(12.5)
const averageOrderValue = ref(100000)
const refundRate = ref(3.2)
const profitMargin = ref(28.5)
const customerLifetimeValue = ref(850000)
const averageRetentionDays = ref(180)
const topSellingProduct = ref('Áo sơ mi nam cao cấp')
const topCategory = ref('Áo sơ mi')
const cartAbandonmentRate = ref(68.5)
const customerSatisfaction = ref(92.3)

// Change calculations
const revenueChange = computed(() => ({
  value: 12.5,
  type: 'positive',
  icon: 'bi bi-graph-up'
}))

const ordersChange = computed(() => ({
  value: 8.3,
  type: 'positive',
  icon: 'bi bi-graph-up'
}))

const customersChange = computed(() => ({
  value: 15.2,
  type: 'positive',
  icon: 'bi bi-graph-up'
}))

const productsChange = computed(() => ({
  value: 6.7,
  type: 'positive',
  icon: 'bi bi-graph-up'
}))

// Additional computed properties
const dailyAverageRevenue = computed(() => Math.round(totalRevenue.value / 30))
const dailyAverageOrders = computed(() => Math.round(totalOrders.value / 30))
const averageItemsPerOrder = computed(() => Math.round(totalProducts.value / totalOrders.value))
const conversionRate = computed(() => 4.2)
const inventoryTurnover = computed(() => 85)

// Chart insights
const trendDirection = computed(() => 'positive')
const trendIcon = computed(() => 'bi bi-graph-up')
const trendText = computed(() => 'Tăng trưởng ổn định')
const peakValue = computed(() => 8500000)
const volatility = computed(() => 12.8)

// Dynamic chart data with detailed analytics
const dynamicChartData = computed(() => {
  if (!dailySales.value || dailySales.value.length === 0) {
    return {
      labels: [],
      datasets: []
    }
  }
  
  const baseData = dailySales.value.map(day => {
    switch (selectedMetric.value) {
      case 'revenue': return day.revenue || 0
      case 'orders': return day.orders || Math.round((day.revenue || 0) / 100000)
      case 'customers': return day.customers || Math.round(((day.orders || Math.round((day.revenue || 0) / 100000)) * 0.3))
      case 'profit': return day.profit || Math.round((day.revenue || 0) * 0.28)
      default: return day.revenue || 0
    }
  })
  const labels = dailySales.value.map(day => formatDate(day.date))
  
  // Add comparison data if selected
  const comparisonData = comparisonPeriod.value !== 'none' ? 
    dailySales.value.map(day => {
      const comparisonValue = baseData[Math.max(0, baseData.indexOf(baseData[baseData.length - 1]) - 7)] || baseData[0]
      return comparisonValue * (0.8 + Math.random() * 0.4) // Simulate comparison data
    }) : null

  const datasets = [{
    label: getMetricLabel(selectedMetric.value),
    data: baseData,
    borderColor: getMetricColor(selectedMetric.value),
    backgroundColor: getMetricColor(selectedMetric.value, 0.1),
    borderWidth: 3,
    fill: true,
    tension: 0.4,
    pointBackgroundColor: '#ffffff',
    pointBorderColor: getMetricColor(selectedMetric.value),
    pointBorderWidth: 3,
    pointRadius: 6,
    pointHoverRadius: 8,
    pointHoverBackgroundColor: '#ffffff',
    pointHoverBorderColor: getMetricColor(selectedMetric.value),
    pointHoverBorderWidth: 4
  }]

  // Add comparison dataset if comparison is enabled
  if (comparisonData) {
    datasets.push({
      label: comparisonPeriod.value === 'previous' ? `${getMetricLabel(selectedMetric.value)} (Kỳ trước)` : `${getMetricLabel(selectedMetric.value)} (Năm trước)`,
      data: comparisonData,
      borderColor: getMetricColor(selectedMetric.value, 0.6),
      backgroundColor: getMetricColor(selectedMetric.value, 0.05),
      borderWidth: 2,
      borderDash: [5, 5],
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      pointHoverRadius: 6,
      pointBackgroundColor: '#ffffff',
      pointBorderColor: getMetricColor(selectedMetric.value, 0.6)
    })
  }

  return { labels, datasets }
})

const dynamicChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: comparisonPeriod.value !== 'none',
      position: 'top',
      labels: {
        usePointStyle: true,
        padding: 20,
        font: {
          size: 12,
          family: 'Inter, sans-serif'
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
      displayColors: true,
      intersect: false,
      mode: 'index',
      callbacks: {
        title: function(context) {
          return 'Ngày: ' + context[0].label
        },
        label: function(context) {
          const value = formatMetricValue(context.parsed.y, selectedMetric.value)
          const change = context.datasetIndex === 0 && comparisonPeriod.value !== 'none' ? 
            calculateChange(context.parsed.y, context.data) : null
          return context.dataset.label + ': ' + value + 
            (change ? ` (${change > 0 ? '+' : ''}${change}%)` : '')
        },
        afterLabel: function(context) {
          if (context.datasetIndex === 0) {
            const dayData = dailySales.value[context.dataIndex]
            return [
              `Đơn hàng: ${dayData.orders || Math.round(context.parsed.y / 100000)}`,
              `Khách hàng: ${dayData.customers || Math.round((dayData.orders || context.parsed.y / 100000) * 0.3)}`,
              `Lợi nhuận: ${formatCurrency(dayData.profit || Math.round(context.parsed.y * 0.28))}`
            ]
          }
          return []
        }
      }
    },
    annotation: {
      annotations: {
        line1: {
          type: 'line',
          mode: 'horizontal',
          scaleID: 'y',
          value: getAverageValue(),
          borderColor: '#ef4444',
          borderWidth: 2,
          borderDash: [5, 5],
          label: {
            content: 'Trung bình: ' + formatMetricValue(getAverageValue(), selectedMetric.value),
            enabled: true,
            position: 'end'
          }
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: true,
        color: 'rgba(0, 0, 0, 0.05)',
        drawBorder: false
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
        color: 'rgba(0, 0, 0, 0.05)',
        drawBorder: false
      },
      ticks: {
        callback: function(value) {
          return formatMetricValue(value, selectedMetric.value)
        },
        font: {
          size: 11
        },
        color: '#6b7280',
        padding: 10
      }
    }
  },
  interaction: {
    intersect: false,
    mode: 'index'
  },
  elements: {
    point: {
      hoverBackgroundColor: '#ffffff',
      hoverBorderWidth: 4
    }
  }
}))

// Order distribution data
const orderDistributionData = computed(() => {
  switch (orderDistributionType.value) {
    case 'status':
      return {
        labels: ['Đã giao', 'Đang giao', 'Đang xử lý', 'Chờ xử lý'],
        datasets: [{
          data: [orderStatus.value.delivered, orderStatus.value.shipped, orderStatus.value.processing, orderStatus.value.pending],
          backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444']
        }]
      }
    case 'payment':
      return {
        labels: ['Thành công', 'Chờ thanh toán', 'Thất bại'],
        datasets: [{
          data: [1100, 100, 50],
          backgroundColor: ['#10b981', '#f59e0b', '#ef4444']
        }]
      }
    case 'shipping':
      return {
        labels: ['Giao hàng nhanh', 'Giao hàng tiêu chuẩn', 'Tự lấy hàng'],
        datasets: [{
          data: [600, 500, 150],
          backgroundColor: ['#3b82f6', '#10b981', '#8b5cf6']
        }]
      }
    default:
      return {}
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
      displayColors: true,
      callbacks: {
        title: function(context) {
          return 'Phân bố đơn hàng'
        },
        label: function(context) {
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((context.parsed / total) * 100).toFixed(1)
          return context.label + ': ' + context.parsed + ' đơn (' + percentage + '%)'
        },
        afterLabel: function(context) {
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((context.parsed / total) * 100).toFixed(1)
          
          if (orderDistributionType.value === 'status') {
            const statusInfo = {
              'Đã giao': { color: '#10b981', description: 'Đơn hàng đã giao thành công' },
              'Đang giao': { color: '#3b82f6', description: 'Đơn hàng đang trong quá trình vận chuyển' },
              'Đang xử lý': { color: '#f59e0b', description: 'Đơn hàng đang được xử lý' },
              'Chờ xử lý': { color: '#ef4444', description: 'Đơn hàng chờ xử lý' }
            }
            return statusInfo[context.label]?.description || ''
          }
          
          return ''
        }
      }
    },
    datalabels: {
      display: true,
      color: '#ffffff',
      font: {
        weight: 'bold',
        size: 11
      },
      formatter: function(value, context) {
        const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
        const percentage = ((value / total) * 100).toFixed(1)
        return percentage + '%'
      }
    }
  },
  elements: {
    arc: {
      borderWidth: 2,
      borderColor: '#ffffff'
    }
  }
}))

// Customer analytics data
const customerAnalyticsData = computed(() => {
  switch (customerAnalyticsType.value) {
    case 'segments':
      return {
        labels: ['VIP', 'Thường xuyên', 'Mới', 'Tiềm năng'],
        datasets: [{
          label: 'Số lượng khách hàng',
          data: [45, 120, 180, 105],
          backgroundColor: ['#f59e0b', '#10b981', '#3b82f6', '#8b5cf6']
        }]
      }
    case 'behavior':
      return {
        labels: ['Mua hàng thường xuyên', 'Mua theo mùa', 'Mua một lần', 'Không hoạt động'],
        datasets: [{
          label: 'Số lượng',
          data: [85, 95, 140, 130],
          backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#6b7280']
        }]
      }
    case 'geography':
      return {
        labels: ['TP.HCM', 'Hà Nội', 'Đà Nẵng', 'Khác'],
        datasets: [{
          label: 'Khách hàng',
          data: [200, 120, 80, 50],
          backgroundColor: ['#ef4444', '#3b82f6', '#10b981', '#8b5cf6']
        }]
      }
    default:
      return {}
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
      displayColors: true,
      callbacks: {
        title: function(context) {
          return 'Phân tích khách hàng'
        },
        label: function(context) {
          const total = context.dataset.data.reduce((sum, val) => sum + val, 0)
          const percentage = ((context.parsed.y / total) * 100).toFixed(1)
          
          let description = ''
          if (customerAnalyticsType.value === 'segments') {
            const segmentInfo = {
              'VIP': 'Khách hàng VIP với giá trị đơn hàng cao',
              'Thường xuyên': 'Khách hàng mua hàng thường xuyên',
              'Mới': 'Khách hàng mới đăng ký',
              'Tiềm năng': 'Khách hàng có tiềm năng phát triển'
            }
            description = segmentInfo[context.label] || ''
          }
          
          return [
            context.label + ': ' + context.parsed.y + ' khách hàng (' + percentage + '%)',
            description
          ]
        }
      }
    },
    datalabels: {
      display: true,
      color: '#374151',
      font: {
        weight: 'bold',
        size: 10
      },
      anchor: 'end',
      align: 'top',
      formatter: function(value, context) {
        return value.toLocaleString('vi-VN')
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: true,
        color: 'rgba(0, 0, 0, 0.05)',
        drawBorder: false
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
        color: 'rgba(0, 0, 0, 0.05)',
        drawBorder: false
      },
      ticks: {
        font: {
          size: 11
        },
        color: '#6b7280',
        callback: function(value) {
          return value.toLocaleString('vi-VN')
        }
      }
    }
  },
  elements: {
    bar: {
      borderRadius: 4,
      borderSkipped: false
    }
  }
}))

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
  // Implement analytics update logic
}

const exportReport = (type) => {
  // Implement report export logic
}

const exportData = (format) => {
  // Implement data export logic
}

// New methods for enhanced analytics
const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const applyFilters = () => {
  // Apply filters with date range
    granularity: chartGranularity.value,
    comparison: comparisonPeriod.value,
    reportType: reportType.value
  })
  // Implement filter application logic
}

const toggleRealtime = () => {
  isRealtimeActive.value = !isRealtimeActive.value
  if (isRealtimeActive.value) {
    startRealtimeUpdates()
  } else {
    stopRealtimeUpdates()
  }
}

const refreshData = () => {
  lastUpdated.value = new Date().toLocaleTimeString('vi-VN')
  // Implement data refresh logic
}

const toggleChartType = () => {
  chartType.value = chartType.value === 'line' ? 'bar' : 'line'
}

const exportChart = () => {
  // Implement chart export logic
}

const viewTopProducts = () => {
  // Navigate to products page with filter
}

const generateDetailedReport = () => {
  const reportData = {
    period: {
      start: startDate.value,
      end: endDate.value,
      granularity: chartGranularity.value
    },
    metrics: {
      revenue: {
        total: totalRevenue.value,
        average: Math.round(getAverageValue()),
        growth: getPerformanceInsights().growthRate,
        volatility: getPerformanceInsights().volatility,
        bestDay: getPerformanceInsights().bestDay,
        worstDay: getPerformanceInsights().worstDay
      },
      orders: {
        total: totalOrders.value,
        average: dailyAverageOrders.value,
        growth: ordersChange.value
      },
      customers: {
        total: totalCustomers.value,
        new: totalCustomers.value,
        growth: customersChange.value,
        segments: customerAnalyticsData.value
      },
      performance: {
        conversionRate: conversionRate.value,
        averageOrderValue: averageOrderValue.value,
        profitMargin: profitMargin.value,
        inventoryTurnover: inventoryTurnover.value
      }
    },
    insights: getTrendAnalysis(),
    charts: {
      revenue: dynamicChartData.value,
      orders: orderDistributionData.value,
      customers: customerAnalyticsData.value
    },
    generatedAt: new Date().toISOString(),
    generatedBy: 'Admin Dashboard'
  }
  
  // Here you would typically send this data to a backend service to generate a PDF/Excel report
  
  // For demo purposes, show success message
  alert('Báo cáo chi tiết đã được tạo thành công!')
}

const startRealtimeUpdates = () => {
  // Implement real-time data updates
}

const stopRealtimeUpdates = () => {
  // Stop real-time data updates
}

// Helper methods
const getMetricLabel = (metric) => {
  const labels = {
    revenue: 'Doanh thu',
    orders: 'Đơn hàng',
    customers: 'Khách hàng',
    profit: 'Lợi nhuận'
  }
  return labels[metric] || metric
}

const getMetricColor = (metric, alpha = 1) => {
  const colors = {
    revenue: `rgba(59, 130, 246, ${alpha})`,
    orders: `rgba(16, 185, 129, ${alpha})`,
    customers: `rgba(139, 92, 246, ${alpha})`,
    profit: `rgba(245, 158, 11, ${alpha})`
  }
  return colors[metric] || `rgba(107, 114, 128, ${alpha})`
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

// Helper methods for detailed analytics
const getAverageValue = () => {
  if (!dailySales.value || dailySales.value.length === 0) return 0
  const currentData = dailySales.value.map(day => {
    switch (selectedMetric.value) {
      case 'revenue': return day.revenue || 0
      case 'orders': return day.orders || Math.round((day.revenue || 0) / 100000)
      case 'customers': return day.customers || Math.round(((day.orders || Math.round((day.revenue || 0) / 100000)) * 0.3))
      case 'profit': return day.profit || Math.round((day.revenue || 0) * 0.28)
      default: return day.revenue || 0
    }
  })
  return currentData.length > 0 ? currentData.reduce((sum, val) => sum + val, 0) / currentData.length : 0
}

const calculateChange = (currentValue, dataArray) => {
  const previousValue = dataArray[dataArray.length - 2] || dataArray[0]
  return Math.round(((currentValue - previousValue) / previousValue) * 100)
}

const getTrendAnalysis = () => {
  if (!dailySales.value || dailySales.value.length === 0) {
    return {
      direction: 'positive',
      percentage: '0.0',
      strength: 'yếu'
    }
  }
  
  const currentData = dailySales.value.map(day => day.revenue || 0)
  if (currentData.length < 2) {
    return {
      direction: 'positive',
      percentage: '0.0',
      strength: 'yếu'
    }
  }
  
  const firstHalf = currentData.slice(0, Math.floor(currentData.length / 2))
  const secondHalf = currentData.slice(Math.floor(currentData.length / 2))
  
  const firstAvg = firstHalf.length > 0 ? firstHalf.reduce((sum, val) => sum + val, 0) / firstHalf.length : 0
  const secondAvg = secondHalf.length > 0 ? secondHalf.reduce((sum, val) => sum + val, 0) / secondHalf.length : 0
  
  const trend = firstAvg > 0 ? ((secondAvg - firstAvg) / firstAvg) * 100 : 0
  
  return {
    direction: trend > 0 ? 'positive' : 'negative',
    percentage: Math.abs(trend).toFixed(1),
    strength: Math.abs(trend) > 10 ? 'mạnh' : Math.abs(trend) > 5 ? 'trung bình' : 'yếu'
  }
}

const getPerformanceInsights = () => {
  if (!dailySales.value || dailySales.value.length === 0) {
    return {
      bestDay: null,
      worstDay: null,
      averageRevenue: 0,
      volatility: '0.0',
      growthRate: 0
    }
  }
  
  const currentData = dailySales.value.map(day => day.revenue || 0)
  const max = Math.max(...currentData)
  const min = Math.min(...currentData)
  const avg = currentData.length > 0 ? currentData.reduce((sum, val) => sum + val, 0) / currentData.length : 0
  
  return {
    bestDay: dailySales.value.find(day => (day.revenue || 0) === max),
    worstDay: dailySales.value.find(day => (day.revenue || 0) === min),
    averageRevenue: avg,
    volatility: avg > 0 ? ((max - min) / avg * 100).toFixed(1) : '0.0',
    growthRate: currentData.length > 1 ? calculateChange(currentData[currentData.length - 1], currentData) : 0
  }
}

// Lifecycle
onMounted(() => {
  // Initialize analytics page
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

.metric-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.8rem;
}

.detail-label {
  color: #6c757d;
}

.detail-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Advanced Filters */
.advanced-filters-section {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

/* Real-time Status */
.realtime-status-section {
  margin-bottom: 1.5rem;
}

.realtime-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.5rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6c757d;
  transition: all 0.3s ease;
}

.status-dot.active {
  background: #28a745;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.status-text {
  font-weight: 500;
  color: #2c3e50;
}

.last-updated {
  display: flex;
  align-items: center;
  color: #6c757d;
  font-size: 0.875rem;
}

/* Insights Section */
.insights-section {
  margin-bottom: 2rem;
}

.insight-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  padding: 1.5rem;
  height: 100%;
}

.insight-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.insight-title {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.insight-icon {
  font-size: 1.25rem;
  color: #007bff;
}

.insight-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.insight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.insight-item:last-child {
  border-bottom: none;
}

.insight-label {
  font-size: 0.875rem;
  color: #6c757d;
}

.insight-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Enhanced Chart Styles */
.chart-title-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.chart-metrics {
  display: flex;
  gap: 0.5rem;
}

.metric-badge {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.chart-tabs {
  display: flex;
  gap: 0.25rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
  padding: 0.25rem;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  background: white;
  color: #007bff;
}

.tab-btn.active {
  background: #007bff;
  color: white;
}

.chart-actions {
  display: flex;
  gap: 0.5rem;
}

.chart-insights {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.chart-insights .insight-item {
  border-bottom: none;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.25rem;
}

.chart-insights .insight-label {
  font-size: 0.75rem;
  color: #6c757d;
}

.chart-insights .insight-value {
  font-size: 0.875rem;
  font-weight: 600;
}

.chart-insights .insight-value.positive {
  color: #28a745;
}

.chart-insights .insight-value.negative {
  color: #dc3545;
}

/* Real-time Metrics */
.realtime-metrics {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.metric-card-small {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  padding: 1rem;
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.metric-title {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  font-size: 0.875rem;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.metric-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 0.875rem;
  color: #6c757d;
}

.stat-value {
  font-weight: 600;
  color: #2c3e50;
}

/* Enhanced Product List */
.product-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-bottom: 1px solid #f8f9fa;
  transition: background-color 0.2s ease;
}

.product-item:hover {
  background: #f8f9fa;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 40px;
  height: 40px;
  border-radius: 0.5rem;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

.product-category {
  font-size: 0.75rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.product-sales {
  font-size: 0.75rem;
  color: #28a745;
}

.product-revenue {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.875rem;
}

/* Performance Metrics */
.performance-metrics {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.performance-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.performance-label {
  font-size: 0.875rem;
  color: #6c757d;
}

.performance-value {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.125rem;
}

.performance-progress {
  width: 100%;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #007bff, #28a745);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* Detailed Report Section */
.detailed-report-section {
  margin-bottom: 2rem;
}

.report-summary-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.report-title {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.report-actions {
  display: flex;
  gap: 0.5rem;
}

.report-content {
  padding: 0;
}

.report-metric {
  text-align: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
  border: 1px solid #e9ecef;
}

.report-metric .metric-label {
  font-size: 0.875rem;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.report-metric .metric-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.report-metric .metric-change {
  font-size: 0.75rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
}

.report-insights {
  margin-top: 1.5rem;
}

.insights-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.insight-card-small {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.5rem;
  transition: all 0.2s ease;
}

.insight-card-small:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.insight-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}

.insight-icon.positive {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.insight-icon.warning {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.insight-icon.info {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.insight-icon.success {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

.insight-description {
  font-size: 0.8rem;
  color: #6c757d;
  line-height: 1.4;
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
