<template>
  <div class="shipping-calculator">
    <h3 class="calculator-title">Tính phí vận chuyển</h3>

    <!-- Address Selection -->
    <div class="address-section">
      <!-- Province Selection -->
      <div class="form-group">
        <label for="province">Tỉnh/Thành phố <span class="required">*</span></label>
        <select
          id="province"
          v-model="selectedProvince"
          @change="onProvinceChange"
          :disabled="loading.provinces"
          class="form-select"
        >
          <option :value="null">-- Chọn Tỉnh/Thành phố --</option>
          <option
            v-for="province in provinces"
            :key="province.ProvinceID"
            :value="province.ProvinceID"
          >
            {{ province.ProvinceName }}
          </option>
        </select>
        <span v-if="errors.provinces" class="error-text">{{ errors.provinces }}</span>
      </div>

      <!-- District Selection -->
      <div class="form-group">
        <label for="district">Quận/Huyện <span class="required">*</span></label>
        <select
          id="district"
          v-model="selectedDistrict"
          @change="onDistrictChange"
          :disabled="loading.districts || !selectedProvince"
          class="form-select"
        >
          <option :value="null">-- Chọn Quận/Huyện --</option>
          <option
            v-for="district in districts"
            :key="district.DistrictID"
            :value="district.DistrictID"
          >
            {{ district.DistrictName }}
          </option>
        </select>
        <span v-if="loading.districts" class="loading-text">Đang tải...</span>
        <span v-if="errors.districts" class="error-text">{{ errors.districts }}</span>
      </div>

      <!-- Ward Selection -->
      <div class="form-group">
        <label for="ward">Phường/Xã <span class="required">*</span></label>
        <select
          id="ward"
          v-model="selectedWard"
          @change="onWardChange"
          :disabled="loading.wards || !selectedDistrict"
          class="form-select"
        >
          <option :value="null">-- Chọn Phường/Xã --</option>
          <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
            {{ ward.WardName }}
          </option>
        </select>
        <span v-if="loading.wards" class="loading-text">Đang tải...</span>
        <span v-if="errors.wards" class="error-text">{{ errors.wards }}</span>
      </div>

      <!-- Service Selection -->
      <div class="form-group" v-if="services.length > 0">
        <label for="service">Dịch vụ vận chuyển <span class="required">*</span></label>
        <select
          id="service"
          v-model="selectedService"
          @change="onServiceChange"
          :disabled="loading.services"
          class="form-select"
        >
          <option :value="null">-- Chọn dịch vụ --</option>
          <option v-for="service in services" :key="service.service_id" :value="service.service_id">
            {{ service.short_name }}
          </option>
        </select>
        <span v-if="loading.services" class="loading-text">Đang tải...</span>
      </div>
    </div>

    <!-- Order Info -->
    <div class="order-info" v-if="showOrderInfo">
      <div class="info-row">
        <span class="info-label">Khối lượng:</span>
        <span class="info-value">{{ formatWeight(totalWeight) }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Giá trị đơn hàng:</span>
        <span class="info-value">{{ formatCurrency(orderValue) }}</span>
      </div>
    </div>

    <!-- Calculate Button -->
    <button
      @click="handleCalculate"
      :disabled="!canCalculateFee || loading.calculating"
      class="btn-calculate"
    >
      <span v-if="loading.calculating">Đang tính...</span>
      <span v-else>Tính phí vận chuyển</span>
    </button>

    <!-- Error Message -->
    <div v-if="errors.calculating" class="error-message">
      {{ errors.calculating }}
    </div>

    <!-- Shipping Fee Result -->
    <div v-if="shippingFee > 0" class="shipping-result">
      <div class="result-row highlight">
        <span class="result-label">Phí vận chuyển:</span>
        <span class="result-value">{{ formattedShippingFee }}</span>
      </div>
      <div class="result-row" v-if="expectedDeliveryTime">
        <span class="result-label">Dự kiến giao hàng:</span>
        <span class="result-value">{{ formatDeliveryTime(expectedDeliveryTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useShipping } from '@/composables/useShipping'

// Props
const props = defineProps({
  // Khối lượng đơn hàng (gram)
  totalWeight: {
    type: Number,
    default: 500,
  },
  // Giá trị đơn hàng (VNĐ)
  orderValue: {
    type: Number,
    default: 0,
  },
  // Hiển thị thông tin đơn hàng
  showOrderInfo: {
    type: Boolean,
    default: true,
  },
  // Auto calculate khi thay đổi địa chỉ
  autoCalculate: {
    type: Boolean,
    default: false,
  },
  // Initial address
  initialAddress: {
    type: Object,
    default: null,
  },
})

// Emits
const emit = defineEmits(['calculated', 'addressChanged'])

// Composable
const {
  provinces,
  districts,
  wards,
  services,
  selectedProvince,
  selectedDistrict,
  selectedWard,
  selectedService,
  shippingFee,
  expectedDeliveryTime,
  loading,
  errors,
  hasSelectedAddress,
  canCalculateFee,
  formattedShippingFee,
  loadProvinces,
  loadDistricts,
  loadWards,
  loadServices,
  calculateShippingFee,
  setAddress,
  getAddress,
  formatCurrency,
  formatWeight,
} = useShipping()

// Methods
const onProvinceChange = async () => {
  if (selectedProvince.value) {
    await loadDistricts(selectedProvince.value)
  }
  emitAddressChange()
}

const onDistrictChange = async () => {
  if (selectedDistrict.value) {
    await loadWards(selectedDistrict.value)
    await loadServices(selectedDistrict.value)

    if (props.autoCalculate && canCalculateFee.value) {
      await handleCalculate()
    }
  }
  emitAddressChange()
}

const onWardChange = () => {
  if (props.autoCalculate && canCalculateFee.value) {
    handleCalculate()
  }
  emitAddressChange()
}

const onServiceChange = () => {
  if (props.autoCalculate && canCalculateFee.value) {
    handleCalculate()
  }
}

const handleCalculate = async () => {
  try {
    const result = await calculateShippingFee({
      totalWeight: props.totalWeight,
      insuranceValue: props.orderValue,
    })

    if (result) {
      emit('calculated', {
        shippingFee: result.shippingFee,
        expectedDeliveryTime: result.expectedDeliveryTime,
        address: getAddress(),
      })
    }
  } catch (error) {
    console.error('Calculate shipping error:', error)
  }
}

const emitAddressChange = () => {
  emit('addressChanged', getAddress())
}

const formatDeliveryTime = (timeString) => {
  if (!timeString) return ''

  try {
    const date = new Date(timeString)
    return date.toLocaleDateString('vi-VN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  } catch (error) {
    return timeString
  }
}

// Lifecycle
onMounted(async () => {
  await loadProvinces()

  // Set initial address if provided
  if (props.initialAddress) {
    await setAddress(props.initialAddress)
  }
})

// Watch for external changes
watch(
  () => props.orderValue,
  () => {
    if (props.autoCalculate && canCalculateFee.value) {
      handleCalculate()
    }
  },
)

watch(
  () => props.totalWeight,
  () => {
    if (props.autoCalculate && canCalculateFee.value) {
      handleCalculate()
    }
  },
)
</script>

<style scoped>
.shipping-calculator {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.calculator-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.address-section {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #555;
}

.required {
  color: #e74c3c;
}

.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-select:focus {
  outline: none;
  border-color: #3498db;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.loading-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #3498db;
}

.error-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #e74c3c;
}

.order-info {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #666;
}

.info-value {
  font-weight: 500;
  color: #333;
}

.btn-calculate {
  width: 100%;
  padding: 12px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-calculate:hover:not(:disabled) {
  background: #2980b9;
}

.btn-calculate:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.error-message {
  margin-top: 12px;
  padding: 12px;
  background: #fee;
  border-left: 4px solid #e74c3c;
  border-radius: 4px;
  color: #c0392b;
  font-size: 14px;
}

.shipping-result {
  margin-top: 20px;
  padding: 16px;
  background: #e8f5e9;
  border-radius: 4px;
  border-left: 4px solid #4caf50;
}

.result-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.result-row:last-child {
  margin-bottom: 0;
}

.result-row.highlight {
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
}

.result-label {
  color: #555;
}

.result-value {
  font-weight: 500;
}
</style>
