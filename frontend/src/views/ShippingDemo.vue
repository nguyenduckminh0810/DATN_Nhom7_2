<template>
  <div class="shipping-demo">
    <div class="container">
      <h1>🚚 Demo GHN Shipping API</h1>
      <p class="subtitle">Test tích hợp API Giao Hàng Nhanh</p>

      <!-- Status Check -->
      <div class="status-card">
        <h3>📊 Trạng thái Backend API</h3>
        <div class="status-info">
          <span>Backend URL:</span>
          <code>{{ backendUrl }}</code>
        </div>
        <button @click="checkBackendStatus" :disabled="checking" class="btn-check">
          {{ checking ? 'Đang kiểm tra...' : 'Kiểm tra kết nối' }}
        </button>
        <div v-if="backendStatus" :class="['status-result', backendStatus.success ? 'success' : 'error']">
          {{ backendStatus.message }}
        </div>
      </div>

      <!-- Shipping Calculator Component -->
      <div class="demo-section">
        <h2>1️⃣ Shipping Calculator Component</h2>
        <p>Component có sẵn với đầy đủ UI và logic</p>
        
        <ShippingCalculator
          :total-weight="demoWeight"
          :order-value="demoOrderValue"
          :show-order-info="true"
          :auto-calculate="false"
          @calculated="handleCalculated"
          @addressChanged="handleAddressChanged"
        />

        <div v-if="calculatedResult" class="result-display">
          <h4>Kết quả tính toán:</h4>
          <pre>{{ JSON.stringify(calculatedResult, null, 2) }}</pre>
        </div>
      </div>

      <!-- Custom Implementation with Composable -->
      <div class="demo-section">
        <h2>2️⃣ Custom Form với useShipping Composable</h2>
        <p>Tự tạo form với composable</p>

        <div class="custom-form">
          <div class="form-row">
            <div class="form-group">
              <label>Tỉnh/Thành phố:</label>
              <select v-model="selectedProvince" @change="onProvinceChange">
                <option :value="null">-- Chọn tỉnh/thành phố --</option>
                <option v-for="p in provinces" :key="p.ProvinceID" :value="p.ProvinceID">
                  {{ p.ProvinceName }}
                </option>
              </select>
              <span v-if="loading.provinces" class="loading">⏳ Đang tải...</span>
            </div>

            <div class="form-group">
              <label>Quận/Huyện:</label>
              <select 
                v-model="selectedDistrict" 
                @change="onDistrictChange"
                :disabled="!selectedProvince"
              >
                <option :value="null">-- Chọn quận/huyện --</option>
                <option v-for="d in districts" :key="d.DistrictID" :value="d.DistrictID">
                  {{ d.DistrictName }}
                </option>
              </select>
              <span v-if="loading.districts" class="loading">⏳ Đang tải...</span>
            </div>

            <div class="form-group">
              <label>Phường/Xã:</label>
              <select 
                v-model="selectedWard"
                :disabled="!selectedDistrict"
              >
                <option :value="null">-- Chọn phường/xã --</option>
                <option v-for="w in wards" :key="w.WardCode" :value="w.WardCode">
                  {{ w.WardName }}
                </option>
              </select>
              <span v-if="loading.wards" class="loading">⏳ Đang tải...</span>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Khối lượng (gram):</label>
              <input type="number" v-model.number="customWeight" min="0" />
            </div>

            <div class="form-group">
              <label>Giá trị đơn hàng (VNĐ):</label>
              <input type="number" v-model.number="customOrderValue" min="0" />
            </div>
          </div>

          <button 
            @click="handleCustomCalculate" 
            :disabled="!canCalculateFee || loading.calculating"
            class="btn-calculate"
          >
            {{ loading.calculating ? '⏳ Đang tính...' : '💰 Tính phí vận chuyển' }}
          </button>

          <div v-if="errors.calculating" class="error-message">
            ❌ {{ errors.calculating }}
          </div>

          <div v-if="shippingFee > 0" class="result-card">
            <h4>✅ Kết quả:</h4>
            <div class="result-item">
              <span>Phí vận chuyển:</span>
              <strong>{{ formattedShippingFee }}</strong>
            </div>
            <div v-if="expectedDeliveryTime" class="result-item">
              <span>Dự kiến giao hàng:</span>
              <strong>{{ formatDate(expectedDeliveryTime) }}</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Direct Service Usage -->
      <div class="demo-section">
        <h2>3️⃣ Sử dụng ShippingService trực tiếp</h2>
        <p>Gọi API trực tiếp từ service</p>

        <div class="service-demo">
          <div class="button-group">
            <button @click="testGetProvinces" class="btn-test">
              📍 Test Get Provinces
            </button>
            <button @click="testGetDistricts" class="btn-test">
              🏘️ Test Get Districts
            </button>
            <button @click="testGetWards" class="btn-test">
              🏠 Test Get Wards
            </button>
            <button @click="testCalculateFee" class="btn-test">
              💵 Test Calculate Fee
            </button>
          </div>

          <div v-if="testResult" class="test-result">
            <h4>📋 Test Result:</h4>
            <pre>{{ testResult }}</pre>
          </div>
        </div>
      </div>

      <!-- Documentation Links -->
      <div class="demo-section">
        <h2>📚 Documentation</h2>
        <div class="doc-links">
          <a href="#" @click.prevent="openDoc('GHN_README.md')">📖 README</a>
          <a href="#" @click.prevent="openDoc('GHN_FRONTEND_GUIDE.md')">📘 Frontend Guide</a>
          <a href="#" @click.prevent="openDoc('test-shipping-api.http')">🧪 API Tests</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ShippingCalculator from '@/components/checkout/ShippingCalculator.vue'
