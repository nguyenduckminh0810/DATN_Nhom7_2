<template>
  <div v-if="showBanner" class="invalid-token-banner">
    <div class="banner-content">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <div class="banner-text">
        <strong>Token không hợp lệ!</strong>
        <p>Phiên đăng nhập của bạn đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.</p>
      </div>
      <button @click="forceLogout" class="btn-logout">
        <i class="bi bi-arrow-clockwise"></i> Đăng nhập lại
      </button>
      <button @click="hideBanner" class="btn-close-banner">
        <i class="bi bi-x"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { validateToken } from '@/utils/tokenValidator'

const showBanner = ref(false)

onMounted(() => {
  const token = localStorage.getItem('auro_token')
  if (token) {
    const validation = validateToken(token)
    if (!validation.valid) {
      console.warn('⚠️ Invalid token detected in banner check:', validation.reason)
      showBanner.value = true
    }
  }
})

const forceLogout = () => {
  localStorage.clear()
  sessionStorage.setItem('auth_clear_reason', 'Token không hợp lệ - đã xóa và yêu cầu đăng nhập lại')
  window.location.href = '/'
}

const hideBanner = () => {
  showBanner.value = false
}
</script>

<style scoped>
.invalid-token-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  padding: 1rem;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
  }
  to {
    transform: translateY(0);
  }
}

.banner-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.banner-content > i {
  font-size: 2rem;
  flex-shrink: 0;
}

.banner-text {
  flex: 1;
}

.banner-text strong {
  display: block;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.banner-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.btn-logout {
  background: white;
  color: #ff6b6b;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.btn-close-banner {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.btn-close-banner:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .banner-content {
    flex-wrap: wrap;
  }
  
  .banner-text {
    flex: 1 1 100%;
    order: 2;
  }
  
  .btn-logout {
    order: 3;
    margin-top: 0.5rem;
  }
}
</style>
