<template>
  <div class="order-success">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <!-- Success Card -->
          <div class="success-card text-center">
            <!-- Success Icon -->
            <div class="success-icon mb-4">
              <div class="checkmark-circle">
                <div class="checkmark"></div>
              </div>
            </div>

            <!-- Success Message -->
            <h1 class="success-title mb-3">Đặt hàng thành công!</h1>
            <p class="success-subtitle mb-4">
              Cảm ơn bạn đã tin tưởng và mua hàng tại AURO
            </p>

            <!-- Order Info -->
            <div class="order-info-box mb-4" v-if="orderId && orderId !== 'pending'">
              <p class="mb-2">Mã đơn hàng của bạn:</p>
              <h3 class="order-id">#{{ orderId }}</h3>
            </div>

            <!-- Email Confirmation -->
            <div class="email-confirmation mb-4" v-if="email">
              <i class="bi bi-envelope-check text-primary fs-2 mb-2"></i>
              <p class="mb-0">
                Chúng tôi đã gửi email xác nhận đến:
              </p>
              <p class="email-text">{{ email }}</p>
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons">
              <router-link 
                v-if="isAuthenticated" 
                to="/orders" 
                class="btn btn-primary btn-lg me-3"
              >
                <i class="bi bi-bag-check me-2"></i>
                Xem đơn hàng của tôi
              </router-link>
              <router-link to="/" class="btn btn-outline-primary btn-lg">
                <i class="bi bi-house me-2"></i>
                Về trang chủ
              </router-link>
            </div>

            <!-- Additional Info -->
            <div class="additional-info mt-5">
              <div class="row text-start">
                <div class="col-md-4">
                  <div class="info-item">
                    <i class="bi bi-truck text-primary fs-3 mb-2"></i>
                    <h6>Giao hàng nhanh</h6>
                    <p class="small text-muted">Đơn hàng sẽ được xử lý và giao trong 2-3 ngày</p>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="info-item">
                    <i class="bi bi-shield-check text-primary fs-3 mb-2"></i>
                    <h6>Đảm bảo chất lượng</h6>
                    <p class="small text-muted">Sản phẩm được kiểm tra kỹ trước khi giao</p>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="info-item">
                    <i class="bi bi-arrow-clockwise text-primary fs-3 mb-2"></i>
                    <h6>Đổi trả dễ dàng</h6>
                    <p class="small text-muted">Hỗ trợ đổi trả trong vòng 7 ngày</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const orderId = ref(null)
const email = ref(null)

const isAuthenticated = computed(() => userStore.isAuthenticated)

onMounted(() => {
  orderId.value = route.query.orderId
  email.value = route.query.email
})
</script>

<style scoped>
.order-success {
  min-height: 80vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.success-card {
  background: white;
  border-radius: 20px;
  padding: 3rem 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.success-icon {
  display: flex;
  justify-content: center;
  align-items: center;
}

.checkmark-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  animation: scaleIn 0.5s ease-in-out;
}

.checkmark {
  width: 60px;
  height: 60px;
  border-right: 6px solid white;
  border-bottom: 6px solid white;
  transform: rotate(45deg);
  animation: checkmark 0.5s ease-in-out 0.3s forwards;
  opacity: 0;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

@keyframes checkmark {
  from {
    opacity: 0;
    transform: rotate(45deg) scale(0);
  }
  to {
    opacity: 1;
    transform: rotate(45deg) scale(1);
  }
}

.success-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
}

.success-subtitle {
  font-size: 1.2rem;
  color: #666;
}

.order-info-box {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 12px;
  padding: 1.5rem;
  border: 2px dashed #667eea;
}

.order-id {
  color: #667eea;
  font-weight: 700;
  font-size: 2rem;
  margin: 0;
}

.email-confirmation {
  padding: 1rem;
}

.email-text {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
}

.action-buttons {
  margin-top: 2rem;
}

.btn-lg {
  padding: 0.8rem 2rem;
  font-size: 1.1rem;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.4);
}

.btn-outline-primary {
  border: 2px solid #667eea;
  color: #667eea;
}

.btn-outline-primary:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.additional-info {
  border-top: 1px solid #e0e0e0;
  padding-top: 2rem;
}

.info-item {
  text-align: center;
  padding: 1rem;
}

.info-item h6 {
  font-weight: 600;
  margin-top: 0.5rem;
  color: #333;
}

@media (max-width: 768px) {
  .success-card {
    padding: 2rem 1rem;
  }

  .success-title {
    font-size: 2rem;
  }

  .btn-lg {
    display: block;
    width: 100%;
    margin-bottom: 1rem;
  }

  .btn-lg.me-3 {
    margin-right: 0 !important;
  }
}
</style>
