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
              <option v-for="status in adminStatuses" :key="status.code" :value="status.code">
                {{ status.label }}
              </option>
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
            <label class="form-label">Từ ngày</label>
            <input type="date" class="form-control" v-model="dateFrom" />
          </div>
          <div class="col-md-2">
            <label class="form-label">Đến ngày</label>
            <input type="date" class="form-control" v-model="dateTo" :min="dateFrom" />
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
              <div class="stat-label">Chờ xác nhận</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card shipping">
            <div class="stat-icon">
              <i class="bi bi-bicycle"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.shipping }}</div>
              <div class="stat-label">Đang giao</div>
            </div>
          </div>
        </div>
        <div class="col-md-2">
          <div class="stat-card shipped">
            <div class="stat-icon">
              <i class="bi bi-truck"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.delivered }}</div>
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
              <div class="stat-value">{{ orderStats.completed }}</div>
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
            Hiển thị {{ paginatedOrders.length }} / {{ filteredTotalItems }} đơn hàng (tổng {{ orders.length }})
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
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectAll"
                  @change="toggleSelectAll"
                />
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
            <tr v-for="order in paginatedOrders" :key="order.id">
              <td>
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="selectedOrders"
                  :value="order.id"
                />
              </td>
              <td>
                <div class="order-code">
                  <strong>#{{ order.soDonHang }}</strong>
                  <div class="order-meta">
                    <!-- <div class="order-id">ID: {{ order.id }}</div> -->
                    <div class="payment-method-label">
                      <span class="badge bg-info">{{ order.paymentMethod }}</span>
                    </div>
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
                  <div class="subtotal-amount text-muted">
                    <small>
                      Tạm tính: {{ formatCurrency(order.tamTinh) }}
                      <span v-if="getOrderShipping(order) > 0">
                        + Ship: {{ formatCurrency(getOrderShipping(order)) }}
                      </span>
                    </small>
                  </div>
                </div>
              </td>
              <td>
                <div class="status-info">
                  <span :class="['status-badge', order.statusClass]">
                    {{ order.statusLabel }}
                  </span>
                </div>
              </td>
              <td>
                <div class="payment-info">
                  <span :class="['payment-badge', getPaymentClass(order.paymentStatus)]">
                    {{ getPaymentText(order.paymentStatus) }}
                  </span>
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
                  <button
                    class="btn btn-sm btn-outline-primary"
                    @click="viewOrder(order)"
                    title="Xem chi tiết"
                  >
                    <i class="bi bi-eye"></i>
                  </button>
                  <button
                    v-if="order.statusCode === STATUS_CODES.PENDING"
                    class="btn btn-sm btn-outline-primary"
                    @click="quickUpdateStatus(order, STATUS_CODES.SHIPPING)"
                    title="Chuyển sang đang giao"
                  >
                    <i class="bi bi-bicycle"></i>
                  </button>
                  <button
                    v-if="order.statusCode === STATUS_CODES.SHIPPING"
                    class="btn btn-sm btn-outline-success"
                    @click="quickUpdateStatus(order, STATUS_CODES.DELIVERED)"
                    title="Xác nhận đã giao"
                  >
                    <i class="bi bi-check-circle"></i>
                  </button>
                  <button
                    v-if="order.statusCode === STATUS_CODES.DELIVERED"
                    class="btn btn-sm btn-outline-success"
                    @click="quickUpdateStatus(order, STATUS_CODES.COMPLETED)"
                    title="Hoàn thành"
                  >
                    <i class="bi bi-check"></i>
                  </button>
                  <button
                    v-if="order.statusCode === STATUS_CODES.PENDING || 
                          order.statusCode === STATUS_CODES.SHIPPING"
                    class="btn btn-sm btn-outline-danger"
                    @click="openCancelModal(order)"
                    title="Hủy đơn hàng"
                  >
                    <i class="bi bi-x"></i>
                  </button>
                  <button
                    v-if="order.statusCode === STATUS_CODES.PENDING"
                    class="btn btn-sm btn-outline-warning"
                    @click="editOrder(order)"
                    title="Chỉnh sửa đơn hàng"
                  >
                    <i class="bi bi-pencil"></i>
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
          <div class="kanban-column" v-for="status in kanbanStatuses" :key="status.code">
            <div class="column-header">
              <h6 class="column-title">{{ status.label }}</h6>
              <span class="column-count">{{ getOrdersByStatus(status.code).length }}</span>
            </div>
            <div class="column-content">
              <div
                v-for="order in getOrdersByStatus(status.code)"
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
                  <div class="order-amount">
                    {{ formatCurrency(getOrderTotal(order)) }}
                  </div>
                </div>
                <div class="card-footer">
                  <div class="order-date">{{ formatDate(order.taoLuc) }}</div>
                  <div class="card-actions">
                    <button class="btn btn-sm btn-outline-primary" @click.stop="viewOrder(order)">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      v-if="canUpdateStatus(order.statusCode)"
                      class="btn btn-sm btn-outline-success"
                      @click.stop="updateOrderStatus(order, getNextStatus(order.statusCode))"
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
            Hiển thị {{ (currentPage - 1) * pageSize + 1 }} -
            {{ Math.min(currentPage * pageSize, filteredTotalItems) }} trong tổng số {{ filteredTotalItems }} đơn
            hàng
          </div>
        </div>
        <div class="col-md-6">
          <nav>
            <ul class="pagination justify-content-end">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">Trước</a>
              </li>
              <li
                class="page-item"
                v-for="page in visiblePages"
                :key="page"
                :class="{ active: currentPage === page }"
              >
                <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === filteredTotalPages }">
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
          <button
            class="btn btn-sm btn-outline-primary"
            @click="bulkUpdateStatus(STATUS_CODES.SHIPPING)"
          >
            <i class="bi bi-bicycle me-1"></i>Đang giao
          </button>
          <button
            class="btn btn-sm btn-outline-success"
            @click="bulkUpdateStatus(STATUS_CODES.DELIVERED)"
          >
            <i class="bi bi-truck me-1"></i>Đã giao
          </button>
          <button
            class="btn btn-sm btn-outline-success"
            @click="bulkUpdateStatus(STATUS_CODES.COMPLETED)"
          >
            <i class="bi bi-check-circle me-1"></i>Hoàn thành
          </button>
          <button
            class="btn btn-sm btn-outline-danger"
            @click="bulkUpdateStatus(STATUS_CODES.CANCELLED)"
          >
            <i class="bi bi-x me-1"></i>Hủy
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
              <table class="table table-sm order-info-table">
                <tbody>
                  <tr>
                    <td>Mã đơn hàng:</td>
                    <td>
                      <strong>#{{ selectedOrder.soDonHang }}</strong>
                    </td>
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
                      <span :class="['status-badge', selectedOrder.statusClass]">
                        {{ selectedOrder.statusLabel }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td>Thanh toán:</td>
                    <td>
                      <span
                        :class="['payment-badge', getPaymentClass(selectedOrder.paymentStatus)]"
                      >
                        {{ getPaymentText(selectedOrder.paymentStatus) }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="selectedOrder.ghiChu">
                    <td>Ghi chú:</td>
                    <td>{{ selectedOrder.ghiChu }}</td>
                  </tr>
                  <tr v-if="selectedOrder.statusCode === 'CANCELLED' || selectedOrder.statusCode === 'DA_HUY' || selectedOrder.statusLabel === 'Đã hủy'">
                    <td>Lý do hủy:</td>
                    <td>
                      <div v-if="selectedOrder.lyDoHuy">

                        <div class="text-danger">{{ selectedOrder.lyDoHuy }}</div>
                      </div>
                      <span v-else class="text-muted">Không có lý do được ghi nhận</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Shipping Info -->
            <div class="col-md-6">
              <h6>Thông tin giao hàng</h6>
              <table class="table table-sm order-info-table">
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
                    <th>Màu sắc / Size</th>
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
                    <td>
                      <div v-if="item.mauSacTen || item.kichCoTen">
                        <span v-if="item.mauSacTen" class="badge bg-light text-dark me-1">
                          Màu: {{ item.mauSacTen }}
                        </span>
                        <span v-if="item.kichCoTen" class="badge bg-light text-dark">
                          Size: {{ item.kichCoTen }}
                        </span>
                      </div>
                      <span v-else class="text-muted">-</span>
                    </td>
                    <td>{{ item.soLuong }}</td>
                    <td>{{ formatCurrency(item.donGia) }}</td>
                    <td>
                      <strong>{{ formatCurrency(item.thanhTien) }}</strong>
                    </td>
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
                  <tr v-if="getOrderShipping(selectedOrder) > 0">
                    <td>Phí giao hàng:</td>
                    <td>{{ formatCurrency(getOrderShipping(selectedOrder)) }}</td>
                  </tr>
                  <tr class="table-active">
                    <td><strong>Tổng cộng:</strong></td>
                    <td>
                      <strong>{{ formatCurrency(getOrderTotal(selectedOrder)) }}</strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeOrderModal">Đóng</button>
          <button type="button" class="btn btn-primary" @click="printOrder()">
            <i class="bi bi-printer me-1"></i>In đơn hàng
          </button>
        </div>
      </div>
    </div>

    <!-- Modal chỉnh sửa đơn hàng -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Chỉnh sửa đơn hàng #{{ editingOrder?.soDonHang }}</h5>
          <button class="btn-close" @click="closeEditModal">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body" v-if="editingOrder">
          <!-- Tab Navigation -->
          <ul class="nav nav-tabs">
            <li class="nav-item">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'info' }"
                @click="activeTab = 'info'"
              >
                Thông tin
              </button>
            </li>
            <li class="nav-item">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'status' }"
                @click="activeTab = 'status'"
              >
                Trạng thái
              </button>
            </li>
          </ul>

          <!-- Tab Content -->
          <div class="tab-content mt-3">
            <!-- Tab Thông tin -->
            <div v-show="activeTab === 'info'" class="tab-pane active">
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label class="form-label"
                      >Địa chỉ giao hàng <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      class="form-control"
                      v-model="editingOrder.diaChiGiao"
                      placeholder="Nhập địa chỉ giao hàng"
                    />
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="mb-3">
                    <label class="form-label">Ghi chú</label>
                    <textarea
                      class="form-control"
                      v-model="editingOrder.ghiChu"
                      rows="3"
                      placeholder="Ghi chú xử lý đơn hàng"
                    ></textarea>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tab Trạng thái -->
            <div v-show="activeTab === 'status'" class="tab-pane active">
              <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select class="form-select" v-model="editingOrder.trangThai">
                  <option v-for="status in adminStatuses" :key="status.code" :value="status.code">
                    {{ status.label }}
                  </option>
                </select>
              </div>
              <div class="alert alert-info">
                <i class="bi bi-info-circle me-2"></i>
                Thay đổi trạng thái sẽ được lưu sau khi nhấn nút "Lưu thay đổi"
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeEditModal">Hủy</button>
          <button type="button" class="btn btn-primary" @click="saveOrderChanges()">
            <i class="bi bi-save me-1"></i>Lưu
          </button>
        </div>
      </div>
    </div>

    <!-- Cancel Order Modal -->
    <div v-if="showCancelModal" class="modal-overlay" @click="closeCancelModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">Hủy đơn hàng #{{ cancelingOrder?.soDonHang }}</h5>
          <button class="btn-close" @click="closeCancelModal"></button>
        </div>
        <div class="modal-body">
          <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle me-2"></i>
            Bạn có chắc chắn muốn hủy đơn hàng này? Vui lòng nhập lý do hủy.
          </div>
          <div class="mb-3">
            <label for="cancelReason" class="form-label">Lý do hủy <span class="text-danger">*</span></label>
            <textarea
              id="cancelReason"
              class="form-control"
              rows="4"
              v-model="cancelReason"
              placeholder="Nhập lý do hủy đơn hàng..."
              required
            ></textarea>
            <small class="form-text text-muted">Lý do hủy sẽ được lưu và hiển thị trong chi tiết đơn hàng.</small>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeCancelModal">Hủy</button>
          <button type="button" class="btn btn-danger" @click="confirmCancelOrder" :disabled="!cancelReason || cancelReason.trim().length === 0">
            Xác nhận hủy đơn
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import apiService from '@/services/api'
import {
  ORDER_STATUS_FOR_ADMIN,
  ORDER_STATUS_FOR_KANBAN,
  normalizeOrderStatus,
  getOrderStatusLabel,
  ORDER_STATUS_CODES,
} from '@/utils/orderStatus'

