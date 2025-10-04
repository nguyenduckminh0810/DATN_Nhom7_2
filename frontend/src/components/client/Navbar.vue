<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
    <div class="container">
      <!-- Brand -->
      <router-link class="navbar-brand fw-bold" to="/">
        <i class="bi bi-shop me-2"></i>AURO
      </router-link>
      
      <!-- Mobile Toggle -->
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <!-- Navigation Menu -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link class="nav-link" :class="{ active: $route.name === 'home' }" to="/">
              Trang chủ
            </router-link>
          </li>
          <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
              Sản phẩm
            </a>
            <ul class="dropdown-menu">
              <li>
                <router-link class="dropdown-item" to="/category/ao">Áo</router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/category/quan">Quần</router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/category/phu-kien">Phụ kiện</router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item" to="/category">Tất cả sản phẩm</router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#about">Giới thiệu</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#contact">Liên hệ</a>
          </li>
        </ul>
        
        <!-- Search Bar -->
        <form class="d-flex me-3" @submit.prevent="handleSearch">
          <div class="input-group">
            <input 
              v-model="searchQuery"
              class="form-control form-control-sm" 
              type="search" 
              placeholder="Tìm kiếm sản phẩm..."
              style="width: 200px;"
            >
            <button class="btn btn-outline-light btn-sm" type="submit">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </form>
        
        <!-- Right Menu -->
        <ul class="navbar-nav">
          <!-- Cart -->
          <li class="nav-item">
            <router-link class="nav-link position-relative" to="/cart">
              <i class="bi bi-cart3"></i>
              <span v-if="cartStore.itemCount > 0" class="badge bg-danger cart-count">
                {{ cartStore.itemCount }}
              </span>
            </router-link>
          </li>
          
          <!-- User Menu -->
          <li v-if="!isLoggedIn" class="nav-item">
            <router-link class="nav-link" to="/login">
              <i class="bi bi-person"></i> Đăng nhập
            </router-link>
          </li>
          
          <li v-else class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
              <i class="bi bi-person-circle"></i> {{ user?.name || 'Tài khoản' }}
            </a>
            <ul class="dropdown-menu dropdown-menu-end">
              <li>
                <router-link class="dropdown-item" to="/profile">
                  <i class="bi bi-person me-2"></i>Thông tin cá nhân
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/orders">
                  <i class="bi bi-bag me-2"></i>Đơn hàng của tôi
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <button class="dropdown-item" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                </button>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const searchQuery = ref('')

// Mock user data - replace with actual auth store
const isLoggedIn = computed(() => {
  return localStorage.getItem('auro_token') !== null
})

const user = computed(() => {
  const userData = localStorage.getItem('auro_user')
  return userData ? JSON.parse(userData) : null
})

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      name: 'category',
      query: { search: searchQuery.value.trim() }
    })
  }
}

const handleLogout = () => {
  localStorage.removeItem('auro_token')
  localStorage.removeItem('auro_user')
  router.push('/')
}
</script>

<style scoped>
.navbar-brand {
  font-size: 1.5rem;
  letter-spacing: 1px;
}

.navbar-nav .nav-link {
  font-weight: 500;
  transition: color 0.3s ease;
}

.navbar-nav .nav-link:hover {
  color: #ffc107 !important;
}

.cart-count {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 0.75rem;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown-menu {
  border: none;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.dropdown-item {
  transition: background-color 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

/* Mobile responsive */
@media (max-width: 991.98px) {
  .input-group {
    margin-top: 1rem;
    width: 100% !important;
  }
  
  .input-group input {
    width: 100% !important;
  }
}
</style>
