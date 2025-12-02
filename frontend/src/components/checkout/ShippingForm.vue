<template>
  <div class="shipping-form-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <h5 class="mb-0"><i class="bi bi-truck me-2"></i>Thông tin vận chuyển</h5>
      </div>

      <!-- Form Body -->
      <div class="section-body">
        <!-- Saved Addresses Selection (Only for authenticated users) -->
        <div v-if="userStore.isAuthenticated && savedAddresses.length > 0" class="form-group">
          <label class="form-label">Địa chỉ đã lưu</label>
          <div class="saved-addresses">
            <!-- Default Address Card (Always shown first) -->
            <div
              v-for="address in savedAddresses"
              :key="address.id"
              class="address-option"
              :class="{ selected: selectedAddressId === address.id }"
              @click="selectAddress(address)"
            >
              <div class="address-radio">
                <input
                  type="radio"
                  :id="'addr-' + address.id"
                  :value="address.id"
                  v-model="selectedAddressId"
                  @change="selectAddress(address)"
                />
              </div>
              <div class="address-content">
                <div class="address-header">
                  <strong>{{ address.hoTen }}</strong>
                  <span v-if="address.macDinh" class="badge bg-warning text-dark ms-2"
                    >Mặc định</span
                  >
                </div>
                <div class="address-details">
                  <div><i class="bi bi-telephone"></i> {{ address.soDienThoai }}</div>
                  <div><i class="bi bi-geo-alt"></i> {{ address.diaChiDayDu }}</div>
                </div>
              </div>
            </div>

            <!-- Manual Input Option -->
            <div
              class="address-option"
              :class="{ selected: selectedAddressId === null }"
              @click="selectManualInput"
            >
              <div class="address-radio">
                <input
                  type="radio"
                  id="addr-manual"
                  :value="null"
                  v-model="selectedAddressId"
                  @change="selectManualInput"
                />
              </div>
              <div class="address-content">
                <div class="address-header">
                  <strong><i class="bi bi-plus-circle me-2"></i>Nhập địa chỉ khác</strong>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Manual Shipping Info Form (shown when no address selected or for guest users) -->
        <div v-show="selectedAddressId === null || !userStore.isAuthenticated">
          <!-- Full Name -->
          <div class="form-group">
            <label class="form-label">Họ tên</label>
            <input
              type="text"
              class="form-control"
              v-model="shippingInfo.fullName"
              placeholder="Nhập họ tên của bạn"
            />
          </div>

          <!-- Email -->
          <div class="form-group">
            <label class="form-label">Email</label>
            <input
              type="email"
              class="form-control"
              v-model="shippingInfo.email"
              placeholder="Nhập email của bạn"
            />
          </div>

          <!-- Phone -->
          <div class="form-group">
            <label class="form-label">Số điện thoại</label>
            <input
              type="tel"
              class="form-control"
              v-model="shippingInfo.phone"
              placeholder="Nhập số điện thoại"
            />
          </div>

          <!-- Address Details with GHN API -->
          <div class="form-group">
            <label class="form-label">Địa chỉ</label>
            <input
              type="text"
              class="form-control mb-2"
              v-model="shippingInfo.address"
              placeholder="Nhập địa chỉ cụ thể"
            />

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
                  <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                    {{ ward.WardName }}
                  </option>
                </select>
                <small v-if="loading.wards" class="text-muted">Đang tải...</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Shipping Fee Display - Always shown when available -->
        <div v-if="shippingFee > 0" class="shipping-fee-card mt-3">
          <!-- Main Fee Display -->
          <div class="fee-header">
            <div class="fee-label">
              <i class="bi bi-truck-front-fill"></i>
              <span>Phí vận chuyển</span>
            </div>
            <div class="fee-main-amount">{{ formattedShippingFee }}</div>
          </div>

          <!-- Delivery Time -->
          <div v-if="expectedDeliveryTime" class="delivery-info">
            <i class="bi bi-calendar-event"></i>
            <span
              >Dự kiến giao: <strong>{{ formatDate(expectedDeliveryTime) }}</strong></span
            >
          </div>

          <!-- Shipping Badge -->
          <div class="shipping-badge">
            <i class="bi bi-lightning-charge-fill"></i>
            <span>Giao hàng nhanh bởi GHN</span>
          </div>

          <!-- Shipping Info Note -->
          <div class="shipping-info-note">
            <i class="bi bi-info-circle"></i>
            <span
              >Phí vận chuyển được tính dựa trên địa chỉ giao hàng và trọng lượng đơn hàng ({{
                formatWeight(totalWeight)
              }})</span
            >
          </div>
        </div>

        <!-- Error Display -->
        <div v-if="errors.calculating" class="alert alert-danger mt-2" role="alert">
          <i class="bi bi-exclamation-triangle me-2"></i>
          {{ errors.calculating }}
        </div>

        <!-- Notes -->
        <div class="form-group">
          <label class="form-label">Ghi chú</label>
          <textarea
            class="form-control"
            rows="3"
            v-model="shippingInfo.notes"
            placeholder="Ghi chú (tùy chọn)"
          ></textarea>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, inject } from 'vue'
