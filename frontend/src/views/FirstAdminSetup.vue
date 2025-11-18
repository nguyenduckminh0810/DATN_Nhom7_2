<template>
  <div style="padding-top: 10px; margin-top: 50px;" >
    <div class="container" >
      <div class="row justify-content-center" style="padding-top: 10px;">
        <div class="col-lg-5 col-md-7">
          <div class="setup-card" >
            <div class="setup-header">

              <h2>Thiết lập Admin đầu tiên</h2>
              <p class="text-muted">
                Chào mừng đến với AURO! Hãy tạo tài khoản quản trị viên đầu tiên để bắt đầu.
              </p>
            </div>

            <div v-if="successMessage" class="alert alert-success">
              <i class="bi bi-check-circle me-2"></i>{{ successMessage }}
              <div class="mt-3">
                <router-link to="/" class="btn btn-success">
                  <i class="bi bi-box-arrow-in-right me-2"></i>Đăng nhập ngay
                </router-link>
              </div>
            </div>

            <div v-if="errorMessage" class="alert alert-danger">
              <i class="bi bi-exclamation-triangle me-2"></i>{{ errorMessage }}
            </div>

            <div v-if="systemHasAdmin" class="alert alert-warning">
              <i class="bi bi-info-circle me-2"></i>
              Hệ thống đã có admin. Vui lòng đăng nhập hoặc liên hệ admin hiện tại.
              <div class="mt-3">
                <router-link to="/" class="btn btn-warning">
                  <i class="bi bi-house-door me-2"></i>Về trang chủ
                </router-link>
              </div>
            </div>

            <form v-if="!successMessage && !systemHasAdmin" @submit.prevent="handleSubmit">
              <div class="mb-3">
                <label class="form-label">
                  <i class="bi bi-person me-2"></i>Họ và tên <span class="text-danger">*</span>
                </label>
                <input
                  v-model="form.hoTen"
                  type="text"
                  class="form-control"
                  placeholder="Nhập họ tên của bạn"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">
                  <i class="bi bi-envelope me-2"></i>Email <span class="text-danger">*</span>
                </label>
                <input
                  v-model="form.email"
                  type="email"
                  class="form-control"
                  placeholder="admin@example.com"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">
                  <i class="bi bi-phone me-2"></i>Số điện thoại
                </label>
                <input
                  v-model="form.soDienThoai"
                  type="tel"
                  class="form-control"
                  placeholder="0123456789 (tùy chọn)"
                  pattern="[0-9]{10,11}"
                />
              </div>

              <div class="mb-3">
                <label class="form-label">
                  <i class="bi bi-lock me-2"></i>Mật khẩu <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                  <input
                    v-model="form.matKhau"
                    :type="showPassword ? 'text' : 'password'"
                    class="form-control"
                    placeholder="Ít nhất 6 ký tự"
                    minlength="6"
                    required
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="showPassword = !showPassword"
                  >
                    <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                  </button>
                </div>
              </div>

              <div class="mb-4">
                <label class="form-label">
                  <i class="bi bi-lock-fill me-2"></i>Xác nhận mật khẩu
                  <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                  <input
                    v-model="form.xacNhanMatKhau"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    class="form-control"
                    placeholder="Nhập lại mật khẩu"
                    required
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="showConfirmPassword = !showConfirmPassword"
                  >
                    <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                  </button>
                </div>
              </div>

              <button type="submit" class="btn btn-warning w-100" :disabled="isSubmitting">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-shield-check me-2"></i>
                {{ isSubmitting ? 'Đang tạo tài khoản...' : 'Tạo tài khoản Admin' }}
              </button>
            </form>


          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const form = ref({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  xacNhanMatKhau: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isSubmitting = ref(false)
const successMessage = ref(null)
const errorMessage = ref(null)
const systemHasAdmin = ref(false)

const handleSubmit = async () => {
  errorMessage.value = null

  // Validate
  if (form.value.matKhau !== form.value.xacNhanMatKhau) {
    errorMessage.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  if (form.value.matKhau.length < 6) {
    errorMessage.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    return
  }

  isSubmitting.value = true

  try {
    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

    const response = await axios.post(`${baseURL}/admin/register-first-admin`, {
      hoTen: form.value.hoTen,
      email: form.value.email,
      soDienThoai: form.value.soDienThoai || null,
      loaiTaiKhoan: 'ADM',
      matKhau: form.value.matKhau,
    })

    if (response.data.success) {
      successMessage.value =
        'Tạo tài khoản admin thành công! Bạn có thể đăng nhập ngay bây giờ.'
      // Clear form
      form.value = {
        hoTen: '',
        email: '',
        soDienThoai: '',
        matKhau: '',
        xacNhanMatKhau: '',
      }
    } else {
      errorMessage.value = response.data.message || 'Có lỗi xảy ra'
    }
  } catch (error) {
    console.error('First admin setup error:', error)
    const message = error.response?.data?.message || error.response?.data || error.message

    if (message.includes('đã có admin')) {
      systemHasAdmin.value = true
      errorMessage.value = message
    } else {
      errorMessage.value = message || 'Có lỗi xảy ra khi tạo tài khoản. Vui lòng thử lại!'
    }
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  document.title = 'Thiết lập Admin đầu tiên - AURO'
})
</script>

<style scoped>
.first-admin-setup {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  padding: 2rem 0;
}

.setup-card {
  background: white;
  border-radius: 1rem;
  padding: 2.5rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.setup-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.logo i {
  font-size: 2.5rem;
  color: white;
}

.setup-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-control:focus,
.input-group .form-control:focus {
  border-color: #ffc107;
  box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

.btn-warning {
  padding: 0.75rem;
  font-weight: 600;
  font-size: 1.1rem;
}

.setup-footer {
  margin-top: 2rem;
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e9ecef;
}

.alert {
  border-radius: 0.5rem;
}
</style>
