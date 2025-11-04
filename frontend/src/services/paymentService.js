import apiService from './api'

/**
 * Service xử lý thanh toán VNPay
 */
class PaymentService {
  /**
   * Tạo URL thanh toán VNPay
   * @param {Object} data - Thông tin thanh toán
   * @param {number} data.donHangId - ID đơn hàng
   * @param {number} data.soTien - Số tiền thanh toán
   * @param {string} data.moTa - Mô tả giao dịch
   * @returns {Promise<Object>} Response chứa paymentUrl
   */
  async taoUrlThanhToan(data) {
    try {
      const response = await apiService.post('/payment/create', {
        donHangId: data.donHangId,
        soTien: data.soTien,
        moTa: data.moTa || `Thanh toán đơn hàng #${data.donHangId}`
      })

      if (response.status === 'success') {
        return {
          success: true,
          paymentUrl: response.paymentUrl,
          txnRef: response.txnRef,
          message: response.message
        }
      } else {
        return {
          success: false,
          message: response.message || 'Không thể tạo URL thanh toán'
        }
      }
    } catch (error) {
      console.error('❌ Error creating VNPay payment URL:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi kết nối server'
      }
    }
  }

  /**
   * Kiểm tra trạng thái thanh toán
   * @param {number} donHangId - ID đơn hàng
   * @returns {Promise<Object>} Thông tin trạng thái thanh toán
   */
  async kiemTraTrangThaiThanhToan(donHangId) {
    try {
      const response = await apiService.get(`/payment/status/${donHangId}`)
      
      return {
        success: true,
        data: response
      }
    } catch (error) {
      console.error('❌ Error checking payment status:', error)
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi kiểm tra trạng thái'
      }
    }
  }

  /**
   * Parse VNPay return URL parameters
   * @param {string} url - URL callback từ VNPay
   * @returns {Object} Parsed parameters
   */
  parseVNPayReturnUrl(url) {
    const params = {}
    const urlObj = new URL(url)
    
    for (const [key, value] of urlObj.searchParams.entries()) {
      params[key] = value
    }
    
    return params
  }

  /**
   * Lấy thông báo lỗi theo mã response code
   * @param {string} responseCode - Mã lỗi VNPay
   * @returns {string} Thông báo lỗi
   */
  getErrorMessage(responseCode) {
    const errorMessages = {
      '00': 'Giao dịch thành công',
      '07': 'Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo)',
      '09': 'Thẻ/Tài khoản chưa đăng ký InternetBanking',
      '10': 'Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần',
      '11': 'Đã hết hạn chờ thanh toán (15 phút)',
      '12': 'Thẻ/Tài khoản bị khóa',
      '13': 'Nhập sai mật khẩu xác thực giao dịch (OTP)',
      '24': 'Khách hàng hủy giao dịch',
      '51': 'Tài khoản không đủ số dư',
      '65': 'Tài khoản đã vượt quá hạn mức giao dịch trong ngày',
      '75': 'Ngân hàng thanh toán đang bảo trì',
      '79': 'Nhập sai mật khẩu thanh toán quá số lần quy định',
      'default': 'Giao dịch không thành công'
    }
    
    return errorMessages[responseCode] || errorMessages['default']
  }

  /**
   * Format số tiền VNĐ
   * @param {number} amount - Số tiền
   * @returns {string} Số tiền đã format
   */
  formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  }
}

// Export singleton instance
const paymentService = new PaymentService()
export default paymentService
