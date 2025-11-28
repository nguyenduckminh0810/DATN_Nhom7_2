// src/services/sizeService.js
import api from './api'

export default {
  // Lấy tất cả kích cỡ
  getAll: async () => {
    try {
      const response = await api.get('/kich-co')
      return response
    } catch (error) {
      console.error('❌ Size API error:', error)
      throw error
    }
  },

  // Lấy kích cỡ theo ID
  getById: (id) => api.get(`/kich-co/${id}`),

  // Thêm kích cỡ mới
  create: (data) => api.post('/kich-co', data),

  // Cập nhật kích cỡ
  update: (id, data) => api.put(`/kich-co/${id}`, data),

  // Xóa kích cỡ
  delete: (id) => api.delete(`/kich-co/${id}`)
}

