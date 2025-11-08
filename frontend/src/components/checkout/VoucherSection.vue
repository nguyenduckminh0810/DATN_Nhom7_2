<template>
  <div class="voucher-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <h5 class="mb-0">
          <i class="bi bi-ticket-perforated me-2"></i>Mã giảm giá
        </h5>
      </div>

        <!-- Voucher Cards -->
        <div class="section-body">
          <!-- Guest Notice -->
          <div v-if="isGuest" class="guest-notice mb-3">
            <div class="guest-notice-content">
              <i class="bi bi-info-circle me-2"></i>
              <span>Vui lòng <a href="/login" class="login-link">đăng nhập</a> để sử dụng voucher</span>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="voucher-loading">
            <div class="spinner-border text-warning" role="status">
              <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="mt-2">Đang tải voucher...</p>
          </div>

          <!-- Voucher Cards -->
          <div v-else-if="availableVouchers.length > 0" class="voucher-cards-container" :class="{ 'guest-disabled': isGuest }">
            <!-- Scroll Controls - Đặt bên ngoài để không che khuất nội dung -->
            <div class="voucher-scroll-controls">
              <button class="scroll-btn scroll-left" @click="scrollVouchers('left')">
                <i class="bi bi-chevron-left"></i>
              </button>
              <button class="scroll-btn scroll-right" @click="scrollVouchers('right')">
                <i class="bi bi-chevron-right"></i>
              </button>
            </div>

            <div class="voucher-cards-wrapper">
              <div class="voucher-cards-scroll" ref="voucherScroll">
                <div v-for="voucher in sortedVouchers" 
                     :key="voucher.id" 
                     class="voucher-card"
                     :class="{ 
                       selected: selectedVoucher?.id === voucher.id && !isGuest,
                       disabled: !isVoucherEligible(voucher) || isGuest
                     }"
                     @click="handleVoucherClick(voucher)">
                  <div class="voucher-perforated-edge"></div>
                  <div class="voucher-content">
                    <div class="voucher-header">
                      <div class="voucher-code">{{ voucher.ma }}</div>
                      <div class="voucher-remaining" v-if="voucher.gioiHanSuDung && voucher.gioiHanSuDung > 0">
                        Còn {{ voucher.gioiHanSuDung }}
                      </div>
                    </div>
                    <div class="voucher-description">
                      {{ getVoucherDescription(voucher) }}
                    </div>
                    <div class="voucher-footer">
                      <div class="voucher-expiry">
                        HSD: {{ formatDate(voucher.ketThucLuc) }}
                      </div>
                      <div class="voucher-conditions">
                        <a href="#" class="text-decoration-none">Điều kiện</a>
                      </div>
                    </div>
                  </div>
                  <div class="voucher-radio">
                    <input type="radio" 
                           :value="voucher.id"
                           v-model="selectedVoucherId"
                           :id="'voucher-' + voucher.id">
                    <label :for="'voucher-' + voucher.id"></label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="voucher-empty">
            <i class="bi bi-ticket-perforated display-4 text-muted"></i>
            <p class="mt-2 text-muted">Không có voucher nào khả dụng</p>
          </div>

        <!-- Manual Voucher Input -->
        <div class="voucher-input-section mt-3" :class="{ 'guest-disabled': isGuest }">
          <div class="input-group">
            <input type="text" 
                   class="form-control modern-voucher-input" 
                   v-model="manualVoucherCode"
                   placeholder="Nhập mã giảm giá"
                   :disabled="loading || isGuest">
            <button class="btn btn-dark voucher-apply-btn" 
                    @click="handleApplyVoucher"
                    :disabled="loading || !manualVoucherCode.trim() || isGuest">
              <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status"></span>
              <i v-else class="bi bi-check me-1"></i>
              {{ loading ? 'Đang kiểm tra...' : 'Áp dụng' }}
            </button>
          </div>
          
          <!-- Voucher Message -->
          <div v-if="voucherMessage" 
               class="voucher-message mt-2"
               :class="voucherMessage.type">
            <i :class="voucherMessage.type === 'success' ? 'bi bi-check-circle' : 'bi bi-exclamation-circle'" class="me-1"></i>
            {{ voucherMessage.text }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useVoucher } from '@/stores/voucher'
import { useCart } from '@/composables/useCart'
import { useUserStore } from '@/stores/user'

