<template>
  <!-- Modal Overlay -->
  <div v-if="isOpen" class="login-popup-overlay" @click="closePopup">
    <div class="login-popup-container" @click.stop>
      <!-- Close Button -->
      <button class="close-btn" @click="closePopup">
        <span class="close-icon">×</span>
      </button>

      <!-- Popup Content -->
      <div class="login-popup-content">
        <!-- Header -->
        <div class="popup-header">
          <h3 class="popup-title">AURO</h3>
          <p class="popup-subtitle">Rất nhiều đặc quyền và quyền lợi mua sắm đang chờ bạn</p>
          <p class="popup-description">Quyền lợi dành riêng cho bạn khi tham gia AURO</p>
        </div>

        <!-- Benefits -->
        <div v-if="!showForgotPassword" class="benefits-section">
          <div class="benefit-item">
            <div class="benefit-icon">%</div>
            <span>Voucher ưu đãi</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">🎁</div>
            <span>Quà tặng độc quyền</span>
          </div>
        </div>

        <!-- Forgot Password Form -->
        <div v-if="showForgotPassword" class="forgot-password-section">
          <div class="forgot-password-header">
            <button class="back-btn" @click="showForgotPassword = false">
              <span>←</span> Quay lại
            </button>
            <h3 class="forgot-password-title">Quên mật khẩu</h3>
            <p class="forgot-password-subtitle">Nhập email của bạn để nhận link đặt lại mật khẩu</p>
          </div>

          <div class="forgot-password-form">
            <!-- Success Message -->
            <div v-if="forgotPasswordSuccess" class="forgot-password-success">
              <i class="bi bi-check-circle me-2"></i>
              {{ forgotPasswordSuccess }}
            </div>

            <!-- Error Message -->
            <div v-if="forgotPasswordError" class="login-error-message">
              <i class="bi bi-exclamation-circle me-2"></i>
              {{ forgotPasswordError }}
            </div>

            <div class="form-group">
              <input
                v-model="forgotPasswordEmail"
                type="email"
                class="form-input"
                :class="{ 'input-error': forgotPasswordError }"
                placeholder="Email của bạn"
                @keydown.enter.prevent.stop="handleForgotPasswordSubmit"
                @input="forgotPasswordError = ''"
                autocomplete="email"
              />
            </div>

            <button 
              type="button" 
              class="login-btn" 
              @click.prevent.stop="handleForgotPasswordSubmit" 
              @mousedown.prevent.stop
              :disabled="isSubmittingForgotPassword"
            >
              <span v-if="isSubmittingForgotPassword" class="spinner"></span>
              {{ isSubmittingForgotPassword ? 'Đang gửi...' : 'GỬI LINK ĐẶT LẠI MẬT KHẨU' }}
            </button>
          </div>
        </div>

        <!-- Login Form -->
        <div v-else class="login-form-section">
          <p class="login-intro">Đăng nhập hoặc đăng ký (miễn phí)</p>

          <!-- Social Login -->
          <div class="social-login">
            <button class="social-btn google-btn" @click="handleSocialLogin('google')">
              <svg class="social-icon" viewBox="0 0 24 24" width="20" height="20">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                />
              </svg>
            </button>
            <button class="social-btn facebook-btn" @click="handleSocialLogin('facebook')">
              <svg class="social-icon" viewBox="0 0 24 24" width="20" height="20">
                <path
                  fill="#1877F2"
                  d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
                />
              </svg>
            </button>
          </div>

          <!-- Divider -->
          <div class="divider">
            <div class="divider-line"></div>
            <span>Hoặc</span>
            <div class="divider-line"></div>
          </div>
          <div class="login-form-wrapper">
            <!-- Error Message -->
            <div v-if="loginError" class="login-error-message">
              <i class="bi bi-exclamation-circle me-2"></i>
              {{ loginError }}
            </div>

            <div class="form-group">
              <input
                v-model="values.login"
                type="text"
                class="form-input"
                :class="{ 'input-error': loginError }"
                placeholder="Email/SĐT của bạn"
                @keydown.enter.prevent.stop="handleLogin"
                @input="loginError = ''"
                autocomplete="username"
              />
            </div>

            <div class="form-group">
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="values.matKhau"
                class="form-input"
                :class="{ 'input-error': loginError }"
                placeholder="Mật khẩu"
                @keydown.enter.prevent.stop="handleLogin"
                @input="loginError = ''"
                autocomplete="current-password"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                <span class="toggle-icon">{{ showPassword ? '🙈' : '👁️' }}</span>
              </button>
            </div>

            <button 
              type="button" 
              class="login-btn" 
              @click.prevent.stop="handleLogin" 
              @mousedown.prevent.stop
              :disabled="isSubmitting"
            >
              <span v-if="isSubmitting" class="spinner"></span>
              {{ isSubmitting ? 'Đang đăng nhập...' : 'ĐĂNG NHẬP' }}
            </button>
          </div>

          <!-- Links -->
          <div class="form-links">
            <button class="link-btn" @click="switchToRegister">Đăng ký tài khoản mới</button>
            <button class="link-btn" @click="handleForgotPassword">Quên mật khẩu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useFormValidation, validationSchemas } from '../../composables/useFormValidation'