defineOptions({
  name: 'AdminOrdersView',
})

// ======= STATE =======
const orders = ref([])
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const totalItems = ref(0)

const searchQuery = ref('')
const selectedStatus = ref('PENDING') // Default to PENDING status
const selectedPayment = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const amountRange = ref({ min: null, max: null })
const sortBy = ref('newest')
const sortDir = ref('desc')

// UI state
const showAdvancedFilters = ref(true)
const viewMode = ref('table')

// UI state cho modal chỉnh sửa
const showEditModal = ref(false)
const editingOrder = ref(null)
const activeTab = ref('info')

// modal & selection
const showOrderModal = ref(false)
const selectedOrder = ref(null)
const selectAll = ref(false)
const selectedOrders = ref([])

// Cancel order modal
const showCancelModal = ref(false)
const cancelingOrder = ref(null)
const cancelReason = ref('')

// ======= CONSTANTS =======
const adminStatuses = ORDER_STATUS_FOR_ADMIN.slice().sort((a, b) => a.sortOrder - b.sortOrder)

const kanbanStatuses = ORDER_STATUS_FOR_KANBAN.slice().sort((a, b) => a.sortOrder - b.sortOrder)

const STATUS_CODES = ORDER_STATUS_CODES

// ======= UTILS =======
const toDate = (value) => (value ? new Date(value) : null)

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (date) => {
  const d = toDate(date)
  return d ? new Intl.DateTimeFormat('vi-VN').format(d) : ''
}

