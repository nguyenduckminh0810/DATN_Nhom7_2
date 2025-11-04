import api from './api'

/**
 * Service xử lý các API liên quan đến vận chuyển GHN (Giao Hàng Nhanh)
 */
const shippingService = {
  /**
   * Lấy danh sách tỉnh/thành phố
   * @returns {Promise<Array>} Danh sách tỉnh/thành phố
   * @example
   * const provinces = await shippingService.getProvinces()
   * // [{ ProvinceID: 202, ProvinceName: "Hồ Chí Minh", Code: "SGN" }, ...]
   */
  async getProvinces() {
    try {
      console.log('🌐 [ShippingService] Fetching provinces from backend...')
      const response = await api.shipping.getProvinces()
      console.log('📦 [ShippingService] Backend response:', response)
      
      // api.shipping.getProvinces() đã return response.data rồi
      // Backend trả về { success, message, data }
      if (response && response.success && response.data) {
        console.log('✅ [ShippingService] Provinces data:', response.data.length, 'items')
        return response.data
      } else {
        console.warn('⚠️ [ShippingService] Unexpected response format:', response)
        return []
      }
    } catch (error) {
      console.error('❌ [ShippingService] Error fetching provinces:', error)
      throw new Error(error.message || 'Không thể tải danh sách tỉnh/thành phố')
    }
  },

  /**
   * Lấy danh sách quận/huyện theo tỉnh
   * @param {number} provinceId - ID của tỉnh/thành phố
   * @returns {Promise<Array>} Danh sách quận/huyện
   * @example
   * const districts = await shippingService.getDistricts(202)
   * // [{ DistrictID: 1542, DistrictName: "Quận 1", ProvinceID: 202 }, ...]
   */
  async getDistricts(provinceId) {
    if (!provinceId) {
      throw new Error('Province ID is required')
    }

    try {
      const response = await apiService.get('/shipping/districts', {
        params: { provinceId },
      })
      
      // Backend trả về { success, message, data }
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      console.error('Error fetching districts:', error)
      throw new Error(error.message || 'Không thể tải danh sách quận/huyện')
    }
  }

  /**
   * Lấy danh sách phường/xã theo quận/huyện
   * @param {number} districtId - ID của quận/huyện
   * @returns {Promise<Array>} Danh sách phường/xã
   * @example
   * const wards = await shippingService.getWards(1542)
   * // [{ WardCode: "21211", WardName: "Phường Bến Nghé", DistrictID: 1542 }, ...]
   */
  async getWards(districtId) {
    if (!districtId) {
      throw new Error('District ID is required')
    }

    try {
      const response = await apiService.get('/shipping/wards', {
        params: { districtId },
      })
      
      // Backend trả về { success, message, data }
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      console.error('Error fetching wards:', error)
      throw new Error(error.message || 'Không thể tải danh sách phường/xã')
    }
  }

  /**
   * Lấy danh sách dịch vụ vận chuyển khả dụng
   * @param {number} toDistrictId - ID quận/huyện đích
   * @returns {Promise<Array>} Danh sách dịch vụ
   * @example
   * const services = await shippingService.getServices(1542)
   * // [{ service_id: 53320, short_name: "Express", service_type_id: 2 }, ...]
   */
  async getServices(toDistrictId) {
    if (!toDistrictId) {
      throw new Error('To District ID is required')
    }

    try {
      const response = await apiService.get('/shipping/services', {
        params: { toDistrictId },
      })
      
      // Backend trả về { success, message, data }
      if (response && response.success && response.data) {
        return response.data
      } else {
        return []
      }
    } catch (error) {
      console.error('Error fetching services:', error)
      throw new Error(error.message || 'Không thể tải danh sách dịch vụ vận chuyển')
    }
  }

  /**
   * Tính phí vận chuyển đơn giản
   * @param {Object} data - Thông tin tính phí
   * @param {number} data.toDistrictId - ID quận/huyện đích
   * @param {string} data.toWardCode - Mã phường/xã đích
   * @param {number} data.totalWeight - Tổng khối lượng (gram)
   * @param {number} data.insuranceValue - Giá trị bảo hiểm (VNĐ)
   * @param {number} data.serviceId - Mã dịch vụ (53320: Express, 53321: Standard)
   * @returns {Promise<Object>} Thông tin phí vận chuyển
   * @example
   * const fee = await shippingService.calculateShippingFee({
   *   toDistrictId: 1542,
   *   toWardCode: "21211",
   *   totalWeight: 500,
   *   insuranceValue: 500000,
   *   serviceId: 53320
   * })
   * // { success: true, shippingFee: 25000, serviceFee: 25000, ... }
   */
  async calculateShippingFee(data) {
    const { toDistrictId, toWardCode, totalWeight, insuranceValue, serviceId } = data

    // Validation
    if (!toDistrictId) throw new Error('To District ID is required')
    if (!toWardCode) throw new Error('To Ward Code is required')
    if (!totalWeight || totalWeight <= 0) throw new Error('Total Weight must be greater than 0')
    if (insuranceValue === undefined || insuranceValue < 0) {
      throw new Error('Insurance Value is required')
    }
    if (!serviceId) throw new Error('Service ID is required')

    try {
      const response = await apiService.post('/shipping/calculate', {
        toDistrictId,
        toWardCode,
        totalWeight,
        insuranceValue,
        serviceId,
      })

      return response
    } catch (error) {
      console.error('Error calculating shipping fee:', error)
      throw new Error(error.message || 'Không thể tính phí vận chuyển')
    }
  }

  /**
   * Tính phí vận chuyển chi tiết (có kích thước hàng)
   * @param {Object} data - Thông tin tính phí chi tiết
   * @param {number} data.fromDistrictId - ID quận/huyện gửi
   * @param {string} data.fromWardCode - Mã phường/xã gửi
   * @param {number} data.toDistrictId - ID quận/huyện đích
   * @param {string} data.toWardCode - Mã phường/xã đích
   * @param {number} data.weight - Khối lượng (gram)
   * @param {number} data.length - Chiều dài (cm)
   * @param {number} data.width - Chiều rộng (cm)
   * @param {number} data.height - Chiều cao (cm)
   * @param {number} data.insuranceValue - Giá trị bảo hiểm (VNĐ)
   * @param {number} data.serviceId - Mã dịch vụ
   * @param {number} [data.serviceTypeId] - Loại dịch vụ (optional)
   * @returns {Promise<Object>} Thông tin phí vận chuyển chi tiết
   */
  async calculateShippingFeeFull(data) {
    const {
      fromDistrictId,
      fromWardCode,
      toDistrictId,
      toWardCode,
      weight,
      length,
      width,
      height,
      insuranceValue,
      serviceId,
      serviceTypeId,
    } = data

    // Validation
    if (!fromDistrictId) throw new Error('From District ID is required')
    if (!fromWardCode) throw new Error('From Ward Code is required')
    if (!toDistrictId) throw new Error('To District ID is required')
    if (!toWardCode) throw new Error('To Ward Code is required')
    if (!weight || weight <= 0) throw new Error('Weight must be greater than 0')
    if (!length || length <= 0) throw new Error('Length must be greater than 0')
    if (!width || width <= 0) throw new Error('Width must be greater than 0')
    if (!height || height <= 0) throw new Error('Height must be greater than 0')
    if (insuranceValue === undefined || insuranceValue < 0) {
      throw new Error('Insurance Value is required')
    }
    if (!serviceId) throw new Error('Service ID is required')

    try {
      const response = await apiService.post('/shipping/calculate-full', {
        fromDistrictId,
        fromWardCode,
        toDistrictId,
        toWardCode,
        weight,
        length,
        width,
        height,
        insuranceValue,
        serviceId,
        serviceTypeId,
      })

      return response
    } catch (error) {
      console.error('Error calculating full shipping fee:', error)
      throw new Error(error.message || 'Không thể tính phí vận chuyển chi tiết')
    }
  }

  /**
   * Tìm địa chỉ theo từ khóa (helper function để search)
   * @param {string} query - Từ khóa tìm kiếm
   * @param {Array} items - Danh sách items cần search
   * @param {string} searchField - Trường cần search
   * @returns {Array} Kết quả tìm kiếm
   */
  searchAddress(query, items, searchField = 'name') {
    if (!query || !items || items.length === 0) return items

    const normalized = query.toLowerCase().trim()
    return items.filter((item) => {
      const value = item[searchField]
      return value && value.toLowerCase().includes(normalized)
    })
  }

  /**
   * Format số tiền VNĐ
   * @param {number} amount - Số tiền
   * @returns {string} Số tiền đã format
   */
  formatCurrency(amount) {
    if (!amount || isNaN(amount)) return '0₫'
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount)
  }

  /**
   * Format khối lượng (gram -> kg)
   * @param {number} grams - Khối lượng gram
   * @returns {string} Khối lượng đã format
   */
  formatWeight(grams) {
    if (!grams || isNaN(grams)) return '0g'
    if (grams >= 1000) {
      return `${(grams / 1000).toFixed(2)}kg`
    }
    return `${grams}g`
  }

  /**
   * Lấy tên dịch vụ theo service ID
   * @param {number} serviceId - ID dịch vụ
   * @returns {string} Tên dịch vụ
   */
  getServiceName(serviceId) {
    const services = {
      53320: 'Giao hàng nhanh (Express)',
      53321: 'Giao hàng tiêu chuẩn (Standard)',
    }
    return services[serviceId] || 'Dịch vụ vận chuyển'
  }

  /**
   * Validate địa chỉ giao hàng
   * @param {Object} address - Địa chỉ cần validate
   * @returns {Object} { valid: boolean, errors: Array }
   */
  validateAddress(address) {
    const errors = []

    if (!address.provinceId) {
      errors.push('Vui lòng chọn Tỉnh/Thành phố')
    }

    if (!address.districtId) {
      errors.push('Vui lòng chọn Quận/Huyện')
    }

    if (!address.wardCode) {
      errors.push('Vui lòng chọn Phường/Xã')
    }

    if (!address.street || address.street.trim().length < 5) {
      errors.push('Vui lòng nhập địa chỉ chi tiết (tối thiểu 5 ký tự)')
    }

    return {
      valid: errors.length === 0,
      errors,
    }
  }

  /**
   * Cache service IDs
   */
  SERVICE_IDS = {
    EXPRESS: 53320, // Giao nhanh 2-3 ngày
    STANDARD: 53321, // Giao tiêu chuẩn 3-5 ngày
  }

  /**
   * Get default service ID
   */
  getDefaultServiceId() {
    return this.SERVICE_IDS.EXPRESS
  }
}

// Export singleton instance
const shippingService = new ShippingService()
export default shippingService
