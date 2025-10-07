<template>
  <div class="login">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
          <div class="card shadow">
            <div class="card-body p-5">
              <!-- Header -->
              <div class="text-center mb-5">
                <div class="auth-header">
                  <div class="auth-icon">
                    <i class="bi bi-person-circle"></i>
                  </div>
                  <h2 class="fw-bold mb-3">Đăng nhập</h2>
                  <p class="text-muted fs-5">Chào mừng bạn quay trở lại!</p>
                </div>
              </div>

              <!-- Login Form -->
              <form @submit.prevent="handleLogin">
                <!-- Email/Phone -->
                <div class="mb-4">
                  <label class="form-label fw-bold">
                    <i class="bi bi-envelope me-2"></i>Email hoặc số điện thoại *
                  </label>
                  <input v-model="form.login" 
                         type="text" 
                         class="form-control modern-input" 
                         placeholder="Nhập email hoặc số điện thoại"
                         required>
                </div>

                <!-- Password -->
                <div class="mb-4">
                  <label class="form-label fw-bold">
                    <i class="bi bi-lock me-2"></i>Mật khẩu *
                  </label>
                  <div class="password-input-group">
                    <input :type="showPassword ? 'text' : 'password'" 
                           v-model="form.password" 
                           class="form-control modern-input" 
                           placeholder="Nhập mật khẩu"
                           required>
                    <button class="btn password-toggle-btn" 
                            type="button" 
                            @click="showPassword = !showPassword">
                      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                </div>

                <!-- Remember Me & Forgot Password -->
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <div class="form-check modern-check">
                    <input v-model="form.rememberMe" 
                           class="form-check-input" 
                           type="checkbox" 
                           id="rememberMe">
                    <label class="form-check-label" for="rememberMe">
                      Ghi nhớ đăng nhập
                    </label>
                  </div>
                  <router-link to="/forgot-password" class="forgot-password-link">
                    Quên mật khẩu?
                  </router-link>
                </div>

                <!-- Submit Button -->
                <button type="submit" 
                        class="btn btn-auro-primary w-100 mb-4 login-btn"
                        :disabled="isSubmitting">
                  <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-box-arrow-in-right me-2"></i>
                  {{ isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}
                </button>

                <!-- Divider -->
                <div class="divider-section">
                  <div class="divider-line"></div>
                  <span class="divider-text">Hoặc</span>
                  <div class="divider-line"></div>
                </div>

                <!-- Social Login -->
                <div class="social-login-section">
                  <div class="d-grid gap-3 mb-4">
                    <button type="button" class="btn social-btn facebook-btn">
                      <i class="bi bi-facebook me-2"></i>Đăng nhập với Facebook
                    </button>
                    <button type="button" class="btn social-btn google-btn">
                      <i class="bi bi-google me-2"></i>Đăng nhập với Google
                    </button>
                  </div>
                </div>

                <!-- Register Link -->
                <div class="register-link-section">
                  <p class="mb-0 text-center">
                    Chưa có tài khoản? 
                    <router-link to="/register" class="register-link">
                      Đăng ký ngay
                    </router-link>
                  </p>
                </div>
              </form>
            </div>
          </div>

          <!-- Guest Checkout -->
          <div class="card modern-guest-card mt-4">
            <div class="card-body text-center">
              <div class="guest-icon mb-3">
                <i class="bi bi-cart"></i>
              </div>
              <h6 class="mb-2 fw-bold">Mua hàng không cần đăng ký</h6>
              <p class="text-muted mb-3">
                Bạn có thể mua hàng và thanh toán mà không cần tạo tài khoản
              </p>
              <router-link to="/checkout" class="btn btn-outline-secondary guest-checkout-btn">
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
/* Login Container */
.login {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--auro-bg) 0%, var(--auro-card) 100%);
  display: flex;
  align-items: center;
  padding: 2rem 0;
}

/* Auth Card */
.modern-auth-card {
  border: 1px solid var(--auro-border);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  background: var(--auro-card);
  backdrop-filter: blur(10px);
}

/* Auth Header */
.auth-header {
  margin-bottom: 2rem;
}

.auth-icon {
  width: 80px;
  height: 80px;
  background: var(--auro-gradient-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.3);
}

.auth-icon i {
  font-size: 2.5rem;
  color: var(--auro-dark);
}

/* Form Controls */
.modern-input {
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 500;
  background: var(--auro-bg);
  color: var(--auro-text);
  transition: all 0.3s ease;
}

.modern-input:focus {
  border-color: var(--auro-accent);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
  background: var(--auro-card);
}

.modern-input::placeholder {
  color: var(--auro-text-light);
}

/* Password Input Group */
.password-input-group {
  position: relative;
}

.password-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--auro-text-light);
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.password-toggle-btn:hover {
  color: var(--auro-accent);
  background: rgba(212, 175, 55, 0.1);
}

/* Modern Checkbox */
.modern-check .form-check-input {
  border: 2px solid var(--auro-border);
  border-radius: 6px;
  width: 18px;
  height: 18px;
  background: var(--auro-bg);
}

.modern-check .form-check-input:checked {
  background-color: var(--auro-accent);
  border-color: var(--auro-accent);
}

.modern-check .form-check-label {
  color: var(--auro-text);
  font-weight: 500;
  cursor: pointer;
  margin-left: 0.5rem;
}

/* Forgot Password Link */
.forgot-password-link {
  color: var(--auro-accent);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.forgot-password-link:hover {
  color: var(--auro-dark);
  text-decoration: underline;
}

/* Login Button */
.login-btn {
  border-radius: 12px;
  padding: 14px 20px;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  transform: none;
  box-shadow: none;
}

/* Divider */
.divider-section {
  display: flex;
  align-items: center;
  margin: 2rem 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--auro-border);
}

.divider-text {
  padding: 0 1rem;
  color: var(--auro-text-light);
  font-size: 14px;
  font-weight: 500;
}

/* Social Login */
.social-btn {
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 500;
  border: 2px solid;
  transition: all 0.3s ease;
}

.facebook-btn {
  border-color: #1877f2;
  color: #1877f2;
  background: rgba(24, 119, 242, 0.05);
}

.facebook-btn:hover {
  background: #1877f2;
  color: white;
  transform: translateY(-2px);
}

.google-btn {
  border-color: #db4437;
  color: #db4437;
  background: rgba(219, 68, 55, 0.05);
}

.google-btn:hover {
  background: #db4437;
  color: white;
  transform: translateY(-2px);
}

/* Register Link */
.register-link-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--auro-border);
}

.register-link {
  color: var(--auro-accent);
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: var(--auro-dark);
  text-decoration: underline;
}

/* Guest Card */
.modern-guest-card {
  border: 1px solid var(--auro-border);
  border-radius: 20px;
  background: var(--auro-bg);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.05);
}

.guest-icon {
  width: 60px;
  height: 60px;
  background: var(--auro-gradient-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.guest-icon i {
  font-size: 1.5rem;
  color: var(--auro-dark);
}

.guest-checkout-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.guest-checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* Responsive */
@media (max-width: 768px) {
  .login {
    padding: 1rem 0;
  }
  
  .modern-auth-card .card-body {
    padding: 2rem !important;
  }
  
  .auth-icon {
    width: 60px;
    height: 60px;
  }
  
  .auth-icon i {
    font-size: 2rem;
  }
}
</style>