const formatTime = (date) => {
  const d = toDate(date)
  return d
    ? new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      }).format(d)
    : ''
}

const formatDateTime = (date) => {
  const d = toDate(date)
  return d
    ? new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      }).format(d)
    : ''
}

// Returns known shipping fee fields from order (tries multiple possible names)
const getOrderShipping = (order) => {
  if (!order) return 0
  // try common field names that backend might use
  return (
    Number(order.phiShip ?? order.shippingFee ?? order.shipFee ?? order.phiVanChuyen ?? order.phiGiao ?? order.phiGiaoHang ?? order.shipping_cost ?? order.phi ?? 0) || 0
  )
}

// Compute total to display: always calculate as tamTinh + shipping
const getOrderTotal = (order) => {
  if (!order) return 0
  const subtotal = Number(order.tamTinh ?? 0) || 0
  const shipping = getOrderShipping(order) || 0
  return subtotal + shipping
}

// ======= API CALL =======
const fetchOrders = async () => {
  loading.value = true
  try {
    // Check if we have any filters applied
    const hasFilters = selectedStatus.value || selectedPayment.value || dateFrom.value || dateTo.value || 
                      searchQuery.value || amountRange.value.min !== null || amountRange.value.max !== null
    
    // If filters are applied, fetch all orders (or a large number) for client-side filtering
    // Otherwise, use pagination from API
    const sizeToFetch = hasFilters ? 10000 : pageSize.value
    const pageToSend = hasFilters ? 0 : Math.max(0, currentPage.value - 1)
    
    const response = await axios.get('/api/don-hang/phan-trang', {
      params: {
        page: pageToSend,
        size: sizeToFetch,
        sortBy: 'id',
        sortDir: sortDir.value,
      },
    })

    const data = response.data
    console.log('API Response:', data)

    orders.value = (data.content || []).map((order) => {
      const statusInfo = normalizeOrderStatus(order.trangThai)

      // Debug: Log status normalization
      if (order.trangThai && order.trangThai !== statusInfo.code) {
        console.log('📝 Normalizing status:', {
          raw: order.trangThai,
          normalized: statusInfo.code,
          label: statusInfo.label,
        })
      }

      return {
        ...order,
        trangThai: statusInfo.code,
        statusCode: statusInfo.code, // Ensure statusCode is always set
        statusLabel: statusInfo.label,
        statusClass: statusInfo.badgeClass,
        statusColor: statusInfo.color,
        rawStatus: order.trangThai,
        paymentStatus: order.paymentStatus || 'pending',
        paymentMethod: order.paymentMethod || 'COD',
      }
    })

    // Only use API pagination if no filters are applied
    if (!hasFilters) {
      totalPages.value = data.totalPages || 1
      totalItems.value = data.totalItems || orders.value.length
    } else {
      // Reset to first page when filters are applied
      currentPage.value = 1
    }

    console.log('Processed orders:', orders.value.length, 'orders')
  } catch (err) {
    console.error('Lỗi khi tải đơn hàng:', err)
    if (err.response) {
      console.error('Response error:', err.response.data)
      console.error('Status:', err.response.status)
    }
  } finally {
    loading.value = false
  }
}

