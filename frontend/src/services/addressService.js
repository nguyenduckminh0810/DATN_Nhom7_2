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
      console.log('📍 Default address response:', response)
      return response // Return full ApiResponse
    } catch (error) {
      // Nếu không có địa chỉ mặc định, return null
      if (error.response?.status === 404) {
        console.log('ℹ️ No default address found')
        return null
      }
      console.error('❌ Error getting default address:', error)
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

  /**
   * Format địa chỉ đầy đủ từ object địa chỉ
   */
  formatFullAddress(address) {
    if (!address) return ''
    
    const parts = [
      address.diaChi1,
      address.phuongXa,
      address.quanHuyen && address.quanHuyen !== 'N/A' ? address.quanHuyen : null,
      address.tinhThanh,
    ].filter(Boolean)
    
    return parts.join(', ')
  },

  /**
   * Normalize text để so sánh (bỏ dấu, lowercase)
   */
  normalizeText(text) {
    if (!text) return ''
    return text.toLowerCase().trim()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "") // Bỏ dấu tiếng Việt
  },

  /**
   * Tìm province trong danh sách GHN provinces
   */
  findProvinceInGHN(provinceName, ghnProvinces) {
    if (!provinceName || !ghnProvinces || ghnProvinces.length === 0) return null
    
    const normalized = this.normalizeText(provinceName)
    
    return ghnProvinces.find((p) => 
      this.normalizeText(p.ProvinceName) === normalized ||
      p.ProvinceName === provinceName
    )
  },

  /**
   * Tìm district trong danh sách GHN districts
   */
  findDistrictInGHN(districtName, ghnDistricts) {
    if (!districtName || !ghnDistricts || ghnDistricts.length === 0) return null
    
    const normalized = this.normalizeText(districtName)
    
    return ghnDistricts.find((d) => 
      this.normalizeText(d.DistrictName) === normalized ||
      d.DistrictName === districtName
    )
  },

  /**
   * Tìm ward trong danh sách GHN wards
   */
  findWardInGHN(wardName, ghnWards) {
    if (!wardName || !ghnWards || ghnWards.length === 0) return null
    
    const normalized = this.normalizeText(wardName)
    
    return ghnWards.find((w) => 
      this.normalizeText(w.WardName) === normalized ||
      w.WardName === wardName
    )
  },
}

export default addressService
