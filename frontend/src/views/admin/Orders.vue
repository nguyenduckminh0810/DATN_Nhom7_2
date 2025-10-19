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
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            class="form-control search-input"
            placeholder="Tìm kiếm theo mã đơn hàng, địa chỉ, ghi chú..."
            v-model="searchQuery"
          />
        </div>
        <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
          <i class="bi bi-funnel me-1"></i>Bộ lọc nâng cao
          <i :class="showAdvancedFilters ? 'bi bi-caret-up' : 'bi bi-caret-down'" class="ms-1"></i>
        </button>
        <button class="btn btn-outline-secondary" @click="clearFilters">
          <i class="bi bi-arrow-clockwise me-1"></i>Xóa bộ lọc
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
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Statistics -->
    <div class="order-stats-section">
      <div class="row g-3">
        <div class="col-md-2">
          <div class="stat-card pending">
            <div class="stat-icon">
              <i class="bi bi-clock"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.pending }}</div>
              <div class="stat-label">Chờ xử lý</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card processing">
            <div class="stat-icon">
              <i class="bi bi-gear"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.processing }}</div>
              <div class="stat-label">Đang xử lý</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card shipped">
            <div class="stat-icon">
              <i class="bi bi-truck"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.shipped }}</div>
              <div class="stat-label">Đã giao</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card delivered">
            <div class="stat-icon">
              <i class="bi bi-check-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.delivered }}</div>
              <div class="stat-label">Hoàn thành</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card payment-pending">
            <div class="stat-icon">
              <i class="bi bi-credit-card"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ paymentStats.pending }}</div>
              <div class="stat-label">Chờ thanh toán</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card payment-paid">
            <div class="stat-icon">
              <i class="bi bi-check-circle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ paymentStats.paid }}</div>
              <div class="stat-label">Đã thanh toán</div>
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
            <i class="bi bi-table"></i>
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'kanban' }]"
            @click="viewMode = 'kanban'"
            title="Kanban Board"
          >
            <i class="bi bi-columns"></i>
          </button>
        </div>
        <div class="table-stats">
          <span class="stats-text">
            Hiển thị {{ filteredOrders.length }} / {{ orders.length }} đơn hàng
          </span>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <!-- Table View -->
      <div v-else-if="viewMode === 'table' && filteredOrders.length > 0" class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th>
                <input type="checkbox" class="form-check-input" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th>
                <button class="sort-btn" @click="sortTable('soDonHang')">
                  Mã đơn hàng
                  <i :class="getSortIcon('soDonHang')"></i>
                </button>
              </th>
              <th>Địa chỉ giao hàng</th>
              <th>Sản phẩm</th>
              <th>
                <button class="sort-btn" @click="sortTable('tongThanhToan')">
                  Tổng tiền
                  <i :class="getSortIcon('tongThanhToan')"></i>
                </button>
              </th>
              <th>Trạng thái</th>
              <th>Thanh toán</th>
              <th>
                <button class="sort-btn" @click="sortTable('taoLuc')">
                  Ngày đặt
                  <i :class="getSortIcon('taoLuc')"></i>
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
                  <strong>#{{ order.soDonHang }}</strong>
                  <div class="order-meta">
                    <div class="order-id">ID: {{ order.id }}</div>
                    <div v-if="order.ghiChu" class="order-note text-muted">
                      <small>{{ order.ghiChu }}</small>
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div class="shipping-address">
                  {{ order.diaChiGiaoSnapshot || 'Chưa có địa chỉ' }}
                </div>
              </td>
              <td>
                <div class="order-items">
                  <div class="item-count">{{ order.chiTietList?.length || 0 }} sản phẩm</div>
                  <div class="item-preview">
                    <span 
                      v-for="item in (order.chiTietList || []).slice(0, 3)" 
                      :key="item.id"
                      class="item-badge"
                      :title="item.tenSanPham"
                    >
                      {{ item.tenSanPham }}
                    </span>
                    <span v-if="(order.chiTietList || []).length > 3" class="more-items">
                      +{{ (order.chiTietList || []).length - 3 }}
                    </span>
                  </div>
                </div>
              </td>
              <td>
                <div class="order-amount">
                  <div class="total-amount">{{ formatCurrency(order.tongThanhToan || order.tamTinh) }}</div>
                  <div class="subtotal-amount text-muted">
                    Tạm tính: {{ formatCurrency(order.tamTinh) }}
                  </div>
                </div>
              </td>
              <td>
                <div class="status-info">
                  <span :class="['status-badge', getStatusClass(order.trangThai)]">
                    {{ getStatusText(order.trangThai) }}
                  </span>
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
                  <div>{{ formatDate(order.taoLuc) }}</div>
                  <div class="order-time">{{ formatTime(order.taoLuc) }}</div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn btn-sm btn-outline-primary" @click="viewOrder(order)" title="Xem chi tiết">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button 
                    v-if="order.trangThai === 'pending'"
                    class="btn btn-sm btn-outline-success" 
                    @click="updateOrderStatus(order, 'processing')"
                    title="Xử lý đơn hàng"
                  >
                    <i class="bi bi-play"></i>
                  </button>
                  <button 
                    v-if="order.trangThai === 'processing'"
                    class="btn btn-sm btn-outline-info" 
                    @click="updateOrderStatus(order, 'shipped')"
                    title="Giao hàng"
                  >
                    <i class="bi bi-truck"></i>
                  </button>
                  <button 
                    v-if="order.trangThai === 'shipped'"
                    class="btn btn-sm btn-outline-success" 
                    @click="updateOrderStatus(order, 'delivered')"
                    title="Hoàn thành"
                  >
                    <i class="bi bi-check"></i>
                  </button>
                  <button 
                    v-if="['pending', 'processing'].includes(order.trangThai)"
                    class="btn btn-sm btn-outline-danger" 
                    @click="updateOrderStatus(order, 'cancelled')"
                    title="Hủy đơn hàng"
                  >
                    <i class="bi bi-x"></i>
                  </button>
                  <button 
                    v-if="order.paymentStatus === 'pending'"
                    class="btn btn-sm btn-outline-warning" 
                    @click="updatePaymentStatus(order, 'paid')"
                    title="Đánh dấu đã thanh toán"
                  >
                    <i class="bi bi-credit-card"></i>
                  </button>
                  <button
                  v-if="['pending', 'processing'].includes(order.trangThai)"
                  class="btn btn-sm btn-outline-danger"
                  @click="deleteOrder(order)"
                  title="Xóa đơn hàng"
                >
                  <i class="bi bi-trash"></i>
                </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty State -->
      <div v-else-if="!loading && filteredOrders.length === 0" class="empty-state">
        <i class="bi bi-inbox empty-icon"></i>
        <h5>Không có đơn hàng nào</h5>
        <p>Không tìm thấy đơn hàng phù hợp với bộ lọc của bạn.</p>
        <button class="btn btn-primary" @click="clearFilters">
          <i class="bi bi-arrow-clockwise me-1"></i>Xóa bộ lọc
        </button>
      </div>

      <!-- Kanban Board View -->
      <div v-else-if="viewMode === 'kanban' && filteredOrders.length > 0" class="kanban-board">
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
                  <div class="order-number">#{{ order.soDonHang }}</div>
                  <span :class="['payment-badge-sm', getPaymentClass(order.paymentStatus)]">
                    {{ getPaymentText(order.paymentStatus) }}
                  </span>
                </div>
                <div class="card-body">
                  <div class="shipping-address">
                    {{ order.diaChiGiaoSnapshot || 'Chưa có địa chỉ' }}
                  </div>
                  <div class="order-items">
                    <div class="item-count">{{ order.chiTietList?.length || 0 }} sản phẩm</div>
                    <div class="item-preview">
                      <span 
                        v-for="item in (order.chiTietList || []).slice(0, 2)" 
                        :key="item.id"
                        class="item-badge"
                      >
                        {{ item.tenSanPham }}
                      </span>
                      <span v-if="(order.chiTietList || []).length > 2" class="more-items">
                        +{{ (order.chiTietList || []).length - 2 }}
                      </span>
                    </div>
                  </div>
                  <div class="order-amount">{{ formatCurrency(order.tongThanhToan || order.tamTinh) }}</div>
                </div>
                <div class="card-footer">
                  <div class="order-date">{{ formatDate(order.taoLuc) }}</div>
                  <div class="card-actions">
                    <button class="btn btn-sm btn-outline-primary" @click.stop="viewOrder(order)">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button 
                      v-if="canUpdateStatus(order.trangThai)"
                      class="btn btn-sm btn-outline-success" 
                      @click.stop="updateOrderStatus(order, getNextStatus(order.trangThai))"
                    >
                      <i class="bi bi-arrow-right"></i>
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
    <div v-if="!loading && filteredOrders.length > 0" class="pagination-section">
      <div class="row align-items-center">
        <div class="col-md-6">
          <div class="pagination-info">
            Hiển thị {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalItems) }} 
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
            <i class="bi bi-check me-1"></i>Xử lý
          </button>
          <button class="btn btn-sm btn-outline-primary" @click="bulkUpdateStatus('shipped')">
            <i class="bi bi-truck me-1"></i>Giao hàng
          </button>
          <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('delivered')">
            <i class="bi bi-check-circle me-1"></i>Hoàn thành
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkUpdateStatus('cancelled')">
            <i class="bi bi-x me-1"></i>Hủy
          </button>
          <button class="btn btn-sm btn-outline-info" @click="bulkUpdatePayment('paid')">
            <i class="bi bi-credit-card me-1"></i>Đã thanh toán
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="bulkDeleteOrders">
          <i class="bi bi-trash me-1"></i>Xóa
        </button>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content modal-lg" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Chi tiết đơn hàng #{{ selectedOrder?.soDonHang }}</h5>
          <button class="btn-close" @click="closeOrderModal">
            <i class="bi bi-x"></i>
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
                    <td><strong>#{{ selectedOrder.soDonHang }}</strong></td>
                  </tr>
                  <tr>
                    <td>Ngày đặt:</td>
                    <td>{{ formatDateTime(selectedOrder.taoLuc) }}</td>
                  </tr>
                  <tr>
                    <td>Cập nhật:</td>
                    <td>{{ formatDateTime(selectedOrder.capNhatLuc) }}</td>
                  </tr>
                  <tr>
                    <td>Trạng thái:</td>
                    <td>
                      <span :class="['status-badge', getStatusClass(selectedOrder.trangThai)]">
                        {{ getStatusText(selectedOrder.trangThai) }}
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
                  <tr v-if="selectedOrder.ghiChu">
                    <td>Ghi chú:</td>
                    <td>{{ selectedOrder.ghiChu }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Shipping Info -->
            <div class="col-md-6">
              <h6>Thông tin giao hàng</h6>
              <table class="table table-sm">
                <tbody>
                  <tr>
                    <td>Địa chỉ:</td>
                    <td>{{ selectedOrder.diaChiGiaoSnapshot || 'Chưa có địa chỉ' }}</td>
                  </tr>
                  <tr>
                    <td>Kênh bán:</td>
                    <td>{{ selectedOrder.kenhBan || 'online' }}</td>
                  </tr>
                  <tr>
                    <td>Tiền tệ:</td>
                    <td>{{ selectedOrder.tienTe || 'VND' }}</td>
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
                  <tr v-for="item in selectedOrder.chiTietList" :key="item.id">
                    <td>
                      <div>
                        <div class="item-name">{{ item.tenSanPham }}</div>
                        <div class="item-variant text-muted">{{ item.tenHienThi }}</div>
                      </div>
                    </td>
                    <td>{{ item.soLuong }}</td>
                    <td>{{ formatCurrency(item.donGia) }}</td>
                    <td><strong>{{ formatCurrency(item.thanhTien) }}</strong></td>
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
                    <td>{{ formatCurrency(selectedOrder.tamTinh) }}</td>
                  </tr>
                  <tr class="table-active">
                    <td><strong>Tổng cộng:</strong></td>
                    <td><strong>{{ formatCurrency(selectedOrder.tongThanhToan || selectedOrder.tamTinh) }}</strong></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeOrderModal">Đóng</button>
          <button 
            v-if="selectedOrder.paymentStatus === 'pending'"
            type="button" 
            class="btn btn-warning" 
            @click="updatePaymentStatus(selectedOrder, 'paid')"
          >
            <i class="bi bi-credit-card me-1"></i>Đánh dấu đã thanh toán
          </button>
          <button type="button" class="btn btn-primary" @click="printOrder(selectedOrder)">
            <i class="bi bi-printer me-1"></i>In đơn hàng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";

// ======= STATE =======
const orders = ref([]);
const loading = ref(false);

const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalItems = ref(0);

const searchQuery = ref("");
const selectedStatus = ref("");
const selectedPayment = ref("");
const selectedDate = ref("");
const amountRange = ref({ min: null, max: null });
const sortBy = ref("newest");
const sortDir = ref("desc");

// UI state
const showAdvancedFilters = ref(false);
const viewMode = ref("table");

// modal & selection
const showOrderModal = ref(false);
const selectedOrder = ref(null);
const selectAll = ref(false);
const selectedOrders = ref([]);

// ======= CONSTANTS =======
const orderStatuses = ref([
  { value: "pending", label: "Chờ xử lý", color: "#ffc107" },
  { value: "processing", label: "Đang xử lý", color: "#17a2b8" },
  { value: "shipped", label: "Đã giao", color: "#6f42c1" },
  { value: "delivered", label: "Hoàn thành", color: "#28a745" },
  { value: "cancelled", label: "Đã hủy", color: "#dc3545" },
]);

// ======= UTILS =======
const toDate = (value) => (value ? new Date(value) : null);

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫';
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

const formatDate = (date) => {
  const d = toDate(date);
  return d ? new Intl.DateTimeFormat("vi-VN").format(d) : "";
};

const formatTime = (date) => {
  const d = toDate(date);
  return d ? new Intl.DateTimeFormat("vi-VN", {
    hour: '2-digit',
    minute: '2-digit'
  }).format(d) : "";
};

const formatDateTime = (date) => {
  const d = toDate(date);
  return d
    ? new Intl.DateTimeFormat("vi-VN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      }).format(d)
    : "";
};

// ======= API CALL =======
const fetchOrders = async () => {
  loading.value = true;
  try {
    const pageToSend = Math.max(0, currentPage.value - 1);
    const response = await axios.get("/api/don-hang/phan-trang", {
      params: {
        page: pageToSend,
        size: pageSize.value,
        sortBy: "id",
        sortDir: sortDir.value,
      },
    });

    const data = response.data;
    console.log('API Response:', data); // Debug
    
    // Map dữ liệu từ backend sang frontend với payment status
    orders.value = (data.content || []).map(order => ({
      ...order,
      // Thêm payment status mặc định (backend chưa có field này)
      paymentStatus: order.paymentStatus || 'pending',
      paymentMethod: order.paymentMethod || 'COD'
    }));
    
    totalPages.value = data.totalPages || 1;
    totalItems.value = data.totalItems || orders.value.length;
    
    console.log('Processed orders:', orders.value); // Debug
    
  } catch (err) {
    console.error("Lỗi khi tải đơn hàng:", err);
    if (err.response) {
      console.error("Response error:", err.response.data);
      console.error("Status:", err.response.status);
    }
  } finally {
    loading.value = false;
  }
};

// ======= COMPUTED =======

// Thống kê trạng thái đơn hàng
const orderStats = computed(() => ({
  pending: orders.value.filter((o) => o.trangThai === "pending").length,
  processing: orders.value.filter((o) => o.trangThai === "processing").length,
  shipped: orders.value.filter((o) => o.trangThai === "shipped").length,
  delivered: orders.value.filter((o) => o.trangThai === "delivered").length,
  cancelled: orders.value.filter((o) => o.trangThai === "cancelled").length,
}));

// Thống kê thanh toán
const paymentStats = computed(() => ({
  pending: orders.value.filter((o) => o.paymentStatus === "pending").length,
  paid: orders.value.filter((o) => o.paymentStatus === "paid").length,
  failed: orders.value.filter((o) => o.paymentStatus === "failed").length,
}));

// Tổng số đơn hàng và đơn chờ xử lý
const totalOrders = computed(() => orders.value.length);
const pendingOrders = computed(() => orderStats.value.pending);

// Lọc và sắp xếp
const filteredOrders = computed(() => {
  let filtered = orders.value.slice();

  // Tìm kiếm theo số đơn hoặc địa chỉ
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (o) =>
        String(o.soDonHang || "").toLowerCase().includes(query) ||
        String(o.diaChiGiaoSnapshot || "").toLowerCase().includes(query) ||
        String(o.ghiChu || "").toLowerCase().includes(query)
    );
  }

  // Lọc theo trạng thái
  if (selectedStatus.value) {
    filtered = filtered.filter(
      (o) => o.trangThai === selectedStatus.value
    );
  }

  // Lọc theo thanh toán
  if (selectedPayment.value) {
    filtered = filtered.filter(
      (o) => o.paymentStatus === selectedPayment.value
    );
  }

  // Sắp xếp
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case "oldest":
        return new Date(a.taoLuc) - new Date(b.taoLuc);
      case "amount-high":
        return (b.tongThanhToan ?? b.tamTinh ?? 0) - (a.tongThanhToan ?? a.tamTinh ?? 0);
      case "amount-low":
        return (a.tongThanhToan ?? a.tamTinh ?? 0) - (b.tongThanhToan ?? b.tamTinh ?? 0);
      case "newest":
      default:
        return new Date(b.taoLuc) - new Date(a.taoLuc);
    }
  });

  return filtered;
});

