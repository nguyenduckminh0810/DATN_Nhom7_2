import { ref, computed } from 'vue'
import apiService from '@/services/api'

// Voucher store để chia sẻ thông tin voucher giữa các components
const selectedVoucher = ref(null)
const selectedVoucherId = ref(null)
const manualVoucherCode = ref('')
const voucherMessage = ref(null)
const availableVouchers = ref([])
const loading = ref(false)

export function useVoucher() {

  // Function để load vouchers từ API
  const loadVouchers = async () => {
    try {
      loading.value = true
      const response = await apiService.voucher.getAvailable()
      availableVouchers.value = response.data || []
    } catch (error) {
      console.error('Lỗi khi tải voucher:', error)
      voucherMessage.value = {
        type: 'error',
        text: 'Không thể tải danh sách voucher'
      }
    } finally {
      loading.value = false
    }
  }

  // Function để kiểm tra voucher
  const checkVoucher = async (code, khachHangId, donHangTong, phiVanChuyen) => {
    try {
      const response = await apiService.voucher.kiemTra({
        maVoucher: code,
        khachHangId: khachHangId,
        donHangTong: donHangTong,
        phiVanChuyen: phiVanChuyen
      })
      
      if (response.success) {
        return { success: true, data: response.data }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Lỗi khi kiểm tra voucher:', error)
      return { success: false, message: 'Lỗi khi kiểm tra voucher' }
    }
  }

  // Function để áp dụng voucher
  const applyVoucherToOrder = async (code, khachHangId, donHangTong, phiVanChuyen) => {
    try {
      const response = await apiService.voucher.apDung({
        maVoucher: code,
        khachHangId: khachHangId,
        donHangTong: donHangTong,
        phiVanChuyen: phiVanChuyen
      })
      
      if (response.success) {
        return { success: true, data: response.data }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('Lỗi khi áp dụng voucher:', error)
      return { success: false, message: 'Lỗi khi áp dụng voucher' }
    }
  }

  // Function để chọn voucher
  const selectVoucher = (voucher) => {
    // Nếu voucher đã được chọn, bỏ chọn
    if (selectedVoucher.value && selectedVoucher.value.id === voucher.id) {
      selectedVoucher.value = null
      selectedVoucherId.value = null
      manualVoucherCode.value = ''
      
      voucherMessage.value = {
        type: 'info',
        text: `Đã bỏ chọn voucher ${voucher.ma}`
      }
    } else {
      // Chọn voucher mới
      selectedVoucher.value = voucher
      selectedVoucherId.value = voucher.id
      manualVoucherCode.value = voucher.ma
      
      voucherMessage.value = {
        type: 'success',
        text: `Đã chọn voucher ${voucher.ma}`
      }
    }
  }

  // Function để clear voucher selection
  const clearVoucherSelection = () => {
    selectedVoucher.value = null
    selectedVoucherId.value = null
    manualVoucherCode.value = ''
    
    voucherMessage.value = {
      type: 'info',
      text: 'Đã bỏ chọn tất cả voucher'
    }
  }

  // Function để áp dụng voucher manual
  const applyVoucher = async (khachHangId, donHangTong, phiVanChuyen) => {
    if (!manualVoucherCode.value.trim()) {
      voucherMessage.value = {
        type: 'error',
        text: 'Vui lòng nhập mã giảm giá'
      }
      return
    }

    try {
      loading.value = true
      const result = await checkVoucher(manualVoucherCode.value, khachHangId, donHangTong, phiVanChuyen)
      
      if (result.success) {
        selectedVoucher.value = result.data
        voucherMessage.value = {
          type: 'success',
          text: `Voucher ${manualVoucherCode.value} hợp lệ!`
        }
      } else {
        voucherMessage.value = {
          type: 'error',
          text: result.message || 'Voucher không hợp lệ'
        }
      }
    } catch (error) {
      voucherMessage.value = {
        type: 'error',
        text: 'Lỗi khi kiểm tra voucher'
      }
    } finally {
      loading.value = false
    }
  }

  // Computed để lấy vouchers có thể sử dụng
  const eligibleVouchers = computed(() => {
    return availableVouchers.value.filter(voucher => {
      // Có thể thêm logic kiểm tra điều kiện ở đây
      return true
    })
  })

  return {
    selectedVoucher,
    selectedVoucherId,
    manualVoucherCode,
    voucherMessage,
    availableVouchers,
    loading,
    selectVoucher,
    clearVoucherSelection,
    applyVoucher,
    loadVouchers,
    checkVoucher,
    applyVoucherToOrder,
    eligibleVouchers
  }
}
