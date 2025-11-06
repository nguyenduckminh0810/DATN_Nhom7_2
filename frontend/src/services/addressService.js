import api from './api'

/**
 * Service để quản lý địa chỉ giao hàng
 */
const addressService = {
  /**
   * Lấy tất cả địa chỉ của khách hàng
   */
  async getAllAddresses() {
    const response = await api.get('/dia-chi')
    return response // Return the full ApiResponse object {success, message, data}
  },

  /**
   * Lấy địa chỉ mặc định
   */
  async getDefaultAddress() {
    try {
      const response = await api.get('/dia-chi/mac-dinh')
      return response // Return full ApiResponse
    } catch (error) {
      // Nếu không có địa chỉ mặc định, return null
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Lấy chi tiết một địa chỉ
   */
  async getAddressById(id) {
    const response = await api.get(`/dia-chi/${id}`)
    return response // Return full ApiResponse
  },

  /**
   * Thêm địa chỉ mới
   */
  async createAddress(addressData) {
    console.log('addressService.createAddress called with:', addressData) // Debug log
    try {
      const response = await api.post('/dia-chi', addressData)
      console.log('addressService.createAddress response:', response) // Debug log
      return response // Return full ApiResponse
    } catch (error) {
      console.error('addressService.createAddress error:', error) // Debug log
      throw error
    }
  },

  /**
   * Cập nhật địa chỉ
   */
  async updateAddress(id, addressData) {
    console.log('addressService.updateAddress called with:', id, addressData) // Debug log
    try {
      const response = await api.put(`/dia-chi/${id}`, addressData)
      console.log('addressService.updateAddress response:', response) // Debug log
      return response // Return full ApiResponse
    } catch (error) {
      console.error('addressService.updateAddress error:', error) // Debug log
      throw error
    }
  },

  /**
   * Đặt địa chỉ làm mặc định
   */
  async setDefaultAddress(id) {
    const response = await api.patch(`/dia-chi/${id}/mac-dinh`)
    return response // Return full ApiResponse
  },

  /**
   * Xóa địa chỉ
   */
  async deleteAddress(id) {
    const response = await api.delete(`/dia-chi/${id}`)
    return response // Return full ApiResponse
  },
}

export default addressService
