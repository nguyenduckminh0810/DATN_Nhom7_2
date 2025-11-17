import api from './api'
import cartService from './cartService'

/**
 * Order Service
 * Xử lý các API liên quan đến đơn hàng
 */
const orderService = {
  /**
   * Tạo đơn hàng từ giỏ hàng (cho người dùng đã đăng nhập)
   * POST /api/khach-hang/don-hang/tao-tu-gio-hang
   * @param {Object} orderData - Thông tin đơn hàng
   * @returns {Promise} Response từ server
   */
  async createOrderFromCart(orderData) {
    try {
      console.log('📦 Creating order from cart:', orderData)
      const response = await cartService.createOrderFromCart(orderData)
      console.log('✅ Order created:', response)
      return response
    } catch (error) {
      console.error('❌ Error creating order:', error)
      throw error
    }
  },

  /**
   * Tạo đơn hàng guest (không đăng nhập)
   * POST /api/khach-hang/don-hang/guest-checkout
   * @param {Object} guestOrderData - Thông tin đơn hàng guest
   * @returns {Promise} Response từ server
   */
  async guestCheckout(guestOrderData) {
    try {
      console.log('📦 Guest checkout:', guestOrderData)
      const response = await cartService.guestCheckout(guestOrderData)
      console.log('✅ Guest order created:', response)
      return response
    } catch (error) {
      console.error('❌ Error guest checkout:', error)
      throw error
    }
  },

  /**
   * Lấy danh sách đơn hàng
   * GET /api/khach-hang/don-hang/don-hang-cua-toi
   * @param {Object} params - Query parameters (page, size, status, etc.)
   * @returns {Promise} Danh sách đơn hàng
   */
  async getOrders(params = {}) {
    try {
      const response = await api.get('/khach-hang/don-hang/don-hang-cua-toi', { params })
      return response
    } catch (error) {
      console.error('Error fetching orders:', error)
      throw error
    }
  },

  /**
   * Lấy đơn hàng của tôi (alias của getOrders)
   * GET /api/khach-hang/don-hang/don-hang-cua-toi
   * @param {Object} params - Query parameters
   * @returns {Promise} Danh sách đơn hàng
   */
  async getMyOrders(params = {}) {
    return this.getOrders(params)
  },

  /**
   * Lấy chi tiết đơn hàng
   * GET /api/khach-hang/don-hang/{id}
   * @param {Number} orderId - ID đơn hàng
   * @returns {Promise} Chi tiết đơn hàng
   */
  async getOrderById(orderId) {
    try {
      const response = await api.get(`/khach-hang/don-hang/${orderId}`)
      return response
    } catch (error) {
      console.error('Error fetching order details:', error)
      throw error
    }
  },

  /**
   * Hủy đơn hàng
   * PUT /api/khach-hang/don-hang/{id}/huy
   * @param {Number} orderId - ID đơn hàng
   * @param {String} reason - Lý do hủy
   * @returns {Promise} Response từ server
   */
  async cancelOrder(orderId, reason = '') {
    try {
      const response = await api.put(`/khach-hang/don-hang/${orderId}/huy`, { reason })
      return response
    } catch (error) {
      console.error('Error canceling order:', error)
      throw error
    }
  },

  async submitReview(orderId, payload) {
    try {
      const response = await api.post(`/khach-hang/don-hang/${orderId}/danh-gia`, payload)
      return response
    } catch (error) {
      console.error('Error submitting review:', error)
      throw error
    }
  },

  /**
   * Kiểm tra trạng thái thanh toán VNPay
   * GET /api/payment/vnpay-return
   * @param {Object} params - Query parameters từ VNPay redirect
   * @returns {Promise} Kết quả thanh toán
   */
  async checkVNPayPayment(params) {
    try {
      const response = await api.get('/payment/vnpay-return', { params })
      return response
    } catch (error) {
      console.error('Error checking VNPay payment:', error)
      throw error
    }
  }
}

export default orderService