import { useShipping } from '@/composables/useShipping'
import { useCart } from '@/composables/useCart'
import { useUserStore } from '@/stores/user'
import addressService from '@/services/addressService'

const REORDER_STORAGE_KEY = 'auro_reorder_shipping'
const MAX_REORDER_STORAGE_AGE = 1000 * 60 * 30

// Get user store
const userStore = useUserStore()

// Try to get shipping instance from parent Cart.vue
const injectedShipping = inject('shipping', null)

// Initialize shipping composable - SỬ DỤNG INSTANCE TỪ CART.VUE NẾU CÓ
console.log('🚀 ShippingForm component initializing...')
const shipping = injectedShipping || useShipping()

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
  loadServices,
  calculateShippingFee,
} = shipping

console.log('✅ useShipping initialized. Provinces:', provinces.value.length)

// Initialize cart composable with fallback
const cart = useCart()
const items = computed(() => cart?.items?.value || [])
const total = computed(() => {
  if (cart?.totalPrice?.value != null) {
    return cart.totalPrice.value
  }
  if (cart?.total?.value != null) {
    return cart.total.value
  }
  return 0
})

// Shipping info form data - Inject từ parent Cart.vue
const shippingFormData = inject('shippingFormData', null)

// Nếu không có inject (fallback), tạo local ref
const shippingInfo =
  shippingFormData ||
  ref({
    fullName: '',
    email: '',
    phone: '',
    address: '',
    notes: '',
  })

// Saved addresses state
const savedAddresses = ref([])
const selectedAddressId = ref(null)
const loadingAddresses = ref(false)
const reorderPrefilled = ref(false)

// Load saved addresses for authenticated users
const loadSavedAddresses = async () => {
  if (!userStore.isAuthenticated) {
    return
  }

  loadingAddresses.value = true
  try {
    const response = await addressService.getAllAddresses()
    if (response.success && response.data) {
      // Sắp xếp địa chỉ: mặc định lên đầu
      savedAddresses.value = (response.data || []).sort((a, b) => {
        if (a.macDinh && !b.macDinh) return -1
        if (!a.macDinh && b.macDinh) return 1
        return 0
      })

      // Format địa chỉ đầy đủ cho mỗi địa chỉ
      savedAddresses.value = savedAddresses.value.map((addr) => ({
        ...addr,
        diaChiDayDu: addressService.formatFullAddress(addr),
      }))

      console.log('✅ Loaded saved addresses:', savedAddresses.value.length)

      if (!reorderPrefilled.value) {
        const defaultAddress = savedAddresses.value.find((addr) => addr.macDinh)
        if (defaultAddress) {
          console.log('🎯 Auto-selecting default address:', defaultAddress.hoTen)
          await selectAddress(defaultAddress)
        }
      }
    } else {
      // Tài khoản không phải khách hàng (Admin/Staff)
      savedAddresses.value = []
    }
  } catch (error) {
    console.error('Error loading saved addresses:', error)
    savedAddresses.value = []
  } finally {
    loadingAddresses.value = false
  }
}

