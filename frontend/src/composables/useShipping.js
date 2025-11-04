import { ref, reactive, computed, watch } from 'vue'
import shippingService from '@/services/shippingService'

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
      console.log('🚀 [useShipping] Calling GHN API to load provinces...')
      const result = await shippingService.getProvinces()
      console.log('📦 [useShipping] Raw result from shippingService:', result)
      console.log('📊 [useShipping] Result type:', typeof result, 'Is Array:', Array.isArray(result))
      
      provinces.value = result
      console.log('✅ [useShipping] Provinces stored in state:', provinces.value.length, 'items')
      console.log('📋 [useShipping] First 3 provinces:', provinces.value.slice(0, 3))
    } catch (error) {
      errors.provinces = error.message
      console.error('❌ [useShipping] Error loading provinces:', error)
      console.error('❌ [useShipping] Error stack:', error.stack)
    } finally {
      loading.provinces = false
      console.log('🏁 [useShipping] loadProvinces completed. Total provinces:', provinces.value.length)
    }
  }

  const loadDistricts = async (provinceId) => {
    if (!provinceId) {
      districts.value = []
      return
    }
    loading.districts = true
    errors.districts = null
    selectedDistrict.value = null
    selectedWard.value = null
    wards.value = []
    try {
      console.log('Calling GHN API to load districts for province:', provinceId)
      districts.value = await shippingService.getDistricts(provinceId)
      console.log('Loaded districts from GHN API:', districts.value.length)
    } catch (error) {
      errors.districts = error.message
      console.error('Error loading districts:', error)
    } finally {
      loading.districts = false
    }
  }

  const loadWards = async (districtId) => {
    if (!districtId) {
      wards.value = []
      return
    }
    loading.wards = true
    errors.wards = null
    selectedWard.value = null
    try {
      console.log('Calling GHN API to load wards for district:', districtId)
      wards.value = await shippingService.getWards(districtId)
      console.log('Loaded wards from GHN API:', wards.value.length)
    } catch (error) {
      errors.wards = error.message
      console.error('Error loading wards:', error)
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
      console.log('Calling GHN API to load services for district:', toDistrictId)
      services.value = await shippingService.getServices(toDistrictId)
      console.log('Loaded services from GHN API:', services.value.length)
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
      console.error('Error loading services:', error)
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
    const effectiveServiceId = serviceId || selectedService.value || 53320
    loading.calculating = true
    errors.calculating = null
    try {
      console.log('Calling GHN API to calculate shipping fee...')
      const response = await shippingService.calculateShippingFee({
        toDistrictId: selectedDistrict.value,
        toWardCode: selectedWard.value,
        totalWeight,
        insuranceValue,
        serviceId: effectiveServiceId,
      })
      if (response.success) {
        shippingFee.value = response.shippingFee || 0
        expectedDeliveryTime.value = response.expectedDeliveryTime || null
        console.log('Calculated shipping fee from GHN API:', shippingFee.value)
        return response
      } else {
        throw new Error(response.message || 'Khong the tinh phi van chuyen')
      }
    } catch (error) {
      errors.calculating = error.message
      console.error('Error calculating shipping fee:', error)
      throw error
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
      console.log('Calling GHN API to calculate full shipping fee...')
      const response = await shippingService.calculateShippingFeeFull({
        ...params,
        toDistrictId: selectedDistrict.value,
        toWardCode: selectedWard.value,
        serviceId: params.serviceId || selectedService.value,
      })
      if (response.success) {
        shippingFee.value = response.shippingFee || 0
        expectedDeliveryTime.value = response.expectedDeliveryTime || null
        console.log('Calculated full shipping fee from GHN API:', shippingFee.value)
        return response
      } else {
        throw new Error(response.message || 'Khong the tinh phi van chuyen')
      }
    } catch (error) {
      errors.calculating = error.message
      console.error('Error calculating full shipping fee:', error)
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

  watch([selectedProvince, selectedDistrict, selectedWard], ([province, district, ward]) => {
    if (province && district && ward) {
      console.log('Auto-calculating shipping fee for:', { province, district, ward })
      calculateShippingFee({ totalWeight: 500, insuranceValue: 0 })
    } else {
      shippingFee.value = 0
      expectedDeliveryTime.value = null
    }
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
