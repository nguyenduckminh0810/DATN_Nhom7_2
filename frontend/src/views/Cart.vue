<template>
  <div class="checkout-page">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb modern-breadcrumb">
          <li class="breadcrumb-item">
            <router-link to="/" class="breadcrumb-link">
              <i class="bi bi-house me-1"></i>Trang chủ
            </router-link>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
          Giỏ hàng & Thanh toán
          </li>
        </ol>
      </nav>

    <!-- Header -->
    <div class="page-header mb-4">
      <h1 class="display-6 fw-bold text-center mb-3">Giỏ hàng & Thanh toán</h1>
      <div class="text-center">
        <div class="section-divider mx-auto"></div>
        </div>
      </div>

      <!-- Empty Cart -->
      <div v-if="isEmpty" class="empty-cart-state">
        <div class="empty-cart-content">
          <i class="bi bi-cart-x display-1 text-muted mb-4"></i>
          <h4 class="mb-3">Giỏ hàng trống</h4>
          <p class="text-muted mb-4">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
          <router-link to="/category" class="btn btn-auro-primary btn-lg">
            <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
          </router-link>
        </div>
      </div>

    <!-- Main Layout: 2 Columns -->
    <div v-else class="checkout-layout">
      <!-- Left Column (60%) -->
      <div class="checkout-left">
        <!-- Shipping Form -->
        <ShippingForm />
        
        <!-- Payment Methods -->
        <PaymentMethods />
                  </div>
                  
      <!-- Right Column (40%) -->
      <div class="checkout-right">
        <!-- Cart Items -->
        <CartItems />
        
        <!-- Voucher Section -->
        <VoucherSection />

        <!-- Order Summary -->
        <OrderSummary />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, provide } from 'vue'
import { useCart } from '@/composables/useCart'
import { useShipping } from '@/composables/useShipping'
import ShippingForm from '@/components/checkout/ShippingForm.vue'
import PaymentMethods from '@/components/checkout/PaymentMethods.vue'
import CartItems from '@/components/checkout/CartItems.vue'
import VoucherSection from '@/components/checkout/VoucherSection.vue'
import OrderSummary from '@/components/checkout/OrderSummary.vue'

// Sử dụng cart store
const { items, isEmpty } = useCart()

// Sử dụng shipping composable
const { shippingFee, expectedDeliveryTime } = useShipping()

// Provide shipping info để OrderSummary có thể sử dụng
provide('shippingInfo', {
  shippingFee,
  expectedDeliveryTime
})
</script>

<style scoped>
.checkout-page {
  background-color: #f8f9fa;
  min-height: 100vh;
  width: 100%;
  padding: 2rem 3rem;
}

.checkout-layout {
  display: flex;
  gap: 2rem;
  width: 100%;
}

.checkout-left {
  flex: 0 0 60%;
  min-width: 0;
}

.checkout-right {
  flex: 0 0 40%;
  min-width: 0;
}

.section-divider {
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  border-radius: 2px;
}

/* Page Header */
.page-header {
  width: 100%;
}

/* Breadcrumb Styles */
.modern-breadcrumb {
  background: none;
  padding: 0;
  margin-bottom: 0;
}

.breadcrumb-item {
  font-size: 0.9rem;
}

.breadcrumb-link {
  color: #666;
  text-decoration: none;
  transition: color 0.2s ease;
}

.breadcrumb-link:hover {
  color: #B8860B;
}

.breadcrumb-item.active {
  color: #333;
  font-weight: 500;
}

/* Empty Cart Styles */
.empty-cart-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.empty-cart-content {
  text-align: center;
  max-width: 400px;
}

.empty-cart-content h4 {
  color: #333;
  font-weight: 600;
}

.btn-auro-primary {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.btn-auro-primary:hover {
  background: linear-gradient(135deg, #DAA520 0%, #B8860B 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(184, 134, 11, 0.3);
  color: white;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .checkout-page {
    padding: 2rem 2rem;
  }
}

@media (max-width: 992px) {
  .checkout-page {
    padding: 1.5rem 1.5rem;
  }
  
  .checkout-layout {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .checkout-left,
  .checkout-right {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .checkout-page {
    padding: 1rem 1rem;
  }
  
  .checkout-layout {
    gap: 1rem;
  }
  
  .section-divider {
    width: 40px;
    height: 2px;
  }
}

@media (max-width: 768px) {
  .cart-header-content,
  .summary-header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .cart-header-content {
    flex-direction: row;
  align-items: center;
    justify-content: space-between;
  }
  
  .cart-actions-section {
    order: 2;
  }
  
  .cart-clear-section {
    order: 3;
  }
  
  .cart-count-badge,
  .summary-count-badge {
    font-size: 0.8rem;
    padding: 0.3rem 0.6rem;
  }
  
  .clear-all-button {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .checkout-page {
    padding: 1rem 0.75rem;
  }
  
  .cart-title,
  .summary-title {
    font-size: 1rem;
  }
}

/* Common Section Styles */
.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: visible;
  margin-bottom: 2rem;
  position: relative;
}

.section-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 16px 16px 0 0;
  position: relative;
  z-index: 2;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
}

.section-header i {
  font-size: 1.2rem;
  margin-right: 0.5rem;
}

/* Cart Header Layout */
.cart-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 1rem;
}

.cart-title-section {
  flex: 1;
  min-width: 0;
}

.cart-title {
  color: white !important;
  font-weight: 700;
  font-size: 1.1rem;
  white-space: nowrap;
}

.cart-actions-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.cart-clear-section {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.cart-count-badge {
  background: rgba(255, 255, 255, 0.95);
  color: #B8860B;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.clear-all-button {
  background: rgba(220, 53, 69, 0.9);
  border: 2px solid rgba(255, 255, 255, 0.8);
  color: white;
  padding: 0.5rem;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.clear-all-button:hover {
  background: rgba(220, 53, 69, 1);
  border-color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
}

.clear-all-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(220, 53, 69, 0.3);
}

/* Summary Header Layout */
.summary-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 1rem;
}

.summary-title-section {
  flex: 1;
  min-width: 0;
}

.summary-title {
  color: white !important;
  font-weight: 700;
  font-size: 1.1rem;
  white-space: nowrap;
}

.summary-count-section {
  flex-shrink: 0;
}

.summary-count-badge {
  background: rgba(255, 255, 255, 0.95);
  color: #B8860B;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Enhanced visual improvements */
.section-header {
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

.cart-title i,
.summary-title i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.cart-count-badge,
.summary-count-badge {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.clear-all-button i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}


.section-body {
  padding: 1.5rem;
}

/* Form Styles */
.form-group {
  margin-bottom: 1.25rem;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.form-control,
.form-select {
  width: 100%;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.form-control:focus,
.form-select:focus {
  border-color: #B8860B;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1);
  outline: none;
}

.form-row {
  display: flex;
  gap: 0.75rem;
}

.form-col-title {
  flex: 0 0 80px;
}

.form-col-name {
  flex: 1;
}

.form-col-province {
  flex: 1;
}

.form-col-district {
  flex: 1;
}

.checkbox-group {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.checkbox-group .form-check {
  margin-bottom: 0;
}

.checkbox-group .form-check-label {
  font-size: 0.9rem;
  color: #555;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .form-col-title,
  .form-col-province,
  .form-col-district {
    flex: 1;
  }
  
  .checkbox-group {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .section-body {
    padding: 1rem;
  }
}
</style>