// Select a saved address
const selectAddress = async (address) => {
  console.log('📍 Selecting address:', address.hoTen)
  selectedAddressId.value = address.id

  // Fill shipping info with selected address
  shippingInfo.value.fullName = address.hoTen
  shippingInfo.value.phone = address.soDienThoai
  shippingInfo.value.address = address.diaChi1

  // Fill email from user if available
  if (!shippingInfo.value.email && userStore.userEmail) {
    shippingInfo.value.email = userStore.userEmail
  }

  // IMPORTANT: Set province/district/ward text values first (for validation)
  shippingInfo.value.province = address.tinhThanh
  shippingInfo.value.district = address.quanHuyen
  shippingInfo.value.ward = address.phuongXa

  console.log('🔍 Looking for province:', address.tinhThanh)

  // Find and set province using helper
  const province = addressService.findProvinceInGHN(address.tinhThanh, provinces.value)

  if (province) {
    console.log('✅ Found province:', province.ProvinceName)
    selectedProvince.value = province.ProvinceID

    // Update shippingInfo with province name
    shippingInfo.value.province = province.ProvinceName
    shippingInfo.value.provinceId = province.ProvinceID

    await loadDistricts(province.ProvinceID)

    console.log('🔍 Looking for district:', address.quanHuyen)

    // Find and set district using helper
    const district = addressService.findDistrictInGHN(address.quanHuyen, districts.value)

    if (district) {
      console.log('✅ Found district:', district.DistrictName)
      selectedDistrict.value = district.DistrictID

      // Update shippingInfo with district name
      shippingInfo.value.district = district.DistrictName
      shippingInfo.value.districtId = district.DistrictID

      await loadWards(district.DistrictID)
      await loadServices(district.DistrictID)

      console.log('🔍 Looking for ward:', address.phuongXa)

      // Find and set ward using helper
      const ward = addressService.findWardInGHN(address.phuongXa, wards.value)

      if (ward) {
        console.log('✅ Found ward:', ward.WardName)
        selectedWard.value = ward.WardCode

        // Update shippingInfo with ward name
        shippingInfo.value.ward = ward.WardName
        shippingInfo.value.wardCode = ward.WardCode

        // Auto calculate shipping fee
        console.log('💰 Auto-calculating shipping fee...')
        console.log('⚖️ Total weight:', totalWeight.value)
        console.log('💵 Total value:', total.value)

        try {
          const result = await calculateShippingFee({
            totalWeight: totalWeight.value,
            insuranceValue: total.value,
          })

          if (result && result.success) {
            console.log('✅ Shipping fee calculated:', result.shippingFee)
          } else {
            console.warn('⚠️ Failed to calculate shipping fee:', result?.error)
          }
        } catch (error) {
          console.error('❌ Error calculating shipping fee:', error)
        }
      } else {
        console.warn('⚠️ Ward not found:', address.phuongXa)
        console.warn(
          'Available wards:',
          wards.value.map((w) => w.WardName),
        )
        // Giữ lại giá trị ward từ địa chỉ đã lưu để validation vẫn pass
        shippingInfo.value.ward = address.phuongXa
      }
    } else {
      console.warn('⚠️ District not found:', address.quanHuyen)
      console.warn(
        'Available districts:',
        districts.value.map((d) => d.DistrictName),
      )
      // Giữ lại giá trị district từ địa chỉ đã lưu để validation vẫn pass
      shippingInfo.value.district = address.quanHuyen
      shippingInfo.value.ward = address.phuongXa
    }
  } else {
    console.warn('⚠️ Province not found:', address.tinhThanh)
    console.warn(
      'Available provinces:',
      provinces.value.slice(0, 5).map((p) => p.ProvinceName),
    )
    // Giữ lại giá trị province/district/ward từ địa chỉ đã lưu để validation vẫn pass
    shippingInfo.value.province = address.tinhThanh
    shippingInfo.value.district = address.quanHuyen
    shippingInfo.value.ward = address.phuongXa
  }
}

