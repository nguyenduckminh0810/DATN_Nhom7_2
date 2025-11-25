<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="brand">
          <i class="bi bi-shop"></i>
          <span v-if="!sidebarCollapsed" class="brand-text">AURO Admin</span>
        </div>
        <button class="sidebar-toggle" @click="toggleSidebar">
          <i class="bi bi-list"></i>
        </button>
      </div>

      <nav class="sidebar-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/admin" class="nav-link" :class="{ active: $route.path === '/admin' }">
              <i class="bi bi-house"></i>
              <span v-if="!sidebarCollapsed">Dashboard</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/products"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/products') }"
            >
              <i class="bi bi-box"></i>
              <span v-if="!sidebarCollapsed">Sản phẩm</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/categories"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/categories') }"
            >
              <i class="bi bi-folder"></i>
              <span v-if="!sidebarCollapsed">Danh mục</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/orders"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/orders') }"
            >
              <i class="bi bi-cart3"></i>
              <span v-if="!sidebarCollapsed">Đơn hàng</span>
              <!-- <span v-if="!sidebarCollapsed" class="badge bg-danger">3</span> -->
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/users"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/users') }"
            >
              <i class="bi bi-people"></i>
              <span v-if="!sidebarCollapsed">Tài khoản</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/analytics"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/analytics') }"
            >
              <i class="bi bi-bar-chart"></i>
              <span v-if="!sidebarCollapsed">Thống kê</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/promotions"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/promotions') }"
            >
              <i class="bi bi-tag"></i>
              <span v-if="!sidebarCollapsed">Khuyến mãi</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link
              to="/admin/settings"
              class="nav-link"
              :class="{ active: $route.path.startsWith('/admin/settings') }"
            >
              <i class="bi bi-gear"></i>
              <span v-if="!sidebarCollapsed">Cài đặt</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">
            <i class="bi bi-person-circle"></i>
          </div>
          <div v-if="!sidebarCollapsed" class="user-details">
            <div class="user-name">Admin User</div>
            <div class="user-role">Quản trị viên</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="admin-main" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- Header -->
      <header class="admin-header">
        <div class="header-left">
          <button class="mobile-sidebar-toggle" @click="toggleSidebar">
            <i class="bi bi-list"></i>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-item">{{ currentPageTitle }}</span>
          </div>
        </div>

        <div class="header-right">
          <!-- Search -->
          <div class="header-search">
            <div class="search-input-group">
              <i class="bi bi-search search-icon"></i>
              <input type="text" class="search-input" placeholder="Tìm kiếm..." />
            </div>
          </div>

          <!-- Notifications -->
          <div class="header-notifications">
            <button class="notification-btn" @click="toggleNotifications">
              <i class="bi bi-bell"></i>
              <span class="notification-badge">5</span>
            </button>
          </div>

          <!-- User Menu -->
          <div class="header-user">
            <div class="user-dropdown">
              <button class="user-btn" @click="toggleUserMenu">
                <div class="user-avatar-small">
                  <i class="bi bi-person-circle"></i>
                </div>
                <span class="user-name">Admin</span>
                <i class="bi bi-caret-down"></i>
              </button>

              <div v-if="showUserMenu" class="user-dropdown-menu">
                <router-link to="/" class="dropdown-item">
                  <i class="bi bi-house me-2"></i>Về trang chủ
                </router-link>
                <hr class="dropdown-divider" />
                <a href="#" class="dropdown-item">
                  <i class="bi bi-person me-2"></i>Thông tin cá nhân
                </a>
                <a href="#" class="dropdown-item"> <i class="bi bi-gear me-2"></i>Cài đặt </a>
                <hr class="dropdown-divider" />
                <a href="#" class="dropdown-item" @click="logout">
                  <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                </a>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="admin-content">
        <router-view v-slot="{ Component, route }">
          <transition name="page" mode="out-in" appear>
            <div :key="route.path" class="router-view-wrapper">
              <Suspense>
                <component :is="Component" />
                <template #fallback>
                  <div class="loading-fallback">Đang tải...</div>
                </template>
              </Suspense>
            </div>
          </transition>
        </router-view>
      </main>
    </div>

    <!-- Notifications Panel -->
    <div v-if="showNotifications" class="notifications-panel">
      <div class="notifications-header">
        <h6>Thông báo</h6>
        <button class="close-btn" @click="toggleNotifications">
          <i class="bi bi-x"></i>
        </button>
      </div>
      <div class="notifications-list">
        <div class="notification-item">
          <div class="notification-icon">
            <i class="bi bi-cart3 text-primary"></i>
          </div>
          <div class="notification-content">
            <div class="notification-title">Đơn hàng mới</div>
            <div class="notification-text">Có 3 đơn hàng mới cần xử lý</div>
            <div class="notification-time">5 phút trước</div>
          </div>
        </div>
        <div class="notification-item">
          <div class="notification-icon">
            <i class="bi bi-exclamation-triangle text-warning"></i>
          </div>
          <div class="notification-content">
            <div class="notification-title">Cảnh báo</div>
            <div class="notification-text">Sản phẩm "Áo sơ mi" sắp hết hàng</div>
            <div class="notification-time">1 giờ trước</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Overlay for mobile -->
    <div v-if="showMobileOverlay" class="mobile-overlay" @click="closeMobileSidebar"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useToast } from '../composables/useToast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { success, error } = useToast()