// ======= COMPUTED =======

const orderStats = computed(() => {
  const stats = {
    pending: 0,
    shipping: 0,
    delivered: 0,
    completed: 0,
    cancelled: 0,
  }

  orders.value.forEach((order) => {
    switch (order.statusCode) {
      case ORDER_STATUS_CODES.PENDING:
        stats.pending += 1
        break
      case ORDER_STATUS_CODES.SHIPPING:
        stats.shipping += 1
        break
      case ORDER_STATUS_CODES.DELIVERED:
        stats.delivered += 1
        break
      case ORDER_STATUS_CODES.COMPLETED:
        stats.completed += 1
        break
      case ORDER_STATUS_CODES.CANCELLED:
        stats.cancelled += 1
        break
      default:
        break
    }
  })

  return stats
})

const paymentStats = computed(() => ({
  pending: orders.value.filter((o) => o.paymentStatus === 'pending').length,
  paid: orders.value.filter((o) => o.paymentStatus === 'paid' || o.paymentStatus === 'PAID').length,
  failed: orders.value.filter((o) => o.paymentStatus === 'failed').length,
}))

const totalOrders = computed(() => orders.value.length)
const pendingOrders = computed(() => orderStats.value.pending)

const filteredOrders = computed(() => {
  let filtered = orders.value.slice()

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (o) =>
        String(o.soDonHang || '')
          .toLowerCase()
          .includes(query) ||
        String(o.diaChiGiaoSnapshot || '')
          .toLowerCase()
          .includes(query) ||
        String(o.ghiChu || '')
          .toLowerCase()
          .includes(query),
    )
  }

  // Filter by status - only filter if a status is selected (not empty string)
  if (selectedStatus.value && selectedStatus.value.trim() !== '') {
    // Normalize selected status to ensure it matches the statusCode format
    const selectedStatusNormalized = normalizeOrderStatus(selectedStatus.value).code
    
    // Debug: Log filter info
    console.log('🔍 Filtering by status:', {
      selectedStatus: selectedStatus.value,
      selectedStatusNormalized,
      totalOrders: orders.value.length,
      ordersStatusCodes: orders.value.map(o => ({ 
        id: o.id, 
        statusCode: o.statusCode, 
        rawStatus: o.rawStatus,
        soDonHang: o.soDonHang
      }))
    })
    
    filtered = filtered.filter((o) => {
      // Compare normalized statusCode with normalized selectedStatus
      const orderStatusCode = o.statusCode || ''
      const matches = String(orderStatusCode).toUpperCase().trim() === String(selectedStatusNormalized).toUpperCase().trim()
      
      if (!matches && orderStatusCode) {
        console.log('❌ Status mismatch:', {
          orderId: o.id,
          soDonHang: o.soDonHang,
          orderStatusCode,
          selectedStatusNormalized,
          rawStatus: o.rawStatus,
        })
      }
      
      return matches
    })
    
    console.log('✅ Filtered orders count:', filtered.length, 'out of', orders.value.length)
  }

  if (selectedPayment.value) {
    filtered = filtered.filter((o) => o.paymentStatus === selectedPayment.value)
  }

  // Filter by date range
  if (dateFrom.value || dateTo.value) {
    filtered = filtered.filter((o) => {
      const orderDate = new Date(o.taoLuc)
      orderDate.setHours(0, 0, 0, 0) // Reset time to start of day
      
      if (dateFrom.value && dateTo.value) {
        // Both dates selected - filter by range
        const fromDate = new Date(dateFrom.value)
        fromDate.setHours(0, 0, 0, 0)
        const toDate = new Date(dateTo.value)
        toDate.setHours(23, 59, 59, 999) // End of day
        return orderDate >= fromDate && orderDate <= toDate
      } else if (dateFrom.value) {
        // Only from date selected
        const fromDate = new Date(dateFrom.value)
        fromDate.setHours(0, 0, 0, 0)
        return orderDate >= fromDate
      } else if (dateTo.value) {
        // Only to date selected
        const toDate = new Date(dateTo.value)
        toDate.setHours(23, 59, 59, 999)
        return orderDate <= toDate
      }
      return true
    })
  }

  if (amountRange.value.min !== null || amountRange.value.max !== null) {
    filtered = filtered.filter((o) => {
      const amount = getOrderTotal(o)
      const min = amountRange.value.min ?? 0
      const max = amountRange.value.max ?? Infinity
      return amount >= min && amount <= max
    })
  }

  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'oldest':
        return new Date(a.taoLuc) - new Date(b.taoLuc)
      case 'amount-high':
        return getOrderTotal(b) - getOrderTotal(a)
      case 'amount-low':
        return getOrderTotal(a) - getOrderTotal(b)
      case 'newest':
      default:
        return new Date(b.taoLuc) - new Date(a.taoLuc)
    }
  })

  return filtered
})

