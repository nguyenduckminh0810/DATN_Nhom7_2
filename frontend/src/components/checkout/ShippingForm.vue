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

        <!-- Address Details with GHN API -->
        <div class="form-group">
          <label class="form-label">Địa chỉ</label>
          <input type="text" 
                 class="form-control mb-2" 
                 v-model="shippingInfo.address"
                 placeholder="Nhập địa chỉ cụ thể">
          
          <div class="form-row">
            <div class="form-col-province">
              <select 
                class="form-select" 
                v-model="selectedProvince"
                @change="onProvinceChange"
                :disabled="loading.provinces"
              >
                <option :value="null">Chọn tỉnh/thành phố</option>
                <option 
                  v-for="province in provinces" 
                  :key="province.ProvinceID" 
                  :value="province.ProvinceID"
                >
                  {{ province.ProvinceName }}
                </option>
              </select>
              <small v-if="loading.provinces" class="text-muted">Đang tải...</small>
            </div>
            <div class="form-col-district">
              <select 
                class="form-select" 
                v-model="selectedDistrict"
                @change="onDistrictChange"
                :disabled="loading.districts || !selectedProvince"
              >
                <option :value="null">Chọn quận/huyện</option>
                <option 
                  v-for="district in districts" 
                  :key="district.DistrictID" 
                  :value="district.DistrictID"
                >
                  {{ district.DistrictName }}
                </option>
              </select>
              <small v-if="loading.districts" class="text-muted">Đang tải...</small>
            </div>
            <div class="form-col-ward">
              <select 
                class="form-select" 
                v-model="selectedWard"
                @change="onWardChange"
                :disabled="loading.wards || !selectedDistrict"
              >
                <option :value="null">Chọn xã/phường</option>
                <option 
                  v-for="ward in wards" 
                  :key="ward.WardCode" 
                  :value="ward.WardCode"
                >
                  {{ ward.WardName }}
                </option>
              </select>
              <small v-if="loading.wards" class="text-muted">Đang tải...</small>
            </div>
          </div>

          <!-- Shipping Fee Display -->
          <div v-if="shippingFee > 0" class="shipping-fee-display mt-3">
            <div class="fee-info">
              <i class="bi bi-truck me-2"></i>
              <span>Phí vận chuyển: </span>
              <strong class="fee-amount">{{ formattedShippingFee }}</strong>
            </div>
            <div v-if="expectedDeliveryTime" class="delivery-info mt-2">
              <i class="bi bi-calendar-check me-2"></i>
              <span>Dự kiến giao hàng: {{ formatDate(expectedDeliveryTime) }}</span>
            </div>
          </div>

          <!-- Error Display -->
          <div v-if="errors.calculating" class="alert alert-danger mt-2" role="alert">
            <i class="bi bi-exclamation-triangle me-2"></i>
            {{ errors.calculating }}
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
import { ref, onMounted, watch, computed } from 'vue'
import { useShipping } from '@/composables/useShipping'
import { useCart } from '@/composables/useCart'

// Initialize shipping composable
const {
  provinces,
  districts,
  wards,
  selectedProvince,
  selectedDistrict,
  selectedWard,
  shippingFee,
  expectedDeliveryTime,
  loading,
  errors,
  formattedShippingFee,
  loadProvinces,
  loadDistricts,
  loadWards,
  calculateShippingFee,
} = useShipping()

// Initialize cart composable with fallback
const cart = useCart()
const items = computed(() => cart?.items?.value || [])
const total = computed(() => cart?.total?.value || 0)

// Shipping info form data
const shippingInfo = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  notes: ''
})

// Calculate total weight from cart items
const totalWeight = computed(() => {
  if (!items.value || items.value.length === 0) {
    return 500 // Default 500g if cart is empty
  }
  return items.value.reduce((sum, item) => {
    const weight = item?.weight || 500 // Default 500g if not specified
    const quantity = item?.quantity || 1
    return sum + (weight * quantity)
  }, 0)
})

const onProvinceChange = async () => {
  if (selectedProvince.value) {
    await loadDistricts(selectedProvince.value)
  }
}

const onDistrictChange = async () => {
  if (selectedDistrict.value) {
    await loadWards(selectedDistrict.value)
  }
}

const onWardChange = async () => {
  if (selectedWard.value && selectedDistrict.value) {
    // Auto calculate shipping fee when address is complete
    try {
      await calculateShippingFee({
        totalWeight: totalWeight.value,
        insuranceValue: total.value,
      })
    } catch (error) {
      console.error('Error calculating shipping fee:', error)
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      day: 'numeric',
      month: 'numeric',
      year: 'numeric',
    })
  } catch {
    return dateString
  }
}

// Load provinces on mount
onMounted(async () => {
  try {
    await loadProvinces()
    console.log('✅ ShippingForm mounted successfully')
  } catch (error) {
    console.error('❌ Error loading provinces on mount:', error)
  }
})

// Watch for cart changes and recalculate shipping
watch([totalWeight, total], async () => {
  if (selectedDistrict.value && selectedWard.value) {
    try {
      await calculateShippingFee({
        totalWeight: totalWeight.value,
        insuranceValue: total.value,
      })
    } catch (error) {
      // Silent fail, error already handled in composable
      console.error('Error in watch recalculate:', error)
    }
  }
}, { immediate: false })
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

/* Shipping Fee Display */
.shipping-fee-display {
  padding: 1rem;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8f4 100%);
  border-radius: 8px;
  border-left: 4px solid #4caf50;
}

.fee-info {
  display: flex;
  align-items: center;
  font-size: 1rem;
  color: #2e7d32;
}

.fee-amount {
  color: #1b5e20;
  font-size: 1.2rem;
  margin-left: 0.5rem;
}

.delivery-info {
  font-size: 0.9rem;
  color: #555;
  display: flex;
  align-items: center;
}

.text-muted {
  font-size: 0.85rem;
  color: #6c757d;
  margin-top: 0.25rem;
  display: block;
}

.alert {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
}

.alert-danger {
  background-color: #fee;
  border-left: 4px solid #dc3545;
  color: #c0392b;
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