// Reactive state
const sidebarCollapsed = ref(false)
const showUserMenu = ref(false)
const showNotifications = ref(false)
const showMobileOverlay = ref(false)

// Removed lowStockCount since inventory menu item is hidden

// Computed
const currentPageTitle = computed(() => {
  const titles = {
    '/admin': 'Dashboard',
    '/admin/products': 'Quản lý sản phẩm',
    '/admin/categories': 'Quản lý danh mục',
    '/admin/orders': 'Quản lý đơn hàng',
    '/admin/users': 'Quản lý người dùng',
    '/admin/analytics': 'Thống kê',
    '/admin/inventory': 'Quản lý tồn kho',
    '/admin/promotions': 'Khuyến mãi & Voucher',
    '/admin/settings': 'Cài đặt',
  }

  for (const [path, title] of Object.entries(titles)) {
    if (route.path.startsWith(path)) {
      return title
    }
  }

  return 'Admin'
})

// Methods
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  showMobileOverlay.value = !sidebarCollapsed.value && window.innerWidth < 768
}

const closeMobileSidebar = () => {
  sidebarCollapsed.value = false
  showMobileOverlay.value = false
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

const logout = async () => {
  try {
    await userStore.logout()
    success('Đăng xuất thành công! 👋')
    setTimeout(() => {
      router.push('/login')
    }, 500)
  } catch (err) {
    error('Có lỗi khi đăng xuất. Vui lòng thử lại.')
    console.error('Logout error:', err)
  }
}

const handleResize = () => {
  if (window.innerWidth >= 768) {
    showMobileOverlay.value = false
  } else if (!sidebarCollapsed.value) {
    showMobileOverlay.value = true
  }
}

const handleClickOutside = (event) => {
  if (!event.target.closest('.user-dropdown')) {
    showUserMenu.value = false
  }
  if (
    !event.target.closest('.header-notifications') &&
    !event.target.closest('.notifications-panel')
  ) {
    showNotifications.value = false
  }
}

// Lifecycle
onMounted(() => {
  window.addEventListener('resize', handleResize)
  document.addEventListener('click', handleClickOutside)

  // Check initial screen size
  handleResize()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* Sidebar */
.admin-sidebar {
  width: 280px;
  background: linear-gradient(180deg, #f8fafb 0%, #ffffff 100%);
  color: #475569;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: fixed;
  height: 100vh;
  z-index: 1000;
  box-shadow: 2px 0 15px rgba(0, 0, 0, 0.05);
  border-right: 1px solid #e2e8f0;
}

.admin-sidebar.collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 1.5rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
}

.brand i {
  font-size: 1.5rem;
  color: #6366f1;
}

.brand-text {
  transition: opacity 0.3s ease;
}

.admin-sidebar.collapsed .brand-text {
  opacity: 0;
}

.sidebar-toggle {
  background: none;
  border: none;
  color: #64748b;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background-color: #f1f5f9;
  color: #475569;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 0.25rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.5rem;
  color: #64748b;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  border-radius: 8px;
  margin: 0 0.5rem;
}

.nav-link:hover {
  background-color: #f1f5f9;
  color: #475569;
}

.nav-link.active {
  background-color: #eef2ff;
  color: #6366f1;
  font-weight: 500;
}

.nav-link i {
  font-size: 1.25rem;
  min-width: 20px;
}

.nav-link span {
  transition: opacity 0.3s ease;
}

.admin-sidebar.collapsed .nav-link span {
  opacity: 0;
}

.nav-link .badge {
  margin-left: auto;
  font-size: 0.75rem;
}

.nav-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
  margin: 1rem 1.5rem;
}

.back-to-store {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
}

.back-to-store:hover {
  background-color: #e0f2fe;
  color: #0284c7;
  border-color: #0284c7;
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid #e2e8f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.user-details {
  transition: opacity 0.3s ease;
}

.admin-sidebar.collapsed .user-details {
  opacity: 0;
}

.user-name {
  font-weight: 600;
  font-size: 0.9rem;
  color: #334155;
}

.user-role {
  font-size: 0.8rem;
  color: #94a3b8;
}

/* Main Content */
.admin-main {
  flex: 1;
  margin-left: 280px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
}

.admin-main.sidebar-collapsed {
  margin-left: 70px;
}

/* Header */
.admin-header {
  background: white;
  padding: 1rem 2rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.mobile-sidebar-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.mobile-sidebar-toggle:hover {
  background-color: #f8f9fa;
}

.breadcrumb-item {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-search {
  position: relative;
}

.search-input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  color: #6c757d;
  z-index: 1;
}

.search-input {
  padding: 0.5rem 0.75rem 0.5rem 2.5rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  width: 300px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.header-notifications {
  position: relative;
}

.notification-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.notification-btn:hover {
  background-color: #f8f9fa;
  color: #6366f1;
}

.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #e74c3c;
  color: white;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.header-user {
  position: relative;
}

.user-dropdown {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.user-btn:hover {
  background-color: #f8f9fa;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 1000;
  padding: 0.5rem;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #6c757d;
  text-decoration: none;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
  color: #2c3e50;
}

.dropdown-item:first-child {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  border-radius: 6px;
  margin: 0 0 0.25rem 0;
  padding: 0.65rem 0.9rem;
}

.dropdown-item:first-child:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
  color: white;
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.dropdown-item:first-child i {
  color: white;
}

.dropdown-divider {
  margin: 0.5rem 0;
  border: none;
  border-top: 1px solid #e9ecef;
}

/* Content */
.admin-content {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
}

.router-view-wrapper {
  width: 100%;
  height: 100%;
  min-height: 100%;
}

.loading-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #64748b;
  font-size: 0.875rem;
}

/* Notifications Panel */
.notifications-panel {
  position: fixed;
  top: 80px;
  right: 2rem;
  width: 350px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.notifications-header {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notifications-header h6 {
  margin: 0;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.close-btn:hover {
  background-color: #f8f9fa;
}

.notifications-list {
  padding: 0.5rem 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  transition: background-color 0.3s ease;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  font-size: 1rem;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.notification-text {
  font-size: 0.85rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.notification-time {
  font-size: 0.75rem;
  color: #adb5bd;
}

/* Mobile Overlay */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

/* Page Transitions */
.page-enter-active {
  transition: all 0.2s ease-out;
}

.page-leave-active {
  transition: all 0.15s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive */
@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);
  }

  .admin-sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .admin-main {
    margin-left: 0;
  }

  .mobile-sidebar-toggle {
    display: block;
  }

  .header-search {
    display: none;
  }

  .search-input {
    width: 200px;
  }

  .notifications-panel {
    right: 1rem;
    width: calc(100vw - 2rem);
  }
}

@media (max-width: 480px) {
  .admin-header {
    padding: 1rem;
  }

  .admin-content {
    padding: 1rem;
  }

  .header-right {
    gap: 0.5rem;
  }
}
</style>