const visiblePages = computed(() => {
  const pages = [];
  const start = Math.max(1, currentPage.value - 2);
  const end = Math.min(totalPages.value, start + 4);
  for (let i = start; i <= end; i++) pages.push(i);
  return pages;
});

// ======= METHODS =======
const getStatusText = (status) => {
  const map = {
    pending: "Chờ xử lý",
    processing: "Đang xử lý",
    shipped: "Đã giao",
    delivered: "Hoàn thành",
    cancelled: "Đã hủy",
  };
  return map[status] || status;
};

const getStatusClass = (status) => {
  const map = {
    pending: "bg-warning",
    processing: "bg-info",
    shipped: "bg-primary",
    delivered: "bg-success",
    cancelled: "bg-danger",
  };
  return map[status] || "bg-secondary";
};

const getPaymentText = (status) => {
  const map = {
    pending: "Chờ thanh toán",
    paid: "Đã thanh toán",
    failed: "Thanh toán thất bại",
  };
  return map[status] || status;
};

const getPaymentClass = (status) => {
  const map = {
    pending: "bg-warning",
    paid: "bg-success",
    failed: "bg-danger",
  };
  return map[status] || "bg-secondary";
};

const getSortIcon = (field) => {
  return 'bi bi-arrow-down';
};

const sortTable = (field) => {
  console.log('Sort by:', field);
};

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    fetchOrders();
  }
};

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value;
};