// Select manual input
const selectManualInput = () => {
  selectedAddressId.value = null
  // Clear form
  shippingInfo.value = {
    fullName: userStore.userName || '',
    email: userStore.userEmail || '',
    phone: userStore.userPhone || '',
    address: '',
    notes: '',
  }
  selectedProvince.value = null
  selectedDistrict.value = null
  selectedWard.value = null
}

// UI state (removed showBreakdown as no longer needed)

// Calculate total weight from cart items - ONLY selected items
const totalWeight = computed(() => {
  if (!items.value || items.value.length === 0) {
    return 500 // Default 500g if cart is empty
  }

  // Chỉ tính trọng lượng của sản phẩm được chọn (selected !== false)
  const selectedItems = items.value.filter((item) => item.selected !== false)

  if (selectedItems.length === 0) {
    return 500 // Default 500g if no items selected
  }

  return selectedItems.reduce((sum, item) => {
    const weight = item?.weight || 500 // Default 500g if not specified
    const quantity = item?.quantity || 1
    return sum + weight * quantity
  }, 0)
})

// Format weight helper
const formatWeight = (grams) => {
  if (grams >= 1000) {
    return `${(grams / 1000).toFixed(1)}kg`
  }
  return `${grams}g`
}

const loadReorderPayload = () => {
  try {
    const raw = localStorage.getItem(REORDER_STORAGE_KEY)
    if (!raw) {
      return null
    }
    const payload = JSON.parse(raw)
    if (!payload || typeof payload !== 'object' || !payload.shipping) {
      localStorage.removeItem(REORDER_STORAGE_KEY)
      return null
    }
    if (payload.createdAt && Date.now() - payload.createdAt > MAX_REORDER_STORAGE_AGE) {
      localStorage.removeItem(REORDER_STORAGE_KEY)
      return null
    }
    return payload
  } catch (error) {
    console.error('❌ [SHIPPING FORM] Không thể đọc dữ liệu mua lại:', error)
    localStorage.removeItem(REORDER_STORAGE_KEY)
    return null
  }
}

const applyReorderShippingIfNeeded = async () => {
  const payload = loadReorderPayload()
  if (!payload?.shipping) {
    return
  }

  const shipping = payload.shipping
  reorderPrefilled.value = true
  selectedAddressId.value = null

  if (shippingInfo.value) {
    if (shipping.fullName) {
      shippingInfo.value.fullName = shipping.fullName
    }
    if (shipping.phone) {
      shippingInfo.value.phone = shipping.phone
    }
    if (shipping.email) {
      shippingInfo.value.email = shipping.email
    }
    if (shipping.addressLine) {
      shippingInfo.value.address = shipping.addressLine
    }
    if (shipping.note) {
      shippingInfo.value.notes = shipping.note
    }
  }

  let provinceMatched = false
  if (shipping.provinceName) {
    const province = addressService.findProvinceInGHN(shipping.provinceName, provinces.value)
    if (province) {
      selectedProvince.value = province.ProvinceID
      if (shippingInfo.value) {
        shippingInfo.value.province = province.ProvinceName
        shippingInfo.value.provinceId = province.ProvinceID
      }
      await loadDistricts(province.ProvinceID, true)
      provinceMatched = true
    }
  }

  let districtMatched = false
  if (provinceMatched && shipping.districtName) {
    const district = addressService.findDistrictInGHN(shipping.districtName, districts.value)
    if (district) {
      selectedDistrict.value = district.DistrictID
      if (shippingInfo.value) {
        shippingInfo.value.district = district.DistrictName
        shippingInfo.value.districtId = district.DistrictID
      }
      await loadWards(district.DistrictID, true)
      await loadServices(district.DistrictID)
      districtMatched = true
    }
  }

  if (districtMatched && shipping.wardName) {
    const ward = addressService.findWardInGHN(shipping.wardName, wards.value)
    if (ward) {
      selectedWard.value = ward.WardCode
      if (shippingInfo.value) {
        shippingInfo.value.ward = ward.WardName
        shippingInfo.value.wardCode = ward.WardCode
      }
    }
  }

  if (selectedDistrict.value) {
    try {
      await loadServices(selectedDistrict.value)
    } catch (error) {
      console.error('❌ [SHIPPING FORM] Lỗi load dịch vụ giao hàng khi mua lại:', error)
    }
  }

  if (selectedWard.value && selectedDistrict.value) {
    try {
      await calculateShippingFee({
        totalWeight: totalWeight.value,
        insuranceValue: total.value,
      })
    } catch (error) {
      console.error('❌ [SHIPPING FORM] Lỗi tính phí vận chuyển khi mua lại:', error)
    }
  }

  localStorage.removeItem(REORDER_STORAGE_KEY)

  if (window.$toast) {
    window.$toast.success('Đã điền sẵn thông tin giao hàng từ đơn hàng trước', 'Mua lại')
  }
}

