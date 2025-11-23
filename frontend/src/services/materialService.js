// src/services/materialService.js
import api from './api'

export default {
  // Lấy tất cả chất liệu
  getAll: async () => {
    try {
      const response = await api.get('/chat-lieu')
      console.log('📡 Material API response:', response)
      console.log('📡 Material API response type:', typeof response)
      console.log('📡 Material API response is array:', Array.isArray(response))
      
      // API service đã trả về response.data rồi, nên response ở đây đã là data
      // Nếu response là array thì trả về luôn
      if (Array.isArray(response)) {
        return response
      }
      
      // Nếu response có data property
      if (response && Array.isArray(response.data)) {
        return response.data
      }
      
      // Nếu response có content property (pagination)
      if (response && Array.isArray(response.content)) {
        return response.content
      }
      
      // Nếu không phải array, trả về mảng rỗng hoặc wrap vào array
      return Array.isArray(response) ? response : []
    } catch (error) {
      console.error('❌ Material API error:', error)
      console.error('❌ Error response:', error.response)
      console.error('❌ Error message:', error.message)
      throw error
    }
  },

  // Lấy chất liệu theo ID
  getById: (id) => api.get(`/chat-lieu/${id}`),

  // Thêm chất liệu mới
  create: (data) => api.post('/chat-lieu', data),

  // Cập nhật chất liệu
  update: (id, data) => api.put(`/chat-lieu/${id}`, data),

  // Xóa chất liệu
  delete: (id) => api.delete(`/chat-lieu/${id}`)
}