const { 
  selectedVoucher, 
  selectedVoucherId, 
  manualVoucherCode, 
  voucherMessage, 
  availableVouchers,
  loading,
  selectVoucher, 
  applyVoucher,
  loadVouchers
} = useVoucher()

const { items, formatPrice } = useCart()
const { user } = useUserStore()

const voucherScroll = ref(null)

// Kiểm tra user là guest (chưa đăng nhập)
const isGuest = computed(() => {
  return !user.value || !user.value.id
})

// Load vouchers khi component mount
onMounted(() => {
  loadVouchers()
})

// Tính subtotal để kiểm tra điều kiện voucher
const subtotal = computed(() => {
  return items.value
    .filter(item => item.selected !== false)
    .reduce((total, item) => total + (item.price * item.quantity), 0)
})

// Sắp xếp vouchers: eligible trước, disabled sau
const sortedVouchers = computed(() => {
  return [...availableVouchers.value].sort((a, b) => {
    const aEligible = isVoucherEligible(a)
    const bEligible = isVoucherEligible(b)
    
    // Nếu cả hai cùng eligible hoặc cùng disabled, sắp xếp theo id
    if (aEligible === bEligible) {
      return a.id - b.id
    }
    
    // Eligible trước disabled
    return aEligible ? -1 : 1
  })
})

// Kiểm tra voucher có đủ điều kiện không
const isVoucherEligible = (voucher) => {
  // Guest không thể sử dụng voucher
  if (isGuest.value) {
    return false
  }
  return subtotal.value >= (voucher.donToiThieu || 0)
}

// Function để handle click voucher với kiểm tra điều kiện
const handleVoucherClick = (voucher) => {
  // Guest không thể chọn voucher
  if (isGuest.value) {
    voucherMessage.value = {
      type: 'error',
      text: 'Vui lòng đăng nhập để sử dụng voucher'
    }
    return
  }
  
  if (!isVoucherEligible(voucher)) {
    voucherMessage.value = {
      type: 'error',
      text: `Voucher ${voucher.ma} yêu cầu đơn hàng tối thiểu ${formatPrice(voucher.donToiThieu || 0)}`
    }
    return
  }
  
  selectVoucher(voucher)
}

// Function để áp dụng voucher manual
const handleApplyVoucher = async () => {
  // Guest không thể áp dụng voucher
  if (isGuest.value) {
    voucherMessage.value = {
      type: 'error',
      text: 'Vui lòng đăng nhập để sử dụng voucher'
    }
    return
  }
  
  const khachHangId = user.value?.id || null
  await applyVoucher(khachHangId, subtotal.value)
}