import { useShipping } from '@/composables/useShipping'
import shippingService from '@/services/shippingService'
import apiService from '@/services/api'

// Backend status
const backendUrl = ref(import.meta.env.VITE_API_URL || 'http://localhost:8080/api')
const backendStatus = ref(null)
const checking = ref(false)

// Demo data
const demoWeight = ref(500)
const demoOrderValue = ref(500000)
const calculatedResult = ref(null)

// Custom form with composable
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
  canCalculateFee,
  formattedShippingFee,
  loadProvinces,
  loadDistricts,
  loadWards,
  calculateShippingFee,
} = useShipping()

const customWeight = ref(1000)
const customOrderValue = ref(750000)

// Service test
const testResult = ref(null)

// Methods
const checkBackendStatus = async () => {
  checking.value = true
  backendStatus.value = null

  try {
    // Try to ping the API
    const response = await fetch(`${backendUrl.value}/shipping/provinces`, {
      method: 'GET',
    })

    if (response.ok) {
      backendStatus.value = {
        success: true,
        message: '✅ Backend đang hoạt động! API sẵn sàng.',
      }
    } else {
      backendStatus.value = {
        success: false,
        message: `⚠️ Backend phản hồi với status: ${response.status}`,
      }
    }
  } catch (error) {
    backendStatus.value = {
      success: false,
      message: `❌ Không thể kết nối backend: ${error.message}. Đảm bảo Spring Boot đang chạy ở port 8080.`,
    }
  } finally {
    checking.value = false
  }
}

const handleCalculated = (data) => {
  calculatedResult.value = data
  console.log('Calculated:', data)
}

const handleAddressChanged = (address) => {
  console.log('Address changed:', address)
}

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

const handleCustomCalculate = async () => {
  try {
    await calculateShippingFee({
      totalWeight: customWeight.value,
      insuranceValue: customOrderValue.value,
    })
  } catch (error) {
    console.error('Calculate error:', error)
  }
}

const testGetProvinces = async () => {
  try {
    testResult.value = 'Loading...'
    const result = await shippingService.getProvinces()
    testResult.value = `✅ Success! Found ${result.length} provinces\n\nSample data:\n${JSON.stringify(result.slice(0, 3), null, 2)}`
  } catch (error) {
    testResult.value = `❌ Error: ${error.message}`
  }
}

