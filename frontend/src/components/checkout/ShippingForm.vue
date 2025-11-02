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

          <!-- Shipping Fee Display - Shopee GHN Style -->
          <div v-if="shippingFee > 0" class="shipping-fee-card mt-3">
            <!-- Main Fee Display -->
            <div class="fee-header">
              <div class="fee-label">
                <i class="bi bi-truck-front-fill"></i>
                <span>Phí vận chuyển</span>
              </div>
              <div class="fee-main-amount">{{ formattedShippingFee }}</div>
            </div>

            <!-- Fee Breakdown - Collapsible -->
            <div class="fee-breakdown">
              <button 
                type="button"
                class="breakdown-toggle"
                @click="showBreakdown = !showBreakdown"
              >
                <span>{{ showBreakdown ? 'Ẩn' : 'Xem' }} chi tiết phí</span>
                <i class="bi" :class="showBreakdown ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
              </button>

              <transition name="slide-fade">
                <div v-if="showBreakdown" class="breakdown-details">
                  <div class="breakdown-item">
                    <span class="item-label">
                      <i class="bi bi-box-seam"></i>
                      Phí cơ bản
                    </span>
                    <span class="item-value">20,000₫</span>
                  </div>
                  
                  <div class="breakdown-item" v-if="provinceFeeAmount > 0">
                    <span class="item-label">
                      <i class="bi bi-geo-alt"></i>
                      Phí khoảng cách ({{ currentProvinceName }})
                    </span>
                    <span class="item-value">{{ formatMoney(provinceFeeAmount) }}</span>
                  </div>
                  
                  <div class="breakdown-item" v-if="districtFeeAmount > 0">
                    <span class="item-label">
                      <i class="bi bi-building"></i>
                      Phí quận/huyện
                    </span>
                    <span class="item-value">{{ formatMoney(districtFeeAmount) }}</span>
                  </div>
                  
                  <div class="breakdown-item" v-if="wardFeeAmount > 0">
                    <span class="item-label">
                      <i class="bi bi-pin-map"></i>
                      Phí xã/phường ({{ wardPositionText }})
                    </span>
                    <span class="item-value">{{ formatMoney(wardFeeAmount) }}</span>
                  </div>
                  
                  <div class="breakdown-item">
                    <span class="item-label">
                      <i class="bi bi-boxes"></i>
                      Phí trọng lượng ({{ formatWeight(totalWeight) }})
                    </span>
                    <span class="item-value">{{ formatMoney(weightFeeAmount) }}</span>
                  </div>
                  
                  <div class="breakdown-item" v-if="insuranceFeeAmount > 0">
                    <span class="item-label">
                      <i class="bi bi-shield-check"></i>
                      Phí bảo hiểm
                    </span>
                    <span class="item-value">{{ formatMoney(insuranceFeeAmount) }}</span>
                  </div>
                  
                  <div class="breakdown-divider"></div>
                  
                  <div class="breakdown-item breakdown-total">
                    <span class="item-label">Tổng cộng</span>
                    <span class="item-value total">{{ formattedShippingFee }}</span>
                  </div>
                </div>
              </transition>
            </div>

            <!-- Delivery Time -->
            <div v-if="expectedDeliveryTime" class="delivery-info">
              <i class="bi bi-calendar-event"></i>
              <span>Dự kiến giao: <strong>{{ formatDate(expectedDeliveryTime) }}</strong></span>
            </div>

            <!-- Shipping Badge -->
            <div class="shipping-badge">
              <i class="bi bi-lightning-charge-fill"></i>
              <span>Giao hàng nhanh bởi GHN</span>
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

// UI state
const showBreakdown = ref(false)

// Fee breakdown amounts (mock calculation)
const provinceFeeAmount = computed(() => {
  if (!selectedProvince.value) return 0
  
  const bigCities = [202, 201, 203, 204]
  if (bigCities.includes(selectedProvince.value)) return 0
  
  if ([269, 271, 273, 275, 277, 279, 281, 283, 285, 287, 289, 291, 293, 295, 297, 299, 301, 303, 305, 307, 380, 382, 384, 386].includes(selectedProvince.value)) {
    return 30000
  }
  if ([309, 311, 313, 315, 317, 319, 321, 323, 325, 327, 329, 331, 333].includes(selectedProvince.value)) {
    return 40000
  }
  if ([335, 337, 339, 341, 343].includes(selectedProvince.value)) {
    return 45000
  }
  if ([345, 347, 349, 351, 353, 355, 357, 359, 361, 363, 365, 367, 369, 371, 373, 375, 377].includes(selectedProvince.value)) {
    return 25000
  }
  return 35000
})

const districtFeeAmount = computed(() => {
  if (!selectedDistrict.value) return 0
  
  const districtName = districts.value.find(d => d.DistrictID === selectedDistrict.value)?.DistrictName || ''
  
  if (districtName.includes('Quận 1') || districtName.includes('Hoàn Kiếm') || 
      districtName.includes('Hải Châu') || districtName.includes('Ninh Kiều')) {
    return 0
  }
  if (districtName.includes('Quận') || districtName.startsWith('TP ')) {
    return 5000
  }
  if (districtName.includes('Huyện') || districtName.includes('TX ')) {
    return 10000
  }
  return 8000
})

