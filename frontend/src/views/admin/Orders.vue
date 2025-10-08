<template>
  <div class="admin-orders">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý đơn hàng</h1>
        <p class="page-subtitle">Quản lý và theo dõi đơn hàng của khách hàng</p>
      </div>
      <div class="header-right">
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-label">Tổng đơn hàng:</span>
            <span class="stat-value">{{ totalOrders }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Chờ xử lý:</span>
            <span class="stat-value text-warning">{{ pendingOrders }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Advanced Filters and Search -->
    <div class="filters-section">
      <!-- Quick Search -->
      <div class="search-row">
        <div class="search-box">
          <i class="ph-magnifying-glass search-icon"></i>
          <input
            type="text"
            class="form-control search-input"
            placeholder="Tìm kiếm đơn hàng, khách hàng, số điện thoại..."
            v-model="searchQuery"
          />
        </div>
        <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
          <i class="ph-funnel me-1"></i>Bộ lọc nâng cao
          <i :class="showAdvancedFilters ? 'ph-caret-up' : 'ph-caret-down'" class="ms-1"></i>
        </button>
        <button class="btn btn-outline-secondary" @click="clearFilters">
          <i class="ph-arrow-clockwise me-1"></i>Xóa bộ lọc
        </button>
      </div>

      <!-- Advanced Filters -->
      <div v-if="showAdvancedFilters" class="advanced-filters">
        <div class="row g-3">
          <div class="col-md-2">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" v-model="selectedStatus">
              <option value="">Tất cả trạng thái</option>
              <option value="pending">Chờ xử lý</option>
              <option value="processing">Đang xử lý</option>
              <option value="shipped">Đã giao</option>
              <option value="delivered">Hoàn thành</option>
              <option value="cancelled">Đã hủy</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">Thanh toán</label>
            <select class="form-select" v-model="selectedPayment">
              <option value="">Tất cả thanh toán</option>
              <option value="pending">Chờ thanh toán</option>
              <option value="paid">Đã thanh toán</option>
              <option value="failed">Thanh toán thất bại</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">Ngày đặt</label>
            <input type="date" class="form-control" v-model="selectedDate">
          </div>
          <div class="col-md-2">
            <label class="form-label">Khoảng tiền</label>
            <div class="amount-range">
              <input type="number" class="form-control" placeholder="Từ" v-model.number="amountRange.min">
              <span class="range-separator">-</span>
              <input type="number" class="form-control" placeholder="Đến" v-model.number="amountRange.max">
            </div>
          </div>
          <div class="col-md-2">
            <label class="form-label">Sắp xếp</label>
            <select class="form-select" v-model="sortBy">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="amount-high">Giá cao nhất</option>
              <option value="amount-low">Giá thấp nhất</option>
              <option value="customer-name">Tên khách hàng</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">Loại đơn hàng</label>
            <select class="form-select" v-model="orderType">
              <option value="">Tất cả</option>
              <option value="urgent">Khẩn cấp</option>
              <option value="vip">VIP</option>
              <option value="bulk">Số lượng lớn</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Statistics -->
    <div class="order-stats-section">
      <div class="row g-3">
        <div class="col-md-3">
          <div class="stat-card pending">
            <div class="stat-icon">
              <i class="ph-clock"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.pending }}</div>
              <div class="stat-label">Chờ xử lý</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card processing">
            <div class="stat-icon">
              <i class="ph-gear"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.processing }}</div>
              <div class="stat-label">Đang xử lý</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card shipped">
            <div class="stat-icon">
              <i class="ph-truck"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.shipped }}</div>
              <div class="stat-label">Đã giao</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="stat-card delivered">
            <div class="stat-icon">
              <i class="ph-check-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.delivered }}</div>
              <div class="stat-label">Hoàn thành</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="orders-table">
      <!-- Table Actions -->
      <div class="table-actions">
        <div class="view-options">
          <span class="view-label">Hiển thị:</span>
          <button 
            :class="['view-btn', { active: viewMode === 'table' }]"
            @click="viewMode = 'table'"
            title="Dạng bảng"
          >
            <i class="ph-table"></i>
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'kanban' }]"
            @click="viewMode = 'kanban'"
            title="Kanban Board"
          >
            <i class="ph-columns"></i>
          </button>
        </div>
        <div class="table-stats">
          <span class="stats-text">
            Hiển thị {{ filteredOrders.length }} / {{ orders.length }} đơn hàng
          </span>
        </div>
      </div>

      <!-- Table View -->
      <div v-if="viewMode === 'table'" class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th>
                <button class="sort-btn" @click="sortTable('orderNumber')">
                  Mã đơn hàng
                  <i :class="getSortIcon('orderNumber')"></i>
                </button>
              </th>
              <th>
                <button class="sort-btn" @click="sortTable('customer')">
                  Khách hàng
                  <i :class="getSortIcon('customer')"></i>
                </button>
              </th>
              <th>Sản phẩm</th>
              <th>
                <button class="sort-btn" @click="sortTable('total')">
                  Tổng tiền
                  <i :class="getSortIcon('total')"></i>
                </button>
              </th>
              <th>Trạng thái</th>
              <th>Thanh toán</th>
              <th>
                <button class="sort-btn" @click="sortTable('createdAt')">
                  Ngày đặt
                  <i :class="getSortIcon('createdAt')"></i>
                </button>
              </th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id">
              <td>
                <input type="checkbox" class="form-check-input" v-model="selectedOrders" :value="order.id">
              </td>
              <td>
                <div class="order-code">
                  <strong>#{{ order.orderNumber }}</strong>
                  <div class="order-meta">
                    <div class="order-id">ID: {{ order.id }}</div>
                    <div class="order-badges">
                      <span v-if="order.isUrgent" class="urgent-badge">Khẩn cấp</span>
                      <span v-if="order.isVip" class="vip-badge">VIP</span>
                      <span v-if="order.items.length > 5" class="bulk-badge">Số lượng lớn</span>
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div class="customer-info">
                  <div class="customer-name">{{ order.customer.name }}</div>
                  <div class="customer-details">
                    <div class="customer-phone">{{ order.customer.phone }}</div>
                    <div class="customer-email">{{ order.customer.email }}</div>
                  </div>
                  <div v-if="order.customer.totalOrders > 0" class="customer-stats">
                    {{ order.customer.totalOrders }} đơn hàng
                  </div>
                </div>
              </td>
              <td>
                <div class="order-items">
                  <div class="item-count">{{ order.items.length }} sản phẩm</div>
                  <div class="item-preview">
                    <img 
                      v-for="item in order.items.slice(0, 3)" 
                      :key="item.id"
                      :src="item.image" 
                      :alt="item.name"
                      class="item-thumbnail"
                      :title="item.name"
                    >
                    <span v-if="order.items.length > 3" class="more-items">+{{ order.items.length - 3 }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="order-amount">
                  <div class="total-amount">{{ formatCurrency(order.total) }}</div>
                  <div v-if="order.discount > 0" class="discount-amount">
                    Giảm: {{ formatCurrency(order.discount) }}
                  </div>
                  <div v-if="order.shippingFee > 0" class="shipping-fee">
                    Phí ship: {{ formatCurrency(order.shippingFee) }}
                  </div>
                </div>
              </td>
              <td>
                <div class="status-info">
                  <span :class="['status-badge', getStatusClass(order.status)]">
                    {{ getStatusText(order.status) }}
                  </span>
                  <div v-if="order.estimatedDelivery" class="delivery-info">
                    <i class="ph-calendar"></i>
                    {{ formatDate(order.estimatedDelivery) }}
                  </div>
                </div>
              </td>
              <td>
                <div class="payment-info">
                  <span :class="['payment-badge', getPaymentClass(order.paymentStatus)]">
                    {{ getPaymentText(order.paymentStatus) }}
                  </span>
                  <div class="payment-method">{{ order.paymentMethod }}</div>
                </div>
              </td>
              <td>
                <div class="order-date">
                  <div>{{ formatDate(order.createdAt) }}</div>
                  <div class="order-time">{{ formatTime(order.createdAt) }}</div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn btn-sm btn-outline-primary" @click="viewOrder(order)" title="Xem chi tiết">
                    <i class="ph-eye"></i>
                  </button>
                  <button 
                    v-if="order.status === 'pending'"
                    class="btn btn-sm btn-outline-success" 
                    @click="updateOrderStatus(order, 'processing')"
                    title="Xử lý đơn hàng"
                  >
                    <i class="ph-play"></i>
                  </button>
                  <button 
                    v-if="order.status === 'processing'"
                    class="btn btn-sm btn-outline-info" 
                    @click="updateOrderStatus(order, 'shipped')"
                    title="Giao hàng"
                  >
                    <i class="ph-truck"></i>
                  </button>
                  <button 
                    v-if="order.status === 'shipped'"
                    class="btn btn-sm btn-outline-success" 
                    @click="updateOrderStatus(order, 'delivered')"
                    title="Hoàn thành"
                  >
                    <i class="ph-check"></i>
                  </button>
                  <button 
                    v-if="['pending', 'processing'].includes(order.status)"
                    class="btn btn-sm btn-outline-danger" 
                    @click="updateOrderStatus(order, 'cancelled')"
                    title="Hủy đơn hàng"
                  >
                    <i class="ph-x"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-secondary" @click="printOrder(order)" title="In đơn hàng">
                    <i class="ph-printer"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Kanban Board View -->
      <div v-if="viewMode === 'kanban'" class="kanban-board">
        <div class="kanban-columns">
          <div class="kanban-column" v-for="status in orderStatuses" :key="status.value">
            <div class="column-header">
              <h6 class="column-title">{{ status.label }}</h6>
              <span class="column-count">{{ getOrdersByStatus(status.value).length }}</span>
            </div>
            <div class="column-content">
              <div 
                v-for="order in getOrdersByStatus(status.value)" 
                :key="order.id"
                class="kanban-card"
                @click="viewOrder(order)"
              >
                <div class="card-header">
                  <div class="order-number">#{{ order.orderNumber }}</div>
                  <div class="order-badges">
                    <span v-if="order.isUrgent" class="urgent-badge">Khẩn cấp</span>
                    <span v-if="order.isVip" class="vip-badge">VIP</span>
                  </div>
                </div>
                <div class="card-body">
                  <div class="customer-info">
                    <div class="customer-name">{{ order.customer.name }}</div>
                    <div class="customer-phone">{{ order.customer.phone }}</div>
                  </div>
                  <div class="order-items">
                    <div class="item-count">{{ order.items.length }} sản phẩm</div>
                    <div class="item-preview">
                      <img 
                        v-for="item in order.items.slice(0, 2)" 
                        :key="item.id"
                        :src="item.image" 
                        :alt="item.name"
                        class="item-thumbnail"
                      >
                      <span v-if="order.items.length > 2" class="more-items">+{{ order.items.length - 2 }}</span>
                    </div>
                  </div>
                  <div class="order-amount">{{ formatCurrency(order.total) }}</div>
                </div>
                <div class="card-footer">
                  <div class="order-date">{{ formatDate(order.createdAt) }}</div>
                  <div class="card-actions">
                    <button class="btn btn-sm btn-outline-primary" @click.stop="viewOrder(order)">
                      <i class="ph-eye"></i>
                    </button>
                    <button 
                      v-if="canUpdateStatus(order.status)"
                      class="btn btn-sm btn-outline-success" 
                      @click.stop="showStatusModal(order)"
                    >
                      <i class="ph-arrow-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-section">
      <div class="row align-items-center">
        <div class="col-md-6">
          <div class="pagination-info">
            Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, totalItems) }} 
            trong tổng số {{ totalItems }} đơn hàng
          </div>
        </div>
        <div class="col-md-6">
          <nav>
            <ul class="pagination justify-content-end">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">Trước</a>
              </li>
              <li class="page-item" v-for="page in visiblePages" :key="page" :class="{ active: currentPage === page }">
                <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">Sau</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- Bulk Actions -->
    <div v-if="selectedOrders.length > 0" class="bulk-actions">
      <div class="bulk-actions-content">
        <span class="selected-count">{{ selectedOrders.length }} đơn hàng đã chọn</span>
        <div class="bulk-buttons">
          <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('processing')">
            <i class="ph-check me-1"></i>Xử lý
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="bulkUpdateStatus('shipped')">
            <i class="ph-truck me-1"></i>Giao hàng
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('delivered')">
            <i class="ph-check-circle me-1"></i>Hoàn thành
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkUpdateStatus('cancelled')">
            <i class="ph-x me-1"></i>Hủy
          </button>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content modal-lg" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Chi tiết đơn hàng #{{ selectedOrder?.orderNumber }}</h5>
          <button class="btn-close" @click="closeOrderModal">
            <i class="ph-x"></i>
          </button>
        </div>
        <div class="modal-body" v-if="selectedOrder">
          <div class="row">
            <!-- Order Info -->
            <div class="col-md-6">
              <h6>Thông tin đơn hàng</h6>
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <td>Mã đơn hàng:</td>
                    <td><strong>#{{ selectedOrder.orderNumber }}</strong></td>
                  </tr>
                  <tr>
                    <td>Ngày đặt:</td>
                    <td>{{ formatDateTime(selectedOrder.createdAt) }}</td>
                  </tr>
                  <tr>
                    <td>Trạng thái:</td>
                    <td>
                      <span :class="['status-badge', getStatusClass(selectedOrder.status)]">
                        {{ getStatusText(selectedOrder.status) }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td>Thanh toán:</td>
                    <td>
                      <span :class="['payment-badge', getPaymentClass(selectedOrder.paymentStatus)]">
                        {{ getPaymentText(selectedOrder.paymentStatus) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Customer Info -->
            <div class="col-md-6">
              <h6>Thông tin khách hàng</h6>
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <td>Tên:</td>
                    <td>{{ selectedOrder.customer.name }}</td>
                  </tr>
                  <tr>
                    <td>Email:</td>
                    <td>{{ selectedOrder.customer.email }}</td>
                  </tr>
                  <tr>
                    <td>Số điện thoại:</td>
                    <td>{{ selectedOrder.customer.phone }}</td>
                  </tr>
                  <tr>
                    <td>Địa chỉ:</td>
                    <td>{{ selectedOrder.shippingAddress }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Order Items -->
          <div class="mt-4">
            <h6>Sản phẩm đã đặt</h6>
            <div class="table-responsive">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in selectedOrder.items" :key="item.id">
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <img :src="item.image" :alt="item.name" class="item-thumbnail">
                        <div>
                          <div class="item-name">{{ item.name }}</div>
                          <div class="item-variant">{{ item.variant }}</div>
                        </div>
                      </div>
                    </td>
                    <td>{{ item.quantity }}</td>
                    <td>{{ formatCurrency(item.price) }}</td>
                    <td><strong>{{ formatCurrency(item.price * item.quantity) }}</strong></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="row mt-4">
            <div class="col-md-6"></div>
            <div class="col-md-6">
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <td>Tạm tính:</td>
                    <td>{{ formatCurrency(selectedOrder.subtotal) }}</td>
                  </tr>
                  <tr v-if="selectedOrder.discount > 0">
                    <td>Giảm giá:</td>
                    <td class="text-success">-{{ formatCurrency(selectedOrder.discount) }}</td>
                  </tr>
                  <tr>
                    <td>Phí vận chuyển:</td>
                    <td>{{ formatCurrency(selectedOrder.shippingFee) }}</td>
                  </tr>
                  <tr>
                    <td>Thuế:</td>
                    <td>{{ formatCurrency(selectedOrder.tax) }}</td>
                  </tr>
                  <tr class="table-active">
                    <td><strong>Tổng cộng:</strong></td>
                    <td><strong>{{ formatCurrency(selectedOrder.total) }}</strong></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeOrderModal">Đóng</button>
          <button type="button" class="btn btn-primary" @click="printOrder(selectedOrder)">
            <i class="ph-printer me-1"></i>In đơn hàng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// Reactive data
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedPayment = ref('')
const selectedDate = ref('')
const sortBy = ref('newest')
const selectedOrders = ref([])
const selectAll = ref(false)
const currentPage = ref(1)
const itemsPerPage = 10

// Advanced filters
const showAdvancedFilters = ref(false)
const amountRange = ref({ min: null, max: null })
const orderType = ref('')
const viewMode = ref('table') // 'table' or 'kanban'
const tableSort = ref({ field: '', direction: 'asc' })

// Order statuses for Kanban
const orderStatuses = ref([
  { value: 'pending', label: 'Chờ xử lý', color: '#ffc107' },
  { value: 'processing', label: 'Đang xử lý', color: '#17a2b8' },
  { value: 'shipped', label: 'Đã giao', color: '#6f42c1' },
  { value: 'delivered', label: 'Hoàn thành', color: '#28a745' },
  { value: 'cancelled', label: 'Đã hủy', color: '#dc3545' }
])
const showOrderModal = ref(false)
const selectedOrder = ref(null)

// Mock data
const orders = ref([
  {
    id: 1,
    orderNumber: 'ORD-001',
    customer: {
      name: 'Nguyễn Văn A',
      email: 'nguyenvana@email.com',
      phone: '0123456789',
      totalOrders: 5
    },
    items: [
      {
        id: 1,
        name: 'Áo sơ mi nam cao cấp',
        variant: 'M - Trắng',
        price: 450000,
        quantity: 2,
        image: 'https://images.unsplash.com/photo-1594938298605-cd64d190e6bc?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80'
      },
      {
        id: 2,
        name: 'Quần âu nam',
        variant: 'L - Đen',
        price: 650000,
        quantity: 1,
        image: 'https://images.unsplash.com/photo-1506629905607-1a5a1b1b1b1b?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80'
      }
    ],
    subtotal: 1550000,
    discount: 200000,
    shippingFee: 50000,
    tax: 140000,
    total: 1540000,
    status: 'pending',
    paymentStatus: 'pending',
    paymentMethod: 'COD',
    shippingAddress: '123 Đường ABC, Quận 1, TP.HCM',
    createdAt: new Date('2024-01-15T10:30:00'),
    estimatedDelivery: new Date('2024-01-20'),
    isUrgent: false,
    isVip: false
  },
  {
    id: 2,
    orderNumber: 'ORD-002',
    customer: {
      name: 'Trần Thị B',
      email: 'tranthib@email.com',
      phone: '0987654321',
      totalOrders: 12
    },
    items: [
      {
        id: 3,
        name: 'Áo khoác nam',
        variant: 'XL - Xám',
        price: 850000,
        quantity: 1,
        image: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80'
      }
    ],
    subtotal: 850000,
    discount: 0,
    shippingFee: 50000,
    tax: 90000,
    total: 990000,
    status: 'processing',
    paymentStatus: 'paid',
    paymentMethod: 'Bank Transfer',
    shippingAddress: '456 Đường XYZ, Quận 2, TP.HCM',
    createdAt: new Date('2024-01-16T14:20:00'),
    estimatedDelivery: new Date('2024-01-21'),
    isUrgent: true,
    isVip: false
  },
  {
    id: 3,
    orderNumber: 'ORD-003',
    customer: {
      name: 'Lê Văn C',
      email: 'levanc@email.com',
      phone: '0369852147',
      totalOrders: 8
    },
    items: [
      {
        id: 4,
        name: 'Áo thun nam',
        variant: 'M - Xanh navy',
        price: 250000,
        quantity: 3,
        image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80'
      }
    ],
    subtotal: 750000,
    discount: 75000,
    shippingFee: 30000,
    tax: 70500,
    total: 775500,
    status: 'shipped',
    paymentStatus: 'paid',
    paymentMethod: 'Credit Card',
    shippingAddress: '789 Đường DEF, Quận 3, TP.HCM',
    createdAt: new Date('2024-01-17T09:15:00'),
    estimatedDelivery: new Date('2024-01-22'),
    isUrgent: false,
    isVip: true
  }
])

// Computed
const orderStats = computed(() => ({
  pending: orders.value.filter(order => order.status === 'pending').length,
  processing: orders.value.filter(order => order.status === 'processing').length,
  shipped: orders.value.filter(order => order.status === 'shipped').length,
  delivered: orders.value.filter(order => order.status === 'delivered').length,
  cancelled: orders.value.filter(order => order.status === 'cancelled').length
}))

const filteredOrders = computed(() => {
  let filtered = orders.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(order =>
      order.orderNumber.toLowerCase().includes(query) ||
      order.customer.name.toLowerCase().includes(query) ||
      order.customer.email.toLowerCase().includes(query) ||
      order.customer.phone.includes(query)
    )
  }

  if (selectedStatus.value) {
    filtered = filtered.filter(order => order.status === selectedStatus.value)
  }

  if (selectedPayment.value) {
    filtered = filtered.filter(order => order.paymentStatus === selectedPayment.value)
  }

  if (selectedDate.value) {
    const selected = new Date(selectedDate.value)
    filtered = filtered.filter(order => {
      const orderDate = new Date(order.createdAt)
      return orderDate.toDateString() === selected.toDateString()
    })
  }

  // Amount range filter
  if (amountRange.value.min !== null && amountRange.value.min !== '') {
    filtered = filtered.filter(order => order.total >= amountRange.value.min)
  }
  if (amountRange.value.max !== null && amountRange.value.max !== '') {
    filtered = filtered.filter(order => order.total <= amountRange.value.max)
  }

  // Order type filter
  if (orderType.value) {
    switch (orderType.value) {
      case 'urgent':
        filtered = filtered.filter(order => order.isUrgent)
        break
      case 'vip':
        filtered = filtered.filter(order => order.isVip)
        break
      case 'bulk':
        filtered = filtered.filter(order => order.items.length > 5)
        break
    }
  }

  // Sorting
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'oldest':
        return new Date(a.createdAt) - new Date(b.createdAt)
      case 'amount-high':
        return b.total - a.total
      case 'amount-low':
        return a.total - b.total
      case 'customer-name':
        return a.customer.name.localeCompare(b.customer.name)
      case 'newest':
      default:
        return new Date(b.createdAt) - new Date(a.createdAt)
    }
  })

  return filtered
})