import { useToast } from '../../composables/useToast'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'switchToRegister'])
const router = useRouter()
const userStore = useUserStore()
const { success } = useToast() // Chỉ dùng success, không dùng error toast

// Import cart store for syncing after login
import { useCartStore } from '@/stores/cart'

// Form validation - chỉ dùng values, không dùng handleSubmit
const {
  values,
  errors,
  setValue,
  setTouched,
  getFieldError,
  hasFieldError,
} = useFormValidation(validationSchemas.login)

// Reactive data
const showPassword = ref(false)
const isSubmitting = ref(false) // Tự quản lý submitting state
const loginError = ref('') // Thông báo lỗi đăng nhập

// Forgot password state
const showForgotPassword = ref(false)
const forgotPasswordEmail = ref('')
const forgotPasswordError = ref('')
const forgotPasswordSuccess = ref('')
const isSubmittingForgotPassword = ref(false)

const form = ref({
  login: '',
  password: '',
  rememberMe: false,
})

// Methods
const closePopup = () => {
  emit('close')
}

const switchToRegister = () => {
  emit('switchToRegister')
}

const handleForgotPassword = () => {
  showForgotPassword.value = true
  forgotPasswordEmail.value = ''
  forgotPasswordError.value = ''
  forgotPasswordSuccess.value = ''
}

const handleForgotPasswordSubmit = async (e) => {
  // Ngăn chặn mọi hành vi mặc định
  if (e) {
    if (typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    if (typeof e.stopPropagation === 'function') {
      e.stopPropagation()
    }
    if (typeof e.stopImmediatePropagation === 'function') {
      e.stopImmediatePropagation()
    }
  }

  // Ngăn chặn submit nếu đang xử lý
  if (isSubmittingForgotPassword.value) {
    return false
  }

  // Xóa lỗi và success cũ
  forgotPasswordError.value = ''
  forgotPasswordSuccess.value = ''

  // Validate email
  const email = forgotPasswordEmail.value?.trim() || ''
  
  if (!email) {
    forgotPasswordError.value = 'Vui lòng nhập email của bạn'
    return false
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    forgotPasswordError.value = 'Email không hợp lệ'
    return false
  }

  // Set submitting state
  isSubmittingForgotPassword.value = true

  try {
    const response = await userStore.forgotPassword(email)

    if (response && response.success) {
      forgotPasswordSuccess.value = response.message || 'Link đặt lại mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.'
      forgotPasswordEmail.value = ''
      
      // Tự động quay lại form login sau 3 giây
      setTimeout(() => {
        showForgotPassword.value = false
        forgotPasswordSuccess.value = ''
      }, 3000)
    } else {
      forgotPasswordError.value = response?.message || 'Không thể gửi email. Vui lòng thử lại sau.'
    }
  } catch (error) {
    // Xử lý lỗi từ API
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 403) {
        forgotPasswordError.value = 'Chức năng quên mật khẩu đang được bảo trì. Vui lòng liên hệ quản trị viên.'
      } else if (status === 404) {
        forgotPasswordError.value = data?.message || 'Email không tồn tại trong hệ thống'
      } else if (status === 400 || status === 422) {
        forgotPasswordError.value = data?.message || 'Email không hợp lệ'
      } else if (status === 500) {
        forgotPasswordError.value = 'Có lỗi xảy ra từ server. Vui lòng thử lại sau hoặc liên hệ quản trị viên.'
      } else {
        forgotPasswordError.value = data?.message || 'Không thể gửi email. Vui lòng thử lại sau.'
      }
    } else if (error.request) {
      forgotPasswordError.value = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng'
    } else {
      forgotPasswordError.value = 'Có lỗi xảy ra. Vui lòng thử lại.'
    }
  } finally {
    isSubmittingForgotPassword.value = false
  }

  return false
}

