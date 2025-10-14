import apiService  from "./api";

class VoucherService {
    //Lấy voucher
    async getAvailable() {
        try {
            const response = await apiService.voucher.getAvailable()
            return {
                success: true,
                data: response.data || response,
                message: 'Lấy danh sách voucher thành công'
            }
        } catch (error) {
            return {
                success: false,
                data: null,
                message: error.message || 'Lỗi khi lấy danh sách voucher'
            }
        }
    }
    // Áp dụng voucher
    async apply(voucherData) {
        try {
          const response = await apiService.voucher.apDung(voucherData)
          return {
            success: true,
            data: response.data || response,
            message: response.message || 'Áp dụng voucher thành công'
          }
        } catch (error) {
          return {
            success: false,
            data: null,
            message: error.message || 'Lỗi khi áp dụng voucher'
          }
        }
      }
    
      // Kiểm tra voucher
      async check(voucherData) {
        try {
          const response = await apiService.voucher.kiemTra(voucherData)
          return {
            success: true,
            data: response.data || response,
            message: response.message || 'Kiểm tra voucher thành công'
          }
        } catch (error) {
          return {
            success: false,
            data: null,
            message: error.message || 'Lỗi khi kiểm tra voucher'
          }
        }
      }
    }
    
    const voucherService = new VoucherService()
    export default voucherService

