<template>
  <div class="users-page-root">
    <div class="admin-users">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <h1 class="page-title">Quản lý người dùng</h1>
            <p class="page-subtitle">Quản lý tài khoản khách hàng và nhân viên</p>
          </div>
          <div class="header-actions" v-if="isAdmin">
            <router-link to="/admin/register-staff" class="btn btn-primary btn-add-staff">
              <i class="bi bi-person-plus-fill me-2"></i>
              <span>Thêm nhân viên</span>
            </router-link>
          </div>
        </div>
        <div class="header-stats-row">
          <div class="stat-card">
            <div class="stat-icon bg-primary-subtle">
              <i class="bi bi-people-fill text-primary"></i>
            </div>
            <div class="stat-content">
              <span class="stat-label">Tổng người dùng</span>
              <span class="stat-value">{{ totalUsers }}</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon bg-info-subtle">
              <i class="bi bi-person-fill text-info"></i>
            </div>
            <div class="stat-content">
              <span class="stat-label">Khách hàng</span>
              <span class="stat-value">{{ customerCount }}</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon bg-success-subtle">
              <i class="bi bi-person-badge-fill text-success"></i>
            </div>
            <div class="stat-content">
              <span class="stat-label">Nhân viên</span>
              <span class="stat-value">{{ staffCount }}</span>
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
              placeholder="Tìm kiếm người dùng, email, số điện thoại..."
              v-model="searchQuery"
            />
          </div>
          <button class="btn btn-outline-primary" @click="toggleAdvancedFilters">
            <i class="bi bi-funnel me-1"></i>Bộ lọc nâng cao
            <i
              :class="showAdvancedFilters ? 'bi bi-caret-up' : 'bi bi-caret-down'"
              class="ms-1"
            ></i>
          </button>
          <button class="btn btn-outline-secondary" @click="clearFilters">
            <i class="bi bi-arrow-clockwise me-1"></i>Xóa bộ lọc
          </button>
        </div>

        <!-- Advanced Filters -->
        <div v-if="showAdvancedFilters" class="advanced-filters">
          <div class="row g-3">
            <div class="col-md-3">
              <label class="form-label">Vai trò</label>
              <select class="form-select" v-model="selectedRole">
                <option value="">Tất cả vai trò</option>
                <option value="admin">Quản trị viên</option>
                <option value="staff">Nhân viên</option>
                <option value="customer">Khách hàng</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">Trạng thái</label>
              <select class="form-select" v-model="selectedStatus">
                <option value="">Tất cả trạng thái</option>
                <option value="active">Hoạt động</option>
                <option value="inactive">Không hoạt động</option>
                <option value="banned">Bị cấm</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">Ngày đăng ký</label>
              <input type="date" class="form-control" v-model="registrationDate" />
            </div>
            <div class="col-md-3">
              <label class="form-label">Sắp xếp</label>
              <select class="form-select" v-model="sortBy">
                <option value="newest">Mới nhất</option>
                <option value="oldest">Cũ nhất</option>
                <option value="name-asc">Tên A-Z</option>
                <option value="name-desc">Tên Z-A</option>
                <option value="orders-high">Đơn hàng nhiều nhất</option>
                <option value="orders-low">Đơn hàng ít nhất</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label">Loại người dùng</label>
              <div class="user-type-filters">
                <span
                  v-for="type in userTypes"
                  :key="type.value"
                  :class="['type-filter', { active: selectedUserTypes.includes(type.value) }]"
                  @click="toggleUserType(type.value)"
                >
                  <i :class="type.icon"></i>
                  {{ type.label }}
                </span>
              </div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Hiển thị</label>
              <select class="form-select" v-model="viewMode">
                <option value="table">Table View</option>
                <option value="grid">Grid View</option>
                <option value="cards">Cards View</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- User Statistics -->
      <div class="user-stats-section">
        <div class="row g-3">
          <div class="col-md-3">
            <div class="stat-card total">
              <div class="stat-icon">
                <i class="bi bi-people"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.total }}</div>
                <div class="stat-label">Tổng người dùng</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stat-card active">
              <div class="stat-icon">
                <i class="bi bi-check-circle"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.active }}</div>
                <div class="stat-label">Hoạt động</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stat-card customers">
              <div class="stat-icon">
                <i class="bi bi-cart3"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.customers }}</div>
                <div class="stat-label">Khách hàng</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stat-card staff">
              <div class="stat-icon">
                <i class="bi bi-person-gear"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.staff }}</div>
                <div class="stat-label">Nhân viên</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Users Content -->
      <div class="users-content">
        <!-- Table View -->
        <div v-if="viewMode === 'table'" class="users-table">
          <div class="table-responsive">
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
                  <th @click="sortUsers('name')" class="sortable">
                    Người dùng <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th>Liên hệ</th>
                  <th @click="sortUsers('role')" class="sortable">
                    Vai trò <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th @click="sortUsers('orderCount')" class="sortable">
                    Đơn hàng <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th @click="sortUsers('totalSpent')" class="sortable">
                    Tổng chi tiêu <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th @click="sortUsers('status')" class="sortable">
                    Trạng thái <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th @click="sortUsers('createdAt')" class="sortable">
                    Ngày tham gia <i class="bi bi-arrows-vertical ms-1"></i>
                  </th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in paginatedUsers" :key="user.id">
                  <td>
                    <input
                      type="checkbox"
                      class="form-check-input"
                      v-model="selectedUsers"
                      :value="user.id"
                    />
                  </td>
                  <td>
                    <div class="user-info">
                      <div class="user-avatar">
                        <img
                          :src="user.avatar || avatarUrl(user.name, 40)"
                          :alt="user.name"
                          class="avatar-img"
                        />
                        <span :class="['status-indicator', getStatusClass(user.status)]"></span>
                      </div>
                      <div class="user-details">
                        <div class="user-name">{{ user.name }}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="contact-info">
                      <div class="contact-email">{{ user.email }}</div>
                      <div class="contact-phone">{{ user.phone || 'Chưa cập nhật' }}</div>
                    </div>
                  </td>
                  <td>
                    <span :class="['role-badge', getRoleClass(user.role)]">
                      {{ getRoleText(user.role) }}
                    </span>
                  </td>
                  <td>
                    <div class="order-stats">
                      <div class="order-count">{{ user.orderCount }} đơn</div>
                      <div class="order-value">{{ formatCurrency(user.avgOrderValue) }}/đơn</div>
                    </div>
                  </td>
                  <td>
                    <div class="spending-info">
                      <div class="total-spent">{{ formatCurrency(user.totalSpent) }}</div>
                      <div class="avg-order">TB: {{ formatCurrency(user.avgOrderValue) }}</div>
                    </div>
                  </td>
                  <td>
                    <span :class="['status-badge', getStatusClass(user.status)]">
                      {{ getStatusText(user.status) }}
                    </span>
                  </td>
                  <td>
                    <div class="join-date">
                      <div>{{ formatDate(user.createdAt) }}</div>
                      <div class="join-time">{{ formatTime(user.createdAt) }}</div>
                    </div>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button
                        class="btn btn-sm btn-outline-primary"
                        @click="viewUser(user)"
                        title="Xem chi tiết"
                      >
                        <i class="bi bi-eye"></i>
                      </button>
                      <button
                        v-if="isAdmin && user.role !== 'admin'"
                        class="btn btn-sm btn-outline-success"
                        @click="editUser(user)"
                        title="Chỉnh sửa"
                      >
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button
                        v-if="isAdmin && user.role !== 'admin'"
                        class="btn btn-sm btn-outline-danger"
                        @click="softDeleteUser(user)"
                        title="Xoá"
                      >
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Grid View -->
        <div v-else-if="viewMode === 'grid'" class="users-grid">
          <div class="row g-3">
            <div v-for="user in paginatedUsers" :key="user.id" class="col-lg-4 col-md-6">
              <div class="user-grid-card">
                <div class="card-header">
                  <div class="user-avatar-large">
                    <img
                      :src="user.avatar || avatarUrl(user.name, 60)"
                      :alt="user.name"
                      class="avatar-large-img"
                    />
                    <span :class="['status-indicator', getStatusClass(user.status)]"></span>
                  </div>
                  <div class="user-info-header">
                    <h6 class="user-name">{{ user.name }}</h6>
                    <span :class="['role-badge', getRoleClass(user.role)]">
                      {{ getRoleText(user.role) }}
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <div class="user-details-grid">
                    <div class="detail-item">
                      <i class="bi bi-envelope"></i>
                      <span>{{ user.email }}</span>
                    </div>
                    <div class="detail-item">
                      <i class="bi bi-telephone"></i>
                      <span>{{ user.phone || 'Chưa cập nhật' }}</span>
                    </div>
                    <div class="detail-item">
                      <i class="bi bi-cart3"></i>
                      <span>{{ user.orderCount }} đơn hàng</span>
                    </div>
                    <div class="detail-item">
                      <i class="bi bi-currency-dollar"></i>
                      <span>{{ formatCurrency(user.totalSpent) }}</span>
                    </div>
                  </div>
                  <div class="user-stats-grid">
                    <div class="stat-item">
                      <span class="stat-label">Đơn hàng:</span>
                      <span class="stat-value">{{ user.orderCount }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">Chi tiêu:</span>
                      <span class="stat-value">{{ formatCurrency(user.totalSpent) }}</span>
                    </div>
                  </div>
                </div>
                <div class="card-footer">
                  <div class="action-buttons-grid">
                    <button
                      class="btn btn-sm btn-outline-primary"
                      @click="viewUser(user)"
                      title="Xem chi tiết"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      v-if="isAdmin && user.role !== 'admin'"
                      class="btn btn-sm btn-outline-success"
                      @click="editUser(user)"
                      title="Chỉnh sửa"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      v-if="isAdmin && user.role !== 'admin'"
                      class="btn btn-sm btn-outline-danger"
                      @click="softDeleteUser(user)"
                      title="Xoá"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Cards View -->
        <div v-else-if="viewMode === 'cards'" class="users-cards">
          <div class="row g-3">
            <div v-for="user in paginatedUsers" :key="user.id" class="col-lg-6 col-xl-4">
              <div class="user-card">
                <div class="card-header">
                  <div class="user-avatar-section">
                    <div class="user-avatar-card">
                      <img
                        :src="user.avatar || avatarUrl(user.name, 50)"
                        :alt="user.name"
                        class="avatar-card-img"
                      />
                      <span :class="['status-indicator', getStatusClass(user.status)]"></span>
                    </div>
                    <div class="user-info-card">
                      <h6 class="user-name">{{ user.name }}</h6>
                      <p class="user-email">{{ user.email }}</p>
                    </div>
                  </div>
                  <span :class="['role-badge', getRoleClass(user.role)]">
                    {{ getRoleText(user.role) }}
                  </span>
                </div>
                <div class="card-body">
                  <div class="user-metrics">
                    <div class="metric-item">
                      <div class="metric-icon">
                        <i class="bi bi-cart3"></i>
                      </div>
                      <div class="metric-content">
                        <div class="metric-value">{{ user.orderCount }}</div>
                        <div class="metric-label">Đơn hàng</div>
                      </div>
                    </div>
                    <div class="metric-item">
                      <div class="metric-icon">
                        <i class="bi bi-currency-dollar"></i>
                      </div>
                      <div class="metric-content">
                        <div class="metric-value">{{ formatCurrency(user.totalSpent) }}</div>
                        <div class="metric-label">Chi tiêu</div>
                      </div>
                    </div>
                  </div>
                  <div class="user-contact-info">
                    <div class="contact-item">
                      <i class="bi bi-telephone"></i>
                      <span>{{ user.phone || 'Chưa cập nhật' }}</span>
                    </div>
                    <div class="contact-item">
                      <i class="bi bi-calendar"></i>
                      <span>Tham gia: {{ formatDate(user.createdAt) }}</span>
                    </div>
                  </div>
                </div>
                <div class="card-footer">
                  <div class="action-buttons-card">
                    <button
                      class="btn btn-sm btn-outline-primary"
                      @click="viewUser(user)"
                      title="Xem chi tiết"
                    >
                      <i class="bi bi-eye"></i>
                    </button>
                    <button
                      v-if="isAdmin && user.role !== 'admin'"
                      class="btn btn-sm btn-outline-success"
                      @click="editUser(user)"
                      title="Chỉnh sửa"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button
                      v-if="isAdmin && user.role !== 'admin'"
                      class="btn btn-sm btn-outline-danger"
                      @click="softDeleteUser(user)"
                      title="Xoá"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
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
              Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} -
              {{ Math.min(currentPage * itemsPerPage, totalItems) }} trong tổng số
              {{ totalItems }} người dùng
            </div>
          </div>
          <div class="col-md-6">
            <nav>
              <ul class="pagination justify-content-end">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                  <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)"
                    >Trước</a
                  >
                </li>
                <li
                  class="page-item"
                  v-for="page in visiblePages"
                  :key="page"
                  :class="{ active: currentPage === page }"
                >
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
      <div v-if="selectedUsers.length > 0" class="bulk-actions">
        <div class="bulk-actions-content">
          <span class="selected-count">{{ selectedUsers.length }} người dùng đã chọn</span>
          <div class="bulk-buttons">
            <button class="btn btn-sm btn-outline-success" @click="bulkUpdateStatus('active')">
              <i class="bi bi-check me-1"></i>Kích hoạt
            </button>
            <button class="btn btn-sm btn-outline-warning" @click="bulkUpdateStatus('inactive')">
              <i class="bi bi-pause me-1"></i>Ngừng hoạt động
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="bulkUpdateStatus('banned')">
              <i class="bi bi-ban me-1"></i>Cấm
            </button>
            <button class="btn btn-sm btn-outline-info" @click="bulkExport">
              <i class="bi bi-download me-1"></i>Xuất dữ liệu
            </button>
          </div>
        </div>
      </div>

      <!-- User Detail Modal -->
      <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
        <div class="modal-content modal-lg" @click.stop>
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết người dùng: {{ selectedUser?.name }}</h5>
            <button class="btn-close" @click="closeUserModal">
              <i class="bi bi-x"></i>
            </button>
          </div>
          <div class="modal-body" v-if="selectedUser">
            <div class="row">
              <!-- User Profile -->
              <div class="col-md-4">
                <div class="user-profile-card">
                  <div class="profile-avatar">
                    <img
                      :src="selectedUser.avatar || avatarUrl(selectedUser.name, 120)"
                      :alt="selectedUser.name"
                      class="avatar-large"
                    />
                    <span
                      :class="['status-indicator-large', getStatusClass(selectedUser.status)]"
                    ></span>
                  </div>
                  <h5 class="profile-name">{{ selectedUser.name }}</h5>
                  <p class="profile-role">
                    <span :class="['role-badge', getRoleClass(selectedUser.role)]">
                      {{ getRoleText(selectedUser.role) }}
                    </span>
                  </p>
                  <div class="profile-stats">
                    <div class="stat-item">
                      <span class="stat-label">Đơn hàng:</span>
                      <span class="stat-value">{{ selectedUser.orderCount }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">Tổng chi tiêu:</span>
                      <span class="stat-value">{{ formatCurrency(selectedUser.totalSpent) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- User Details -->
              <div class="col-md-8">
                <div class="user-details-tabs">
                  <ul class="nav nav-tabs" role="tablist">
                    <li class="nav-item">
                      <button
                        class="nav-link active"
                        data-bs-toggle="tab"
                        data-bs-target="#profile-tab"
                      >
                        Thông tin cá nhân
                      </button>
                    </li>
                    <li class="nav-item">
                      <button
                        class="nav-link"
                        data-bs-toggle="tab"
                        data-bs-target="#orders-tab"
                        @click="onOrdersTabClick"
                      >
                        Đơn hàng ({{ selectedUser.orderCount }})
                      </button>
                    </li>
                    <li class="nav-item">

                    </li>
                  </ul>

                  <div class="tab-content">
                    <!-- Profile Tab -->
                    <div class="tab-pane fade show active" id="profile-tab">
                      <div class="profile-details">
                        <table class="table table-sm">
                          <tbody>
                            <tr>
                              <td>Họ tên:</td>
                              <td>{{ selectedUser.name }}</td>
                            </tr>
                            <tr>
                              <td>Email:</td>
                              <td>{{ selectedUser.email }}</td>
                            </tr>
                            <tr>
                              <td>Số điện thoại:</td>
                              <td>{{ selectedUser.phone || 'Chưa cập nhật' }}</td>
                            </tr>
                            <tr>
                              <td>Địa chỉ:</td>
                              <td>{{ selectedUser.address || 'Chưa cập nhật' }}</td>
                            </tr>
                            <tr>
                              <td>Ngày sinh:</td>
                              <td>
                                {{
                                  selectedUser.birthday
                                    ? formatDate(selectedUser.birthday)
                                    : 'Chưa cập nhật'
                                }}
                              </td>
                            </tr>
                            <tr>
                              <td>Giới tính:</td>
                              <td>{{ selectedUser.gender || 'Chưa cập nhật' }}</td>
                            </tr>
                            <tr>
                              <td>Ngày tham gia:</td>
                              <td>{{ formatDateTime(selectedUser.createdAt) }}</td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>

                    <!-- Orders Tab -->
                    <div class="tab-pane fade" id="orders-tab">
                      <div class="orders-list">
                        <div v-if="ordersLoading" class="text-center py-4">
                          <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Đang tải...</span>
                          </div>
                        </div>
                        <div
                          v-else-if="userOrders && userOrders.length > 0"
                          class="table-responsive"
                        >
                          <table class="table table-sm">
                            <thead>
                              <tr>
                                <th>Mã đơn</th>
                                <th>Ngày đặt</th>
                                <th>Tổng tiền</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                              </tr>
                            </thead>
                            <tbody>
                              <tr v-for="order in userOrders" :key="order.id">
                                <td>{{ order.soDonHang || `#${order.id}` }}</td>
                                <td>{{ formatDate(order.taoLuc || order.createdAt) }}</td>
                                <td>{{ formatCurrency(order.tongThanhToan || order.total || 0) }}</td>
                                <td>
                                  <span :class="['status-badge', getOrderStatusClass(order.trangThai || order.status)]">
                                    {{ getOrderStatusText(order.trangThai || order.status) }}
                                  </span>
                                </td>
                                <td>
                                  <button
                                    class="btn btn-sm btn-outline-primary"
                                    @click="viewOrderDetail(order.id)"
                                    title="Xem chi tiết"
                                  >
                                    <i class="bi bi-eye"></i>
                                  </button>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                          <!-- Pagination for orders -->
                          <div v-if="ordersTotalPages > 1" class="d-flex justify-content-between align-items-center mt-3">
                            <div class="pagination-info">
                              Hiển thị {{ (ordersCurrentPage - 1) * ordersPageSize + 1 }} -
                              {{ Math.min(ordersCurrentPage * ordersPageSize, ordersTotalItems) }} trong tổng số
                              {{ ordersTotalItems }} đơn hàng
                            </div>
                            <nav>
                              <ul class="pagination mb-0">
                                <li class="page-item" :class="{ disabled: ordersCurrentPage === 1 }">
                                  <a class="page-link" href="#" @click.prevent="changeOrdersPage(ordersCurrentPage - 1)">
                                    Trước
                                  </a>
                                </li>
                                <li
                                  class="page-item"
                                  v-for="page in visibleOrdersPages"
                                  :key="page"
                                  :class="{ active: ordersCurrentPage === page }"
                                >
                                  <a class="page-link" href="#" @click.prevent="changeOrdersPage(page)">
                                    {{ page }}
                                  </a>
                                </li>
                                <li class="page-item" :class="{ disabled: ordersCurrentPage === ordersTotalPages }">
                                  <a class="page-link" href="#" @click.prevent="changeOrdersPage(ordersCurrentPage + 1)">
                                    Sau
                                  </a>
                                </li>
                              </ul>
                            </nav>
                          </div>
                        </div>
                        <div v-else class="empty-state">
                          <i class="bi bi-bag display-4 text-muted"></i>
                          <p class="text-muted">Chưa có đơn hàng nào</p>
                        </div>
                      </div>
                    </div>

                    <!-- Activity Tab -->
                    <div class="tab-pane fade" id="activity-tab">
                      <div class="activity-list">
                        <div v-if="selectedUser.activities && selectedUser.activities.length > 0">
                          <div
                            v-for="activity in selectedUser.activities"
                            :key="activity.id"
                            class="activity-item"
                          >
                            <div class="activity-icon">
                              <i :class="getActivityIcon(activity.type)"></i>
                            </div>
                            <div class="activity-content">
                              <div class="activity-description">{{ activity.description }}</div>
                              <div class="activity-time">
                                {{ formatDateTime(activity.createdAt) }}
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
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeUserModal">Đóng</button>
            <button
              v-if="selectedUser && selectedUser.role !== 'admin'"
              type="button"
              class="btn btn-primary"
              @click="editUser(selectedUser)"
            >
              <i class="bi bi-pencil me-1"></i>Chỉnh sửa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <Modal
      v-model="showConfirm"
      :title="confirmTitle"
      :showFooter="true"
      :confirmDisabled="confirmLoading"
      @confirm="onConfirm"
    >
      <p>{{ confirmMessage }}</p>
    </Modal>

    <!-- Edit User Modal -->
    <div v-if="showEditModal" class="edit-modal-overlay" @click="closeEditModal">
      <div class="edit-modal-content" @click.stop>
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-pencil-square me-2"></i>Chỉnh sửa thông tin người dùng
          </h5>
          <button type="button" class="btn-close" @click="closeEditModal">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveUserEdit">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label"> <i class="bi bi-envelope me-1"></i>Email </label>
                <input
                  type="email"
                  class="form-control"
                  v-model="editForm.email"
                  placeholder="Nhập email"
                  required
                  :disabled="!canEditInfo"
                />
              </div>
              <div class="col-md-6">
                <label class="form-label">
                  <i class="bi bi-telephone me-1"></i>Số điện thoại
                </label>
                <input
                  type="tel"
                  class="form-control"
                  v-model="editForm.phone"
                  placeholder="Nhập số điện thoại"
                  :disabled="!canEditInfo"
                />
              </div>
              <div class="col-md-6">
                <label class="form-label"> <i class="bi bi-person-badge me-1"></i>Vai trò </label>
                <select class="form-select" v-model="editForm.role" :disabled="!canEditInfo">
                  <option value="customer">Khách hàng</option>
                  <option value="staff">Nhân viên</option>
                  <option value="admin">Quản trị viên</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label">
                  <i class="bi bi-check-circle me-1"></i>Trạng thái
                </label>
                <select class="form-select" v-model="editForm.status" :disabled="!canEditStatus">
                  <option value="active">Hoạt động</option>
                  <option value="inactive">Ngừng hoạt động</option>
                  <option value="banned">Bị cấm</option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label"> <i class="bi bi-card-text me-1"></i>Ghi chú </label>
                <textarea
                  class="form-control"
                  v-model="editForm.note"
                  rows="3"
                  placeholder="Ghi chú về người dùng này..."
                ></textarea>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeEditModal">
            <i class="bi bi-x-circle me-1"></i>Hủy
          </button>
          <button
            type="button"
            class="btn btn-primary"
            @click="saveUserEdit"
            :disabled="editLoading || !canSave"
            v-if="canSave"
          >
            <span v-if="editLoading" class="spinner-border spinner-border-sm me-1"></span>
            <i v-else class="bi bi-check-circle me-1"></i>
            Lưu thay đổi
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({
  name: 'AdminUsersPage',
})
import { ref, computed, onMounted, nextTick } from 'vue'
import api from '@/services/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import Modal from '@/components/common/Modal.vue'

// Reactive data
const searchQuery = ref('')
const selectedRole = ref('')
const selectedStatus = ref('')
const selectedDate = ref('')
const registrationDate = ref('')
const sortBy = ref('newest')
const selectedUsers = ref([])
const selectAll = ref(false)
const currentPage = ref(1)
const itemsPerPage = 10
const showUserModal = ref(false)
const selectedUser = ref(null)
const showAdvancedFilters = ref(false)
const viewMode = ref('table')
const selectedUserTypes = ref([])
const sortField = ref('')
const sortDirection = ref('desc')

// Edit modal
const showEditModal = ref(false)
const editLoading = ref(false)
const editForm = ref({
  id: null,
  email: '',
  phone: '',
  role: 'customer',
  status: 'active',
  note: '',
})

// Orders state
const userOrders = ref([])
const ordersLoading = ref(false)
const ordersCurrentPage = ref(1)
const ordersPageSize = ref(10)
const ordersTotalItems = ref(0)
const ordersTotalPages = ref(0)

// User types for filtering
const userTypes = ref([
  { value: 'vip', label: 'VIP', icon: 'bi bi-crown' },
  { value: 'new', label: 'Mới', icon: 'bi bi-star' },
  { value: 'inactive', label: 'Không hoạt động', icon: 'bi bi-clock' },
  { value: 'high-value', label: 'Chi tiêu cao', icon: 'bi bi-currency-dollar' },
])

// Dữ liệu người dùng lấy từ API
const users = ref([])

const { success: toastSuccess, error: toastError } = useToast()

// Fetch from backend admin API
const fetchUsers = async () => {
  try {
    const res = await api.adminUsers.getAll({ page: 0, size: 50 })
    const content = res?.content || res?.data || []
    users.value = Array.isArray(content)
      ? content.map((u) => {
          const email = u.email || ''
          const baseName = email
            ? email.split('@')[0] || 'Người dùng'
            : u.soDienThoai || 'Người dùng'
          const roleMap = { ADM: 'admin', STF: 'staff', CUS: 'customer', GST: 'customer' }

          // Lấy orderCount và totalSpent từ backend
          const orderCount = u.orderCount || 0
          const totalSpent = u.totalSpent || 0
          const avgOrderValue = orderCount > 0 ? totalSpent / orderCount : 0

          return {
            id: u.id,
            name: baseName,
            email: u.email || 'N/A',
            phone: u.soDienThoai || 'Chưa cập nhật',
            address: '—',
            birthday: null,
            gender: null,
            role: roleMap[u.vaiTroMa] || 'customer',
            status: u.trangThai ? 'active' : 'inactive',
            avatar: null,
            orderCount,
            totalSpent,
            avgOrderValue,
            createdAt: u.taoLuc ? new Date(u.taoLuc) : new Date(),
            lastLogin: null,
            isVip: totalSpent > 10000000, // VIP nếu chi tiêu > 10 triệu
            isNew: false,
            isHighValue: totalSpent > 5000000, // High value nếu chi tiêu > 5 triệu
            orders: [],
            activities: [],
          }
        })
      : []
  } catch (e) {
    console.error('Không thể tải danh sách người dùng từ API.', e)
    users.value = []
    toastError('Không thể tải danh sách người dùng từ server')
  }
}

onMounted(() => {
  fetchUsers()
})

// Computed
const totalUsers = computed(() => users.value.length)
const customerCount = computed(() => users.value.filter((user) => user.role === 'customer').length)
const staffCount = computed(
  () => users.value.filter((user) => user.role === 'staff' || user.role === 'admin').length,
)

// User statistics
const userStats = computed(() => ({
  total: users.value.length,
  active: users.value.filter((user) => user.status === 'active').length,
  customers: users.value.filter((user) => user.role === 'customer').length,
  staff: users.value.filter((user) => user.role === 'staff' || user.role === 'admin').length,
}))

const filteredUsers = computed(() => {
  let filtered = users.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (user) =>
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        user.phone?.toLowerCase().includes(query),
    )
  }

  if (selectedRole.value) {
    filtered = filtered.filter((user) => user.role === selectedRole.value)
  }

  if (selectedStatus.value) {
    filtered = filtered.filter((user) => user.status === selectedStatus.value)
  }

  if (registrationDate.value) {
    const selected = new Date(registrationDate.value)
    filtered = filtered.filter((user) => {
      const userDate = new Date(user.createdAt)
      return userDate.toDateString() === selected.toDateString()
    })
  }

  // User type filters
  if (selectedUserTypes.value.length > 0) {
    filtered = filtered.filter((user) => {
      return selectedUserTypes.value.some((type) => {
        switch (type) {
          case 'vip':
            return user.isVip
          case 'new':
            return user.isNew
          case 'inactive':
            return user.status === 'inactive'
          case 'high-value':
            return user.isHighValue
          default:
            return false
        }
      })
    })
  }

  // Sorting
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'oldest':
        return new Date(a.createdAt) - new Date(b.createdAt)
      case 'name-asc':
        return a.name.localeCompare(b.name)
      case 'name-desc':
        return b.name.localeCompare(a.name)
      case 'orders-high':
        return b.orderCount - a.orderCount
      case 'orders-low':
        return a.orderCount - b.orderCount
      case 'newest':
      default:
        return new Date(b.createdAt) - new Date(a.createdAt)
    }
  })

  return filtered
})

