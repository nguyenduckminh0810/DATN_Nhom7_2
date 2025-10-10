import { ref } from 'vue'

// Voucher store để chia sẻ thông tin voucher giữa các components
const selectedVoucher = ref(null)
const selectedVoucherId = ref(null)
const manualVoucherCode = ref('')
const voucherMessage = ref(null)

export function useVoucher() {

  // Function để chọn voucher
  const selectVoucher = (voucher) => {
    // Nếu voucher đã được chọn, bỏ chọn
    if (selectedVoucher.value && selectedVoucher.value.id === voucher.id) {
      selectedVoucher.value = null
      selectedVoucherId.value = null
      manualVoucherCode.value = ''
      
      voucherMessage.value = {
        type: 'info',
        text: `Đã bỏ chọn voucher ${voucher.code}`
      }
    } else {
      // Chọn voucher mới
      selectedVoucher.value = voucher
      selectedVoucherId.value = voucher.id
      manualVoucherCode.value = voucher.code
      
      voucherMessage.value = {
        type: 'success',
        text: `Đã chọn voucher ${voucher.code}`
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
  const applyVoucher = () => {
    if (manualVoucherCode.value.trim()) {
      voucherMessage.value = {
        type: 'success',
        text: `Đã áp dụng mã ${manualVoucherCode.value}`
      }
    } else {
      voucherMessage.value = {
        type: 'error',
        text: 'Vui lòng nhập mã giảm giá'
      }
    }
  }

  return {
    selectedVoucher,
    selectedVoucherId,
    manualVoucherCode,
    voucherMessage,
    selectVoucher,
    clearVoucherSelection,
    applyVoucher
  }
}
