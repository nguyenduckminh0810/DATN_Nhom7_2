import api from '@/services/api'

const SanPhamService = {
  // Thêm sản phẩm
  async themSanPham(payload) {
    return await api.post('/api/admin/san-pham', payload)
  },

  // Lấy danh sách sản phẩm
  async layDanhSach() {
    return await api.get('/api/admin/san-pham')
  },

  // Lấy sản phẩm theo id
  async layTheoId(id) {
    return await api.get(`/api/admin/san-pham/${id}`)
  },

  // Cập nhật sản phẩm
  async capNhat(id, payload) {
    return await api.put(`/api/admin/san-pham/${id}`, payload)
  },

  // Xóa sản phẩm
  async xoa(id) {
    return await api.delete(`/api/admin/san-pham/${id}`)
  },
}

export default SanPhamService
