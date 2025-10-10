<template>
  <div class="payment-methods-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <h5 class="mb-0">
          <i class="bi bi-credit-card me-2"></i>Hình thức thanh toán
        </h5>
      </div>

      <!-- Payment Options -->
      <div class="section-body">
        <div class="payment-options">
          <!-- COD -->
          <div class="form-check payment-option" :class="{ active: paymentMethod === 'cod' }">
            <input class="form-check-input" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="cod"
                   id="payment-cod">
            <label class="form-check-label" for="payment-cod">
              <div class="payment-info">
                <div class="payment-header">
                  <i class="bi bi-cash-coin payment-icon"></i>
                  <strong>Thanh toán khi nhận hàng</strong>
                </div>
                <small>Thanh toán bằng tiền mặt khi nhận hàng</small>
              </div>
            </label>
          </div>

          <!-- ZaloPay -->
          <div class="form-check payment-option" :class="{ active: paymentMethod === 'zalopay' }">
            <input class="form-check-input" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="zalopay"
                   id="payment-zalopay">
            <label class="form-check-label" for="payment-zalopay">
              <div class="payment-info">
                <div class="payment-header">
                  <i class="bi bi-wallet2 payment-icon zalopay"></i>
                  <strong>ZaloPay</strong>
                </div>
                <small>Thanh toán qua ví điện tử ZaloPay</small>
              </div>
            </label>
          </div>

          <!-- Momo -->
          <div class="form-check payment-option" :class="{ active: paymentMethod === 'momo' }">
            <input class="form-check-input" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="momo"
                   id="payment-momo">
            <label class="form-check-label" for="payment-momo">
              <div class="payment-info">
                <div class="payment-header">
                  <i class="bi bi-wallet2 payment-icon momo"></i>
                  <strong>Ví Momo</strong>
                </div>
                <small>Thanh toán qua ví điện tử Momo</small>
              </div>
            </label>
          </div>

          <!-- VNPay -->
          <div class="form-check payment-option" :class="{ active: paymentMethod === 'vnpay' }">
            <input class="form-check-input" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="vnpay"
                   id="payment-vnpay">
            <label class="form-check-label" for="payment-vnpay">
              <div class="payment-info">
                <div class="payment-header">
                  <i class="bi bi-qr-code payment-icon vnpay"></i>
                  <strong>Ví điện tử VNPAY</strong>
                </div>
                <small>Quét QR để thanh toán</small>
              </div>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const paymentMethod = ref('cod')
</script>

<style scoped>
.payment-methods-section {
  margin-bottom: 2rem;
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

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.payment-option {
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 1rem;
  transition: all 0.2s ease;
  cursor: pointer;
  margin-bottom: 0;
}

.payment-option:hover,
.payment-option.active {
  border-color: #B8860B;
  background: rgba(184, 134, 11, 0.05);
}

.payment-option .form-check-input {
  margin-top: 0.25rem;
}

.payment-info {
  margin-left: 0.5rem;
}

.payment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.25rem;
}

.payment-icon {
  font-size: 1.25rem;
  color: #B8860B;
}

.payment-icon.zalopay {
  color: #0068ff;
}

.payment-icon.momo {
  color: #d82d8b;
}

.payment-icon.vnpay {
  color: #0052a5;
}

.payment-info strong {
  font-size: 1rem;
  font-weight: 600;
  color: #000;
}

.payment-info small {
  font-size: 0.85rem;
  color: #6c757d;
  display: block;
}
</style>
