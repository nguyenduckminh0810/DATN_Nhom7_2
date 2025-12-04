<template>
  <!-- Top Bar -->
  <div class="top-bar">
    <div class="container">
      <div class="row align-items-center">
        <div class="col-12 text-center">
          <span class="top-bar-text">Miễn phí vận chuyển cho đơn hàng từ 500.000đ</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Main Navbar -->
  <nav class="navbar navbar-expand-lg navbar-light bg-white fixed-top modern-navbar">
    <div class="container">
      <!-- Brand -->
      <router-link class="navbar-brand fw-bold" to="/">
        <div class="brand-container">
          <div class="brand-icon">⚡</div>
          <div class="brand-text-container">
            <span class="brand-text">AURO</span>
            <span class="brand-subtitle">MENSWEAR</span>
          </div>
        </div>
      </router-link>

      <!-- Mobile Toggle -->
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <!-- Navigation Menu -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav mx-auto">
          <li class="nav-item">
            <a
              class="nav-link modern-nav-link"
              :class="{ active: $route.name === 'home' }"
              href="#"
              @click.prevent="scrollToNewProducts"
            >
              <span class="nav-text new-text">NEW</span>
            </a>
          </li>

          <!-- dynamic parent categories -->
          <li
            v-for="parent in parentCategories"
            :key="parent.id"
            class="nav-item dropdown dropdown-hover"
          >
            <a
              class="nav-link modern-nav-link dropdown-toggle"
              href="#"
              role="button"
              data-bs-toggle="dropdown"
              data-bs-auto-close="outside"
            >
              <span class="nav-text">{{ parent.name }}</span>
            </a>

            <ul class="dropdown-menu modern-dropdown" v-if="childrenFor(parent.slug).length">
              <li v-for="c in childrenFor(parent.slug)" :key="c.id">
                <router-link class="dropdown-item modern-dropdown-item" :to="`/category/${c.slug}`">
                  <i class="bi bi-tshirt me-2"></i>{{ c.name }}
                </router-link>
              </li>
              <li class="dropdown-divider" v-if="childrenFor(parent.slug).length"></li>
              <li>
                <router-link
                  class="dropdown-item modern-dropdown-item"
                  :to="`/category/${parent.slug}`"
                >
                  <i class="bi bi-grid-3x3-gap me-2"></i>Tất cả {{ parent.name }}
                </router-link>
              </li>
            </ul>

            <!-- if no children, clicking goes to parent category page -->
            <template v-else>
              <router-link :to="`/category/${parent.slug}`" class="nav-link modern-nav-link">
                <!-- nothing extra, link already shown in parent text -->
              </router-link>
            </template>
          </li>

          <li class="nav-item">
            <a
              class="nav-link modern-nav-link sale-link"
              href="#"
              @click.prevent="scrollToBestSellers"
            >
              <div class="sale-container">
                <span class="hot-badge">Hot</span>
              </div>
            </a>
          </li>
        </ul>

        <!-- Search Bar -->
        <div class="search-container">
          <div class="search-input-group">
            <input
              type="text"
              class="form-control search-input"
              placeholder="Tìm kiếm..."
              @keyup.enter="handleSearch"
              v-model="searchQuery"
            />
            <button class="search-btn" @click="handleSearch">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>

        <!-- Right Menu -->
        <ul class="navbar-nav ms-3">
          <li class="nav-item me-3">
            <router-link class="nav-link modern-nav-link track-order-link" to="/theo-doi-don-hang">
              <i class="bi bi-search-heart me-1"></i>
              Theo dõi đơn
            </router-link>
          </li>
          <!-- Cart -->
          <li class="nav-item me-3 cart-nav-item">
            <button class="nav-link modern-nav-link cart-trigger" @click="toggleMiniCart">
              <i class="bi bi-cart3 cart-icon"></i>
            </button>
            <span v-if="isLoggedIn && cartStore.itemCount > 0" class="badge modern-cart-badge">
              {{ cartStore.itemCount }}
            </span>
          </li>

          <!-- User Menu -->
          <li v-if="!isLoggedIn" class="nav-item">
            <button class="nav-link modern-nav-link login-btn" @click="openLoginPopup">
              <i class="bi bi-person"></i>
            </button>
          </li>

          <li v-else class="nav-item dropdown dropdown-hover user-dropdown">
            <a
              class="nav-link modern-nav-link dropdown-toggle"
              href="#"
              role="button"
              data-bs-toggle="dropdown"
              data-bs-auto-close="outside"
            >
              <i class="bi bi-person-circle"></i>
            </a>
            <ul class="dropdown-menu modern-dropdown">
              <li v-if="isAdmin">
                <router-link class="dropdown-item modern-dropdown-item admin-link" to="/admin">
                  <i class="bi bi-speedometer2 me-2"></i>Trang quản trị
                </router-link>
              </li>
              <li v-if="isAdmin"><hr class="dropdown-divider" /></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/profile">
                  <i class="bi bi-person me-2"></i>Thông tin cá nhân
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/orders">
                  <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
                </router-link>
              </li>
              <li><hr class="dropdown-divider" /></li>
              <li>
                <button class="dropdown-item modern-dropdown-item" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                </button>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>

  <!-- Search Modal -->
  <SearchModal />

  <!-- Login Popup -->
  <LoginPopup
    :isOpen="showLoginPopup"
    @close="closeLoginPopup"
    @switchToRegister="handleSwitchToRegister"
  />

  <!-- Register Popup -->
  <RegisterPopup
    :isOpen="showRegisterPopup"
    @close="closeRegisterPopup"
    @switchToLogin="handleSwitchToLogin"
  />

  <!-- Mini Cart -->
  <MiniCart :isOpen="showMiniCart" @close="closeMiniCart" />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import SearchModal from '../common/SearchModal.vue'
