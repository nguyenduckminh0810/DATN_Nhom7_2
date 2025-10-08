<template>
  <!-- Modal Overlay -->
  <div v-if="isOpen" class="register-popup-overlay" @click="closePopup">
    <div class="register-popup-container" @click.stop>
      <!-- Close Button -->
      <button class="close-btn" @click="closePopup">
        <span class="close-icon">×</span>
      </button>

      <!-- Popup Content -->
      <div class="register-popup-content">
        <!-- Header -->
        <div class="popup-header">
          <h3 class="popup-title">AURO</h3>
          <p class="popup-subtitle">Rất nhiều đặc quyền và quyền lợi mua sắm đang chờ bạn</p>
          <p class="popup-description">Quyền lợi dành riêng cho bạn khi tham gia AURO</p>
        </div>

        <!-- Benefits -->
        <div class="benefits-section">
          <div class="benefit-item">
            <div class="benefit-icon">%</div>
            <span>Voucher ưu đãi</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">🎁</div>
            <span>Quà tặng độc quyền</span>
          </div>
        </div>

        <!-- Register Form -->
        <div class="register-form-section">
          <p class="register-intro">Đăng nhập hoặc đăng ký (miễn phí)</p>
          
          <!-- Social Register -->
          <div class="social-register">
            <button class="social-btn google-btn" @click="handleSocialRegister('google')">
              <svg class="social-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
            </button>
            <button class="social-btn facebook-btn" @click="handleSocialRegister('facebook')">
              <svg class="social-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="#1877F2" d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
            </button>
          </div>

          <!-- Divider -->
          <div class="divider">
            <div class="divider-line"></div>
            <span>Hoặc</span>
            <div class="divider-line"></div>
          </div>

          <!-- Form -->
          <form @submit.prevent="handleRegister">
            <div class="form-group">
              <input 
                v-model="form.fullName" 
                type="text" 
                class="form-input" 
                placeholder="Họ và tên"
                required
              >
            </div>

            <div class="form-group">
              <input 
                v-model="form.email" 
                type="email" 
                class="form-input" 
                placeholder="Email"
                required
              >
            </div>

            <div class="form-group">
              <input 
                v-model="form.phone" 
                type="tel" 
                class="form-input" 
                placeholder="Số điện thoại"
                required
              >
            </div>
            
            <div class="form-group">
              <input 
                :type="showPassword ? 'text' : 'password'" 
                v-model="form.password" 
                class="form-input" 
                placeholder="Mật khẩu"
                required
              >
              <button 
                type="button" 
                class="password-toggle" 
                @click="showPassword = !showPassword"
              >
                <span class="toggle-icon">{{ showPassword ? '🙈' : '👁️' }}</span>
              </button>
            </div>

            <div class="form-group">
              <input 
                :type="showConfirmPassword ? 'text' : 'password'" 
                v-model="form.confirmPassword" 
                class="form-input" 
                placeholder="Xác nhận mật khẩu"
                required
              >
              <button 
                type="button" 
                class="password-toggle" 
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <span class="toggle-icon">{{ showConfirmPassword ? '🙈' : '👁️' }}</span>
              </button>
            </div>

            <!-- Terms and Conditions -->
            <div class="terms-section">
              <label class="terms-checkbox">
                <input 
                  v-model="form.acceptTerms" 
                  type="checkbox" 
                  required
                >
                <span class="checkmark"></span>
                <span class="terms-text">
                  Tôi đồng ý với 
                  <a href="#" class="terms-link">Điều khoản sử dụng</a>
                  và 
                  <a href="#" class="terms-link">Chính sách bảo mật</a>
                </span>
              </label>
            </div>

            <button 
              type="submit" 
              class="register-btn"
              :disabled="isSubmitting"
            >
              <span v-if="isSubmitting" class="spinner"></span>
              {{ isSubmitting ? 'Đang đăng ký...' : 'ĐĂNG KÝ TÀI KHOẢN' }}
            </button>
          </form>

          <!-- Links -->
          <div class="form-links">
            <button class="link-btn" @click="switchToLogin">Đăng nhập</button>
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

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'switchToLogin'])
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
  acceptTerms: false
})

// Methods
const closePopup = () => {
  emit('close')
}

const switchToLogin = () => {
  emit('switchToLogin')
}

const handleForgotPassword = () => {
  // TODO: Implement forgot password functionality
  alert('Chức năng quên mật khẩu sẽ được triển khai')
}

const handleSocialRegister = (provider) => {
  // TODO: Implement social register
  alert(`Đăng ký với ${provider} sẽ được triển khai`)
}

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
  const response = await userStore.register({
    email: form.value.email,
    soDienThoai: form.value.phone,
    matKhau: form.value.password,
    hoTen: form.value.fullName,
    loaiTaiKhoan: 'CUSTOMER'
  })
  
  if (response.success) {
    success('Đăng ký thành công!')
    
    // Handle redirect
    const redirectUrl = localStorage.getItem('auro_redirect')
    if (redirectUrl) {
      localStorage.removeItem('auro_redirect')
      closePopup()
      router.push(redirectUrl)
    } else {
      closePopup()
      window.location.reload()
    }
  } else {
    error(response.message || 'Đăng ký thất bại!')
  }
} catch (err) {
  error('Có lỗi xảy ra khi đăng ký!')
} finally {
  isSubmitting.value = false
}
}
</script>

<style scoped>
/* Overlay */
.register-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

/* Popup Container */
.register-popup-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 450px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: popupSlideIn 0.3s ease-out;
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
.register-popup-content {
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

/* Register Form Section */
.register-form-section {
  text-align: center;
}

.register-intro {
  font-size: 14px;
  color: var(--auro-text-light);
  margin-bottom: 20px;
}

/* Social Register */
.social-register {
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
  transition: all 0.3s ease;
  background: var(--auro-bg);
  color: var(--auro-text);
}

.form-input:focus {
  outline: none;
  border-color: var(--auro-accent);
  background: var(--auro-card);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
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

/* Terms Section */
.terms-section {
  margin-bottom: 20px;
  text-align: left;
}

.terms-checkbox {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  font-size: 14px;
  line-height: 1.4;
}

.terms-checkbox input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--auro-border);
  border-radius: 4px;
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
  position: relative;
  transition: all 0.3s ease;
}

.terms-checkbox input[type="checkbox"]:checked + .checkmark {
  background: var(--auro-accent);
  border-color: var(--auro-accent);
}

.terms-checkbox input[type="checkbox"]:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--auro-dark);
  font-size: 12px;
  font-weight: bold;
}

.terms-text {
  color: var(--auro-text);
}

.terms-link {
  color: var(--auro-accent);
  text-decoration: underline;
  transition: color 0.3s ease;
}

.terms-link:hover {
  color: var(--auro-dark);
}

/* Register Button */
.register-btn {
  width: 100%;
  padding: 15px;
  background: var(--auro-gradient);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(26, 26, 26, 0.3);
}

.register-btn:disabled {
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
  .register-popup-container {
    margin: 10px;
    max-width: none;
  }
  
  .register-popup-content {
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
