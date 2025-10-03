<template>
  <div class="register">
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
                <h4 class="mt-3">Đăng ký tài khoản</h4>
                <p class="text-muted">Tạo tài khoản để mua sắm thuận tiện hơn!</p>
              </div>

              <!-- Register Form -->
              <form @submit.prevent="handleRegister">
                <!-- Full Name -->
                <div class="mb-3">
                  <label class="form-label">Họ và tên *</label>
                  <input v-model="form.fullName" 
                         type="text" 
                         class="form-control" 
                         placeholder="Nhập họ và tên"
                         required>
                </div>

                <!-- Email -->
                <div class="mb-3">
                  <label class="form-label">Email *</label>
                  <input v-model="form.email" 
                         type="email" 
                         class="form-control" 
                         placeholder="Nhập địa chỉ email"
                         required>
                </div>

                <!-- Phone -->
                <div class="mb-3">
                  <label class="form-label">Số điện thoại *</label>
                  <input v-model="form.phone" 
                         type="tel" 
                         class="form-control" 
                         placeholder="Nhập số điện thoại"
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
                  <div class="form-text">
                    Mật khẩu phải có ít nhất 6 ký tự
                  </div>
                </div>

                <!-- Confirm Password -->
                <div class="mb-3">
                  <label class="form-label">Xác nhận mật khẩu *</label>
                  <div class="input-group">
                    <input :type="showConfirmPassword ? 'text' : 'password'" 
                           v-model="form.confirmPassword" 
                           class="form-control" 
                           placeholder="Nhập lại mật khẩu"
                           required>
                    <button class="btn btn-outline-secondary" 
                            type="button" 
                            @click="showConfirmPassword = !showConfirmPassword">
                      <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                </div>

                <!-- Terms and Conditions -->
                <div class="mb-3">
                  <div class="form-check">
                    <input v-model="form.acceptTerms" 
                           class="form-check-input" 
                           type="checkbox" 
                           id="acceptTerms"
                           required>
                    <label class="form-check-label" for="acceptTerms">
                      Tôi đồng ý với 
                      <a href="#" class="text-warning text-decoration-none">Điều khoản sử dụng</a>
                      và 
                      <a href="#" class="text-warning text-decoration-none">Chính sách bảo mật</a>
                    </label>
                  </div>
                </div>

                <!-- Newsletter -->
                <div class="mb-3">
                  <div class="form-check">
                    <input v-model="form.subscribeNewsletter" 
                           class="form-check-input" 
                           type="checkbox" 
                           id="subscribeNewsletter">
                    <label class="form-check-label" for="subscribeNewsletter">
                      Đăng ký nhận thông tin khuyến mãi và sản phẩm mới
                    </label>
                  </div>
                </div>

                <!-- Submit Button -->
                <button type="submit" 
                        class="btn btn-warning w-100 mb-3"
                        :disabled="isSubmitting">
                  <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-person-plus me-2"></i>
                  {{ isSubmitting ? 'Đang đăng ký...' : 'Đăng ký' }}
                </button>

                <!-- Divider -->
                <div class="text-center mb-3">
                  <span class="text-muted">Hoặc</span>
                </div>

                <!-- Social Register -->
                <div class="d-grid gap-2 mb-3">
                  <button type="button" class="btn btn-outline-primary">
                    <i class="bi bi-facebook me-2"></i>Đăng ký với Facebook
                  </button>
                  <button type="button" class="btn btn-outline-danger">
                    <i class="bi bi-google me-2"></i>Đăng ký với Google
                  </button>
                </div>

                <!-- Login Link -->
                <div class="text-center">
                  <p class="mb-0">
                    Đã có tài khoản? 
                    <router-link to="/login" class="text-warning text-decoration-none fw-bold">
                      Đăng nhập ngay
                    </router-link>
                  </p>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Reactive data
const isSubmitting = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = ref({
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
  subscribeNewsletter: false
})

// Methods
const handleRegister = async () => {
  // Validation
  if (form.value.password !== form.value.confirmPassword) {
    alert('Mật khẩu xác nhận không khớp')
    return
  }

  if (form.value.password.length < 6) {
    alert('Mật khẩu phải có ít nhất 6 ký tự')
    return
  }

  if (!form.value.acceptTerms) {
    alert('Vui lòng đồng ý với điều khoản sử dụng')
    return
  }

  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Mock registration
    const userData = {
      id: Date.now(),
      name: form.value.fullName,
      email: form.value.email,
      phone: form.value.phone,
      role: 'CUSTOMER'
    }
    
    // Store user data
    localStorage.setItem('auro_token', 'mock-jwt-token')
    localStorage.setItem('auro_user', JSON.stringify(userData))
    
    // Redirect to home
    router.push('/')
    
    alert('Đăng ký thành công! Chào mừng bạn đến với AURO!')
    
  } catch (error) {
    console.error('Register error:', error)
    alert('Có lỗi xảy ra khi đăng ký. Vui lòng thử lại.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register {
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

.form-text {
  font-size: 0.8rem;
  color: #6c757d;
}
</style>
