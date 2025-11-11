// src/services/colorService.js
import api from './api'

export default {
  // Lấy tất cả màu sắc
  getAll: async () => {
    try {
      const response = await api.get('/mau-sac')
      console.log('📡 Color API response:', response)
      return response
    } catch (error) {
      console.error('❌ Color API error:', error)
      throw error
    }
  },

  // Thêm màu mới
  create: (data) => api.post('/mau-sac', data),

  // Xóa màu
  delete: (id) => api.delete(`/mau-sac/${id}`)
}

