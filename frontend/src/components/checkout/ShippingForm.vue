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
        <!-- Title & Full Name -->
        <div class="form-group">
          <label class="form-label">Họ tên</label>
          <div class="form-row">
            <div class="form-col-title">
              <select class="form-select" v-model="shippingInfo.title">
                <option value="anh">Anh</option>
                <option value="chi">Chị</option>
              </select>
            </div>
            <div class="form-col-name">
              <input type="text" 
                     class="form-control" 
                     v-model="shippingInfo.fullName"
                     placeholder="Nhập họ tên của bạn">
            </div>
          </div>
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
  title: 'anh',
  fullName: '',
  email: '',
  phone: '',
  address: '',
  province: '',
  district: '',
  notes: ''
})
</script>

<style scoped>
.shipping-form-section {
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
}
</style>
