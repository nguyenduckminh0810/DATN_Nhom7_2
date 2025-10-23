// src/services/colorService.js
import api from './api'

export default {
  // Lấy tất cả màu sắc
  getAll: () => api.get('/mau-sac'),

  // Thêm màu mới
  create: (data) => api.post('/mau-sac', data),

  // Xóa màu
  delete: (id) => api.delete(`/mau-sac/${id}`)
}