// Paginated orders based on filtered results
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// Recalculate total items and pages based on filtered orders
const filteredTotalItems = computed(() => filteredOrders.value.length)
const filteredTotalPages = computed(() => Math.ceil(filteredTotalItems.value / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const totalPagesToUse = filteredTotalPages.value
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPagesToUse, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const getPaymentText = (status) => {
  const map = {
    pending: 'Chờ thanh toán',
    paid: 'Đã thanh toán',
    PAID: 'Đã thanh toán',
    failed: 'Thanh toán thất bại',
  }
  return map[status] || status
}

const getPaymentClass = (status) => {
  const map = {
    pending: 'bg-warning',
    paid: 'bg-success',
    PAID: 'bg-success',
    failed: 'bg-danger',
  }
  return map[status] || 'bg-secondary'
}

const getSortIcon = () => 'bi bi-arrow-down'

const sortTable = (field) => {
  if (sortBy.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortDir.value = 'asc'
  }
  console.log(`Sorting by ${field} in ${sortDir.value} order`)
}

const changePage = (page) => {
  if (page >= 1 && page <= filteredTotalPages.value) {
    currentPage.value = page
    // Only fetch from API if we don't have all orders loaded
    // For now, we'll just change the page without fetching
    // If needed, we can fetch all orders when filters are applied
  }
}

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedStatus.value = ''
  selectedPayment.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  amountRange.value = { min: null, max: null }
  sortBy.value = 'newest'
}

const viewOrder = (order) => {
  selectedOrder.value = order
  showOrderModal.value = true
}

const closeOrderModal = () => {
  showOrderModal.value = false
  selectedOrder.value = null
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedOrders.value = paginatedOrders.value.map((o) => o.id)
  } else {
    selectedOrders.value = []
  }
}

