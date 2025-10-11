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
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <!-- Navigation Menu -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav mx-auto">
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link" :class="{ active: $route.name === 'home' }" to="/">
              <span class="nav-text new-text">NEW</span>
            </router-link>
          </li>
          <li class="nav-item dropdown dropdown-hover">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside">
              <span class="nav-text">ÁO</span>
            </a>
            <ul class="dropdown-menu modern-dropdown">
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-thun">
                  <i class="bi bi-tshirt me-2"></i>Áo thun
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-so-mi">
                  <i class="bi bi-shop me-2"></i>Áo sơ mi
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-polo">
                  <i class="bi bi-tshirt me-2"></i>Áo polo
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-khoac">
                  <i class="bi bi-hanger me-2"></i>Áo khoác
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-len">
                  <i class="bi bi-tshirt me-2"></i>Áo len
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao">
                  <i class="bi bi-grid-3x3-gap me-2"></i>Tất cả áo
                </router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item dropdown dropdown-hover">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside">
              <span class="nav-text">QUẦN</span>
            </a>
            <ul class="dropdown-menu modern-dropdown">
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-jean">
                  <i class="bi bi-shop me-2"></i>Quần jean
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-kaki">
                  <i class="bi bi-shop me-2"></i>Quần kaki
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-short">
                  <i class="bi bi-shop me-2"></i>Quần short
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-tay">
                  <i class="bi bi-shop me-2"></i>Quần tây
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-jogger">
                  <i class="bi bi-shop me-2"></i>Quần jogger
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan">
                  <i class="bi bi-grid-3x3-gap me-2"></i>Tất cả quần
                </router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link sale-link" to="/category/sale">
              <div class="sale-container">
                <span class="sale-text">SALE</span>
                <span class="sale-percent">-50%</span>
              </div>
            </router-link>
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
            >
            <button class="search-btn" @click="handleSearch">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>
        
        <!-- Right Menu -->
        <ul class="navbar-nav">          
          <!-- Cart -->
          <li class="nav-item">
            <button 
              class="nav-link modern-nav-link position-relative cart-trigger"
              @click="toggleMiniCart"
            >
              <i class="bi bi-cart3"></i>
              <span v-if="cartStore.itemCount > 0" class="badge modern-cart-badge">
                {{ cartStore.itemCount }}
              </span>
            </button>
          </li>
          
          <!-- User Menu -->
          <li v-if="!isLoggedIn" class="nav-item">
            <button class="nav-link modern-nav-link login-btn" @click="openLoginPopup">
              <i class="bi bi-person"></i>
            </button>
          </li>
          
          <li v-else class="nav-item dropdown dropdown-hover">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside">
              <i class="bi bi-person-circle"></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-end modern-dropdown">
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
              <li><hr class="dropdown-divider"></li>
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
  <MiniCart 
    :isOpen="showMiniCart" 
    @close="closeMiniCart"
  />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { useProductStore } from '../../stores/product'
import SearchModal from '../common/SearchModal.vue'
import LoginPopup from '../auth/LoginPopup.vue'
import RegisterPopup from '../auth/RegisterPopup.vue'
import MiniCart from '../cart/MiniCart.vue'
import { useUserStore } from '../../stores/user'
import { useToast } from '../../composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const productStore = useProductStore()
const { success, error } = useToast()

// Login popup state
const showLoginPopup = ref(false)
const showRegisterPopup = ref(false)
const showMiniCart = ref(false)

// Search state
const searchQuery = ref('')

// Mock user data - replace with actual auth store
const isLoggedIn = computed(() => userStore.isAuthenticated)
const user = computed(() => userStore.user)

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
    success('Đăng xuất thành công!')
    setTimeout(() => {
      router.push('/')
    }, 500)
  } catch (err) {
    error('Có lỗi khi đăng xuất. Vui lòng thử lại.')
    console.error('Logout error:', error)
  }
}

// Mini cart methods
const toggleMiniCart = () => {
  showMiniCart.value = !showMiniCart.value
}

const closeMiniCart = () => {
  showMiniCart.value = false
}

// Search handler
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`)
    searchQuery.value = ''
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
  window.removeEventListener('scroll', handleScroll)
})

// Initialize dropdown hover behavior
const initializeDropdownHover = () => {
  // Disable Bootstrap's default dropdown behavior for hover dropdowns
  const hoverDropdowns = document.querySelectorAll('.dropdown-hover')
  hoverDropdowns.forEach(dropdown => {
    const toggle = dropdown.querySelector('[data-bs-toggle="dropdown"]')
    if (toggle) {
      toggle.addEventListener('click', (e) => {
        e.preventDefault()
      })
    }
  })
}
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
  padding: 0.5rem 0;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.8rem;
  font-weight: 500;
  margin-top: 25px; /* Account for top bar */
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

.sale-text {
  font-size: 1.2rem;
  font-weight: 900;
  color: #dc3545 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  line-height: 1;
}

.sale-percent {
  position: absolute;
  top: -8px;
  right: -12px;
  font-size: 0.6rem;
  font-weight: 900;
  color: #dc3545;
  line-height: 1;
  background: white;
  padding: 2px 4px;
  border-radius: 3px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
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
.modern-cart-badge {
  background: var(--auro-gradient-accent) !important;
  color: var(--auro-dark) !important;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: -5px;
  right: -5px;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
}

/* Cart Trigger Button */
.cart-trigger {
  background: none;
  border: none;
  cursor: pointer;
}

.cart-trigger:hover {
  color: var(--auro-accent) !important;
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
}

.brand-container {
  transition: all 0.3s ease;
}

.brand-container:hover {
  transform: scale(1.05);
}

.brand-text {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
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
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-50%);
  border-radius: 2px;
}

.modern-nav-link:hover::before,
.modern-nav-link.active::before {
  width: 90%;
}

.modern-nav-link:hover {
  color: #B8860B !important;
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
  border-color: #B8860B;
  box-shadow: 0 4px 20px rgba(184, 134, 11, 0.15);
  transform: translateY(-1px);
}

.search-input-group:focus-within {
  border-color: #B8860B;
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
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  border: none;
  padding: 14px 20px;
  color: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.search-btn:hover {
  background: linear-gradient(135deg, #DAA520 0%, #FFD700 100%);
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
  color: #B8860B;
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
  color: #B8860B;
}

.modern-cart-badge {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%) !important;
  color: white !important;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  min-width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: -6px;
  right: -6px;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
  border: 2px solid white;
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
  color: #B8860B;
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
  color: #B8860B !important;
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
  color: #B8860B !important;
  transform: scale(1.1);
}

</style>
