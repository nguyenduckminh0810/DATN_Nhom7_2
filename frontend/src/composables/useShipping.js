import { ref, reactive, computed, watch, onMounted } from 'vue'
import shippingService from '@/services/shippingService'

const SHIPPING_STORAGE_KEY = 'auro_shipping_state'

// Helper function to save shipping state to localStorage
const saveShippingState = (state) => {
  try {
    localStorage.setItem(SHIPPING_STORAGE_KEY, JSON.stringify(state))
  } catch (error) {}
}

// Helper function to load shipping state from localStorage
const loadShippingState = () => {
  try {
    const saved = localStorage.getItem(SHIPPING_STORAGE_KEY)
    if (saved) {
      const state = JSON.parse(saved)
      return state
    }
  } catch (error) {}
  return null
}

export function useShipping() {
  const provinces = ref([])
  const districts = ref([])
  const wards = ref([])
  const services = ref([])

  const selectedProvince = ref(null)
  const selectedDistrict = ref(null)
  const selectedWard = ref(null)
  const selectedService = ref(null)

  const shippingFee = ref(0)
  const expectedDeliveryTime = ref(null)

  const loading = reactive({
    provinces: false,
    districts: false,
    wards: false,
    services: false,
    calculating: false,
  })

  const errors = reactive({
    provinces: null,
    districts: null,
    wards: null,
    services: null,
    calculating: null,
  })

  const hasSelectedAddress = computed(() => {
    return selectedProvince.value && selectedDistrict.value && selectedWard.value
  })

  const canCalculateFee = computed(() => {
    return hasSelectedAddress.value && selectedService.value
  })

  const formattedShippingFee = computed(() => {
    return shippingService.formatCurrency(shippingFee.value)
  })

  const loadProvinces = async () => {
    loading.provinces = true
    errors.provinces = null
    try {
      const result = await shippingService.getProvinces()
      
      provinces.value = result
      
      // Restore shipping state from localStorage SAU KHI provinces đã load
      const savedState = loadShippingState()
      if (savedState) {
        
        // Kiểm tra xem state có quá cũ không (> 1 giờ)
        const isExpired = Date.now() - (savedState.timestamp || 0) > 60 * 60 * 1000
        
        if (!isExpired) {
          // CHỈ RESTORE ĐỊA CHỈ, KHÔNG RESTORE PHÍ (sẽ tính lại)
          selectedProvince.value = savedState.selectedProvince
          selectedDistrict.value = savedState.selectedDistrict
          selectedWard.value = savedState.selectedWard
          selectedService.value = savedState.selectedService
          // KHÔNG restore shippingFee và expectedDeliveryTime - để tính lại
          
          
          // Load districts và wards nếu có saved state (skipReset = true để giữ nguyên selected values)
          if (savedState.selectedProvince) {
            await loadDistricts(savedState.selectedProvince, true)
          }
          if (savedState.selectedDistrict) {
            await loadWards(savedState.selectedDistrict, true)
            await loadServices(savedState.selectedDistrict)
          }
          
          // TỰ ĐỘNG TÍNH LẠI PHÍ nếu đã có đầy đủ địa chỉ
          if (savedState.selectedProvince && savedState.selectedDistrict && savedState.selectedWard) {
            try {
              await calculateShippingFee({
                totalWeight: 500, // Default weight, sẽ được override khi ShippingForm tính lại
                insuranceValue: 0
              })
            } catch (error) {
            }
          }
        } else {
          localStorage.removeItem(SHIPPING_STORAGE_KEY)
        }
      }
    } catch (error) {
      errors.provinces = error.message
    } finally {
      loading.provinces = false
    }
  }

  const loadDistricts = async (provinceId, skipReset = false) => {
    if (!provinceId) {
      districts.value = []
      return
    }
    loading.districts = true
    errors.districts = null
    
    // Chỉ reset nếu không phải restore state
    if (!skipReset) {
      selectedDistrict.value = null
      selectedWard.value = null
      wards.value = []
    }
    
    try {
      districts.value = await shippingService.getDistricts(provinceId)
    } catch (error) {
      errors.districts = error.message
    } finally {
      loading.districts = false
    }
  }

  const loadWards = async (districtId, skipReset = false) => {
    if (!districtId) {
      wards.value = []
      return
    }
    loading.wards = true
    errors.wards = null
    
    // Chỉ reset nếu không phải restore state
    if (!skipReset) {
      selectedWard.value = null
    }
    
    try {
      wards.value = await shippingService.getWards(districtId)
    } catch (error) {
      errors.wards = error.message
    } finally {
      loading.wards = false
    }
  }

  const loadServices = async (toDistrictId) => {
    if (!toDistrictId) {
      services.value = []
      return
    }
    loading.services = true
    errors.services = null
    try {
      services.value = await shippingService.getServices(toDistrictId)
      if (services.value.length > 0 && !selectedService.value) {
        const expressService = services.value.find(
          (s) => s.service_id === shippingService.SERVICE_IDS.EXPRESS,
        )
        selectedService.value = expressService
          ? expressService.service_id
          : services.value[0].service_id
      }
    } catch (error) {
      errors.services = error.message
    } finally {
      loading.services = false
    }
  }

  const calculateShippingFee = async (params) => {
    const { totalWeight, insuranceValue, serviceId } = params
    if (!selectedDistrict.value || !selectedWard.value) {
      errors.calculating = 'Vui long chon day du dia chi giao hang'
      return null
    }
    
    
    let effectiveServiceId = serviceId || selectedService.value
    
    // Nếu không có service, thử với danh sách service phổ biến
    const fallbackServices = [
      53320, // Express
      53321, // Standard  
      100039, // Nhanh
      2 // Tiêu chuẩn
    ]
    
    // Debug removed
    
    
    loading.calculating = true
    errors.calculating = null
    
    // Hàm helper để thử tính phí với 1 service ID
    const tryCalculateWithService = async (svcId) => {
      try {
        const response = await shippingService.calculateShippingFee({
          toDistrictId: selectedDistrict.value,
          toWardCode: selectedWard.value,
          totalWeight,
          insuranceValue,
          serviceId: svcId,
        })
        
        if (response && response.total) {
          return { success: true, data: response, serviceId: svcId }
        }
        return { success: false }
      } catch (error) {
        return { success: false, error: error.message }
      }
    }
    
    try {
      let result = null
      
      // Thử với service hiện tại trước
      if (effectiveServiceId) {
        result = await tryCalculateWithService(effectiveServiceId)
      }
      
      // Nếu fail, thử lần lượt các service fallback
      if (!result || !result.success) {
        
        for (const fallbackSvcId of fallbackServices) {
          if (fallbackSvcId === effectiveServiceId) continue // Đã thử rồi
          
          result = await tryCalculateWithService(fallbackSvcId)
          if (result.success) {
            // Cập nhật selectedService với service thành công
            selectedService.value = result.serviceId
            break
          }
        }
      }
      
      // Nếu vẫn không thành công sau khi thử hết
      if (!result || !result.success) {
        const errorMsg = 'GHN không hỗ trợ giao hàng đến địa chỉ này. Vui lòng chọn địa chỉ khác.'
        errors.calculating = errorMsg
        console.error('❌ All services failed for this address')
        return { success: false, error: errorMsg }
      }
      
      // Success - Lưu kết quả
      const response = result.data
      shippingFee.value = response.total || 0
      expectedDeliveryTime.value = response.expected_delivery_time || null
      
      // Lưu shipping state vào localStorage
      saveShippingState({
        selectedProvince: selectedProvince.value,
        selectedDistrict: selectedDistrict.value,
        selectedWard: selectedWard.value,
        selectedService: selectedService.value,
        shippingFee: shippingFee.value,
        expectedDeliveryTime: expectedDeliveryTime.value,
        timestamp: Date.now()
      })
      
      return { success: true, shippingFee: shippingFee.value, expectedDeliveryTime: expectedDeliveryTime.value }
      
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message
      errors.calculating = errorMessage
      
      // KIỂM TRA LỖI "ROUTE NOT FOUND SERVICE"
      if (errorMessage.includes('route not found service')) {
        errors.calculating = 'GHN không hỗ trợ giao hàng đến địa chỉ này. Vui lòng chọn địa chỉ khác.'
      }
      
      return { success: false, error: errors.calculating }
    } finally {
      loading.calculating = false
    }
  }

  const calculateShippingFeeFull = async (params) => {
    if (!selectedDistrict.value || !selectedWard.value) {
      errors.calculating = 'Vui long chon day du dia chi giao hang'
      return null
    }
    loading.calculating = true
    errors.calculating = null
    try {
      const response = await shippingService.calculateShippingFeeFull({
        ...params,
        toDistrictId: selectedDistrict.value,
        toWardCode: selectedWard.value,
        serviceId: params.serviceId || selectedService.value,
      })
      if (response.success) {
        shippingFee.value = response.shippingFee || 0
        expectedDeliveryTime.value = response.expectedDeliveryTime || null
        return response
      } else {
        throw new Error(response.message || 'Khong the tinh phi van chuyen')
      }
    } catch (error) {
      errors.calculating = error.message
      throw error
    } finally {
      loading.calculating = false
    }
  }

  const reset = () => {
    provinces.value = []
    districts.value = []
    wards.value = []
    services.value = []
    selectedProvince.value = null
    selectedDistrict.value = null
    selectedWard.value = null
    selectedService.value = null
    shippingFee.value = 0
    expectedDeliveryTime.value = null
    Object.keys(loading).forEach((key) => {
      loading[key] = false
    })
    Object.keys(errors).forEach((key) => {
      errors[key] = null
    })
  }

  const resetFee = () => {
    shippingFee.value = 0
    expectedDeliveryTime.value = null
    errors.calculating = null
  }

  const setAddress = async (address) => {
    if (address.provinceId) {
      selectedProvince.value = address.provinceId
      await loadDistricts(address.provinceId)
    }
    if (address.districtId) {
      selectedDistrict.value = address.districtId
      await loadWards(address.districtId)
      await loadServices(address.districtId)
    }
    if (address.wardCode) {
      selectedWard.value = address.wardCode
    }
  }

  const getAddress = () => {
    return {
      provinceId: selectedProvince.value,
      districtId: selectedDistrict.value,
      wardCode: selectedWard.value,
      provinceName: provinces.value.find((p) => p.ProvinceID === selectedProvince.value)?.ProvinceName,
      districtName: districts.value.find((d) => d.DistrictID === selectedDistrict.value)?.DistrictName,
      wardName: wards.value.find((w) => w.WardCode === selectedWard.value)?.WardName,
    }
  }

  const validateAddress = (address) => {
    return shippingService.validateAddress(address)
  }

  // Không auto-calculate nữa vì đã có restore state
  watch([selectedProvince, selectedDistrict, selectedWard], ([province, district, ward]) => {
    if (!province || !district || !ward) {
      // Reset fee khi bỏ chọn địa chỉ
      shippingFee.value = 0
      expectedDeliveryTime.value = null
    }
    // Không auto-calculate ở đây vì sẽ gây duplicate call
    // Calculate được trigger từ onWardChange trong ShippingForm.vue
  })

  return {
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
    calculateShippingFeeFull,
    reset,
    resetFee,
    setAddress,
    getAddress,
    validateAddress,
    formatCurrency: shippingService.formatCurrency,
    formatWeight: shippingService.formatWeight,
    getServiceName: shippingService.getServiceName,
    SERVICE_IDS: shippingService.SERVICE_IDS,
  }
}