import LoginPopup from '../auth/LoginPopup.vue'
import RegisterPopup from '../auth/RegisterPopup.vue'
import MiniCart from '../cart/MiniCart.vue'
import { useUserStore } from '../../stores/user'
import { useToast } from '../../composables/useToast'
import categoryService from '../../services/categoryService' // << added

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const { success, error } = useToast()

// Login popup state
const showLoginPopup = ref(false)
const showRegisterPopup = ref(false)
const showMiniCart = ref(false)

// Search state
const searchQuery = ref('')

// categories for navbar
const categories = ref([])

// Normalize API items to { id, name, slug, parentId }
const normalize = (item) => ({
  id: item.id,
  name: item.ten || item.name || item.title || item.label || '',
  slug: item.slug || item.slugLoai || '',
  parentId: item.idCha ?? item.parentId ?? null,
  status: item.hoatDong === 1 || item.hoatDong === true ? 'active' : 'inactive',
})

async function loadCategories() {
  try {
    const res = await categoryService.getAll()
    let raw = []
    if (res && res.success) raw = res.data
    else raw = res.data || res
    if (raw?.content && Array.isArray(raw.content)) raw = raw.content
    if (!Array.isArray(raw)) raw = []
    // map then filter only active
    categories.value = raw.map(normalize).filter((c) => c.status === 'active')
  } catch (e) {
    console.warn('Could not load categories for navbar', e)
    categories.value = []
  }
}

// computed parent categories (top-level)
const parentCategories = computed(() =>
  categories.value.filter((c) => !c.parentId).sort((a, b) => a.name.localeCompare(b.name)),
)

// get children by parent slug (fallback to slug-prefix if parent not found)
function childrenFor(parentSlug) {
  if (!categories.value.length) return []
  const parent = categories.value.find((c) => c.slug === parentSlug)
  if (parent)
    return categories.value
      .filter((c) => c.parentId === parent.id)
      .sort((a, b) => a.name.localeCompare(b.name))
  return categories.value
    .filter((c) => c.slug && c.slug.startsWith(parentSlug + '-'))
    .sort((a, b) => a.name.localeCompare(b.name))
}

