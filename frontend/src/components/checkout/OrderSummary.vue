<template>
  <div class="order-summary-section">
    <div class="section-card">
      <!-- Header -->
      <div class="section-header">
        <div class="summary-header-layout">
          <div class="summary-header-left">
            <h5 class="mb-0 me-3">
              <i class="bi bi-receipt me-2"></i>Tóm tắt đơn hàng
            </h5>
            <span class="badge bg-white text-warning">
              {{ selectedItemsCount }} / {{ totalItemsCount }} sản phẩm
            </span>
          </div>
          <div class="summary-header-right">
            <!-- Optional: Add any summary-specific actions here if needed in the future -->
          </div>
        </div>
      </div>

      <!-- Summary Body -->
      <div class="section-body">
        <!-- Summary Items -->
        <div class="summary-section">
          <div class="summary-item">
            <span class="summary-label">Tạm tính:</span>
            <span class="summary-value">{{ formatPrice(subtotal) }}</span>
          </div>
          
          <div class="summary-item">
            <span class="summary-label">Phí vận chuyển:</span>
            <span class="summary-value">{{ formatPrice(shippingFee) }}</span>
          </div>
          
          <div class="summary-item voucher-item">
            <span class="summary-label">
              <i class="bi bi-ticket-perforated me-1"></i>
              <span v-if="selectedVoucher">Voucher {{ selectedVoucher.ma }}:</span>
              <span v-else-if="manualVoucherCode">Voucher {{ manualVoucherCode }}:</span>
              <span v-else>Voucher giảm giá:</span>
            </span>
            <span class="summary-value" :class="{ 'no-discount': discountAmount === 0 }">
              <span v-if="discountAmount > 0">-{{ formatPrice(discountAmount) }}</span>
              <span v-else>Chưa chọn</span>
            </span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-total">
            <span class="total-label">Tổng cộng:</span>
            <span class="total-value">{{ formatPrice(finalTotal) }}</span>
          </div>
        </div>

        <!-- Checkout Button -->
        <div class="checkout-section mt-4">
          <button class="btn btn-dark btn-lg w-100 checkout-btn" 
                  :disabled="selectedItemsCount === 0 || isProcessing"
                  @click="handleCheckout">
            <span v-if="isProcessing" class="spinner-border spinner-border-sm me-2" role="status"></span>
            <i v-else class="bi bi-credit-card me-2"></i>
            {{ isProcessing ? 'ĐANG XỬ LÝ...' : 'ĐẶT HÀNG' }}
          </button>
        </div>

        <!-- Security Info -->
        <div class="security-info mt-3">
          <div class="security-badges">
            <div class="security-badge">
              <i class="bi bi-shield-check"></i>
              <span>Bảo mật</span>
            </div>
            <div class="security-badge">
              <i class="bi bi-truck"></i>
              <span>Giao hàng nhanh</span>
            </div>
            <div class="security-badge">
              <i class="bi bi-arrow-clockwise"></i>
              <span>Đổi trả dễ dàng</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, ref } from 'vue'
import { useCart } from '@/composables/useCart'
import { useVoucher } from '@/stores/voucher'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import orderService from '@/services/orderService'
import cartService from '@/services/cartService'
import paymentService from '@/services/paymentService'

const { items, formatPrice, clearCart } = useCart()
const { selectedVoucher, manualVoucherCode } = useVoucher()
const userStore = useUserStore()
const router = useRouter()

// Computed để check authentication - phải dùng computed để reactive
const isAuthenticated = computed(() => userStore.isAuthenticated)

// Inject shipping composable từ parent (chứa toàn bộ state GHN)
const shipping = inject('shipping', null)

// Inject shipping info from parent (backward compatibility)
const shippingInfo = inject('shippingInfo', { 
  shippingFee: computed(() => 0), 
  expectedDeliveryTime: computed(() => null) 
})

// Inject form data from parent
const shippingFormData = inject('shippingFormData', ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  notes: ''
}))
const selectedPaymentMethod = inject('selectedPaymentMethod', ref(null))

// Processing state
const isProcessing = ref(false)

// Tính toán dựa trên real cart data
const totalItemsCount = computed(() => {
  return items.value.length
})

const selectedItemsCount = computed(() => {
  return items.value.filter(item => item.selected !== false).length
})

const subtotal = computed(() => {
  return items.value
    .filter(item => item.selected !== false)
    .reduce((total, item) => total + (item.price * item.quantity), 0)
})

// Sử dụng shipping fee từ GHN API
const shippingFee = computed(() => {
  // Luôn dùng phí ship từ GHN API (mặc định 0 nếu chưa tính)
  return shippingInfo.shippingFee.value || 0
})

