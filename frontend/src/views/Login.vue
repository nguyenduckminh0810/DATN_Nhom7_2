<template>
  <div class="login">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
          <div class="card shadow">
            <div class="card-body p-5">
              <!-- Header -->
              <div class="text-center mb-4">
                <h2 class="fw-bold text-warning">
                  <i class="bi bi-shop me-2"></i>AURO
                </h2>
                <h4 class="mt-3">Đăng nhập</h4>
                <p class="text-muted">Chào mừng bạn quay trở lại!</p>
              </div>

              <!-- Login Form -->
              <form @submit.prevent="handleLogin">
                <!-- Email/Phone -->
                <div class="mb-3">
                  <label class="form-label">Email hoặc số điện thoại *</label>
                  <input v-model="form.login" 
                         type="text" 
                         class="form-control" 
                         placeholder="Nhập email hoặc số điện thoại"
                         required>
                </div>

                <!-- Password -->
                <div class="mb-3">
                  <label class="form-label">Mật khẩu *</label>
                  <div class="input-group">
                    <input :type="showPassword ? 'text' : 'password'" 
                           v-model="form.password" 
                           class="form-control" 
                           placeholder="Nhập mật khẩu"
                           required>
                    <button class="btn btn-outline-secondary" 
                            type="button" 
                            @click="showPassword = !showPassword">
                      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                </div>

                <!-- Remember Me & Forgot Password -->
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div class="form-check">
                    <input v-model="form.rememberMe" 
                           class="form-check-input" 
                           type="checkbox" 
                           id="rememberMe">
                    <label class="form-check-label" for="rememberMe">
                      Ghi nhớ đăng nhập
                    </label>
                  </div>
                  <router-link to="/forgot-password" class="text-decoration-none">
                    Quên mật khẩu?
                  </router-link>
                </div>

                <!-- Submit Button -->
                <button type="submit" 
                        class="btn btn-warning w-100 mb-3"
                        :disabled="isSubmitting">
                  <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-box-arrow-in-right me-2"></i>
                  {{ isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}
                </button>

                <!-- Divider -->
                <div class="text-center mb-3">
                  <span class="text-muted">Hoặc</span>
                </div>

                <!-- Social Login -->
                <div class="d-grid gap-2 mb-3">
                  <button type="button" class="btn btn-outline-primary">
                    <i class="bi bi-facebook me-2"></i>Đăng nhập với Facebook
                  </button>
                  <button type="button" class="btn btn-outline-danger">
                    <i class="bi bi-google me-2"></i>Đăng nhập với Google
                  </button>
                </div>

                <!-- Register Link -->
                <div class="text-center">
                  <p class="mb-0">
                    Chưa có tài khoản? 
                    <router-link to="/register" class="text-warning text-decoration-none fw-bold">
                      Đăng ký ngay
                    </router-link>
                  </p>
                </div>
              </form>
            </div>
          </div>

          <!-- Guest Checkout -->
          <div class="card mt-3">
            <div class="card-body text-center">
              <h6 class="mb-2">Mua hàng không cần đăng ký</h6>
              <p class="text-muted small mb-3">
                Bạn có thể mua hàng và thanh toán mà không cần tạo tài khoản
              </p>
              <router-link to="/checkout" class="btn btn-outline-secondary">
                <i class="bi bi-cart me-2"></i>Mua hàng ngay
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// Reactive data
const isSubmitting = ref(false)
const showPassword = ref(false)

const form = ref({
  login: '',
  password: '',
  rememberMe: false
})

// Methods
const handleLogin = async () => {
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Mock login validation
    if (form.value.login && form.value.password) {
      // Mock user data
      const userData = {
        id: 1,
        name: 'Nguyễn Văn A',
        email: form.value.login.includes('@') ? form.value.login : 'user@example.com',
        phone: form.value.login.includes('@') ? '0123456789' : form.value.login,
        role: 'CUSTOMER'
      }
      
      // Store user data
      localStorage.setItem('auro_token', 'mock-jwt-token')
      localStorage.setItem('auro_user', JSON.stringify(userData))
      
      // Redirect to intended page or home
      const redirectTo = route.query.redirect || '/'
      router.push(redirectTo)
      
    } else {
      throw new Error('Thông tin đăng nhập không hợp lệ')
    }
    
  } catch (error) {
    console.error('Login error:', error)
    alert('Email/số điện thoại hoặc mật khẩu không đúng')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 80vh;
  display: flex;
  align-items: center;
}

.card {
  border: none;
  border-radius: 1rem;
}

.form-control {
  border-radius: 0.5rem;
  border: 1px solid #dee2e6;
  padding: 0.75rem 1rem;
}

.form-control:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

.btn-warning {
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  font-weight: 500;
}

.btn-outline-primary,
.btn-outline-danger {
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
}

.form-check-input:checked {
  background-color: #ffc107;
  border-color: #ffc107;
}

.form-check-label {
  cursor: pointer;
}

.input-group .btn {
  border-color: #dee2e6;
}

.input-group .btn:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.text-warning {
  color: #ffc107 !important;
}

a.text-warning:hover {
  color: #e0a800 !important;
}
</style>
