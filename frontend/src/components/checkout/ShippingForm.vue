<template>
  <div class="shipping-form-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <h5 class="mb-0">
          <i class="bi bi-truck me-2"></i>Thông tin vận chuyển
        </h5>
      </div>

      <!-- Form Body -->
      <div class="section-body">
        <!-- Full Name -->
        <div class="form-group">
          <label class="form-label">Họ tên</label>
          <input type="text" 
                 class="form-control" 
                 v-model="shippingInfo.fullName"
                 placeholder="Nhập họ tên của bạn">
        </div>

        <!-- Email -->
        <div class="form-group">
          <label class="form-label">Email</label>
          <input type="email" 
                 class="form-control" 
                 v-model="shippingInfo.email"
                 placeholder="Nhập email của bạn">
        </div>

        <!-- Phone -->
        <div class="form-group">
          <label class="form-label">Số điện thoại</label>
          <input type="tel" 
                 class="form-control" 
                 v-model="shippingInfo.phone"
                 placeholder="Nhập số điện thoại">
        </div>

        <!-- Address Details -->
        <div class="form-group">
          <label class="form-label">Địa chỉ</label>
          <input type="text" 
                 class="form-control mb-2" 
                 v-model="shippingInfo.address"
                 placeholder="Nhập địa chỉ cụ thể">
          
          <div class="form-row">
            <div class="form-col-province">
              <select class="form-select" v-model="shippingInfo.province">
                <option value="">Chọn tỉnh/thành phố</option>
                <option value="hanoi">Hà Nội</option>
                <option value="hcm">TP. Hồ Chí Minh</option>
                <option value="danang">Đà Nẵng</option>
                <option value="haiphong">Hải Phòng</option>
              </select>
            </div>
            <div class="form-col-district">
              <select class="form-select" v-model="shippingInfo.district">
                <option value="">Chọn quận/huyện</option>
                <option value="ba-dinh">Ba Đình</option>
                <option value="hoan-kiem">Hoàn Kiếm</option>
                <option value="dong-da">Đống Đa</option>
              </select>
            </div>
            <div class="form-col-ward">
              <select class="form-select" v-model="shippingInfo.ward">
                <option value="">Chọn xã/phường</option>
                <option value="phuong-1">Phường 1</option>
                <option value="phuong-2">Phường 2</option>
                <option value="phuong-3">Phường 3</option>
                <option value="phuong-4">Phường 4</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Notes -->
        <div class="form-group">
          <label class="form-label">Ghi chú</label>
          <textarea class="form-control" 
                    rows="3" 
                    v-model="shippingInfo.notes"
                    placeholder="Ghi chú (tùy chọn)"></textarea>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const shippingInfo = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  province: '',
  district: '',
  ward: '',
  notes: ''
})
</script>

<style scoped>
.shipping-form-section {
  margin-bottom: 2rem;
}

/* Reset any conflicting styles */
.shipping-form-section * {
  box-sizing: border-box;
}

.shipping-form-section input,
.shipping-form-section select,
.shipping-form-section textarea {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
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
  border: 2px solid #e9ecef !important;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  background-color: white;
}

.form-control:focus,
.form-select:focus {
  border-color: #B8860B !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.form-control:hover,
.form-select:hover {
  border-color: #B8860B !important;
}

/* Ensure all form controls have visible borders */
.form-control:not(:focus):not(:hover),
.form-select:not(:focus):not(:hover) {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

/* Force border visibility for all states */
input[type="text"],
input[type="email"],
input[type="tel"],
textarea,
select {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

input[type="text"]:focus,
input[type="email"]:focus,
input[type="tel"]:focus,
textarea:focus,
select:focus {
  border-color: #B8860B !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.form-row {
  display: flex;
  gap: 0.75rem;
}

.form-col-province {
  flex: 1;
  min-width: 0;
}

.form-col-district {
  flex: 1;
  min-width: 0;
}

.form-col-ward {
  flex: 1;
  min-width: 0;
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
  
  .form-col-province,
  .form-col-district,
  .form-col-ward {
    flex: 1;
    min-width: auto;
  }
  
  .checkbox-group {
    flex-direction: column;
    gap: 0.75rem;
  }
}

@media (max-width: 480px) {
  .form-row {
    gap: 0.5rem;
  }
  
  .form-col-province,
  .form-col-district,
  .form-col-ward {
    min-width: 0;
  }
}
</style>
