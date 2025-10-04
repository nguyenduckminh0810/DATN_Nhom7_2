<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white fixed-top modern-navbar">
    <div class="container">
      <!-- Brand -->
      <router-link class="navbar-brand fw-bold" to="/">
        <span class="brand-text">AURO</span>
        <span class="brand-subtitle">MENSWEAR</span>
      </router-link>
      
      <!-- Mobile Toggle -->
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <!-- Navigation Menu -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link" :class="{ active: $route.name === 'home' }" to="/">
              <span>Trang chủ</span>
            </router-link>
          </li>
          <li class="nav-item dropdown">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
              <span>Sản phẩm</span>
            </a>
            <ul class="dropdown-menu modern-dropdown">
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/ao">
                  <i class="ph-t-shirt me-2"></i>Áo
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/quan">
                  <i class="ph-bag me-2"></i>Quần
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category/phu-kien">
                  <i class="ph-watch me-2"></i>Phụ kiện
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item modern-dropdown-item" to="/category">
                  <i class="ph-grid-four me-2"></i>Tất cả sản phẩm
                </router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item">
            <a class="nav-link modern-nav-link" href="#about">
              <span>Giới thiệu</span>
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link modern-nav-link" href="#contact">
              <span>Liên hệ</span>
            </a>
          </li>
        </ul>
        
        <!-- Search Button -->
        <div class="me-4">
          <button 
            class="btn modern-search-btn" 
            type="button"
            data-bs-toggle="modal" 
            data-bs-target="#searchModal"
          >
            <i class="ph-magnifying-glass"></i>
            <span class="d-none d-lg-inline ms-2">Tìm kiếm</span>
          </button>
        </div>
        
        <!-- Right Menu -->
        <ul class="navbar-nav">
          <!-- Wishlist -->
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link position-relative" to="/wishlist">
              <i class="ph-heart"></i>
              <span v-if="wishlistStore.itemCount > 0" class="badge modern-cart-badge">
                {{ wishlistStore.itemCount }}
              </span>
            </router-link>
          </li>
          
          <!-- Cart -->
          <li class="nav-item">
            <router-link class="nav-link modern-nav-link position-relative" to="/cart">
              <i class="ph-shopping-cart"></i>
              <span v-if="cartStore.itemCount > 0" class="badge modern-cart-badge">
                {{ cartStore.itemCount }}
              </span>
            </router-link>
          </li>
          
          <!-- User Menu -->
          <li v-if="!isLoggedIn" class="nav-item">
            <router-link class="nav-link modern-nav-link" to="/login">
              <i class="ph-user"></i>
              <span class="d-none d-lg-inline ms-1">Đăng nhập</span>
            </router-link>
          </li>
          
          <li v-else class="nav-item dropdown">
            <a class="nav-link modern-nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
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
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { useWishlistStore } from '../../stores/wishlist'
import SearchModal from '../ui/SearchModal.vue'

const router = useRouter()
const cartStore = useCartStore()
const wishlistStore = useWishlistStore()

// Mock user data - replace with actual auth store
const isLoggedIn = computed(() => {
  return localStorage.getItem('auro_token') !== null
})

const user = computed(() => {
  const userData = localStorage.getItem('auro_user')
  return userData ? JSON.parse(userData) : null
})

// Search functionality removed - now using SearchModal

const handleLogout = () => {
  localStorage.removeItem('auro_token')
  localStorage.removeItem('auro_user')
  router.push('/')
}
</script>

<style scoped>
/* Modern Navbar */
.modern-navbar {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--auro-border);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  padding: 1rem 0;
  transition: all 0.3s ease;
}

.modern-navbar:hover {
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.12);
}

/* Brand */
.navbar-brand {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-decoration: none;
  color: var(--auro-primary) !important;
  transition: all 0.3s ease;
}

.navbar-brand:hover {
  color: var(--auro-accent) !important;
  transform: translateY(-1px);
}

.brand-text {
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 3px;
  line-height: 1;
}

.brand-subtitle {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 2px;
  color: var(--auro-text-light);
  margin-top: -2px;
}

/* Navigation Links */
.modern-nav-link {
  position: relative;
  font-weight: 500;
  color: var(--auro-text) !important;
  padding: 0.75rem 1rem !important;
  border-radius: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
}

.modern-nav-link::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--auro-gradient-accent);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.modern-nav-link:hover::before,
.modern-nav-link.active::before {
  width: 80%;
}

.modern-nav-link:hover {
  color: var(--auro-accent) !important;
  background: rgba(212, 175, 55, 0.1);
  transform: translateY(-1px);
}

.modern-nav-link.active {
  color: var(--auro-accent) !important;
  background: rgba(212, 175, 55, 0.1);
}

/* Search */
.modern-search {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--auro-shadow);
  border: 1px solid var(--auro-border);
  transition: all 0.3s ease;
}

.modern-search:hover {
  box-shadow: var(--auro-shadow-hover);
  border-color: var(--auro-accent);
}

.modern-search-input {
  border: none !important;
  background: var(--auro-card);
  padding: 12px 16px;
  font-size: 14px;
  width: 250px;
}

.modern-search-input:focus {
  box-shadow: none !important;
  background: var(--auro-light);
}

.modern-search-btn {
  background: var(--auro-gradient-accent);
  border: none;
  color: var(--auro-dark);
  padding: 12px 16px;
  transition: all 0.3s ease;
}

.modern-search-btn:hover {
  background: var(--auro-accent);
  transform: scale(1.05);
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
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.3);
}

/* Dropdown */
.modern-dropdown {
  border: none;
  border-radius: 16px;
  box-shadow: var(--auro-shadow-hover);
  padding: 8px;
  margin-top: 8px;
  background: var(--auro-card);
  border: 1px solid var(--auro-border);
}

.modern-dropdown-item {
  border-radius: 8px;
  padding: 12px 16px;
  margin: 2px 0;
  transition: all 0.3s ease;
  color: var(--auro-text);
  font-weight: 500;
}

.modern-dropdown-item:hover {
  background: rgba(212, 175, 55, 0.1);
  color: var(--auro-accent);
  transform: translateX(4px);
}

/* Mobile responsive */
@media (max-width: 991.98px) {
  .modern-search {
    margin-top: 1rem;
    width: 100% !important;
  }
  
  .modern-search-input {
    width: 100% !important;
  }
  
  .brand-text {
    font-size: 24px;
  }
  
  .brand-subtitle {
    font-size: 9px;
  }
}

@media (max-width: 576px) {
  .modern-navbar {
    padding: 0.75rem 0;
  }
  
  .brand-text {
    font-size: 20px;
    letter-spacing: 2px;
  }
}
</style>