const clearFilters = () => {
  searchQuery.value = "";
  selectedStatus.value = "";
  selectedPayment.value = "";
  selectedDate.value = "";
  amountRange.value = { min: null, max: null };
  sortBy.value = "newest";
};

const viewOrder = (order) => {
  selectedOrder.value = order;
  showOrderModal.value = true;
};

const closeOrderModal = () => {
  showOrderModal.value = false;
  selectedOrder.value = null;
};

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedOrders.value = filteredOrders.value.map((o) => o.id);
  } else {
    selectedOrders.value = [];
  }
};

const updateOrderStatus = async (order, newStatus) => {
  if (confirm(`Cập nhật đơn hàng #${order.soDonHang} sang trạng thái "${getStatusText(newStatus)}"?`)) {
    try {
      await axios.put(`/api/don-hang/${order.id}`, {
        trangThai: newStatus
      });
      
      // Update local state
      order.trangThai = newStatus;
      order.capNhatLuc = new Date().toISOString();
      
      console.log(`Đã cập nhật đơn hàng #${order.soDonHang} sang ${newStatus}`);
    } catch (err) {
      console.error('Lỗi khi cập nhật trạng thái:', err);
      alert('Có lỗi khi cập nhật trạng thái đơn hàng');
    }
  }
};

const updatePaymentStatus = async (order, newStatus) => {
  if (confirm(`Cập nhật trạng thái thanh toán đơn hàng #${order.soDonHang} thành "${getPaymentText(newStatus)}"?`)) {
    try {
      // Gọi API update payment status (cần thêm endpoint trong backend)
      // await axios.patch(`/api/don-hang/${order.id}/payment`, {
      //   paymentStatus: newStatus
      // });
      
      // Update local state (tạm thời)
      order.paymentStatus = newStatus;
      order.capNhatLuc = new Date().toISOString();
      
      console.log(`Đã cập nhật thanh toán đơn hàng #${order.soDonHang} sang ${newStatus}`);
    } catch (err) {
      console.error('Lỗi khi cập nhật trạng thái thanh toán:', err);
      alert('Có lỗi khi cập nhật trạng thái thanh toán');
    }
  }
};