const totalItems = computed(() => filteredOrders.value.length)
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

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

const formatTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatDateTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const getStatusText = (status) => {
  const statuses = {
    'pending': 'Chờ xử lý',
    'processing': 'Đang xử lý',
    'shipped': 'Đã giao',
    'delivered': 'Hoàn thành',
    'cancelled': 'Đã hủy'
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    'pending': 'bg-warning',
    'processing': 'bg-info',
    'shipped': 'bg-primary',
    'delivered': 'bg-success',
    'cancelled': 'bg-danger'
  }
  return classes[status] || 'bg-secondary'
}

const getPaymentText = (status) => {
  const statuses = {
    'pending': 'Chờ thanh toán',
    'paid': 'Đã thanh toán',
    'failed': 'Thanh toán thất bại'
  }
  return statuses[status] || status
}

const getPaymentClass = (status) => {
  const classes = {
    'pending': 'bg-warning',
    'paid': 'bg-success',
    'failed': 'bg-danger'
  }
  return classes[status] || 'bg-secondary'
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedStatus.value = ''
  selectedPayment.value = ''
  selectedDate.value = ''
  amountRange.value = { min: null, max: null }
  orderType.value = ''
  sortBy.value = 'newest'
}

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const sortTable = (field) => {
  if (tableSort.value.field === field) {
    tableSort.value.direction = tableSort.value.direction === 'asc' ? 'desc' : 'asc'
  } else {
    tableSort.value.field = field
    tableSort.value.direction = 'asc'
  }
}