// Check if user is admin or staff
const isAdmin = computed(() => {
  const role = userStore.userRole
  return role === 'admin' || role === 'staff'
})

// Login popup methods
const openLoginPopup = () => {
  showLoginPopup.value = true
}

const closeLoginPopup = () => {
  showLoginPopup.value = false
}

const handleSwitchToRegister = () => {
  closeLoginPopup()
  showRegisterPopup.value = true
}

const closeRegisterPopup = () => {
  showRegisterPopup.value = false
}

const handleSwitchToLogin = () => {
  closeRegisterPopup()
  showLoginPopup.value = true
}

const handleLogout = async () => {
  try {
    await userStore.logout()

    // Xóa giỏ hàng khi đăng xuất
    cartStore.clearCart()
    console.log('🗑️ Cart cleared on logout')

    success('Đăng xuất thành công!')
    setTimeout(() => {
      router.push('/')
    }, 500)
  } catch (err) {
    error('Có lỗi khi đăng xuất. Vui lòng thử lại.')
    console.error('Logout error:', err)
  }
}

// Mini cart methods
const toggleMiniCart = () => {
  showMiniCart.value = !showMiniCart.value
}

const closeMiniCart = () => {
  showMiniCart.value = false
}

// Scroll to new products section
const scrollToNewProducts = async () => {
  // If not on home page, navigate to home first
  if (router.currentRoute.value.name !== 'home') {
    await router.push('/')
    // Wait for navigation to complete
    await new Promise((resolve) => setTimeout(resolve, 100))
  }

  // Find and scroll to new arrivals section
  const newArrivalsSection = document.querySelector('#new-arrivals')
  if (newArrivalsSection) {
    const navbarHeight = 80 // Account for fixed navbar
    const targetPosition = newArrivalsSection.offsetTop - navbarHeight

    window.scrollTo({
      top: targetPosition,
      behavior: 'smooth',
    })
  } else {
    // Fallback: scroll to approximate position if section not found
    window.scrollTo({
      top: window.innerHeight * 0.8, // Scroll past hero section
      behavior: 'smooth',
    })
  }
}

// Scroll to best sellers section
const scrollToBestSellers = async () => {
  // If not on home page, navigate to home first
  if (router.currentRoute.value.name !== 'home') {
    await router.push('/')
    // Wait for navigation to complete
    await new Promise((resolve) => setTimeout(resolve, 100))
  }

  // Find and scroll to best sellers section
  const bestSellersSection = document.querySelector('.best-sellers-section')
  if (bestSellersSection) {
    const navbarHeight = 80 // Account for fixed navbar
    const targetPosition = bestSellersSection.offsetTop - navbarHeight

    window.scrollTo({
      top: targetPosition,
      behavior: 'smooth',
    })
  } else {
    // Fallback: scroll down if section not found
    window.scrollTo({
      top: window.innerHeight * 1.5,
      behavior: 'smooth',
    })
  }
}

// Search handler
const handleSearch = () => {
  const query = searchQuery.value.trim()
  if (query) {
    // Navigate to search page with query parameter
    router.push(`/search?q=${encodeURIComponent(query)}`)
    // Clear search input after navigation
    searchQuery.value = ''
  } else {
    // Optional: Show a message or focus the input if empty
    // For now, just do nothing if query is empty
  }
}

// Scroll behavior for promo bar
let lastScrollTop = 0
let isPromoBarVisible = true

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const promoBar = document.querySelector('.top-bar')
  const navbar = document.querySelector('.modern-navbar')
  const mainContent = document.querySelector('.main-content')

  if (scrollTop > lastScrollTop && scrollTop > 100) {
    // Scrolling down - hide promo bar
    if (isPromoBarVisible) {
      promoBar.style.transform = 'translateY(-100%)'
      navbar.style.marginTop = '0px'
      if (mainContent) mainContent.style.paddingTop = '55px' // Reduced padding when promo bar is hidden
      isPromoBarVisible = false
    }
  } else {
    // Scrolling up - show promo bar
    if (!isPromoBarVisible) {
      promoBar.style.transform = 'translateY(0)'
      navbar.style.marginTop = '25px'
      if (mainContent) mainContent.style.paddingTop = '80px' // Full padding when promo bar is visible
      isPromoBarVisible = true
    }
  }

  lastScrollTop = scrollTop <= 0 ? 0 : scrollTop
}