const wardFeeAmount = computed(() => {
  if (!selectedWard.value || wards.value.length === 0) return 0
  
  const wardIndex = wards.value.findIndex(w => w.WardCode === selectedWard.value)
  const totalWards = wards.value.length
  const wardName = wards.value[wardIndex]?.WardName || ''
  const distanceRatio = totalWards > 1 ? wardIndex / (totalWards - 1) : 0
  
  let fee = 0
  
  if (wardName.includes('Phường') && !wardName.includes('Xã')) {
    if (wardName.includes('Bến Nghé') || wardName.includes('Bến Thành') || 
        wardName.includes('Hàng Bạc') || wardName.includes('Hàng Gai')) {
      fee = 0
    } else {
      fee = Math.round(1000 + distanceRatio * 3000)
    }
  } else if (wardName.includes('Thị trấn')) {
    fee = Math.round(2000 + distanceRatio * 3000)
  } else if (wardName.includes('Xã')) {
    if (wardName.includes('Sơn') || wardName.includes('Thượng') || 
        wardName.includes('Cao') || wardName.includes('Núi')) {
      fee = Math.round(5000 + distanceRatio * 5000)
    } else {
      fee = Math.round(3000 + distanceRatio * 4000)
    }
  } else {
    fee = Math.round(2000 + distanceRatio * 3000)
  }
  
  return Math.round(fee / 500) * 500
})

const weightFeeAmount = computed(() => {
  return Math.ceil(totalWeight.value / 1000) * 5000
})

const insuranceFeeAmount = computed(() => {
  return total.value > 3000000 ? Math.ceil(total.value * 0.005) : 0
})

const currentProvinceName = computed(() => {
  return provinces.value.find(p => p.ProvinceID === selectedProvince.value)?.ProvinceName || ''
})

const wardPositionText = computed(() => {
  if (!selectedWard.value || wards.value.length === 0) return ''
  
  const wardIndex = wards.value.findIndex(w => w.WardCode === selectedWard.value)
  const totalWards = wards.value.length
  const distanceRatio = totalWards > 1 ? wardIndex / (totalWards - 1) : 0
  const distancePercent = Math.round(distanceRatio * 100)
  
  if (distancePercent === 0) return 'Gần nhất'
  if (distancePercent < 30) return 'Gần'
  if (distancePercent < 70) return 'Trung bình'
  if (distancePercent < 100) return 'Xa'
  return 'Xa nhất'
})

const formatMoney = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatWeight = (grams) => {
  if (grams >= 1000) {
    return `${(grams / 1000).toFixed(1)}kg`
  }
  return `${grams}g`
}

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

/* Shipping Fee Card - Shopee GHN Style */
.shipping-fee-card {
  background: linear-gradient(135deg, #fff5e6 0%, #ffffff 100%);
  border: 2px solid #ee4d2d;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(238, 77, 45, 0.1);
}

.fee-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b45 100%);
  color: white;
}

.fee-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
}

.fee-label i {
  font-size: 1.2rem;
}

.fee-main-amount {
  font-size: 1.5rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.fee-breakdown {
  background: white;
}

.breakdown-toggle {
  width: 100%;
  padding: 0.75rem 1.25rem;
  border: none;
  background: transparent;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  color: #ee4d2d;
  font-weight: 600;
  font-size: 0.9rem;
  transition: background 0.2s;
}

.breakdown-toggle:hover {
  background: #fff5e6;
}

.breakdown-toggle i {
  transition: transform 0.3s;
}

.breakdown-details {
  padding: 0 1.25rem 1rem;
  background: white;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  font-size: 0.9rem;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
}

.item-label i {
  color: #ee4d2d;
  font-size: 0.95rem;
}

.item-value {
  font-weight: 600;
  color: #333;
}

.breakdown-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #ddd, transparent);
  margin: 0.5rem 0;
}

.breakdown-total {
  padding-top: 0.75rem;
  font-size: 1rem;
}

.breakdown-total .item-label {
  color: #333;
  font-weight: 700;
}

.breakdown-total .item-value.total {
  color: #ee4d2d;
  font-size: 1.1rem;
  font-weight: 700;
}

.delivery-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  font-size: 0.9rem;
  color: #555;
}

.delivery-info i {
  color: #28a745;
  font-size: 1rem;
}

.delivery-info strong {
  color: #333;
}

.shipping-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem;
  background: linear-gradient(135deg, #fff9e6 0%, #fffcf0 100%);
  border-top: 1px solid #ffe0b2;
  font-size: 0.85rem;
  color: #f57c00;
  font-weight: 600;
}

.shipping-badge i {
  font-size: 1rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* Slide fade transition */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Old styles for compatibility */
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