const handleSocialLogin = (provider) => {
  alert(`Đăng nhập với ${provider} sẽ được triển khai`)
}

const handleLogin = async (e) => {
  // Ngăn chặn mọi hành vi mặc định - QUAN TRỌNG để không reload trang
  if (e) {
    if (typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    if (typeof e.stopPropagation === 'function') {
      e.stopPropagation()
    }
    if (typeof e.stopImmediatePropagation === 'function') {
      e.stopImmediatePropagation()
    }
  }

  // Ngăn chặn submit nếu đang xử lý
  if (isSubmitting.value) {
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    return false
  }

  // Xóa lỗi cũ
  loginError.value = ''

  // Validate form trước - tự validate không dùng handleSubmit
  const loginValue = values.value.login?.trim() || ''
  const matKhauValue = values.value.matKhau?.trim() || ''

  if (!loginValue) {
    loginError.value = 'Vui lòng nhập email hoặc số điện thoại'
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    return false
  }

  if (!matKhauValue) {
    loginError.value = 'Vui lòng nhập mật khẩu'
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    return false
  }

  if (matKhauValue.length < 6) {
    loginError.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
    return false
  }

  // Set submitting state
  isSubmitting.value = true

  // Wrap toàn bộ logic trong try-catch để đảm bảo luôn return false
  try {
    // Backend expects 'login' and 'matKhau'
    const payload = {
      login: loginValue,
      matKhau: matKhauValue,
    }

    const response = await userStore.login(payload)

    if (response && response.success) {
      success('Đăng nhập thành công!')
      
      // Đồng bộ giỏ hàng local lên backend sau khi đăng nhập
      try {
        const cartStore = useCartStore()
        const localItems = cartStore.items || []
        
        if (localItems.length > 0) {
          // Import cartService
          const cartService = (await import('@/services/cartService')).default
          
          // Thêm từng item vào backend cart
          for (const item of localItems) {
            try {
              if (item.bienTheId || item.variantId) {
                await cartService.addToCart({
                  bienTheId: item.bienTheId || item.variantId,
                  soLuong: item.quantity || 1
                })
              }
            } catch (err) {
              // Bỏ qua lỗi sync item
            }
          }
          
          // Load lại cart từ backend để cập nhật IDs
          await cartStore.loadCart()
        } else {
          // Vẫn load cart từ backend (có thể đã có items từ session trước)
          const cartStore = useCartStore()
          await cartStore.loadCart()
        }
      } catch (syncError) {
        // Không throw error, tiếp tục xử lý
      }

      // Handle redirect
      const redirectUrl = localStorage.getItem('auro_redirect')
      if (redirectUrl) {
        localStorage.removeItem('auro_redirect')
        closePopup()
        router.push(redirectUrl)
      } else {
        closePopup()
        // Không reload trang nữa, chỉ cần đóng popup
        // Cart đã được đồng bộ ở trên
      }
    } else {
      // Hiển thị lỗi trong popup
      loginError.value = response?.message || 'Tài khoản hoặc mật khẩu không đúng'
    }
  } catch (error) {
    // Xử lý lỗi từ API - chỉ hiển thị trong popup, không log ra console
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401 || status === 403) {
        loginError.value = data?.message || 'Tài khoản hoặc mật khẩu không đúng'
      } else if (status === 400 || status === 422) {
        loginError.value = data?.message || 'Thông tin đăng nhập không hợp lệ'
      } else {
        loginError.value = 'Đăng nhập không đúng. Vui lòng thử lại.'
      }
    } else if (error.request) {
      loginError.value = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng'
    } else {
      loginError.value = 'Đăng nhập không đúng. Vui lòng thử lại.'
    }
  } finally {
    // Luôn reset submitting state
    isSubmitting.value = false
    
    // Đảm bảo ngăn chặn mọi hành vi mặc định
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault()
    }
  }
  
  // Luôn return false để ngăn form submit mặc định - QUAN TRỌNG
  return false
}
</script>

<style scoped>
/* Overlay */
.login-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(8px);
  }
}

/* Popup Container */
.login-popup-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 450px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
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

/* Add font-family to the container or body if needed */
.login-popup-container {
  font-family: var(--auro-body-font);
}

@keyframes popupSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Close Button */
.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: #000;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.close-icon {
  font-size: 20px;
  font-weight: bold;
  line-height: 1;
}

.close-btn:hover {
  background: #333;
  transform: scale(1.1);
}

