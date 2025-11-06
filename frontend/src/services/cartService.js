import api from './api'

/**
 * Giỏ hàng API Service
 * Kết nối với backend API để lưu giỏ hàng vào database
 */
const cartService = {
  /**
   * Xem giỏ hàng của người dùng (đã đăng nhập) hoặc session (guest)
   * GET /api/gio-hang
   */
  async getCart() {
    try {
      const response = await api.get('/gio-hang')
      return response // ✅ api.get() đã return response.data rồi
    } catch (error) {
      console.error('🔴 Error fetching cart:', error)
      throw error
    }
  },

  /**
   * Thêm sản phẩm vào giỏ hàng
   * POST /api/gio-hang/them
   * @param {Object} payload - { bienTheId: Long, soLuong: Integer }
   */
  async addToCart(payload) {
    try {
      const response = await api.post('/gio-hang/them', payload)
      return response // ✅ api.post() đã return response.data rồi, không cần .data nữa
    } catch (error) {
      console.error('🔴 CartService.addToCart error:', error)
      throw error
    }
  },

  /**
   * Cập nhật số lượng sản phẩm trong giỏ
   * PUT /api/gio-hang/chi-tiet/{id}?soLuong={soLuong}
   * @param {Long} chiTietId - ID của GioHangChiTiet
   * @param {Integer} soLuong - Số lượng mới
   */
  async updateQuantity(chiTietId, soLuong) {
    try {
      const response = await api.put(`/gio-hang/chi-tiet/${chiTietId}`, null, {
        params: { soLuong }
      })
      return response // ✅ api.put() đã return response.data rồi
    } catch (error) {
      console.error('Error updating quantity:', error)
      throw error
    }
  },

  /**
   * Xóa sản phẩm khỏi giỏ hàng
   * DELETE /api/gio-hang/chi-tiet/{id}
   * @param {Long} chiTietId - ID của GioHangChiTiet
   */
  async removeFromCart(chiTietId) {
    try {
      const response = await api.delete(`/gio-hang/chi-tiet/${chiTietId}`)
      return response // ✅ api.delete() đã return response.data rồi
    } catch (error) {
      console.error('Error removing from cart:', error)
      throw error
    }
  },

  /**
   * Xóa toàn bộ giỏ hàng
   * DELETE /api/gio-hang/clear
   */
  async clearCart() {
    try {
      const response = await api.delete('/gio-hang/clear')
      return response // ✅ api.delete() đã return response.data rồi
    } catch (error) {
      console.error('Error clearing cart:', error)
      throw error
    }
  },

  /**
   * Đồng bộ giỏ hàng từ localStorage vào database
   * Gọi khi người dùng vừa đăng nhập
   * @param {Array} localCartItems - Các items từ localStorage
   */
  async syncLocalCart(localCartItems) {
    try {
      const addPromises = localCartItems.map(item => {
        // Chuyển đổi format từ localStorage sang format API
        const payload = {
          bienTheId: item.variantId || item.bienTheId,
          soLuong: item.quantity || 1
        }
        return this.addToCart(payload)
      })

      await Promise.all(addPromises)
    } catch (error) {
      console.error('❌ Error syncing local cart:', error)
      throw error
    }
  },

  /**
   * Tạo đơn hàng từ giỏ hàng (cho người dùng đã đăng nhập)
   * POST /api/khach-hang/don-hang/tao-tu-gio-hang
   * @param {Object} payload - Thông tin đơn hàng
   */
  async createOrderFromCart(payload) {
    try {
      const response = await api.post('/khach-hang/don-hang/tao-tu-gio-hang', payload)
      return response // ✅ api.post() đã return response.data rồi
    } catch (error) {
      console.error('Error creating order from cart:', error)
      throw error
    }
  },

  /**
   * Tạo đơn hàng guest (không đăng nhập)
   * POST /api/khach-hang/don-hang/guest-checkout
   * @param {Object} payload - Thông tin đơn hàng guest
   */
  async guestCheckout(payload) {
    try {
      const response = await api.post('/khach-hang/don-hang/guest-checkout', payload)
      return response // ✅ api.post() đã return response.data rồi
    } catch (error) {
      console.error('Error guest checkout:', error)
      throw error
    }
  }
}

export default cartService
