<template>
  <div class="payment-result-page">
    <div class="container">
      <div class="result-card">
        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <h4>Đang xử lý kết quả thanh toán...</h4>
          <p>Vui lòng đợi trong giây lát</p>
        </div>

        <!-- Success State -->
        <div v-else-if="paymentResult && paymentResult.success" class="success-state">
          <div class="icon-wrapper success">
            <i class="bi bi-check-circle-fill"></i>
          </div>
          <h2>Thanh toán thành công!</h2>
          <p class="message">{{ paymentResult.message }}</p>
          
          <div class="payment-details">
            <div class="detail-item">
              <span class="label">Mã đơn hàng:</span>
              <span class="value">#{{ paymentResult.soDonHang }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Số tiền:</span>
              <span class="value amount">{{ formatMoney(paymentResult.soTien) }}</span>
            </div>
            <div class="detail-item" v-if="paymentResult.nganHang">
              <span class="label">Ngân hàng:</span>
              <span class="value">{{ paymentResult.nganHang }}</span>
            </div>
            <div class="detail-item" v-if="paymentResult.maGiaoDich">
              <span class="label">Mã giao dịch:</span>
              <span class="value">{{ paymentResult.maGiaoDich }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <button class="btn btn-primary" @click="goToOrders">
              <i class="bi bi-box-seam me-2"></i>
              Xem đơn hàng
            </button>
            <button class="btn btn-outline-secondary" @click="goToHome">
              <i class="bi bi-house me-2"></i>
              Về trang chủ
            </button>
          </div>
        </div>

        <!-- Error State -->
        <div v-else class="error-state">
          <div class="icon-wrapper error">
            <i class="bi bi-x-circle-fill"></i>
          </div>
          <h2>Thanh toán thất bại</h2>
          <p class="message">{{ errorMessage }}</p>
          
          <div v-if="paymentResult" class="payment-details">
            <div class="detail-item" v-if="paymentResult.soDonHang">
              <span class="label">Mã đơn hàng:</span>
              <span class="value">#{{ paymentResult.soDonHang }}</span>
            </div>
            <div class="detail-item" v-if="paymentResult.responseCode">
              <span class="label">Mã lỗi:</span>
              <span class="value">{{ paymentResult.responseCode }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <button class="btn btn-primary" @click="retryPayment">
              <i class="bi bi-arrow-repeat me-2"></i>
              Thử lại
            </button>
            <button class="btn btn-outline-secondary" @click="goToHome">
              <i class="bi bi-house me-2"></i>
              Về trang chủ
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiService from '@/services/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const paymentResult = ref(null)
const errorMessage = ref('')

// Process VNPay callbacks
onMounted(() => {
  const params = { ...route.query }
  console.log('📥 VNPay callback params:', params)

  // 1) Trường hợp chuẩn: backend đã verify và redirect về FE
  if (params.verified_success === 'true') {
    const oid = params.verified_donHangId || params.donHangId || params.vnp_TxnRef || null
    if (window.$toast) {
      window.$toast.success('Thanh toán thành công!', 'Đang chuyển tới trang đơn hàng…')
    }
    setTimeout(() => router.replace({ name: 'order-success', query: { orderId: oid } }), 600)
    return
  }

  // 2) Fallback: VNPay trả thành công nhưng thiếu verified_success (tránh trường hợp 302 gây hiểu nhầm)
  if (params.vnp_ResponseCode === '00' && (params.vnp_TransactionStatus === '00' || !params.vnp_TransactionStatus)) {
    const oid = params.donHangId || params.vnp_TxnRef || null
    if (window.$toast) {
      window.$toast.success('Thanh toán thành công!', 'Đang chuyển tới trang đơn hàng…')
    }
    setTimeout(() => router.replace({ name: 'order-success', query: { orderId: oid } }), 600)
    return
  }

  // 3) Còn lại → thất bại
  errorMessage.value = getErrorMessage(params.vnp_ResponseCode)
  router.replace({ name: 'cart', query: { error: 'payment_failed' } })
  loading.value = false
})

const formatMoney = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getErrorMessage = (responseCode) => {
  const messages = {
    '24': 'Khách hàng hủy giao dịch',
    '11': 'Đã hết hạn chờ thanh toán',
    '51': 'Tài khoản không đủ số dư',
    '09': 'Thẻ chưa đăng ký InternetBanking'
  }
  return messages[responseCode] || 'Giao dịch không thành công'
}

const goToOrders = () => {
  router.push('/orders')
}

const goToHome = () => {
  router.push('/')
}

const retryPayment = () => {
  if (paymentResult.value?.donHangId) {
    router.push(`/checkout?orderId=${paymentResult.value.donHangId}`)
  } else {
    router.push('/cart')
  }
}
</script>

<style scoped>
.payment-result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 3rem 0;
  display: flex;
  align-items: center;
}

.container {
  max-width: 600px;
}

.result-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  text-align: center;
}

.loading-state h4 {
  margin-top: 1.5rem;
  color: #333;
}

.loading-state p {
  color: #666;
  margin-top: 0.5rem;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #B8860B;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.icon-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  animation: scaleIn 0.5s ease-out;
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.icon-wrapper.success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
}

.icon-wrapper.error {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
}

.icon-wrapper i {
  font-size: 4rem;
}

.icon-wrapper.success i {
  color: #28a745;
}

.icon-wrapper.error i {
  color: #dc3545;
}

h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #333;
}

.success-state h2 {
  color: #28a745;
}

.error-state h2 {
  color: #dc3545;
}

.message {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 2rem;
}

.payment-details {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
}

.detail-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #666;
}

.value {
  font-weight: 600;
  color: #333;
}

.value.amount {
  color: #B8860B;
  font-size: 1.2rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn {
  padding: 0.75rem 2rem;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(184, 134, 11, 0.3);
}

.btn-outline-secondary {
  background: transparent;
  border: 2px solid #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  color: white;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .result-card {
    padding: 2rem 1.5rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