const bulkUpdateStatus = async (statusCode) => {
  if (
    confirm(
      `Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedOrders.value.length} đơn hàng?`,
    )
  ) {
    try {
      const updatePromises = selectedOrders.value.map(async (orderId) => {
        const order = orders.value.find((o) => o.id === orderId)
        if (order) {
          const statusLabel = getOrderStatusLabel(statusCode)
          const statusInfo = normalizeOrderStatus(statusCode)

          const updates = {
            diaChiGiao: order.diaChiGiao || order.diaChiGiaoSnapshot,
            ghiChu: order.ghiChu || '',
            trangThai: statusLabel,
          }
          const response = await axios.put(`/api/don-hang/${orderId}`, updates)

          // ✅ Lấy payment status từ response (backend đã tự động cập nhật nếu là COD và đã hoàn tất)
          const responseData = response.data?.data || response.data
          const updatedPaymentStatus = responseData?.paymentStatus || order.paymentStatus

          const index = orders.value.findIndex((o) => o.id === orderId)
          if (index !== -1) {
            const updatedOrder = {
              ...orders.value[index],
              trangThai: statusInfo.code,
              statusCode: statusInfo.code,
              statusLabel: statusInfo.label,
              statusClass: statusInfo.badgeClass,
              statusColor: statusInfo.color,
              rawStatus: statusLabel,
              // ✅ Cập nhật payment status từ response nếu có
              paymentStatus: updatedPaymentStatus,
              capNhatLuc: new Date().toISOString(),
            }

            orders.value[index] = updatedOrder

            if (selectedOrder.value?.id === orderId) {
              selectedOrder.value = {
                ...updatedOrder,
                // Đảm bảo selectedOrder cũng có payment status mới nhất
                paymentStatus: updatedPaymentStatus,
              }
            }
          }
        }
      })

      await Promise.all(updatePromises)

      selectedOrders.value = []
      selectAll.value = false

      alert(`✅ Đã cập nhật trạng thái cho ${updatePromises.length} đơn hàng`)
    } catch (err) {
      console.error('Lỗi khi cập nhật hàng loạt:', err)

      const errorMessage = err.response?.data?.message || err.message || 'Lỗi không xác định'

      if (errorMessage.includes('\n')) {
        alert(errorMessage)
      } else {
        alert('❌ Có lỗi khi cập nhật trạng thái hàng loạt:\n\n' + errorMessage)
      }
    }
  }
}

const getOrdersByStatus = (statusCode) => {
  return filteredOrders.value.filter((order) => order.statusCode === statusCode)
}

const canUpdateStatus = (status) => {
  return [STATUS_CODES.PENDING, STATUS_CODES.SHIPPING, STATUS_CODES.DELIVERED].includes(status)
}

const getNextStatus = (currentStatus) => {
  const statusFlow = {
    [STATUS_CODES.PENDING]: STATUS_CODES.SHIPPING,
    [STATUS_CODES.SHIPPING]: STATUS_CODES.DELIVERED,
    [STATUS_CODES.DELIVERED]: STATUS_CODES.COMPLETED,
  }
  return statusFlow[currentStatus] || currentStatus
}

const updateOrderStatus = async (order, newStatus) => {
  await quickUpdateStatus(order, newStatus)
}

const printOrder = () => {
  window.print()
}

const editOrder = (order) => {
  editingOrder.value = {
    id: order.id,
    soDonHang: order.soDonHang,
    diaChiGiao: order.diaChiGiao || order.diaChiGiaoSnapshot || '',
    ghiChu: order.ghiChu || '',
    trangThai: order.statusCode || STATUS_CODES.PENDING,
  }

  showEditModal.value = true
  activeTab.value = 'info'
}

const closeEditModal = () => {
  showEditModal.value = false
  editingOrder.value = null
  activeTab.value = 'info'
}