// Check for register popup trigger on mount
onMounted(() => {
  loadCategories()
  window.addEventListener('categories-updated', loadCategories)
  const showRegisterPopupFlag = localStorage.getItem('auro_show_register_popup')
  if (showRegisterPopupFlag === 'true') {
    localStorage.removeItem('auro_show_register_popup')
    showRegisterPopup.value = true
  }

  // Initialize dropdown hover behavior
  initializeDropdownHover()

  // Add scroll listener
  window.addEventListener('scroll', handleScroll, { passive: true })
})

// Cleanup on unmount
onUnmounted(() => {
  window.removeEventListener('categories-updated', loadCategories)
  window.removeEventListener('scroll', handleScroll)
})

// Initialize dropdown hover behavior
const initializeDropdownHover = () => {
  // Disable Bootstrap's default dropdown behavior for hover dropdowns
  const hoverDropdowns = document.querySelectorAll('.dropdown-hover')
  hoverDropdowns.forEach((dropdown) => {
    const toggle = dropdown.querySelector('[data-bs-toggle="dropdown"]')
    if (toggle) {
      toggle.addEventListener('click', (e) => {
        e.preventDefault()
      })
    }
  })
}

// expose boolean for template (fix: "Property 'isLoggedIn' was accessed during render")
const isLoggedIn = computed(() => {
  // prefer store getter; fallback to stored token for hot reload edge cases
  return !!userStore.isAuthenticated || !!localStorage.getItem('auro_token')
})
</script>

<style scoped>
/* Top Bar */
.top-bar {
  background: var(--auro-primary);
  color: white;
  padding: 0.3rem 0;
  font-size: 0.75rem;
  font-weight: 500;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(0);
}

.top-bar-text {
  color: white;
  font-size: 0.75rem;
  font-weight: 500;
}

/* Modern Navbar - Cool Mate Inspired */
.modern-navbar {
  background: white !important;
  border-bottom: 1px solid var(--auro-border);
  padding: 1.15rem 0;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.8rem;
  font-weight: 500;
  margin-top: 34px; /* Account for top bar */
  transition: margin-top 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
}

.modern-navbar:hover {
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.12);
}

/* Brand */
.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--auro-primary) !important;
  transition: all 0.3s ease;
}

.navbar-brand:hover {
  color: var(--auro-accent) !important;
  transform: translateY(-1px);
}

.navbar-brand:focus {
  outline: none !important;
  box-shadow: none !important;
}

.navbar-brand:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.brand-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.brand-container:focus {
  outline: none !important;
  box-shadow: none !important;
}

.brand-container:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

/* Remove focus outline for all router-links */
a:focus,
a:focus-visible,
router-link:focus,
router-link:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.brand-icon {
  font-size: 24px;
  color: var(--auro-primary);
}

.brand-text-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.brand-text {
  font-family: var(--auro-heading-font);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.03rem;
  line-height: 1;
  color: var(--auro-text);
}

.brand-subtitle {
  font-family: var(--auro-body-font);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.03rem;
  color: var(--auro-text-light);
  margin-top: -2px;
}

/* Navigation Links - Cool Mate Inspired */
.modern-nav-link {
  position: relative;
  font-family: var(--auro-body-font);
  font-weight: 600;
  color: var(--auro-text) !important;
  padding: 0.75rem 1.5rem !important;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  text-transform: uppercase;
  font-size: 0.875rem;
  overflow: hidden;
}

.nav-text {
  position: relative;
  z-index: 2;
}