const bulkUpdateStatus = (status) => {
  if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedOrders.value.length} đơn hàng?`)) {
    selectedOrders.value.forEach((orderId) => {
      const order = orders.value.find((o) => o.id === orderId);
      if (order) {
        order.trangThai = status;
        order.capNhatLuc = new Date().toISOString();
      }
    });
    selectedOrders.value = [];
    selectAll.value = false;
  }
};

const bulkUpdatePayment = (status) => {
  if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái thanh toán cho ${selectedOrders.value.length} đơn hàng?`)) {
    selectedOrders.value.forEach((orderId) => {
      const order = orders.value.find((o) => o.id === orderId);
      if (order) {
        order.paymentStatus = status;
        order.capNhatLuc = new Date().toISOString();
      }
    });
    selectedOrders.value = [];
    selectAll.value = false;
  }
};

const getOrdersByStatus = (status) => {
  return filteredOrders.value.filter(order => order.trangThai === status);
};

const canUpdateStatus = (status) => {
  return ['pending', 'processing', 'shipped'].includes(status);
};

const getNextStatus = (currentStatus) => {
  const statusFlow = {
    'pending': 'processing',
    'processing': 'shipped', 
    'shipped': 'delivered'
  };
  return statusFlow[currentStatus] || currentStatus;
};