const testGetDistricts = async () => {
  try {
    testResult.value = 'Loading...'
    const result = await shippingService.getDistricts(202) // TP.HCM
    testResult.value = `✅ Success! Found ${result.length} districts in TP.HCM\n\nSample data:\n${JSON.stringify(result.slice(0, 3), null, 2)}`
  } catch (error) {
    testResult.value = `❌ Error: ${error.message}`
  }
}

const testGetWards = async () => {
  try {
    testResult.value = 'Loading...'
    const result = await shippingService.getWards(1542) // Quận 1
    testResult.value = `✅ Success! Found ${result.length} wards in Quận 1\n\nSample data:\n${JSON.stringify(result.slice(0, 3), null, 2)}`
  } catch (error) {
    testResult.value = `❌ Error: ${error.message}`
  }
}

const testCalculateFee = async () => {
  try {
    testResult.value = 'Calculating...'
    const result = await shippingService.calculateShippingFee({
      toDistrictId: 1443,
      toWardCode: '21012',
      totalWeight: 500,
      insuranceValue: 500000,
      serviceId: 53320,
    })
    testResult.value = `✅ Success!\n\n${JSON.stringify(result, null, 2)}`
  } catch (error) {
    testResult.value = `❌ Error: ${error.message}`
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  } catch {
    return dateString
  }
}

const openDoc = (filename) => {
  alert(`Mở file: ${filename}\nVui lòng xem trong thư mục frontend/`)
}

// Lifecycle
onMounted(async () => {
  await checkBackendStatus()
  await loadProvinces()
})
</script>

<style scoped>
.shipping-demo {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: white;
  font-size: 3rem;
  margin-bottom: 10px;
  text-align: center;
}

.subtitle {
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.status-card,
.demo-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

h2 {
  font-size: 1.8rem;
  margin-bottom: 10px;
  color: #333;
}

h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #555;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.status-info code {
  background: #f5f5f5;
  padding: 5px 10px;
  border-radius: 4px;
  font-family: monospace;
}

.btn-check,
.btn-calculate,
.btn-test {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-check {
  background: #3498db;
  color: white;
}

.btn-check:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-check:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.status-result {
  margin-top: 15px;
  padding: 12px;
  border-radius: 6px;
  font-weight: 500;
}

.status-result.success {
  background: #d4edda;
  color: #155724;
  border-left: 4px solid #28a745;
}

.status-result.error {
  background: #f8d7da;
  color: #721c24;
  border-left: 4px solid #dc3545;
}

.custom-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #555;
}

.form-group select,
.form-group input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group select:disabled {
  background: #e9ecef;
  cursor: not-allowed;
}

.loading {
  font-size: 12px;
  color: #3498db;
  margin-top: 5px;
}

.btn-calculate {
  width: 100%;
  background: #28a745;
  color: white;
  margin-top: 10px;
}

.btn-calculate:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-2px);
}

.btn-calculate:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.error-message {
  margin-top: 15px;
  padding: 12px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 6px;
  border-left: 4px solid #dc3545;
}

.result-card,
.result-display {
  margin-top: 20px;
  padding: 20px;
  background: #d4edda;
  border-radius: 8px;
  border-left: 4px solid #28a745;
}

.result-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item strong {
  color: #155724;
  font-size: 18px;
}

.button-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.btn-test {
  background: #6c5ce7;
  color: white;
}

.btn-test:hover {
  background: #5f4dd4;
  transform: translateY(-2px);
}

.test-result {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.test-result pre {
  background: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  font-size: 14px;
  line-height: 1.5;
}

.result-display pre {
  background: #155724;
  color: #d4edda;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
}

.doc-links {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.doc-links a {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s;
}

.doc-links a:hover {
  background: #764ba2;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  h1 {
    font-size: 2rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .button-group {
    grid-template-columns: 1fr;
  }
}
</style>
