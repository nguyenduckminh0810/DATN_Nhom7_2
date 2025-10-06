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
                  <i class="ph-t-shirt me-2"></i>Áo thun
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-so-mi">
                  <i class="ph-shirt me-2"></i>Áo sơ mi
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-polo">
                  <i class="ph-t-shirt me-2"></i>Áo polo
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-khoac">
                  <i class="ph-coat-hanger me-2"></i>Áo khoác
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao-len">
                  <i class="ph-sweater me-2"></i>Áo len
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao">
                  <i class="ph-grid-four me-2"></i>Tất cả áo
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
                  <i class="ph-pants me-2"></i>Quần jean
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-kaki">
                  <i class="ph-pants me-2"></i>Quần kaki
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-short">
                  <i class="ph-pants me-2"></i>Quần short
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-tay">
                  <i class="ph-pants me-2"></i>Quần tây
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan-jogger">
                  <i class="ph-pants me-2"></i>Quần jogger
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan">
                  <i class="ph-grid-four me-2"></i>Tất cả quần
                </router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link sale-link" to="/category/sale">
              <div class="sale-container">
                <span class="sale-percent">-50%</span>
                <span class="nav-text sale-text">SALE</span>
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
              <i class="ph-magnifying-glass"></i>
            </button>
          </div>
        </div>
        
        <!-- Right Menu -->
        <ul class="navbar-nav">
          <!-- Wishlist -->
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link position-relative" to="/wishlist">
              <i class="ph-heart"></i>
              <span v-if="productStore.wishlistCount > 0" class="badge modern-cart-badge">
                {{ productStore.wishlistCount }}
              </span>
            </router-link>
          </li>
          
          <!-- Cart -->
          <li class="nav-item">
            <button 
              class="nav-link modern-nav-link position-relative cart-trigger"
              @click="toggleMiniCart"
            >
              <i class="ph-shopping-cart"></i>
              <span v-if="cartStore.itemCount > 0" class="badge modern-cart-badge">
                {{ cartStore.itemCount }}
              </span>
            </button>
          </li>
          
          <!-- User Menu -->
          <li v-if="!isLoggedIn" class="nav-item">
            <button class="nav-link modern-nav-link login-btn" @click="openLoginPopup">
              <i class="ph-user"></i>
              <span class="d-none d-lg-inline ms-1">Đăng nhập</span>
            </button>
          </li>
          
          <li v-else class="nav-item dropdown dropdown-hover">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside">
              <i class="ph-user-circle"></i>
              <span class="d-none d-lg-inline ms-1">{{ user?.name || 'Tài khoản' }}</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end modern-dropdown">
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/profile">
                  <i class="ph-user me-2"></i>Thông tin cá nhân
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/orders">
                  <i class="ph-shopping-bag me-2"></i>Đơn hàng của tôi
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <button class="dropdown-item modern-dropdown-item" @click="handleLogout">
                  <i class="ph-sign-out me-2"></i>Đăng xuất
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { useProductStore } from '../../stores/product'
import SearchModal from '../ui/SearchModal.vue'
import LoginPopup from '../ui/LoginPopup.vue'
import RegisterPopup from '../ui/RegisterPopup.vue'
import MiniCart from '../ui/MiniCart.vue'

const router = useRouter()
const cartStore = useCartStore()
const productStore = useProductStore()

// Login popup state
const showLoginPopup = ref(false)
const showRegisterPopup = ref(false)
const showMiniCart = ref(false)

// Search state
const searchQuery = ref('')

// Mock user data - replace with actual auth store
const isLoggedIn = computed(() => {
  return localStorage.getItem('auro_token') !== null
})

const user = computed(() => {
  const userData = localStorage.getItem('auro_user')
  return userData ? JSON.parse(userData) : null
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

const handleLogout = () => {
  localStorage.removeItem('auro_token')
  localStorage.removeItem('auro_user')
  router.push('/')
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

// Check for register popup trigger on mount
onMounted(() => {
  const showRegisterPopupFlag = localStorage.getItem('auro_show_register_popup')
  if (showRegisterPopupFlag === 'true') {
    localStorage.removeItem('auro_show_register_popup')
    showRegisterPopup.value = true
  }
  
  // Initialize dropdown hover behavior
  initializeDropdownHover()
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
  padding: 0.5rem 0;
  font-size: 0.875rem;
  font-weight: 500;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1050;
}

.top-bar-text {
  color: white;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Modern Navbar - Cool Mate Inspired */
.modern-navbar {
  background: white !important;
  border-bottom: 1px solid var(--auro-border);
  padding: 1rem 0;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.875rem;
  font-weight: 500;
  margin-top: 40px; /* Account for top bar */
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
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.sale-percent {
  font-size: 0.75rem;
  color: #dc3545;
  font-weight: 700;
  line-height: 1;
}

.sale-text {
  color: #dc3545 !important;
  font-weight: 700;
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

/* Mobile responsive */
@media (max-width: 991.98px) {
  .search-container {
    margin-left: 0;
    margin-top: 1rem;
    width: 100%;
  }
  
  .search-input-group {
    width: 100%;
  }
  
  .brand-text {
    font-size: 20px;
  }
  
  .brand-subtitle {
    font-size: 9px;
  }
  
  .modern-navbar {
    margin-top: 40px;
  }
  
  /* Disable hover on mobile, use click instead */
  .dropdown-hover:hover .dropdown-menu {
    display: none;
  }
  
  .dropdown-hover .dropdown-menu.show {
    display: block;
  }
}

@media (max-width: 576px) {
  .modern-navbar {
    padding: 0.75rem 0;
  }
  
  .brand-text {
    font-size: 18px;
    letter-spacing: 1px;
  }
  
  .brand-icon {
    font-size: 20px;
  }
  
  .top-bar {
    padding: 0.25rem 0;
  }
  
  .top-bar-text {
    font-size: 0.75rem;
  }
}
</style>