/* Popup Content */
.login-popup-content {
  padding: 40px 30px 30px;
}

/* Header */
.popup-header {
  text-align: center;
  margin-bottom: 30px;
}

.popup-title {
  font-family: var(--auro-heading-font);
  font-size: 24px;
  font-weight: 400;
  color: var(--auro-accent);
  margin-bottom: 15px;
  letter-spacing: 0.03rem;
}

.popup-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: var(--auro-text);
  margin-bottom: 8px;
  line-height: 1.4;
}

.popup-description {
  font-size: 14px;
  color: var(--auro-text-light);
  margin-bottom: 0;
}

/* Benefits Section */
.benefits-section {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 30px;
  padding: 20px 0;
  border-top: 1px solid var(--auro-border);
  border-bottom: 1px solid var(--auro-border);
}

.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.benefit-icon {
  width: 40px;
  height: 40px;
  background: var(--auro-gradient-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: var(--auro-dark);
}

.benefit-item span {
  font-size: 12px;
  color: var(--auro-text-light);
  font-weight: 500;
}

/* Forgot Password Section */
.forgot-password-section {
  text-align: center;
}

.forgot-password-header {
  margin-bottom: 30px;
}

.back-btn {
  background: none;
  border: none;
  color: var(--auro-accent);
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: color 0.3s ease;
}

.back-btn:hover {
  color: var(--auro-dark);
}

.back-btn span {
  font-size: 18px;
  font-weight: bold;
}

.forgot-password-title {
  font-family: var(--auro-heading-font);
  font-size: 24px;
  font-weight: 400;
  color: var(--auro-accent);
  margin-bottom: 10px;
  letter-spacing: 0.03rem;
}

.forgot-password-subtitle {
  font-size: 14px;
  color: var(--auro-text-light);
  margin-bottom: 0;
  line-height: 1.5;
}

.forgot-password-form {
  margin-top: 20px;
}

.forgot-password-success {
  background: #d1fae5;
  border: 1px solid #86efac;
  color: #065f46;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

/* Login Form Section */
.login-form-section {
  text-align: center;
}

.login-intro {
  font-size: 14px;
  color: var(--auro-text-light);
  margin-bottom: 20px;
}

/* Social Login */
.social-login {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 20px;
}

.social-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 2px solid #ddd;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-icon {
  width: 20px;
  height: 20px;
}

.google-btn {
  color: #db4437;
  border-color: #db4437;
}

.google-btn:hover {
  background: #db4437;
  color: white;
  transform: scale(1.1);
}

.facebook-btn {
  color: #1877f2;
  border-color: #1877f2;
}

.facebook-btn:hover {
  background: #1877f2;
  color: white;
  transform: scale(1.1);
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: #ddd;
}

.divider span {
  padding: 0 15px;
  color: var(--auro-text-light);
  font-size: 14px;
}

/* Form */
.login-form-wrapper {
  /* Dùng div thay vì form để tránh reload trang - không cần style */
  display: block;
}

.login-error-message {
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
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

.form-group {
  position: relative;
  margin-bottom: 20px;
}

.form-input {
  width: 100%;
  padding: 15px 20px;
  border: 2px solid var(--auro-border);
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--auro-bg);
  color: var(--auro-text);
  position: relative;
}

.form-input:hover {
  border-color: var(--auro-accent);
  background: var(--auro-card);
  transform: translateY(-1px);
}

.form-input:focus {
  outline: none;
  border-color: var(--auro-accent);
  background: var(--auro-card);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
  transform: translateY(-1px);
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
  top: 50%;
  transform: translateY(-50%);
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

/* Login Button */
.login-btn {
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
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(26, 26, 26, 0.3);
}

.login-btn:hover:not(:disabled)::before {
  left: 100%;
}

.login-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.login-btn:disabled {
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

/* Form Links */
.form-links {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.link-btn {
  background: none;
  border: none;
  color: var(--auro-accent);
  font-size: 14px;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s ease;
}

.link-btn:hover {
  color: var(--auro-dark);
}

/* Responsive */
@media (max-width: 480px) {
  .login-popup-container {
    margin: 10px;
    max-width: none;
  }

  .login-popup-content {
    padding: 30px 20px 20px;
  }

  .benefits-section {
    flex-direction: column;
    gap: 15px;
  }

  .benefit-item {
    flex-direction: row;
    justify-content: flex-start;
  }

  .benefit-icon {
    margin-right: 15px;
    margin-bottom: 0;
  }

  .form-links {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