// Tính toán giảm giá dựa trên voucher
const discountAmount = computed(() => {
  if (!selectedVoucher.value && !manualVoucherCode.value) {
    return 0
  }
  
  // Nếu có voucher được chọn từ list
  if (selectedVoucher.value) {
    const { loai, giaTri, donToiThieu, giamToiDa } = selectedVoucher.value
    
    // Kiểm tra điều kiện đơn hàng tối thiểu
    if (subtotal.value < (donToiThieu || 0)) {
      return 0
    }
    
    let discount = 0
    const loaiUc = String(loai || '').toUpperCase()
    if (loaiUc === 'PERCENT' || loaiUc === 'PHAN_TRAM' || loaiUc === 'GIAM_PHAN_TRAM') {
      discount = Math.floor(subtotal.value * giaTri / 100)
    } else if (loaiUc === 'FIXED' || loaiUc === 'SO_TIEN' || loaiUc === 'GIAM_SO_TIEN') {
      discount = Math.min(giaTri, subtotal.value) 
    } else if (loaiUc === 'FREESHIP') {
      const ship = Number(shippingFee.value || 0)
      const cap = giamToiDa != null && Number(giamToiDa) > 0 ? Number(giamToiDa) : ship
      discount = Math.min(ship, cap, subtotal.value)
    }
    
    return discount
  }
  
  // Nếu có voucher manual và đã được validate
  if (manualVoucherCode.value && manualVoucherCode.value.trim() && selectedVoucher.value) {
    const { loai, giaTri, donToiThieu, giamToiDa } = selectedVoucher.value
    
    // Kiểm tra điều kiện đơn hàng tối thiểu
    if (subtotal.value < (donToiThieu || 0)) {
      return 0
    }
    
    let discount = 0
    const loaiUc = String(loai || '').toUpperCase()
    if (loaiUc === 'PERCENT' || loaiUc === 'PHAN_TRAM' || loaiUc === 'GIAM_PHAN_TRAM') {
      discount = Math.floor(subtotal.value * giaTri / 100)
    } else if (loaiUc === 'FIXED' || loaiUc === 'SO_TIEN' || loaiUc === 'GIAM_SO_TIEN') {
      discount = Math.min(giaTri, subtotal.value)
    } else if (loaiUc === 'FREESHIP') {
      const ship = Number(shippingFee.value || 0)
      const cap = giamToiDa != null && Number(giamToiDa) > 0 ? Number(giamToiDa) : ship
      discount = Math.min(ship, cap, subtotal.value)
    }
    
    return discount
  }
  
  return 0
})

const finalTotal = computed(() => {
  return subtotal.value + shippingFee.value - discountAmount.value
})

// Validate form data trước khi đặt hàng
const validateCheckoutData = () => {
  if (!shippingFormData || !shippingFormData.value) {
    if (window.$toast) {
      window.$toast.error('Không tìm thấy thông tin giao hàng')
    } else {
      alert('Không tìm thấy thông tin giao hàng')
    }
    return false
  }

  const formData = shippingFormData.value

  // Validate Họ tên
  if (!formData.fullName || !formData.fullName.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập họ tên')
    } else {
      alert('Vui lòng nhập họ tên')
    }
    return false
  }

  // Validate Số điện thoại
  if (!formData.phone || !formData.phone.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập số điện thoại')
    } else {
      alert('Vui lòng nhập số điện thoại')
    }
    return false
  }
  
  // Validate format số điện thoại (hỗ trợ: 0xxxxxxxxx, +84xxxxxxxxx, 84xxxxxxxxx)
  // Loại bỏ khoảng trắng, dấu gạch ngang, dấu chấm
  const cleanPhone = formData.phone.trim().replace(/[\s\-\.]/g, '')
  
  // Regex hỗ trợ nhiều format:
  // - 0xxxxxxxxx (10-11 số)
  // - +84xxxxxxxxx (9-10 số sau +84)
  // - 84xxxxxxxxx (9-10 số sau 84)
  const phoneRegex = /^(\+?84|0)[0-9]{9,10}$/
  
  if (!phoneRegex.test(cleanPhone)) {
    if (window.$toast) {
      window.$toast.error('Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam hợp lệ')
    } else {
      alert('Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam hợp lệ')
    }
    return false
  }

  // Validate Địa chỉ
  if (!formData.address || !formData.address.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập địa chỉ giao hàng')
    } else {
      alert('Vui lòng nhập địa chỉ giao hàng')
    }
    return false
  }

  // Validate Province/District/Ward (cần thiết cho GHN API)
  if (!formData.province || !formData.district || !formData.ward) {
    if (window.$toast) {
      window.$toast.error('Vui lòng chọn đầy đủ Tỉnh/Quận/Phường')
    } else {
      alert('Vui lòng chọn đầy đủ Tỉnh/Quận/Phường')
    }
    return false
  }

  // Validate Email
  if (!formData.email || !formData.email.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập email')
    } else {
      alert('Vui lòng nhập email')
    }
    return false
  }
  
  // Validate format email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(formData.email.trim())) {
    if (window.$toast) {
      window.$toast.error('Email không hợp lệ. Vui lòng kiểm tra lại định dạng email')
    } else {
      alert('Email không hợp lệ. Vui lòng kiểm tra lại định dạng email')
    }
    return false
  }

  // Validate Phương thức thanh toán
  if (!selectedPaymentMethod || !selectedPaymentMethod.value) {
    if (window.$toast) {
      window.$toast.error('Vui lòng chọn phương thức thanh toán')
    } else {
      alert('Vui lòng chọn phương thức thanh toán')
    }
    return false
  }

  return true
}