const onProvinceChange = async () => {
  if (selectedProvince.value) {
    await loadDistricts(selectedProvince.value)

    // Update shippingInfo với tên tỉnh/thành và ID
    const province = provinces.value.find((p) => p.ProvinceID === selectedProvince.value)
    if (province && shippingInfo.value) {
      shippingInfo.value.province = province.ProvinceName
      shippingInfo.value.provinceId = province.ProvinceID
    }
  }
}

const onDistrictChange = async () => {
  console.log('🔔 onDistrictChange called, selectedDistrict:', selectedDistrict.value)
  if (selectedDistrict.value) {
    await loadWards(selectedDistrict.value)

    // Load available services cho district này
    console.log('📞 Calling loadServices with districtId:', selectedDistrict.value)
    try {
      await loadServices(selectedDistrict.value)
      console.log('✅ loadServices completed')
    } catch (error) {
      console.error('❌ loadServices failed:', error)
    }

    // Update shippingInfo với tên quận/huyện và ID
    const district = districts.value.find((d) => d.DistrictID === selectedDistrict.value)
    if (district && shippingInfo.value) {
      shippingInfo.value.district = district.DistrictName
      shippingInfo.value.districtId = district.DistrictID
    }
  }
}

const onWardChange = async () => {
  if (selectedWard.value && selectedDistrict.value) {
    // Update shippingInfo với tên phường/xã và WardCode
    const ward = wards.value.find((w) => w.WardCode === selectedWard.value)
    if (ward && shippingInfo.value) {
      shippingInfo.value.ward = ward.WardName
      shippingInfo.value.wardCode = ward.WardCode
    }

    // LOG THÔNG TIN TRƯỚC KHI TÍNH PHÍ
    console.log('=== onWardChange - CALCULATING SHIPPING FEE ===')
    console.log('🛒 Cart items:', items.value.length)
    console.log('✅ Selected items:', items.value.filter((item) => item.selected !== false).length)
    console.log('⚖️ Total weight:', totalWeight.value, 'grams')
    console.log('💰 Total value:', total.value, '₫')
    console.log(
      '📦 Items detail:',
      items.value.map((item) => ({
        name: item.productName,
        selected: item.selected,
        weight: item.weight || 500,
        quantity: item.quantity,
      })),
    )

    // Auto calculate shipping fee when address is complete
    try {
      const result = await calculateShippingFee({
        totalWeight: totalWeight.value,
        insuranceValue: total.value,
      })

      if (!result.success) {
        console.warn('⚠️ Cannot calculate shipping fee:', result.error)
        // Error message đã được set trong errors.calculating
      }
    } catch (error) {
      console.error('❌ Error calculating shipping fee:', error)
      // Error message đã được set trong errors.calculating
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

    await applyReorderShippingIfNeeded()

    // Load saved addresses for authenticated users
    if (userStore.isAuthenticated) {
      await loadSavedAddresses()
    }

    // Auto-fill user information if authenticated (only if no saved address selected)
    if (userStore.isAuthenticated && !selectedAddressId.value && !reorderPrefilled.value) {
      console.log('🔑 User authenticated, auto-filling shipping info...')

      // Only fill if fields are empty (don't overwrite existing data)
      if (!shippingInfo.value.fullName && userStore.userName) {
        shippingInfo.value.fullName = userStore.userName
      }
      if (!shippingInfo.value.email && userStore.userEmail) {
        shippingInfo.value.email = userStore.userEmail
      }
      if (!shippingInfo.value.phone && userStore.userPhone) {
        shippingInfo.value.phone = userStore.userPhone
      }

      console.log('✅ Auto-filled shipping info:', {
        fullName: shippingInfo.value.fullName,
        email: shippingInfo.value.email,
        phone: shippingInfo.value.phone,
      })
    } else {
      if (reorderPrefilled.value) {
        console.log('ℹ️ Đã áp dụng thông tin giao hàng từ đơn mua lại, bỏ qua auto-fill mặc định')
      } else {
        console.log('ℹ️ User not authenticated, skipping auto-fill')
      }
    }

    console.log('✅ ShippingForm mounted successfully')
  } catch (error) {
    console.error('❌ Error loading provinces on mount:', error)
  }
})

