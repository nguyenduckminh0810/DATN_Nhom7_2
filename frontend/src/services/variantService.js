// src/services/variantService.js
import api from './api'

export default {
  // Lấy danh sách biến thể của sản phẩm
  getBySanPham: (sanPhamId) => api.get(`/bien-the/san-pham/${sanPhamId}`),

  // Tạo hoặc cập nhật nhiều biến thể cho sản phẩm
  upsertVariants: (sanPhamId, data) => api.post(`/bien-the/san-pham/${sanPhamId}`, data),

  // Lấy chi tiết một biến thể
  getById: (id) => api.get(`/bien-the/${id}`),

  // Cập nhật tồn kho của một biến thể
  updateStock: (id, stock) => api.patch(`/bien-the/${id}/stock?stock=${stock}`),

  // Xóa một biến thể
  delete: (id) => api.delete(`/bien-the/${id}`),

  // Xóa tất cả biến thể của sản phẩm
  deleteAllBySanPham: (sanPhamId) => api.delete(`/bien-the/san-pham/${sanPhamId}`),

  // Lấy tất cả biến thể với thông tin sản phẩm (cho trang inventory)
  getAllInventoryItems: () => api.get('/bien-the/inventory/all'),

  // Legacy method for compatibility
  upsert: (productId, data) => api.post(`/bien-the/san-pham/${productId}`, data),
}