// Helper function: Extract số ID từ bienTheId/variantId (có thể là string composite hoặc số)
const extractBienTheId = (value) => {
  if (!value) return null
  // Nếu đã là số → trả về
  if (typeof value === 'number') return value
  // Nếu là string → thử parse
  const str = String(value).trim()
  // Nếu có format composite như "1-#808080-M", lấy phần đầu (số)
  const match = str.match(/^(\d+)/)
  if (match) {
    const num = parseInt(match[1], 10)
    return isNaN(num) ? null : num
  }
  // Nếu string là số thuần → parse
  const num = parseInt(str, 10)
  return isNaN(num) ? null : num
}

// Xử lý đặt hàng
const handleCheckout = async () => {
  const token = localStorage.getItem('auro_token')
  
  if (isProcessing.value) {
    return
  }
  
  // Mark processing immediately to prevent multiple handlers passing validation
  isProcessing.value = true

  // Validate dữ liệu
  if (!validateCheckoutData()) {
    isProcessing.value = false
    return
  }

  try {
    // Lấy các sản phẩm đã chọn
    const selectedItems = items.value.filter(item => item.selected !== false)
    
    // ✅ Lưu danh sách ID các sản phẩm đã chọn để xóa sau khi đặt hàng thành công
    const selectedItemIds = selectedItems.map(item => item.id).filter(Boolean)


    if (selectedItems.length === 0) {
      if (window.$toast) {
        window.$toast.error('Vui lòng chọn sản phẩm để đặt hàng')
      }
      isProcessing.value = false
      return
    }

    // **QUAN TRỌNG**: Đồng bộ giỏ hàng với backend trước khi checkout
    // Nếu là guest (không có token), không cần sync cart với backend
    if (!token) {
      console.log('� Guest user - skipping backend cart sync')
    } else {
      // ✅ KHÔNG XÓA các sản phẩm không được chọn - giữ lại trong giỏ hàng
      // User có thể đặt hàng các sản phẩm chưa chọn sau
      
      // Đảm bảo các sản phẩm được chọn có trong backend cart
      for (const item of selectedItems) {
        // Nếu item chưa có ID từ backend (local item), thêm vào backend
        if (!item.id && (item.bienTheId || item.variantId)) {
          try {
            const bienTheIdNum = extractBienTheId(item.bienTheId || item.variantId)
            if (bienTheIdNum) {
              await cartService.addToCart({
                bienTheId: bienTheIdNum,
                soLuong: item.quantity
              })
            }
          } catch (err) {
            // Không throw error, có thể item đã có trong backend
          }
        }
      }
    }

    try {
      const backendCart = await cartService.getCart()
      
      if (!backendCart || !backendCart.chiTietList || backendCart.chiTietList.length === 0) {
        try {
          for (const item of selectedItems) {
            const variantId = item.bienTheId || item.variantId
            const bienTheIdNum = extractBienTheId(variantId)
            if (bienTheIdNum) {
              await cartService.addToCart({ bienTheId: bienTheIdNum, soLuong: item.quantity || 1 })
            }
          }
          const reloaded = await cartService.getCart()
          if (!reloaded || !reloaded.chiTietList || reloaded.chiTietList.length === 0) {
            if (window.$toast) {
              window.$toast.error('Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi đặt hàng.')
            }
            isProcessing.value = false
            return
          }
        } catch (_) {
          if (window.$toast) {
            window.$toast.error('Không thể đồng bộ giỏ hàng. Vui lòng thử lại.')
          }
          isProcessing.value = false
          return
        }
      }
    } catch (error) {
      if (window.$toast) {
        window.$toast.error('Không thể tải giỏ hàng. Vui lòng thử lại.')
      }
      isProcessing.value = false
      return
    }

    let response

    if (token && isAuthenticated.value) {
      const maVoucherValue = selectedVoucher.value?.ma || manualVoucherCode.value || null
      const orderData = {
        hoTen: shippingFormData.value.fullName,
        email: shippingFormData.value.email,
        soDienThoai: shippingFormData.value.phone,
        diaChi: shippingFormData.value.address,
        phuongXa: shippingFormData.value.ward || '',
        quanHuyen: shippingFormData.value.district || '',
        tinhThanh: shippingFormData.value.province || '',
        phuongThucThanhToan: selectedPaymentMethod.value,
        ghiChu: shippingFormData.value.notes || '',
        maVoucher: maVoucherValue,
        // ✅ Gửi danh sách ID các chi tiết giỏ hàng đã chọn để backend chỉ xử lý các sản phẩm này
        selectedCartItemIds: selectedItemIds,
        // Thêm thông tin GHN để tính phí ship (nếu có)
        districtId: shipping?.selectedDistrict?.value || null,
        wardCode: shipping?.selectedWard?.value || null,
        serviceId: shipping?.selectedService?.value || null
      }
      
      try {
        response = await orderService.guestCheckout(orderData)
        
        if (window.$toast) {
          window.$toast.success('Đặt hàng thành công!', 'Cảm ơn bạn đã mua hàng')
        }
      } catch (error) {
        // Xử lý lỗi voucher từ backend
        const errorMessage = error?.data?.message || error?.message || 'Có lỗi xảy ra khi đặt hàng'
        if (window.$toast) {
          window.$toast.error(errorMessage, 'Đặt hàng thất bại')
        }
        throw error
      }
    } else {
      // Guest checkout (không có token)
      const maVoucherValue = selectedVoucher.value?.ma || manualVoucherCode.value || null
      const guestOrderData = {
        hoTen: shippingFormData.value.fullName,
        email: shippingFormData.value.email,
        soDienThoai: shippingFormData.value.phone,
        diaChi: shippingFormData.value.address,
        phuongXa: shippingFormData.value.ward || '',
        quanHuyen: shippingFormData.value.district || '',
        tinhThanh: shippingFormData.value.province || '',
        phuongThucThanhToan: selectedPaymentMethod.value,
        ghiChu: shippingFormData.value.notes || '',
        maVoucher: maVoucherValue,
        // ✅ Gửi danh sách ID các chi tiết giỏ hàng đã chọn để backend chỉ xử lý các sản phẩm này
        selectedCartItemIds: selectedItemIds,
        // Thêm thông tin GHN để tính phí ship (nếu có)
        districtId: shipping?.selectedDistrict?.value || null,
        wardCode: shipping?.selectedWard?.value || null,
        serviceId: shipping?.selectedService?.value || null
      }
      
      try {
        response = await orderService.guestCheckout(guestOrderData)
        
        if (window.$toast) {
          window.$toast.success('Đặt hàng thành công!', 'Kiểm tra email để xác nhận')
        }
      } catch (error) {
        // Xử lý lỗi voucher từ backend
        const errorMessage = error?.data?.message || error?.message || 'Có lỗi xảy ra khi đặt hàng'
        if (window.$toast) {
          window.$toast.error(errorMessage, 'Đặt hàng thất bại')
        }
        throw error
      }
    }



// Lấy id đơn và tổng thanh toán
let donHangId =
  response?.id ||
  response?.data?.id ||
  response?.donHangId ||
  response?.data?.donHangId ||
  null

let tongThanhToan =
  response?.tongThanhToan ||
  response?.data?.tongThanhToan ||
  response?.tongTien ||
  response?.data?.tongTien ||
  0

// Fallback: nếu chưa có id từ response và user đang đăng nhập → lấy đơn mới nhất
if (!donHangId && token && isAuthenticated.value) {
  try {
    const myOrders = await orderService.getMyOrders({ page: 0, size: 1 })
    const newest = myOrders?.content?.[0]
    if (newest?.id) {
      donHangId = newest.id
      if (!tongThanhToan) {
        tongThanhToan =
          newest?.tongThanhToan ||
          newest?.tongTien ||
          Number(finalTotal.value || 0)
      }
    }
  } catch (e) {
    // Ignore error when fetching latest order
  }
}

// Nếu vẫn chưa có tổng tiền → dùng tổng từ UI
if (!tongThanhToan) {
  tongThanhToan = Number(finalTotal.value || 0)
}

// Nếu chọn thanh toán onl -> tạo URL thanh toán và redirect
if ((selectedPaymentMethod.value || '').toString().toUpperCase() === 'VNPAY') {
  try {
    const pay = await paymentService.taoUrlThanhToan({
      donHangId,
      // Ưu tiên số tiền từ backend (đã trừ voucher, phí ship), fallback UI
      soTien: Number(tongThanhToan || finalTotal.value || 0),
      moTa: `Thanh toán đơn hàng #${donHangId}`
    })

    if (pay.success && pay.paymentUrl) {
      // Không xóa giỏ trước khi thanh toán xong
      window.location.href = pay.paymentUrl
      return
    } else {
      if (window.$toast) {
        window.$toast.error(pay.message || 'Không thể tạo URL thanh toán VNPay')
      }
      return
    }
  } catch (e) {
    if (window.$toast) {
      window.$toast.error('Không thể tạo URL thanh toán VNPay')
    }
    return
  }
}

// ✅ Backend đã tự động xóa các chi tiết giỏ hàng đã được đặt hàng
// Chỉ cần reload lại giỏ hàng để đồng bộ với backend
try {
  const { useCartStore } = await import('@/stores/cart')
  const cartStore = useCartStore()
  await cartStore.loadCart()
} catch (error) {
  // Không throw error, vẫn chuyển đến trang thành công
}

router.push({ name: 'order-success', query: { orderId: donHangId } })

  } catch (error) {
    // Restore token nếu có (đã tạm xóa để chuyển sang session cart)
    if (window._tempAuthToken) {
      localStorage.setItem('auro_token', window._tempAuthToken)
      delete window._tempAuthToken
    }
    
    let errorMessage = 'Có lỗi xảy ra khi đặt hàng'
    
    // Ưu tiên lấy message từ error object (api.js handleError trả về { message, status, data })
    if (error.message) {
      errorMessage = error.message
    } else if (error.data?.message) {
      errorMessage = error.data.message
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    }
    
    // Xử lý các status code đặc biệt
    if (error.status === 403) {
      errorMessage = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!'
    } else if (error.status === 401) {
      errorMessage = 'Bạn cần đăng nhập để đặt hàng'
    }
    
    // Hiển thị thông báo lỗi cho user
    if (window.$toast) {
      window.$toast.error(errorMessage, 'Đặt hàng thất bại')
    } else {
      // Fallback nếu không có toast
      alert(errorMessage)
    }
  } finally {
    isProcessing.value = false
  }
}
</script>