const getSortIcon = (field) => {
  if (tableSort.value.field !== field) return 'ph-caret-up-down'
  return tableSort.value.direction === 'asc' ? 'ph-caret-up' : 'ph-caret-down'
}

const getOrdersByStatus = (status) => {
  return filteredOrders.value.filter(order => order.status === status)
}

const canUpdateStatus = (currentStatus) => {
  return ['pending', 'processing', 'shipped'].includes(currentStatus)
}

const updateOrderStatus = (order, newStatus) => {
  order.status = newStatus
  // Add status update to timeline
  if (!order.timeline) {
    order.timeline = []
  }
  order.timeline.push({
    status: newStatus,
    timestamp: new Date(),
    note: `Cập nhật trạng thái thành: ${getStatusText(newStatus)}`
  })
}

const showStatusModal = (order) => {
  selectedOrder.value = order
  // Show modal for status update
}

const printOrder = (order) => {
  // Print order functionality
  window.print()
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedOrders.value = filteredOrders.value.map(o => o.id)
  } else {
    selectedOrders.value = []
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const viewOrder = (order) => {
  selectedOrder.value = order
  showOrderModal.value = true
}

const closeOrderModal = () => {
  showOrderModal.value = false
  selectedOrder.value = null
}

const updateStatus = (order) => {
  const statuses = ['pending', 'processing', 'shipped', 'delivered', 'cancelled']
  const currentIndex = statuses.indexOf(order.status)
  const nextIndex = (currentIndex + 1) % statuses.length
  order.status = statuses[nextIndex]
}


const bulkUpdateStatus = (status) => {
  if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedOrders.value.length} đơn hàng?`)) {
    selectedOrders.value.forEach(orderId => {
      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = status
      }
    })
    selectedOrders.value = []
    selectAll.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Initialize orders page
})
</script>

<style scoped>
.admin-orders {
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

.header-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.search-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.search-row .search-box {
  flex: 1;
}

.advanced-filters {
  border-top: 1px solid #e9ecef;
  padding-top: 1.5rem;
  margin-top: 1rem;
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.range-separator {
  color: #6c757d;
  font-weight: 500;
}

/* Order Statistics */
.order-stats-section {
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.pending {
  border-left: 4px solid #ffc107;
}

.stat-card.processing {
  border-left: 4px solid #17a2b8;
}

.stat-card.shipped {
  border-left: 4px solid #6f42c1;
}

.stat-card.delivered {
  border-left: 4px solid #28a745;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card.pending .stat-icon {
  background: #ffc107;
}

.stat-card.processing .stat-icon {
  background: #17a2b8;
}

.stat-card.shipped .stat-icon {
  background: #6f42c1;
}

.stat-card.delivered .stat-icon {
  background: #28a745;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: #6c757d;
  font-size: 0.9rem;
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  z-index: 1;
}

.search-input {
  padding-left: 2.5rem;
}

.orders-table {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.table-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.view-options {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.view-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-right: 0.5rem;
}

.view-btn {
  padding: 0.5rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn:hover {
  background: #f8f9fa;
}

.view-btn.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.table-stats {
  color: #6c757d;
  font-size: 0.9rem;
}

.sort-btn {
  background: none;
  border: none;
  padding: 0;
  font-weight: 600;
  color: #2c3e50;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.sort-btn:hover {
  color: #3498db;
}

.table {
  margin-bottom: 0;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: #2c3e50;
  padding: 1rem 0.75rem;
}

.table td {
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.order-code {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-id {
  font-size: 0.8rem;
  color: #6c757d;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.customer-name {
  font-weight: 600;
  color: #2c3e50;
}

.customer-email,
.customer-phone {
  font-size: 0.85rem;
  color: #6c757d;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.item-count {
  font-size: 0.9rem;
  color: #6c757d;
}

.item-preview {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.item-thumbnail {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  object-fit: cover;
}

.more-items {
  font-size: 0.8rem;
  color: #6c757d;
  background-color: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.order-amount {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.total-amount {
  font-weight: 600;
  color: #2c3e50;
}

.discount-amount {
  font-size: 0.8rem;
  color: #28a745;
}

.status-badge,
.payment-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  color: white;
}

.order-date {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-time {
  font-size: 0.8rem;
  color: #6c757d;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-buttons .btn {
  padding: 0.375rem 0.75rem;
}

/* Kanban Board */
.kanban-board {
  margin-top: 1rem;
}

.kanban-columns {
  display: flex;
  gap: 1.5rem;
  overflow-x: auto;
  padding-bottom: 1rem;
}

.kanban-column {
  min-width: 300px;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1rem;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #dee2e6;
}

.column-title {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.column-count {
  background: #3498db;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.column-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.kanban-card {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
}

.kanban-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.card-body {
  margin-bottom: 0.75rem;
}

.customer-info {
  margin-bottom: 0.5rem;
}

.customer-name {
  font-weight: 600;
  color: #2c3e50;
}

.customer-phone {
  font-size: 0.8rem;
  color: #6c757d;
}

.order-items {
  margin-bottom: 0.5rem;
}

.item-count {
  font-size: 0.8rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.item-preview {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.item-thumbnail {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  object-fit: cover;
}

.more-items {
  font-size: 0.75rem;
  color: #6c757d;
  background: #e9ecef;
  padding: 0.125rem 0.25rem;
  border-radius: 4px;
}

.order-amount {
  font-weight: 600;
  color: #28a745;
  font-size: 0.9rem;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.5rem;
  border-top: 1px solid #e9ecef;
}

.order-date {
  font-size: 0.8rem;
  color: #6c757d;
}

.card-actions {
  display: flex;
  gap: 0.25rem;
}

.card-actions .btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.pagination-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  color: #6c757d;
  font-size: 0.9rem;
}

.bulk-actions {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.bulk-actions-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.selected-count {
  font-weight: 600;
  color: #2c3e50;
}

.bulk-buttons {
  display: flex;
  gap: 0.5rem;
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
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.modal-lg {
  max-width: 1000px;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.btn-close:hover {
  background-color: #f8f9fa;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
}

.item-variant {
  font-size: 0.8rem;
  color: #6c757d;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-stats {
    width: 100%;
    justify-content: space-around;
  }
  
  .orders-table {
    padding: 1rem;
  }
  
  .table-responsive {
    font-size: 0.9rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .bulk-actions {
    left: 1rem;
    right: 1rem;
    transform: none;
  }
  
  .bulk-actions-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .bulk-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>
