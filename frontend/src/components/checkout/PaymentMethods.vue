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
          <div class="payment-option" 
               :class="{ active: paymentMethod === 'cod' }" 
               @click="paymentMethod = 'cod'">
            <input class="payment-radio" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="cod"
                   id="payment-cod">
            <div class="payment-info">
              <div class="payment-header">
                <i class="bi bi-cash-coin payment-icon"></i>
                <strong>Thanh toán khi nhận hàng</strong>
              </div>
              <small>Thanh toán bằng tiền mặt khi nhận hàng</small>
            </div>
          </div>

          <!-- VNPay -->
          <div class="payment-option" 
               :class="{ active: paymentMethod === 'vnpay' }" 
               @click="paymentMethod = 'vnpay'">
            <input class="payment-radio" 
                   type="radio" 
                   v-model="paymentMethod" 
                   value="vnpay"
                   id="payment-vnpay">
            <div class="payment-info">
              <div class="payment-header">
                <i class="bi bi-qr-code payment-icon vnpay"></i>
                <strong>Ví điện tử VNPAY</strong>
              </div>
              <small>Quét QR để thanh toán</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'

// Inject selectedPaymentMethod từ parent Cart.vue
const injectedPaymentMethod = inject('selectedPaymentMethod', null)

// Nếu không có inject, tạo local ref (fallback)
const paymentMethod = injectedPaymentMethod || ref(null)
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
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  position: relative;
}

/* Custom radio button visual */
.payment-option::before {
  content: '';
  width: 20px;
  height: 20px;
  border: 2px solid #dee2e6;
  background-color: white;
  border-radius: 50%;
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.payment-option.active::before {
  border-color: #B8860B;
  background-color: white;
}

.payment-option.active::after {
  content: '';
  position: absolute;
  left: calc(1rem + 5px);
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  background-color: #B8860B;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.payment-option:hover,
.payment-option.active {
  border-color: #B8860B;
  background: rgba(184, 134, 11, 0.05);
}

.payment-option:hover::before {
  border-color: #B8860B;
}

.payment-radio {
  margin-top: 0.125rem;
  margin-right: 0;
  width: 20px;
  height: 20px;
  border: 2px solid #dee2e6;
  background-color: white;
  border-radius: 50%;
  position: relative;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  opacity: 0; /* Ẩn radio button thực tế */
  position: absolute;
  pointer-events: none;
}

.payment-radio:checked {
  border-color: #B8860B;
  background-color: white;
}

.payment-radio:checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background-color: #B8860B;
  border-radius: 50%;
}

.payment-radio:focus {
  border-color: #B8860B;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1);
  outline: none;
}

.payment-radio:hover {
  border-color: #B8860B;
}

.payment-info {
  flex: 1;
  margin-left: 2.5rem; /* Space cho custom radio button */
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

/* Responsive Design */
@media (max-width: 768px) {
  .payment-option {
    padding: 0.75rem;
    gap: 0.5rem;
  }
  
  .payment-header {
    gap: 0.5rem;
  }
  
  .payment-icon {
    font-size: 1.1rem;
  }
  
  .payment-info strong {
    font-size: 0.95rem;
  }
  
  .payment-info small {
    font-size: 0.8rem;
  }
}
</style>
