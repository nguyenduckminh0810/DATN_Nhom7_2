<template>
  <div class="order-summary-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <div class="summary-header-layout">
          <div class="summary-header-left">
            <h5 class="mb-0 me-3">
              <i class="bi bi-receipt me-2"></i>Tóm tắt đơn hàng
            </h5>
            <span class="badge bg-white text-warning">
              {{ selectedItemsCount }} / {{ totalItemsCount }} sản phẩm
            </span>
          </div>
          <div class="summary-header-right">
            <!-- Optional: Add any summary-specific actions here if needed in the future -->
          </div>
        </div>
      </div>

      <!-- Summary Body -->
      <div class="section-body">
        <!-- Summary Items -->
        <div class="summary-section">
          <div class="summary-item">
            <span class="summary-label">Tạm tính:</span>
            <span class="summary-value">{{ formatPrice(subtotal) }}</span>
          </div>
          
          <div class="summary-item">
            <span class="summary-label">Phí vận chuyển:</span>
            <span class="summary-value">{{ formatPrice(shippingFee) }}</span>
          </div>
          
          <div v-if="discountAmount > 0" class="summary-item discount-item">
            <span class="summary-label">Giảm giá:</span>
            <span class="summary-value">-{{ formatPrice(discountAmount) }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-total">
            <span class="total-label">Tổng cộng:</span>
            <span class="total-value">{{ formatPrice(finalTotal) }}</span>
          </div>
        </div>

        <!-- Checkout Button -->
        <div class="checkout-section mt-4">
          <button class="btn btn-dark btn-lg w-100 checkout-btn" 
                  :disabled="selectedItemsCount === 0">
            <i class="bi bi-credit-card me-2"></i>ĐẶT HÀNG
          </button>
        </div>

        <!-- Security Info -->
        <div class="security-info mt-3">
          <div class="security-badges">
            <div class="security-badge">
              <i class="bi bi-shield-check"></i>
              <span>Bảo mật</span>
            </div>
            <div class="security-badge">
              <i class="bi bi-truck"></i>
              <span>Giao hàng nhanh</span>
            </div>
            <div class="security-badge">
              <i class="bi bi-arrow-clockwise"></i>
              <span>Đổi trả dễ dàng</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCart } from '@/composables/useCart'

const { items, formatPrice } = useCart()

// Tính toán dựa trên real cart data
const totalItemsCount = computed(() => {
  return items.value.length
})

const selectedItemsCount = computed(() => {
  return items.value.filter(item => item.selected !== false).length
})

const subtotal = computed(() => {
  return items.value
    .filter(item => item.selected !== false)
    .reduce((total, item) => total + (item.price * item.quantity), 0)
})

const shippingFee = computed(() => {
  return subtotal.value >= 500000 ? 0 : 30000
})

const discountAmount = computed(() => {
  // TODO: Kết nối với voucher system
  return 0
})

const finalTotal = computed(() => {
  return subtotal.value + shippingFee.value - discountAmount.value
})
</script>

<style scoped>
.order-summary-section {
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
  position: relative;
  overflow: hidden;
}

.section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.section-header h5 i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.summary-header-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.summary-header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.summary-header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 1rem;
}

.badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}


.section-body {
  padding: 1.5rem;
}

.summary-section {
  margin-bottom: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding: 0.5rem 0;
}

.summary-label {
  font-size: 0.9rem;
  color: #666;
}

.summary-value {
  font-weight: 600;
  color: #333;
}

.discount-item .summary-value {
  color: #28a745;
}

.summary-divider {
  height: 1px;
  background: #e9ecef;
  margin: 1rem 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-top: 2px solid #e9ecef;
  margin-top: 1rem;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
}

.total-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #B8860B;
}

.checkout-section {
  margin-bottom: 1rem;
}

.checkout-btn {
  background: #000000;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  font-weight: 700;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.checkout-btn:hover:not(:disabled) {
  background: #333333;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Responsive Design */
@media (max-width: 768px) {
  .summary-header-layout {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .summary-header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .summary-header-right {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
  
  .badge {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }
}

.security-info {
  text-align: center;
}

.security-badges {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.security-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #666;
}

.security-badge i {
  color: #B8860B;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .security-badges {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .summary-total {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