const printOrder = (order) => {
  window.print();
};

const deleteOrder = async (order) => {
  if (confirm(`Bạn có chắc chắn muốn xóa đơn hàng #${order.soDonHang}?`)) {
    try {
      await axios.delete(`/api/don-hang/${order.id}`);
      // Remove the order from local state
      orders.value = orders.value.filter((o) => o.id !== order.id);
      totalItems.value = orders.value.length;
      console.log(`Đã xóa đơn hàng #${order.soDonHang}`);
    } catch (err) {
      console.error("Lỗi khi xóa đơn hàng:", err);
      alert("Có lỗi khi xóa đơn hàng");
    }
  }
};

const bulkDeleteOrders = async () => {
  if (confirm(`Bạn có chắc chắn muốn xóa ${selectedOrders.value.length} đơn hàng?`)) {
    try {
      // Send individual delete requests for each selected order
      await Promise.all(
        selectedOrders.value.map((orderId) =>
          axios.delete(`/api/don-hang/${orderId}`)
        )
      );
      // Remove deleted orders from local state
      orders.value = orders.value.filter((o) => !selectedOrders.value.includes(o.id));
      totalItems.value = orders.value.length;
      selectedOrders.value = [];
      selectAll.value = false;
      console.log(`Đã xóa ${selectedOrders.value.length} đơn hàng`);
    } catch (err) {
      console.error("Lỗi khi xóa hàng loạt đơn hàng:", err);
      alert("Có lỗi khi xóa hàng loạt đơn hàng");
    }
  }
};

// ======= LIFECYCLE =======
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.admin-orders {
  width: 100%;
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
  background: #6366f1;
  color: white;
  border-color: #6366f1;
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
  color: #6366f1;
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
  background: #6366f1;
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