// Helper functions
const getVoucherDescription = (voucher) => {
  if (voucher.loai === 'percent' || voucher.loai === 'PHAN_TRAM') {
    return `Giảm ${voucher.giaTri}% cho đơn từ ${formatPrice(voucher.donToiThieu || 0)}`
  } else {
    return `Giảm ${formatPrice(voucher.giaTri)} cho đơn từ ${formatPrice(voucher.donToiThieu || 0)}`
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

const scrollVouchers = (direction) => {
  const scrollContainer = voucherScroll.value
  if (!scrollContainer) return
  
  const scrollAmount = 300
  const currentScroll = scrollContainer.scrollLeft
  const targetScroll = currentScroll + (direction === 'left' ? -scrollAmount : scrollAmount)
  
  scrollContainer.scrollTo({
    left: targetScroll,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
.voucher-section {
  margin-bottom: 2rem;
}

/* Reset conflicting styles */
.voucher-section * {
  box-sizing: border-box;
}

.voucher-section input {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* Force border visibility for voucher inputs */
.voucher-section input[type="text"] {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

.voucher-section input[type="text"]:focus {
  border-color: #B8860B !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.voucher-section input[type="text"]:hover {
  border-color: #B8860B !important;
}

.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.section-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border: none;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
}

.section-body {
  padding: 1.5rem;
}

.voucher-cards-container {
  position: relative;
}

.voucher-cards-wrapper {
  overflow: hidden;
  border-radius: 12px;
}

.voucher-cards-scroll {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  padding: 0.5rem 2.5rem;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.voucher-cards-scroll::-webkit-scrollbar {
  display: none;
}

.voucher-card {
  flex: 0 0 280px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 2px solid #dee2e6;
  border-radius: 12px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.voucher-card:hover {
  border-color: #B8860B;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(184, 134, 11, 0.15);
}

.voucher-card.selected {
  border-color: #B8860B;
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.1) 0%, rgba(218, 165, 32, 0.1) 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(184, 134, 11, 0.2);
}

.voucher-card.selected .voucher-code {
  color: #B8860B;
  font-weight: 800;
}

.voucher-card.selected .voucher-description {
  color: #B8860B;
  font-weight: 600;
}

.voucher-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(50%);
  transform: none !important;
  box-shadow: none !important;
}

.voucher-card.disabled:hover {
  transform: none !important;
  box-shadow: none !important;
}

.voucher-card.disabled .voucher-content {
  pointer-events: none;
}

.voucher-card.disabled::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  z-index: 1;
}

.voucher-perforated-edge {
  position: absolute;
  top: 0;
  right: 15px;
  width: 2px;
  height: 100%;
  background: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent 8px,
    #ccc 8px,
    #ccc 12px
  );
}

.voucher-content {
  margin-right: 1rem;
}

.voucher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.voucher-code {
  font-weight: 700;
  font-size: 1.1rem;
  color: #B8860B;
}

.voucher-remaining {
  font-size: 0.75rem;
  color: #666;
  background: #fff;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
}

.voucher-description {
  font-size: 0.85rem;
  color: #333;
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.voucher-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.voucher-expiry {
  font-size: 0.75rem;
  color: #666;
}

.voucher-conditions a {
  font-size: 0.75rem;
  color: #B8860B;
}

.voucher-radio {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
}

.voucher-radio input[type="radio"] {
  display: none;
}

.voucher-radio label {
  width: 20px;
  height: 20px;
  border: 2px solid #ccc;
  border-radius: 50%;
  cursor: pointer;
  display: block;
  position: relative;
}

.voucher-radio input[type="radio"]:checked + label {
  border-color: #B8860B;
  background: #B8860B;
}

.voucher-radio input[type="radio"]:checked + label::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.voucher-scroll-controls {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  pointer-events: none;
  padding: 0 0.5rem;
  z-index: 10;
}

.scroll-btn {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #dee2e6;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
}

.scroll-btn:hover {
  background: white;
  border-color: #B8860B;
  color: #B8860B;
  box-shadow: 0 4px 12px rgba(184, 134, 11, 0.2);
}

/* Responsive: Ẩn scroll buttons trên mobile */
@media (max-width: 768px) {
  .voucher-scroll-controls {
    display: none;
  }
  
  .voucher-cards-scroll {
    padding: 0.5rem 1rem;
  }
}

.voucher-input-section .input-group {
  border-radius: 8px;
  overflow: hidden;
}

.modern-voucher-input {
  border: 2px solid #e9ecef !important;
  border-right: none !important;
  padding: 0.75rem 1rem;
  font-size: 0.9rem;
  background-color: white !important;
}

.modern-voucher-input:focus {
  border-color: #B8860B !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.modern-voucher-input:hover {
  border-color: #B8860B !important;
}

.voucher-apply-btn {
  border: 2px solid #B8860B;
  background: #B8860B;
  color: white;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.voucher-apply-btn:hover {
  background: #DAA520;
  border-color: #DAA520;
}

.voucher-message {
  font-size: 0.85rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.voucher-message.success {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.voucher-message.error {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.voucher-message.info {
  background: rgba(13, 202, 240, 0.1);
  color: #0dcaf0;
  border: 1px solid rgba(13, 202, 240, 0.2);
}

/* Guest Notice */
.guest-notice {
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: 8px;
  padding: 0.75rem 1rem;
}

.guest-notice-content {
  display: flex;
  align-items: center;
  font-size: 0.9rem;
  color: #856404;
}

.guest-notice-content i {
  color: #ffc107;
  font-size: 1.1rem;
}

.guest-notice-content .login-link {
  color: #B8860B;
  font-weight: 600;
  text-decoration: underline;
  transition: color 0.2s ease;
}

.guest-notice-content .login-link:hover {
  color: #DAA520;
}

/* Guest Disabled State */
.voucher-cards-container.guest-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.voucher-input-section.guest-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.voucher-input-section.guest-disabled input,
.voucher-input-section.guest-disabled button {
  cursor: not-allowed;
}
</style>