<style scoped>
.order-summary-section {
  margin-bottom: 2rem;
}

.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.section-header {
  background: linear-gradient(135deg, #B8860B 0%, #DAA520 100%);
  color: white;
  padding: 1rem 1.5rem;
  border: none;
  position: relative;
  overflow: hidden;
}

.section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.section-header h5 {
  color: white !important;
  font-weight: 700;
  margin: 0;
  font-size: 1.1rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.section-header h5 i {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.summary-header-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.summary-header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.summary-header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 1rem;
}

.badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}


.section-body {
  padding: 1.5rem;
}

.summary-section {
  margin-bottom: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding: 0.5rem 0;
}

.summary-label {
  font-size: 0.9rem;
  color: #666;
}

.summary-value {
  font-weight: 600;
  color: #333;
}

.discount-item .summary-value {
  color: #28a745;
}

.voucher-item .summary-value {
  color: #28a745;
  font-weight: 700;
}

.voucher-item .summary-label {
  color: #B8860B;
  font-weight: 600;
}

.voucher-item .summary-label i {
  color: #B8860B;
}

.voucher-item .summary-value.no-discount {
  color: #999 !important;
  font-weight: 400 !important;
  font-style: italic;
}

.summary-divider {
  height: 1px;
  background: #e9ecef;
  margin: 1rem 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-top: 2px solid #e9ecef;
  margin-top: 1rem;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
}

.total-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #B8860B;
}

.checkout-section {
  margin-bottom: 1rem;
}

.checkout-btn {
  background: #000000;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  font-weight: 700;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.checkout-btn:hover:not(:disabled) {
  background: #333333;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Responsive Design */
@media (max-width: 768px) {
  .summary-header-layout {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .summary-header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .summary-header-right {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
  
  .badge {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }
}

.security-info {
  text-align: center;
}

.security-badges {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.security-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #666;
}

.security-badge i {
  color: #B8860B;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .security-badges {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .summary-total {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>

