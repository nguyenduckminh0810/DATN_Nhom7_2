// src/services/sanPhamService.js
import api from './api'

export default {
  // Lấy tất cả sản phẩm (phân trang)
  page: (params) => api.get('/san-pham', { params }),

  // Lấy theo ID
  get: (id) => api.get(`/san-pham/id/${id}`),

  // Lấy theo SLUG sản phẩm
  getBySlug: (slug) => api.get(`/san-pham/slug/${slug}`),

  getByProductSlug: (slug) => api.get(`/san-pham/slug/${slug}`),
  // Lấy theo SLUG danh mục
  getByCategorySlug: (slug, params = {}) =>
    api.get(`/san-pham/danh-muc/${slug}`, { params }),

  // CRUD
  create: (data) => api.post('/san-pham', data),
  update: (id, data) => api.put(`/san-pham/id/${id}`, data),
  delete: (id) => api.delete(`/san-pham/id/${id}`)
}