.new-text {
  color: var(--auro-accent) !important;
}

.sale-link {
  position: relative;
}

.sale-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-badge {
  font-size: 0.875rem;
  font-weight: 700;
  font-family: var(--auro-body-font);
  color: #fff;
  line-height: 1;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  padding: 6px 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  letter-spacing: 0.5px;
  text-transform: uppercase;
  transition: all 0.3s ease;
}

.sale-link:hover .hot-badge {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.5);
}

.modern-nav-link::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--auro-gradient-accent);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-50%);
}

.modern-nav-link::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(205, 127, 50, 0.1), transparent);
  transition: left 0.5s ease;
}

.modern-nav-link:hover::before,
.modern-nav-link.active::before {
  width: 80%;
}

.modern-nav-link:hover {
  color: var(--auro-accent) !important;
  background: rgba(205, 127, 50, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(205, 127, 50, 0.2);
}

.modern-nav-link:hover::after {
  left: 100%;
}

.modern-nav-link.active {
  color: var(--auro-accent) !important;
  background: rgba(205, 127, 50, 0.1);
}

/* Search */
.search-container {
  margin-left: 2rem;
}

.search-input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid var(--auro-border);
  border-radius: 25px;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 300px;
}

.search-input-group:hover {
  border-color: var(--auro-accent);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input {
  border: none !important;
  background: transparent;
  padding: 12px 16px;
  font-size: 14px;
  flex: 1;
  outline: none;
}

.search-input:focus {
  box-shadow: none !important;
}

.search-btn {
  background: none;
  border: none;
  padding: 12px 16px;
  color: var(--auro-text);
  transition: all 0.3s ease;
  cursor: pointer;
}

.search-btn:hover {
  color: var(--auro-accent);
}

/* Cart Badge */
.cart-nav-item {
  position: relative;
  overflow: visible !important;
}

.modern-cart-badge {
  background: var(--auro-gradient-accent) !important;
  color: var(--auro-dark) !important;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 1002;
  pointer-events: none;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
  border: 2px solid #fff;
}

/* Cart Trigger Button */
.cart-trigger {
  background: transparent;
  border: none;
  cursor: pointer;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: auto;
  min-width: 52px;
  height: 52px;
  padding: 0.75rem !important;
  overflow: hidden;
  border-radius: 8px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item {
  position: relative;
  overflow: visible;
  padding: 0;
  display: flex;
  align-items: center;
}

/* Đảm bảo nav-item không tạo vùng hover không mong muốn */
.nav-item.me-3 {
  margin-right: 1rem !important;
}

/* Đảm bảo chỉ phần tử con mới có vùng hover, không phải nav-item */

.cart-trigger:hover {
  color: var(--auro-accent) !important;
}

/* Đảm bảo cart-trigger có background khi hover */
.cart-trigger.modern-nav-link:hover {
  background: rgba(184, 134, 11, 0.08) !important;
}

.cart-icon {
  font-size: 1.45rem;
  font-weight: 900 !important;
  color: #212529 !important;
  transition: all 0.3s ease;
}

.cart-trigger:hover .cart-icon {
  color: #b8860b !important;
  transform: scale(1.08);
}

/* Dropdown Hover */
.dropdown-hover:hover .dropdown-menu {
  display: block;
  animation: fadeInDown 0.3s ease;
}

.dropdown-hover .dropdown-menu {
  display: none;
  position: absolute;
  top: calc(100% - 5px);
  left: 0;
  z-index: 1000;
  margin-top: 0;
}

/* User dropdown alignment - căn phải dropdown với icon user */
.user-dropdown .dropdown-menu {
  right: 0;
  left: auto;
}

/* Add hover area to prevent dropdown from closing */
.dropdown-hover::before {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  height: 10px;
  z-index: 999;
}

.dropdown-hover:hover::before {
  display: block;
}

/* Dropdown */
.modern-dropdown {
  border: none;
  border-radius: 16px;
  box-shadow: var(--auro-shadow-hover);
  padding: 12px;
  margin-top: 0;
  background: var(--auro-card);
  border: 1px solid var(--auro-border);
  min-width: 200px;
  position: relative;
}

/* Ensure dropdown stays open when hovering over it */
.modern-dropdown:hover {
  display: block !important;
}

.modern-dropdown-item {
  border-radius: 8px;
  padding: 12px 16px;
  margin: 2px 0;
  transition: all 0.3s ease;
  color: var(--auro-text);
  font-weight: 500;
  display: flex;
  align-items: center;
  text-decoration: none;
}

.modern-dropdown-item:hover {
  background: rgba(205, 127, 50, 0.1);
  color: var(--auro-secondary);
  transform: translateX(4px);
}

.modern-dropdown-item.admin-link {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.modern-dropdown-item.admin-link:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  color: white;
  transform: translateX(4px) scale(1.02);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.modern-dropdown-item i {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.dropdown-divider {
  margin: 8px 0;
  border-color: var(--auro-border);
}

/* Animation */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Login Button */
.login-btn {
  background: none;
  border: none;
  padding: 0;
  text-decoration: none;
}

/* Desktop Enhanced Navbar */
.modern-navbar {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95) !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding: 1.15rem 0;
}

.brand-container {
  transition: all 0.3s ease;
}

.brand-container:hover {
  transform: scale(1.05);
}

.brand-text {
  font-family:
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.modern-nav-link {
  position: relative;
  font-weight: 600;
  color: #212529 !important;
  padding: 0.875rem 1.75rem !important;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  text-transform: uppercase;
  font-size: 0.875rem;
  letter-spacing: 0.02em;
}

.modern-nav-link::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: linear-gradient(135deg, #b8860b 0%, #daa520 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-50%);
  border-radius: 2px;
}

.modern-nav-link:hover::before,
.modern-nav-link.active::before {
  width: 90%;
}

.modern-nav-link:hover {
  color: #b8860b !important;
  background: rgba(184, 134, 11, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(184, 134, 11, 0.2);
}

.search-input-group {
  width: 360px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e9ecef;
  border-radius: 30px;
  overflow: hidden;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.search-input-group:hover {
  border-color: #b8860b;
  box-shadow: 0 4px 20px rgba(184, 134, 11, 0.15);
  transform: translateY(-1px);
}

.search-input-group:focus-within {
  border-color: #b8860b;
  box-shadow: 0 0 0 0.2rem rgba(184, 134, 11, 0.25);
  transform: translateY(-1px);
}

.search-input {
  background: transparent;
  border: none;
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 500;
  color: #212529;
}

.search-input::placeholder {
  color: #6c757d;
  font-weight: 400;
}

.search-btn {
  background: linear-gradient(135deg, #b8860b 0%, #daa520 100%);
  border: none;
  padding: 14px 20px;
  color: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.search-btn:hover {
  background: linear-gradient(135deg, #daa520 0%, #ffd700 100%);
  transform: scale(1.05);
}

.modern-dropdown {
  border: none;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  padding: 16px;
  margin-top: 8px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.08);
  min-width: 220px;
  backdrop-filter: blur(20px);
}

.modern-dropdown-item {
  border-radius: 12px;
  padding: 14px 18px;
  margin: 4px 0;
  transition: all 0.3s ease;
  color: #495057;
  font-weight: 500;
  display: flex;
  align-items: center;
  text-decoration: none;
  font-size: 0.9rem;
}

.modern-dropdown-item:hover {
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.1) 0%, rgba(218, 165, 32, 0.1) 100%);
  color: #b8860b;
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(184, 134, 11, 0.2);
}

.modern-dropdown-item i {
  font-size: 18px;
  width: 24px;
  text-align: center;
  margin-right: 12px;
  color: #6c757d;
}

.modern-dropdown-item:hover i {
  color: #b8860b;
}

.cart-nav-item {
  position: relative;
  overflow: visible !important;
}

.modern-cart-badge {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%) !important;
  color: #fff !important;
  border-radius: 999px;
  font-size:9px;
  font-weight: 700;
  min-width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 0px;
  right: -8px;
  z-index: 1002;
  pointer-events: none;
  border: 2px solid #fff;
  padding: 0 6px;
}
/* Cart icon */
.cart-trigger i {
  font-size: 1.4rem;
  font-weight: 900 !important;
  color: #212529 !important;
  transition: all 0.3s ease;
}

.cart-trigger:hover i {
  font-size: 1.5rem !important;
  color: #b8860b;
  transform: scale(1.1);
}

/*Login icon */
.login-btn i {
  font-size: 1.4rem !important;
  font-weight: 900 !important;
  color: #212529 !important;
  transition: all 0.3s ease;
}

.login-btn:hover i {
  font-size: 1.5rem !important;
  color: #b8860b !important;
  transform: scale(1.1);
}

.bi-person-circle {
  font-size: 1.4rem !important;
  font-weight: 900 !important;
  color: #212529 !important;
  transition: all 0.3s ease;
}

.modern-nav-link:hover .bi-person-circle {
  font-size: 1.5rem !important;
  color: #b8860b !important;
  transform: scale(1.1);
}

.track-order-link {
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  white-space: nowrap; /* Ngăn chữ bị vỡ xuống dòng */
  padding: 0.5rem 1rem !important; /* Tăng padding để có đủ không gian */
  min-width: fit-content; /* Đảm bảo đủ rộng cho nội dung */
}

/* Đảm bảo hover chỉ hoạt động trên phần tử thực tế, không phải khoảng trống */
.track-order-link,
.cart-trigger {
  margin: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Loại bỏ padding mặc định của modern-nav-link cho các phần tử này */
.track-order-link.modern-nav-link {
  padding: 0.5rem 1rem !important;
}

.cart-trigger.modern-nav-link {
  padding: 0.75rem !important;
  width: auto;
  min-width: 52px;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px !important;
  overflow: hidden !important;
}

/* Đảm bảo hover chỉ kích hoạt khi trỏ vào chính phần tử, không phải khoảng trống */
.nav-item .modern-nav-link {
  width: fit-content;
  min-width: fit-content;
}

/* Đảm bảo cart-trigger có vùng hover đầy đủ */
.nav-item .cart-trigger.modern-nav-link {
  width: auto;
  min-width: 52px;
  padding: 0.75rem !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

/* Ngăn hover effect xuất hiện ở khoảng trống giữa các nav-item */
.navbar-nav > .nav-item {
  display: inline-flex;
  align-items: center;
}

/* Loại bỏ hiệu ứng hover mặc định - chỉ áp dụng khi thực sự hover */
.track-order-link.modern-nav-link,
.cart-trigger.modern-nav-link {
  background: transparent !important;
  transform: none !important;
  box-shadow: none !important;
  color: var(--auro-text) !important;
}

.track-order-link.modern-nav-link::before,
.cart-trigger.modern-nav-link::before {
  width: 0 !important;
  display: none;
}

.track-order-link.modern-nav-link::after,
.cart-trigger.modern-nav-link::after {
  left: -100% !important;
  display: none;
}

/* Chỉ áp dụng hover khi thực sự hover */
.track-order-link.modern-nav-link:hover,
.cart-trigger.modern-nav-link:hover {
  background: rgba(184, 134, 11, 0.08) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 16px rgba(184, 134, 11, 0.2) !important;
  color: #b8860b !important;
  border-radius: 8px !important;
}

.track-order-link.modern-nav-link:hover::before,
.cart-trigger.modern-nav-link:hover::before {
  width: 90% !important;
  display: block;
}

.track-order-link.modern-nav-link:hover::after,
.cart-trigger.modern-nav-link:hover::after {
  left: 100% !important;
  display: block;
}

.track-order-link .bi {
  font-size: 1rem;
  flex-shrink: 0; /* Ngăn icon bị co lại */
}
</style>