// Watch for cart changes and recalculate shipping
watch(
  [totalWeight, total],
  async () => {
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
  },
  { immediate: false },
)

const syncProvinceInfo = () => {
  if (!shippingInfo.value) {
    return
  }

  const provinceId = selectedProvince.value
  if (!provinceId) {
    shippingInfo.value.province = ''
    shippingInfo.value.provinceId = null
    return
  }

  const province = provinces.value.find((p) => p.ProvinceID === provinceId)
  if (province) {
    shippingInfo.value.province = province.ProvinceName
    shippingInfo.value.provinceId = province.ProvinceID
  }
}

const syncDistrictInfo = () => {
  if (!shippingInfo.value) {
    return
  }

  const districtId = selectedDistrict.value
  if (!districtId) {
    shippingInfo.value.district = ''
    shippingInfo.value.districtId = null
    shippingInfo.value.ward = ''
    shippingInfo.value.wardCode = null
    return
  }

  const district = districts.value.find((d) => d.DistrictID === districtId)
  if (district) {
    shippingInfo.value.district = district.DistrictName
    shippingInfo.value.districtId = district.DistrictID
  }
}

const syncWardInfo = () => {
  if (!shippingInfo.value) {
    return
  }

  const wardCode = selectedWard.value
  if (!wardCode) {
    shippingInfo.value.ward = ''
    shippingInfo.value.wardCode = null
    return
  }

  const ward = wards.value.find((w) => w.WardCode === wardCode)
  if (ward) {
    shippingInfo.value.ward = ward.WardName
    shippingInfo.value.wardCode = ward.WardCode
  }
}

watch(selectedProvince, syncProvinceInfo)
watch(provinces, syncProvinceInfo)

watch(selectedDistrict, syncDistrictInfo)
watch(districts, syncDistrictInfo)

watch(selectedWard, syncWardInfo)
watch(wards, syncWardInfo)