const saveOrderChanges = async () => {
  if (!editingOrder.value) {
    alert('Không có dữ liệu để lưu!')
    return
  }

  if (!editingOrder.value.diaChiGiao?.trim()) {
    alert('Vui lòng nhập địa chỉ giao hàng!')
    return
  }

  try {
    const statusLabel = getOrderStatusLabel(editingOrder.value.trangThai)
    const statusInfo = normalizeOrderStatus(editingOrder.value.trangThai)

    const updates = {
      diaChiGiao: editingOrder.value.diaChiGiao.trim(),
      ghiChu: editingOrder.value.ghiChu?.trim() || '',
      trangThai: statusLabel,
    }

    const response = await axios.put(`/api/don-hang/${editingOrder.value.id}`, updates)

    // ✅ Lấy payment status từ response (backend đã tự động cập nhật nếu là COD và đã hoàn tất)
    const responseData = response.data?.data || response.data
    const updatedPaymentStatus = responseData?.paymentStatus || editingOrder.value.paymentStatus

    const index = orders.value.findIndex((o) => o.id === editingOrder.value.id)
    if (index !== -1) {
      const updatedOrder = {
        ...orders.value[index],
        diaChiGiao: updates.diaChiGiao,
        diaChiGiaoSnapshot: updates.diaChiGiao,
        ghiChu: updates.ghiChu,
        trangThai: statusInfo.code,
        statusCode: statusInfo.code,
        statusLabel: statusInfo.label,
        statusClass: statusInfo.badgeClass,
        statusColor: statusInfo.color,
        rawStatus: statusLabel,
        // ✅ Cập nhật payment status từ response nếu có
        paymentStatus: updatedPaymentStatus,
        capNhatLuc: new Date().toISOString(),
      }

      orders.value[index] = updatedOrder

      if (selectedOrder.value?.id === editingOrder.value.id) {
        selectedOrder.value = {
          ...updatedOrder,
          // Đảm bảo selectedOrder cũng có payment status mới nhất
          paymentStatus: updatedPaymentStatus,
        }
      }

      console.log('✅ Updated order payment status:', updatedPaymentStatus)
    }

    closeEditModal()
    alert('✅ Cập nhật đơn hàng thành công!')
  } catch (err) {
    console.error('Lỗi cập nhật:', err)

    const errorMessage = err.response?.data?.message || err.message || 'Lỗi không xác định'

    // Hiển thị thông báo lỗi chi tiết
    if (errorMessage.includes('\n')) {
      alert(errorMessage)
    } else {
      alert('❌ Có lỗi khi cập nhật đơn hàng:\n\n' + errorMessage)
    }
  }
}

const quickUpdateStatus = async (order, newStatusCode) => {
  const statusLabel = getOrderStatusLabel(newStatusCode)
  const statusInfo = normalizeOrderStatus(newStatusCode)

  if (confirm(`Cập nhật trạng thái đơn hàng #${order.soDonHang} sang "${statusLabel}"?`)) {
    try {
      console.log('=== UPDATING ORDER STATUS ===')
      console.log('Order ID:', order.id)
      console.log('Order number:', order.soDonHang)
      console.log('New status code:', newStatusCode)
      console.log('Status label:', statusLabel)
      console.log('Current status:', order.trangThai || order.rawStatus)

      const updates = {
        diaChiGiao: order.diaChiGiao || order.diaChiGiaoSnapshot,
        ghiChu: order.ghiChu || '',
        trangThai: statusLabel, // Gửi label tiếng Việt, backend sẽ normalize
      }

      console.log('Sending updates:', updates)

      const response = await axios.put(`/api/don-hang/${order.id}`, updates)
      console.log('Response:', response.data)

      // ✅ Lấy payment status từ response (backend đã tự động cập nhật nếu là COD và đã hoàn tất)
      const responseData = response.data?.data || response.data
      const updatedPaymentStatus = responseData?.paymentStatus || order.paymentStatus

      const index = orders.value.findIndex((o) => o.id === order.id)
      if (index !== -1) {
        const updatedOrder = {
          ...orders.value[index],
          trangThai: statusInfo.code,
          statusCode: statusInfo.code,
          statusLabel: statusInfo.label,
          statusClass: statusInfo.badgeClass,
          statusColor: statusInfo.color,
          rawStatus: statusLabel,
          // ✅ Cập nhật payment status từ response nếu có
          paymentStatus: updatedPaymentStatus,
          capNhatLuc: new Date().toISOString(),
        }

        orders.value[index] = updatedOrder

        if (selectedOrder.value?.id === order.id) {
          selectedOrder.value = {
            ...updatedOrder,
            // Đảm bảo selectedOrder cũng có payment status mới nhất
            paymentStatus: updatedPaymentStatus,
          }
        }

        console.log('✅ Updated order payment status:', updatedPaymentStatus)
      }

      console.log(`✅ Đã cập nhật trạng thái đơn hàng #${order.soDonHang} sang ${statusLabel}`)
      alert(`✅ Đã cập nhật trạng thái đơn hàng thành công!`)
    } catch (err) {
      console.error('❌ Lỗi khi cập nhật trạng thái:', err)
      console.error('Error response:', err.response?.data)

      const errorMessage = err.response?.data?.message || err.message || 'Lỗi không xác định'

      // Hiển thị thông báo lỗi chi tiết với xuống dòng
      if (errorMessage.includes('\n')) {
        // Nếu thông báo có nhiều dòng (như thông báo hết hàng)
        alert(errorMessage)
      } else {
        // Thông báo lỗi thông thường
        alert('❌ Có lỗi khi cập nhật trạng thái:\n\n' + errorMessage)
      }
    }
  }
}

