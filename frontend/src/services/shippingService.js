import api from './api'

/**
 * Service xử lý các API liên quan đến vận chuyển GHN (Giao Hàng Nhanh)
 * Format giống cartService.js
 */
const shippingService = {
  /**
   * Lấy danh sách tỉnh/thành phố
   */
  async getProvinces() {
    try {
      const response = await api.shipping.getProvinces()
      
      // api.shipping.getProvinces() trả về response.data rồi
      // Backend format: { success, message, data }
      if (response && response.success && response.data) {
        return response.data
      } else {
        console.warn('⚠️ [ShippingService] Unexpected response:', response)
        return []
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tải danh sách tỉnh/thành phố')
    }
  },

  /**
   * Lấy danh sách quận/huyện theo tỉnh
   */
  async getDistricts(provinceId) {
    if (!provinceId) {
      throw new Error('Province ID is required')
    }

    try {
      const response = await api.shipping.getDistricts(provinceId)
      
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tải danh sách quận/huyện')
    }
  },

  /**
   * Lấy danh sách phường/xã theo quận/huyện
   */
  async getWards(districtId) {
    if (!districtId) {
      throw new Error('District ID is required')
    }

    try {
      const response = await api.shipping.getWards(districtId)
      
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tải danh sách phường/xã')
    }
  },

  /**
   * Lấy danh sách dịch vụ vận chuyển khả dụng
   */
  async getServices(toDistrictId) {
    if (!toDistrictId) {
      throw new Error('To District ID is required')
    }

    try {
      const response = await api.shipping.getServices(toDistrictId)
      
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tải danh sách dịch vụ')
    }
  },

  /**
   * Tính phí vận chuyển
   */
  async calculateShippingFee(data) {
    try {
      const response = await api.shipping.calculate(data)
      
      if (response && response.success && response.data) {
        return response.data
      } else {
        throw new Error(response?.message || 'Không thể tính phí vận chuyển')
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tính phí vận chuyển')
    }
  },

  /**
   * Tính phí vận chuyển (full response)
   */
  async calculateShippingFeeFull(data) {
    try {
      const response = await api.shipping.calculateFull(data)
      
      if (response && response.success && response.data) {
        return response.data
      } else {
        throw new Error(response?.message || 'Không thể tính phí vận chuyển')
      }
    } catch (error) {
      throw new Error(error.message || 'Không thể tính phí vận chuyển')
    }
  },

  /**
   * Tìm kiếm địa chỉ
   */
  searchAddress(query, items, searchField = 'name') {
    if (!query || !items || items.length === 0) {
      return items || []
    }

    const searchTerm = query.toLowerCase().trim()
    return items.filter((item) => {
      const value = item[searchField]
      return value && value.toLowerCase().includes(searchTerm)
    })
  },

  /**
   * Format tiền tệ
   */
  formatCurrency(amount) {
    if (!amount) return '0đ'
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount)
  },

  /**
   * Format trọng lượng
   */
  formatWeight(grams) {
    if (!grams) return '0g'
    if (grams >= 1000) {
      return `${(grams / 1000).toFixed(1)}kg`
    }
    return `${grams}g`
  },

  /**
   * Get service name
   */
  getServiceName(serviceId) {
    const services = {
      53320: 'Express - Giao nhanh 1-2 ngày',
      53321: 'Standard - Giao tiêu chuẩn 3-5 ngày',
    }
    return services[serviceId] || `Dịch vụ ${serviceId}`
  },

  /**
   * Validate thông tin địa chỉ
   */
  validateAddress(address) {
    const errors = []

    if (!address.fullName || address.fullName.trim().length === 0) {
      errors.push('Vui lòng nhập họ tên')
    }

    if (!address.phone || !/^[0-9]{10,11}$/.test(address.phone)) {
      errors.push('Số điện thoại không hợp lệ')
    }

    if (!address.address || address.address.trim().length === 0) {
      errors.push('Vui lòng nhập địa chỉ cụ thể')
    }

    if (!address.provinceId) {
      errors.push('Vui lòng chọn tỉnh/thành phố')
    }

    if (!address.districtId) {
      errors.push('Vui lòng chọn quận/huyện')
    }

    if (!address.wardCode) {
      errors.push('Vui lòng chọn phường/xã')
    }

    return {
      isValid: errors.length === 0,
      errors,
    }
  },

  /**
   * Service IDs
   */
  SERVICE_IDS: {
    EXPRESS: 53320,
    STANDARD: 53321,
  },

  /**
   * Get default service ID
   */
  getDefaultServiceId() {
    return this.SERVICE_IDS.EXPRESS
  },
}

export default shippingService