const totalItems = computed(() => filteredUsers.value.length)
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

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredUsers.value.slice(start, end)
})

// Methods
const avatarUrl = (name = 'U', size = 40) => {
  const initial = (name && name[0] ? name[0] : 'U').toUpperCase()
  const svg = `<?xml version="1.0" encoding="UTF-8"?>
  <svg xmlns='http://www.w3.org/2000/svg' width='${size}' height='${size}'>
    <rect width='100%' height='100%' rx='${Math.floor(size / 6)}' ry='${Math.floor(size / 6)}' fill='#3498db'/>
    <text x='50%' y='54%' dominant-baseline='middle' text-anchor='middle' font-family='Arial, sans-serif' font-size='${Math.floor(size * 0.5)}' fill='#ffffff'>${initial}</text>
  </svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (date) => {
  if (!date) return 'Chưa cập nhật'
  try {
    const dateObj = date instanceof Date ? date : new Date(date)
    if (isNaN(dateObj.getTime())) return 'Chưa cập nhật'
    return new Intl.DateTimeFormat('vi-VN').format(dateObj)
  } catch (e) {
    return 'Chưa cập nhật'
  }
}

const formatTime = (date) => {
  if (!date) return ''
  try {
    const dateObj = date instanceof Date ? date : new Date(date)
    if (isNaN(dateObj.getTime())) return ''
    return new Intl.DateTimeFormat('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(dateObj)
  } catch (e) {
    return ''
  }
}

const formatDateTime = (date) => {
  if (!date) return 'Chưa cập nhật'
  try {
    const dateObj = date instanceof Date ? date : new Date(date)
    if (isNaN(dateObj.getTime())) return 'Chưa cập nhật'
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(dateObj)
  } catch (e) {
    return 'Chưa cập nhật'
  }
}

const getRoleText = (role) => {
  const roles = {
    customer: 'Khách hàng',
    staff: 'Nhân viên',
    admin: 'Quản trị viên',
  }
  return roles[role] || role
}

const getRoleClass = (role) => {
  const classes = {
    customer: 'bg-primary',
    staff: 'bg-success',
    admin: 'bg-danger',
  }
  return classes[role] || 'bg-secondary'
}

const getStatusText = (status) => {
  const statuses = {
    active: 'Hoạt động',
    inactive: 'Ngừng hoạt động',
    banned: 'Bị cấm',
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    active: 'bg-success',
    inactive: 'bg-warning',
    banned: 'bg-danger',
  }
  return classes[status] || 'bg-secondary'
}

// Order status mapping
const getOrderStatusText = (status) => {
  if (!status) return 'Không xác định'
  
  const statusStr = String(status).toUpperCase()
  const statusMap = {
    // English statuses
    'PENDING': 'Chờ xác nhận',
    'CHO_XAC_NHAN': 'Chờ xác nhận',
    'SHIPPING': 'Đang giao',
    'DANG_GIAO': 'Đang giao',
    'DELIVERING': 'Đang giao',
    'COMPLETED': 'Hoàn tất',
    'HOAN_TAT': 'Hoàn tất',
    'FINISHED': 'Hoàn tất',
    'CANCELLED': 'Đã hủy',
    'DA_HUY': 'Đã hủy',
    'CANCELED': 'Đã hủy',
    // Vietnamese statuses (keep as is)
    'Chờ xác nhận': 'Chờ xác nhận',
    'Đang giao': 'Đang giao',
    'Hoàn tất': 'Hoàn tất',
    'Đã hủy': 'Đã hủy',
  }
  
  return statusMap[statusStr] || statusMap[status] || status
}

const getOrderStatusClass = (status) => {
  if (!status) return 'bg-secondary'
  
  const statusStr = String(status).toUpperCase()
  const classMap = {
    'PENDING': 'bg-warning',
    'CHO_XAC_NHAN': 'bg-warning',
    'SHIPPING': 'bg-info',
    'DANG_GIAO': 'bg-info',
    'DELIVERING': 'bg-info',
    'COMPLETED': 'bg-success',
    'HOAN_TAT': 'bg-success',
    'FINISHED': 'bg-success',
    'CANCELLED': 'bg-danger',
    'DA_HUY': 'bg-danger',
    'CANCELED': 'bg-danger',
    // Vietnamese
    'Chờ xác nhận': 'bg-warning',
    'Đang giao': 'bg-info',
    'Hoàn tất': 'bg-success',
    'Đã hủy': 'bg-danger',
  }
  
  return classMap[statusStr] || classMap[status] || 'bg-secondary'
}

const getActivityIcon = (type) => {
  const icons = {
    login: 'bi bi-box-arrow-in-right',
    order: 'bi bi-bag',
    profile: 'bi bi-person',
    password: 'bi bi-key',
  }
  return icons[type] || 'bi bi-circle'
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedRole.value = ''
  selectedStatus.value = ''
  selectedDate.value = ''
  registrationDate.value = ''
  selectedUserTypes.value = []
  sortBy.value = 'newest'
  currentPage.value = 1
}

const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

const toggleUserType = (type) => {
  const index = selectedUserTypes.value.indexOf(type)
  if (index > -1) {
    selectedUserTypes.value.splice(index, 1)
  } else {
    selectedUserTypes.value.push(type)
  }
}

const sortUsers = (field) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'asc'
  }

  // Update sortBy based on field
  switch (field) {
    case 'name':
      sortBy.value = sortDirection.value === 'asc' ? 'name-asc' : 'name-desc'
      break
    case 'role':
      // Custom sorting for role
      break
    case 'orderCount':
      sortBy.value = sortDirection.value === 'asc' ? 'orders-low' : 'orders-high'
      break
    case 'totalSpent':
      // Custom sorting for totalSpent
      break
    case 'status':
      // Custom sorting for status
      break
    case 'createdAt':
      sortBy.value = sortDirection.value === 'asc' ? 'oldest' : 'newest'
      break
  }
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedUsers.value = paginatedUsers.value.map((u) => u.id)
  } else {
    selectedUsers.value = []
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const viewUser = (user) => {
  selectedUser.value = user
  showUserModal.value = true
  // Reset orders state when opening modal
  userOrders.value = []
  ordersCurrentPage.value = 1
  ordersTotalItems.value = 0
  ordersTotalPages.value = 0

  // Setup Bootstrap tab event listener after modal is shown
  nextTick(() => {
    const ordersTab = document.getElementById('orders-tab')
    if (ordersTab) {
      // Remove existing listener if any
      ordersTab.removeEventListener('shown.bs.tab', handleOrdersTabShown)
      // Add new listener
      ordersTab.addEventListener('shown.bs.tab', handleOrdersTabShown)
    }
  })
}

// Handle orders tab shown event
const handleOrdersTabShown = () => {
  if (selectedUser.value && selectedUser.value.id) {
    fetchUserOrders()
  }
}

const closeUserModal = () => {
  showUserModal.value = false
  selectedUser.value = null
  // Reset orders state when closing modal
  userOrders.value = []
  ordersCurrentPage.value = 1
  ordersTotalItems.value = 0
  ordersTotalPages.value = 0
}

// Fetch orders for selected user
const fetchUserOrders = async () => {
  if (!selectedUser.value || !selectedUser.value.id) return

  try {
    ordersLoading.value = true
    const response = await api.adminUsers.getOrdersByUserId(selectedUser.value.id, {
      page: ordersCurrentPage.value - 1,
      size: ordersPageSize.value,
    })

    const data = response || {}
    userOrders.value = data.content || []
    ordersTotalItems.value = data.totalItems || 0
    ordersTotalPages.value = data.totalPages || 0

    // Update current page if needed
    if (data.currentPage !== undefined) {
      ordersCurrentPage.value = data.currentPage + 1
    }
  } catch (error) {
    console.error('Error fetching user orders:', error)
    toastError('Không thể tải danh sách đơn hàng')
    userOrders.value = []
  } finally {
    ordersLoading.value = false
  }
}

// Pagination for orders
const visibleOrdersPages = computed(() => {
  const pages = []
  const start = Math.max(1, ordersCurrentPage.value - 2)
  const end = Math.min(ordersTotalPages.value, start + 4)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const changeOrdersPage = (page) => {
  if (page >= 1 && page <= ordersTotalPages.value) {
    ordersCurrentPage.value = page
    fetchUserOrders()
  }
}

// View order detail
const viewOrderDetail = (orderId) => {
  // Navigate to order detail page
  window.open(`/admin/orders?orderId=${orderId}`, '_blank')
}

// Handle orders tab click
const onOrdersTabClick = () => {
  // Use nextTick to ensure tab is shown before fetching
  nextTick(() => {
    // Fetch orders when tab is clicked
    if (selectedUser.value && selectedUser.value.id) {
      fetchUserOrders()
    }
  })
}


const userStore = useUserStore()
const isAdmin = computed(() => {
  const role = userStore.userRole
  console.log('Current user role:', role)
  return role === 'admin' || role === 'ADM'
})

const isStaff = computed(() => {
  const role = userStore.userRole
  return role === 'staff' || role === 'STF'
})

const canEditInfo = computed(() => isAdmin.value)
const canEditStatus = computed(() => {
  if (isAdmin.value) return true
  if (isStaff.value) {
    return editForm.value.role === 'customer'
  }
  return false
})
const canSave = computed(() => canEditInfo.value || canEditStatus.value)

// Confirm modal state
const showConfirm = ref(false)
const confirmTitle = ref('Xác nhận')
const confirmMessage = ref('Bạn có chắc chắn?')
const confirmLoading = ref(false)
let confirmAction = null

const openConfirm = (title, message, action) => {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmLoading.value = false
  confirmAction = action
  showConfirm.value = true
}

const onConfirm = async () => {
  if (!confirmAction) return
  try {
    confirmLoading.value = true
    await confirmAction()
    showConfirm.value = false
  } finally {
    confirmLoading.value = false
    confirmAction = null
  }
}

const editUser = (user) => {
  // Cho phép ADMIN và STAFF mở modal (STAFF chỉ được đổi status cho customer, form sẽ readonly)
  if (!isAdmin.value && !isStaff.value) return

  editForm.value = {
    id: user.id,
    email: user.email || '',
    phone: user.phone || '',
    role: user.role || 'customer',
    status: user.status || 'active',
    note: user.note || '',
  }

  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editForm.value = {
    id: null,
    email: '',
    phone: '',
    role: 'customer',
    status: 'active',
    note: '',
  }
}

const saveUserEdit = async () => {
  if (!canSave.value) return

  try {
    editLoading.value = true

    const roleMap = {
      customer: 'CUS',
      staff: 'STF',
      admin: 'ADM',
    }

    let payload = {}

    if (canEditInfo.value) {
      payload = {
        email: editForm.value.email,
        soDienThoai: editForm.value.phone,
        vaiTroMa: roleMap[editForm.value.role] || 'CUS',
        trangThai: editForm.value.status === 'active',
      }
    } else if (canEditStatus.value && isStaff.value) {
      // STAFF chỉ được đổi trạng thái cho customer – UI đã chặn, backend kiểm lại
      payload = {
        trangThai: editForm.value.status === 'active',
      }
    } else {
      toastError('Bạn không có quyền cập nhật thông tin này')
      return
    }

    await api.adminUsers.update(editForm.value.id, payload)

    // Update user in list
    const userIndex = users.value.findIndex((u) => u.id === editForm.value.id)
    if (userIndex !== -1) {
      if (canEditInfo.value) {
        users.value[userIndex].email = editForm.value.email
        users.value[userIndex].phone = editForm.value.phone
        users.value[userIndex].role = editForm.value.role
      }
      users.value[userIndex].status = editForm.value.status
      users.value[userIndex].note = editForm.value.note
    }

    toastSuccess('Cập nhật người dùng thành công')
    closeEditModal()
  } catch (e) {
    toastError('Không thể cập nhật người dùng: ' + (e.message || 'Lỗi không xác định'))
  } finally {
    editLoading.value = false
  }
}

const softDeleteUser = async (user) => {
  if (!isAdmin.value) return
  openConfirm(
    'Xóa tài khoản',
    `Tài khoản của ${user.name} sẽ chuyển sang trạng thái "Ngừng hoạt động". Người dùng sẽ không thể đăng nhập nhưng dữ liệu vẫn được giữ lại. Bạn có chắc chắn?`,
    async () => {
      try {
        // Call API to soft delete (set trangThai = false)
        await api.adminUsers.update(user.id, { trangThai: false })

        // Update user status in list
        user.status = 'inactive'

        toastSuccess('Đã chuyển tài khoản sang trạng thái "Ngừng hoạt động"')
      } catch (e) {
        toastError('Không thể cập nhật trạng thái: ' + (e.message || 'Lỗi không xác định'))
        throw e
      }
    },
  )
}

const bulkUpdateStatus = async (status) => {
  if (selectedUsers.value.length === 0) return

  // Map status string -> Boolean trangThai
  const enabled = status === 'active'

  // RBAC FE: STAFF chỉ được bulk trên customer
  if (isStaff.value) {
    const invalidTargets = selectedUsers.value
      .map((id) => users.value.find((u) => u.id === id))
      .filter((u) => u && u.role !== 'customer')

    if (invalidTargets.length > 0) {
      toastError('Nhân viên chỉ được phép đổi trạng thái tài khoản khách hàng')
      return
    }
  }

  if (
    !confirm(
      `Bạn có chắc chắn muốn cập nhật trạng thái cho ${selectedUsers.value.length} người dùng?`,
    )
  ) {
    return
  }

  try {
    await api.adminUsers.bulkStatus({
      ids: selectedUsers.value,
      trangThai: enabled,
    })

    selectedUsers.value.forEach((userId) => {
      const user = users.value.find((u) => u.id === userId)
      if (user) {
        user.status = status
      }
    })

    toastSuccess('Cập nhật trạng thái hàng loạt thành công')
  } catch (e) {
    console.error('Bulk update status failed', e)
    toastError('Không thể cập nhật trạng thái hàng loạt: ' + (e.message || 'Lỗi không xác định'))
  } finally {
    selectedUsers.value = []
    selectAll.value = false
  }
}

const bulkExport = () => {
  // Implement export functionality
}

// Lifecycle
onMounted(() => {
  // Initialize users page
})
</script>

<style scoped>
.admin-users {
  width: 100%;
}

.page-header {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.header-left {
  flex: 1;
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
  font-size: 1rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-add-staff {
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.btn-add-staff:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.header-stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  font-size: 1.5rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: #6c757d;
  font-weight: 500;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
  padding-left: 2.75rem !important;
}

.users-table {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  position: relative;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.status-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator-large {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid white;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.user-id {
  font-size: 0.8rem;
  color: #6c757d;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.contact-email {
  font-weight: 500;
  color: #2c3e50;
}

.contact-phone {
  font-size: 0.85rem;
  color: #6c757d;
}

.role-badge,
.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  color: white;
}

.order-stats {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-count {
  font-weight: 600;
  color: #2c3e50;
}

.order-value {
  font-size: 0.8rem;
  color: #6c757d;
}

.spending-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.total-spent {
  font-weight: 600;
  color: #2c3e50;
}

.avg-order {
  font-size: 0.8rem;
  color: #6c757d;
}

.join-date {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.join-time {
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

/* User Profile Card */
.user-profile-card {
  text-align: center;
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.profile-avatar {
  position: relative;
  display: inline-block;
  margin-bottom: 1rem;
}

.avatar-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.profile-role {
  margin-bottom: 1.5rem;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  gap: 1rem;
}

.profile-stats .stat-item {
  text-align: center;
}

.profile-stats .stat-label {
  font-size: 0.8rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.profile-stats .stat-value {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

/* Tabs */
.user-details-tabs {
  margin-top: 1rem;
}

.nav-tabs {
  border-bottom: 2px solid #e9ecef;
}

.nav-tabs .nav-link {
  border: none;
  color: #6c757d;
  font-weight: 500;
  padding: 0.75rem 1rem;
  border-radius: 0;
}

.nav-tabs .nav-link.active {
  color: #6366f1;
  border-bottom: 2px solid #6366f1;
  background: none;
}

.tab-content {
  padding: 1.5rem 0;
}

.profile-details table {
  margin-bottom: 0;
}

.profile-details td {
  padding: 0.75rem 0;
  border: none;
}

.profile-details td:first-child {
  font-weight: 600;
  color: #6c757d;
  width: 40%;
}

/* Activity */
.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #e9ecef;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 1.2rem;
}

.activity-content {
  flex: 1;
}

.activity-description {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.activity-time {
  font-size: 0.8rem;
  color: #6c757d;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #6c757d;
}

/* Advanced Filters */
.search-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-input {
  padding-left: 2.75rem !important;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  z-index: 10;
}

.advanced-filters {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-top: 1rem;
}

.user-type-filters {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.type-filter {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border: 1px solid #dee2e6;
  border-radius: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
}

.type-filter:hover {
  border-color: #007bff;
  color: #007bff;
}

.type-filter.active {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

/* User Statistics */
.user-stats-section {
  margin: 1.5rem 0;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border-radius: 0.75rem;
  background: white;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.total {
  border-left: 4px solid #007bff;
}

.stat-card.active {
  border-left: 4px solid #28a745;
}

.stat-card.customers {
  border-left: 4px solid #17a2b8;
}

.stat-card.staff {
  border-left: 4px solid #6f42c1;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  font-size: 1.25rem;
}

.stat-card.total .stat-icon {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.stat-card.active .stat-icon {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.stat-card.customers .stat-icon {
  background: rgba(23, 162, 184, 0.1);
  color: #17a2b8;
}

.stat-card.staff .stat-icon {
  background: rgba(111, 66, 193, 0.1);
  color: #6f42c1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

/* Grid View */
.users-grid {
  margin-top: 1.5rem;
}

.user-grid-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.user-grid-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.user-avatar-large {
  position: relative;
  display: inline-block;
}

.avatar-large-img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info-header {
  flex: 1;
  margin-left: 1rem;
}

.user-details-grid {
  margin-bottom: 1rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #6c757d;
}

.user-stats-grid {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.action-buttons-grid {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

/* Cards View */
.users-cards {
  margin-top: 1.5rem;
}

.user-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 0.75rem;
  overflow: hidden;
  transition: all 0.3s ease;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-avatar-card {
  position: relative;
}

.avatar-card-img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info-card {
  flex: 1;
}

.user-name {
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: #2c3e50;
}

.user-email {
  font-size: 0.875rem;
  color: #6c757d;
  margin: 0;
}

.user-metrics {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.metric-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.metric-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
}

.metric-value {
  font-weight: 600;
  color: #2c3e50;
  line-height: 1;
}

.metric-label {
  font-size: 0.75rem;
  color: #6c757d;
}

.user-contact-info {
  margin-bottom: 1rem;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #6c757d;
}

.action-buttons-card {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

/* Sortable columns */
.sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease;
}

.sortable:hover {
  background-color: #f8f9fa;
}

/* Responsive */
@media (max-width: 992px) {
  .header-stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
  }

  .btn-add-staff {
    width: 100%;
    justify-content: center;
  }

  .header-stats-row {
    grid-template-columns: 1fr;
  }

  .users-table {
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

  .user-profile-card {
    padding: 1rem;
  }

  .profile-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* Edit Modal Styles */
.edit-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.edit-modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.form-label {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
}

.form-label i {
  color: #007bff;
}

.form-control,
.form-select {
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  padding: 0.625rem 0.875rem;
  font-size: 0.9375rem;
  transition: all 0.2s ease;
}

.form-control:focus,
.form-select:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.1);
  outline: none;
}

.form-control::placeholder {
  color: #94a3b8;
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.15em;
}

/* Responsive modal */
@media (max-width: 768px) {
  .edit-modal-content {
    max-width: 95%;
    margin: 0.5rem;
  }
}
</style>
