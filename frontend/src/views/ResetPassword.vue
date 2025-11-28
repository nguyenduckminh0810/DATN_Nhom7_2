<template>
  <div class="reset-password-page">
    <div class="reset-password-container">
      <div class="reset-password-card">
        <div class="card-header">
          <h1 class="card-title">AURO</h1>
          <h2 class="card-subtitle">Đặt lại mật khẩu</h2>
          <p class="card-description">Nhập mật khẩu mới cho tài khoản của bạn</p>
        </div>

        <!-- Success Message -->
        <div v-if="successMessage" class="success-message">
          <i class="bi bi-check-circle me-2"></i>
          {{ successMessage }}
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="error-message">
          <i class="bi bi-exclamation-circle me-2"></i>
          {{ errorMessage }}
        </div>

        <!-- Reset Password Form -->
        <form v-if="!successMessage && token" @submit.prevent="handleResetPassword" class="reset-form">
          <div class="form-group">
            <label for="password" class="form-label">Mật khẩu mới</label>
            <input
              id="password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              :class="{ 'input-error': errors.password }"
              placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
              @input="errors.password = ''"
              autocomplete="new-password"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showPassword = !showPassword"
            >
              <span class="toggle-icon">{{ showPassword ? '🙈' : '👁️' }}</span>
            </button>
            <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
          </div>

          <div class="form-group">
            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
              :class="{ 'input-error': errors.confirmPassword }"
              placeholder="Nhập lại mật khẩu mới"
              @input="errors.confirmPassword = ''"
              autocomplete="new-password"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <span class="toggle-icon">{{ showConfirmPassword ? '🙈' : '👁️' }}</span>
            </button>
            <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
          </div>

          <button
            type="submit"
            class="submit-btn"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting" class="spinner"></span>
            {{ isSubmitting ? 'Đang xử lý...' : 'ĐẶT LẠI MẬT KHẨU' }}
          </button>
        </form>

        <!-- Invalid Token Message -->
        <div v-if="!token && !successMessage" class="error-message">
          <i class="bi bi-exclamation-circle me-2"></i>
          Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
        </div>

        <!-- Back to Login -->
        <div class="form-footer">
          <router-link to="/" class="back-link">
            ← Quay lại trang chủ
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { success } = useToast()

const token = ref('')
const form = ref({
  password: '',
  confirmPassword: '',
})
const errors = ref({
  password: '',
  confirmPassword: '',
})
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

onMounted(() => {
  // Lấy token từ query parameter
  token.value = route.query.token || ''
  
  if (!token.value) {
    errorMessage.value = 'Link đặt lại mật khẩu không hợp lệ. Vui lòng yêu cầu link mới.'
  }
})

const validateForm = () => {
  errors.value = {
    password: '',
    confirmPassword: '',
  }

  let isValid = true

  if (!form.value.password || form.value.password.trim().length === 0) {
    errors.value.password = 'Vui lòng nhập mật khẩu mới'
    isValid = false
  } else if (form.value.password.length < 6) {
    errors.value.password = 'Mật khẩu phải có ít nhất 6 ký tự'
    isValid = false
  }

  if (!form.value.confirmPassword || form.value.confirmPassword.trim().length === 0) {
    errors.value.confirmPassword = 'Vui lòng xác nhận mật khẩu'
    isValid = false
  } else if (form.value.password !== form.value.confirmPassword) {
    errors.value.confirmPassword = 'Mật khẩu xác nhận không khớp'
    isValid = false
  }

  return isValid
}

const handleResetPassword = async () => {
  // Clear previous errors
  errorMessage.value = ''
  successMessage.value = ''

  // Validate form
  if (!validateForm()) {
    return
  }

  if (!token.value) {
    errorMessage.value = 'Token không hợp lệ'
    return
  }

  isSubmitting.value = true

  try {
    const response = await userStore.resetPassword(token.value, form.value.password)

    if (response && response.success) {
      successMessage.value = response.message || 'Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.'
      success('Đặt lại mật khẩu thành công!')
      
      // Redirect to home after 3 seconds
      setTimeout(() => {
        router.push('/')
      }, 3000)
    } else {
      errorMessage.value = response?.message || 'Không thể đặt lại mật khẩu. Vui lòng thử lại.'
    }
  } catch (error) {
    // Handle errors
    if (error.response) {
      const status = error.response.status
      const data = error.response.data

      if (status === 400) {
        errorMessage.value = data?.message || 'Token không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu link đặt lại mật khẩu mới.'
      } else if (status === 404) {
        errorMessage.value = 'Token không tồn tại. Vui lòng yêu cầu link đặt lại mật khẩu mới.'
      } else {
        errorMessage.value = data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.'
      }
    } else if (error.request) {
      errorMessage.value = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng'
    } else {
      errorMessage.value = 'Có lỗi xảy ra. Vui lòng thử lại.'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.reset-password-container {
  width: 100%;
  max-width: 500px;
}

.reset-password-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-title {
  font-family: var(--auro-heading-font);
  font-size: 32px;
  font-weight: 400;
  color: var(--auro-accent);
  margin-bottom: 10px;
  letter-spacing: 0.03rem;
}

.card-subtitle {
  font-size: 24px;
  font-weight: 600;
  color: var(--auro-text);
  margin-bottom: 8px;
}

.card-description {
  font-size: 14px;
  color: var(--auro-text-light);
  margin-bottom: 0;
}

.success-message {
  background: #d1fae5;
  border: 1px solid #86efac;
  color: #065f46;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

.error-message {
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.reset-form {
  margin-top: 20px;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--auro-text);
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 15px 50px 15px 20px;
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--auro-bg);
  color: var(--auro-text);
}

.form-input:hover {
  border-color: var(--auro-accent);
  background: var(--auro-card);
}

.form-input:focus {
  outline: none;
  border-color: var(--auro-accent);
  background: var(--auro-card);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
}

.form-input.input-error {
  border-color: #ef4444;
  background: #fef2f2;
}

.form-input.input-error:focus {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.password-toggle {
  position: absolute;
  right: 15px;
  top: 42px;
  background: none;
  border: none;
  color: var(--auro-text-light);
  cursor: pointer;
  padding: 5px;
}

.toggle-icon {
  font-size: 16px;
  display: block;
}

.password-toggle:hover {
  color: var(--auro-accent);
}

.error-text {
  display: block;
  color: #ef4444;
  font-size: 12px;
  margin-top: 5px;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  background: var(--auro-gradient);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(26, 26, 26, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.back-link {
  color: var(--auro-accent);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: var(--auro-dark);
  text-decoration: underline;
}

@media (max-width: 480px) {
  .reset-password-card {
    padding: 30px 20px;
  }

  .card-title {
    font-size: 28px;
  }

  .card-subtitle {
    font-size: 20px;
  }
}
</style>