// Hủy đơn hàng với lý do
const openCancelModal = (order) => {
  cancelingOrder.value = order
  cancelReason.value = ''
  showCancelModal.value = true
}

const closeCancelModal = () => {
  showCancelModal.value = false
  cancelingOrder.value = null
  cancelReason.value = ''
}

const confirmCancelOrder = async () => {
  if (!cancelReason.value || cancelReason.value.trim().length === 0) {
    alert('Vui lòng nhập lý do hủy đơn hàng')
    return
  }

  try {
    // Sử dụng apiService để tự động thêm JWT token
    const response = await apiService.client.delete(`/don-hang/${cancelingOrder.value.id}`, {
      data: { lyDoHuy: cancelReason.value.trim() }
    })

    console.log('Order cancelled:', response.data)

    // Cập nhật trạng thái đơn hàng trong danh sách
    const index = orders.value.findIndex((o) => o.id === cancelingOrder.value.id)
    if (index !== -1) {
      const statusInfo = normalizeOrderStatus(STATUS_CODES.CANCELLED)
      orders.value[index] = {
        ...orders.value[index],
        trangThai: statusInfo.code,
        statusCode: statusInfo.code,
        statusLabel: statusInfo.label,
        statusClass: statusInfo.badgeClass,
        statusColor: statusInfo.color,
        lyDoHuy: cancelReason.value.trim(),
        capNhatLuc: new Date().toISOString(),
      }
    }

    alert('✅ Đã hủy đơn hàng thành công!')
    closeCancelModal()
  } catch (err) {
    console.error('Error cancelling order:', err)
    const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message || 'Lỗi không xác định'
    alert('❌ Có lỗi khi hủy đơn hàng:\n\n' + errorMessage)
  }
}

// ======= WATCHERS =======
// Validate date range - alert if dateTo is before dateFrom
watch([dateFrom, dateTo], () => {
  if (dateFrom.value && dateTo.value) {
    const fromDate = new Date(dateFrom.value)
    const toDate = new Date(dateTo.value)
    
    if (toDate < fromDate) {
      alert('❌ Bạn không thể lọc ngày quá khứ! Ngày kết thúc phải sau ngày bắt đầu.')
      dateTo.value = '' // Clear the invalid date
      return
    }
  }
}, { deep: true })

// Watch for filter changes and refetch orders
watch([selectedStatus, selectedPayment, dateFrom, dateTo, amountRange], () => {
  fetchOrders()
}, { deep: true })

// Watch search query with debounce
let searchTimeout = null
watch(searchQuery, () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    fetchOrders()
  }, 500) // Wait 500ms after user stops typing
})

// ======= LIFECYCLE =======
onMounted(() => {
  fetchOrders()
})
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

.stat-card.shipping {
  border-left: 4px solid #ff6b6b;
}

.stat-card.shipping .stat-icon {
  background: #ff6b6b;
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

.stat-card.delivered .stat-icon {
  background: #28a745;
}

.stat-card.payment-pending .stat-icon {
  background: #ffc107;
}

.stat-card.payment-pending {
  border-left: 4px solid #ffc107;
}

.stat-card.payment-paid .stat-icon {
  background: #28a745;
}

.stat-card.payment-paid {
  border-left: 4px solid #28a745;
}

.stat-card.payment-paid .stat-icon {
  background: #28a745;
}

.stat-content {
  flex: 1;
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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.35rem 0.85rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  min-width: 110px;
}

.order-info-table td {
  padding: 0.5rem 0.75rem;
  vertical-align: middle;
  border-top: none;
}

.order-info-table td:first-child {
  width: 38%;
  color: #6c757d;
  font-weight: 500;
  white-space: nowrap;
}

.order-info-table td:last-child {
  width: 62%;
  word-break: break-word;
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