// 🔥 FIX: Watch for authentication state changes
// Khi user login tại trang Checkout, tự động load addresses và auto-fill
watch(
  () => userStore.isAuthenticated,
  async (isAuthenticated, wasAuthenticated) => {
    // Chỉ xử lý khi chuyển từ false -> true (user vừa login)
    if (isAuthenticated && !wasAuthenticated) {
      console.log('🔑 [SHIPPING FORM] User just logged in, loading addresses...')

      try {
        // Đảm bảo provinces đã được load trước khi load addresses
        // (vì selectAddress cần provinces để map địa chỉ)
        if (provinces.value.length === 0) {
          console.log('📍 [SHIPPING FORM] Provinces not loaded yet, loading first...')
          await loadProvinces()
        }

        // Load saved addresses (sẽ tự động chọn địa chỉ mặc định nếu có)
        await loadSavedAddresses()

        // Auto-fill user info nếu chưa có địa chỉ được chọn
        // (loadSavedAddresses đã tự động chọn default address nếu có)
        if (!selectedAddressId.value && !reorderPrefilled.value) {
          console.log(
            '🔑 [SHIPPING FORM] No default address found, auto-filling basic user info...',
          )

          // Fill basic user info
          if (!shippingInfo.value.fullName && userStore.userName) {
            shippingInfo.value.fullName = userStore.userName
          }
          if (!shippingInfo.value.email && userStore.userEmail) {
            shippingInfo.value.email = userStore.userEmail
          }
          if (!shippingInfo.value.phone && userStore.userPhone) {
            shippingInfo.value.phone = userStore.userPhone
          }

          console.log('✅ [SHIPPING FORM] Auto-filled after login:', {
            fullName: shippingInfo.value.fullName,
            email: shippingInfo.value.email,
            phone: shippingInfo.value.phone,
          })
        } else if (selectedAddressId.value) {
          console.log('✅ [SHIPPING FORM] Default address auto-selected after login')
        }
      } catch (error) {
        console.error('❌ [SHIPPING FORM] Error handling post-login:', error)
      }
    }
  },
  { immediate: false },
)

// 🔥 FIX: Watch for user data changes (khi user info được update)
watch(
  () => userStore.user,
  (newUser, oldUser) => {
    // Chỉ xử lý khi user data thay đổi và user đã authenticated
    if (newUser && userStore.isAuthenticated && newUser !== oldUser) {
      console.log('👤 [SHIPPING FORM] User data updated, refreshing addresses...')

      // Reload addresses để đảm bảo có data mới nhất
      if (userStore.isAuthenticated) {
        loadSavedAddresses().catch((error) => {
          console.error('❌ [SHIPPING FORM] Error reloading addresses:', error)
        })
      }
    }
  },
  { immediate: false },
)
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
  background: linear-gradient(135deg, #b8860b 0%, #daa520 100%);
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
  border-color: #b8860b !important;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1) !important;
  outline: none !important;
}

.form-control:hover,
.form-select:hover {
  border-color: #b8860b !important;
}

/* Ensure all form controls have visible borders */
.form-control:not(:focus):not(:hover),
.form-select:not(:focus):not(:hover) {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

/* Force border visibility for all states */
input[type='text'],
input[type='email'],
input[type='tel'],
textarea,
select {
  border: 2px solid #e9ecef !important;
  background-color: white !important;
}

input[type='text']:focus,
input[type='email']:focus,
input[type='tel']:focus,
textarea:focus,
select:focus {
  border-color: #b8860b !important;
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

.shipping-info-note {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: #f0f7ff;
  border-top: 1px solid #d1e7ff;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.5;
}

.shipping-info-note i {
  color: #2196f3;
  font-size: 0.95rem;
  margin-top: 0.15rem;
  flex-shrink: 0;
}

@keyframes pulse {
  0%,
  100% {
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

/* Saved Addresses Styles */
.saved-addresses {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.address-option {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.address-option:hover {
  border-color: #b8860b;
  background: #fffbf0;
}

.address-option.selected {
  border-color: #b8860b;
  background: #fff5e6;
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1);
}

.address-radio {
  display: flex;
  align-items: flex-start;
  padding-top: 0.25rem;
}

.address-radio input[type='radio'] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #b8860b;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.address-header .badge {
  font-size: 0.7rem;
  padding: 0.25rem 0.5rem;
}

.address-details {
  font-size: 0.875rem;
  color: #666;
}

.address-details > div {
  margin-bottom: 0.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.address-details i {
  color: #b8860b;
  font-size: 0.9rem;
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
