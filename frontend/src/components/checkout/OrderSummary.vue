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
  console.log('=== DISCOUNT CALCULATION DEBUG ===')
  console.log('selectedVoucher.value:', selectedVoucher.value)
  console.log('manualVoucherCode.value:', manualVoucherCode.value)
  console.log('subtotal.value:', subtotal.value)
  
  if (!selectedVoucher.value && !manualVoucherCode.value) {
    console.log('No voucher selected, returning 0')
    return 0
  }
  
  // Nếu có voucher được chọn từ list
  if (selectedVoucher.value) {
    const { loai, giaTri, donToiThieu } = selectedVoucher.value
    console.log('Voucher data:', { loai, giaTri, donToiThieu })
    
    // Kiểm tra điều kiện đơn hàng tối thiểu
    if (subtotal.value < (donToiThieu || 0)) {
      console.log('Subtotal too low, returning 0')
      return 0
    }
    
    let discount = 0
    if (loai === 'percent' || loai === 'PHAN_TRAM') {
      discount = Math.floor(subtotal.value * giaTri / 100)
    } else if (loai === 'fixed' || loai === 'SO_TIEN') {
      discount = Math.min(giaTri, subtotal.value) 
    } else if (loai === 'freeship') {
      discount = Math.min(30000, subtotal.value)
    }
    
    console.log('Calculated discount:', discount)
    return discount
  }
  
  // Nếu có voucher manual và đã được validate
  if (manualVoucherCode.value && manualVoucherCode.value.trim() && selectedVoucher.value) {
    const { loai, giaTri, donToiThieu } = selectedVoucher.value
    console.log('Manual voucher data:', { loai, giaTri, donToiThieu })
    
    // Kiểm tra điều kiện đơn hàng tối thiểu
    if (subtotal.value < (donToiThieu || 0)) {
      console.log('Manual voucher: Subtotal too low, returning 0')
      return 0
    }
    
    let discount = 0
    if (loai === 'percent' || loai === 'PHAN_TRAM') {
      discount = Math.floor(subtotal.value * giaTri / 100)
    } else if (loai === 'fixed' || loai === 'SO_TIEN') {
      discount = Math.min(giaTri, subtotal.value)
    } else if (loai === 'freeship') {
      discount = Math.min(30000, subtotal.value) // Giả sử phí ship là 30k
    }
    
    console.log('Manual voucher calculated discount:', discount)
    return discount
  }
  
  console.log('No valid voucher found, returning 0')
  return 0
})

const finalTotal = computed(() => {
  return subtotal.value + shippingFee.value - discountAmount.value
})

// Validate form data trước khi đặt hàng
const validateCheckoutData = () => {
  console.log('🔍 Validating checkout data...')
  console.log('shippingFormData:', shippingFormData)
  console.log('selectedPaymentMethod:', selectedPaymentMethod)
  
  if (!shippingFormData || !shippingFormData.value) {
    if (window.$toast) {
      window.$toast.error('Không tìm thấy thông tin giao hàng')
    }
    return false
  }

  const formData = shippingFormData.value
  console.log('Form data:', formData)

  if (!formData.fullName || !formData.fullName.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập họ tên')
    }
    return false
  }

  if (!formData.phone || !formData.phone.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập số điện thoại')
    }
    return false
  }

  if (!formData.address || !formData.address.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập địa chỉ giao hàng')
    }
    return false
  }

  if (!formData.email || !formData.email.trim()) {
    if (window.$toast) {
      window.$toast.error('Vui lòng nhập email')
    }
    return false
  }

  if (!selectedPaymentMethod || !selectedPaymentMethod.value) {
    if (window.$toast) {
      window.$toast.error('Vui lòng chọn phương thức thanh toán')
    }
    return false
  }

  console.log('✅ Validation passed')
  return true
}

// Xử lý đặt hàng
const handleCheckout = async () => {
  console.log('🛒 Starting checkout...')
  console.log('🔐 userStore.isAuthenticated:', userStore.isAuthenticated)
  console.log('🔐 userStore.user:', userStore.user)
  console.log('🔐 isAuthenticated computed:', isAuthenticated.value)
  
  const token = localStorage.getItem('auro_token')
  console.log('🔑 Token exists:', !!token)
  console.log('🔑 Token value:', token ? token.substring(0, 20) + '...' : 'null')
  
  if (isProcessing.value) {
    console.log('⚠️ Already processing')
    return
  }

  // Validate dữ liệu
  if (!validateCheckoutData()) {
    console.log('❌ Validation failed')
    return
  }

  isProcessing.value = true

  try {
    // Lấy các sản phẩm đã chọn
    const selectedItems = items.value.filter(item => item.selected !== false)

    console.log('📦 Selected items:', selectedItems)
    console.log('📦 All items:', items.value)

    if (selectedItems.length === 0) {
      if (window.$toast) {
        window.$toast.error('Vui lòng chọn sản phẩm để đặt hàng')
      }
      isProcessing.value = false
      return
    }

    // **QUAN TRỌNG**: Đồng bộ giỏ hàng với backend trước khi checkout
    console.log('🔄 Syncing cart with backend before checkout...')
    
    // Nếu là guest (không có token), không cần sync cart với backend
    if (!token) {
      console.log('� Guest user - skipping backend cart sync')
    } else {
      // Xóa các sản phẩm không được chọn khỏi backend cart
      const unselectedItems = items.value.filter(item => item.selected === false)
      
      if (unselectedItems.length > 0) {
        console.log('🗑️ Removing unselected items from backend cart:', unselectedItems)
        
        // Xóa từng item không được chọn khỏi backend cart
        for (const item of unselectedItems) {
          if (item.id) {
            try {
              await cartService.removeFromCart(item.id)
              console.log('✅ Removed item from backend:', item.id)
            } catch (err) {
              console.warn('⚠️ Failed to remove item from backend (may not exist):', item.id, err.message)
              // Không throw error, tiếp tục xử lý
            }
          }
        }
      }
      
      // Đảm bảo các sản phẩm được chọn có trong backend cart
      console.log('✅ Ensuring selected items are in backend cart...')
      for (const item of selectedItems) {
        // Nếu item chưa có ID từ backend (local item), thêm vào backend
        if (!item.id && (item.bienTheId || item.variantId)) {
          try {
            const addResponse = await cartService.addToCart({
              bienTheId: item.bienTheId || item.variantId,
              soLuong: item.quantity
            })
            console.log('✅ Added item to backend cart:', item.bienTheId, addResponse)
          } catch (err) {
            console.warn('⚠️ Failed to add item to backend (may already exist):', item.bienTheId, err.message)
            // Không throw error, có thể item đã có trong backend
          }
        } else {
          console.log('ℹ️ Item already in backend:', item.id || item.bienTheId)
        }
      }
    }

    // Reload cart từ backend để đảm bảo sync
    console.log('🔄 Reloading cart from backend to ensure sync...')
    try {
      const backendCart = await cartService.getCart()
      console.log('📦 Backend cart:', backendCart)
      
      if (!backendCart || !backendCart.chiTietList || backendCart.chiTietList.length === 0) {
        if (window.$toast) {
          window.$toast.error('Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi đặt hàng.')
        }
        isProcessing.value = false
        return
      }
      
      console.log('✅ Backend cart has', backendCart.chiTietList.length, 'items')
      
      // NOTE: Không cần transfer items cho authenticated users
      // Backend guest-checkout endpoint sẽ tự động nhận diện user từ token
      // và lưu đơn hàng với khachHangId tương ứng
      console.log('✅ Ready to checkout with current cart state')
    } catch (error) {
      console.error('❌ Failed to load backend cart:', error)
      if (window.$toast) {
        window.$toast.error('Không thể tải giỏ hàng. Vui lòng thử lại.')
      }
      isProcessing.value = false
      return
    }

    let response

    // Xác định đã đăng nhập hay chưa dựa vào token
    if (token && isAuthenticated.value) {
      console.log('👤 User is authenticated - using guest checkout endpoint with token')
      
      // Sử dụng guest checkout format cho cả user đã đăng nhập
      // Backend sẽ tự động map user từ token (auth parameter trong controller)
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
        maVoucher: selectedVoucher.value?.ma || manualVoucherCode.value || null,
        // Thêm thông tin GHN để tính phí ship (nếu có)
        districtId: shipping?.selectedDistrict?.value || null,
        wardCode: shipping?.selectedWard?.value || null,
        serviceId: shipping?.selectedService?.value || null
      }
      
      console.log('📤 Sending order as authenticated user (with token):', orderData)
      console.log('🚚 GHN shipping info:', {
        districtId: orderData.districtId,
        wardCode: orderData.wardCode,
        serviceId: orderData.serviceId
      })
      
      try {
        response = await orderService.guestCheckout(orderData)
        console.log('✅ Order created:', response)
        
        if (window.$toast) {
          window.$toast.success('Đặt hàng thành công!', 'Cảm ơn bạn đã mua hàng')
        }
      } catch (error) {
        console.error('❌ Order creation failed:', error)
        throw error
      }
    } else {
      // Guest checkout (không có token)
      console.log('👻 Guest checkout (no authentication)')
      
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
        maVoucher: selectedVoucher.value?.ma || manualVoucherCode.value || null,
        // Thêm thông tin GHN để tính phí ship (nếu có)
        districtId: shipping?.selectedDistrict?.value || null,
        wardCode: shipping?.selectedWard?.value || null,
        serviceId: shipping?.selectedService?.value || null
      }
      
      console.log('📤 Sending guest order:', guestOrderData)
      console.log('🚚 GHN shipping info:', {
        districtId: guestOrderData.districtId,
        wardCode: guestOrderData.wardCode,
        serviceId: guestOrderData.serviceId
      })
      
      response = await orderService.guestCheckout(guestOrderData)
      
      if (window.$toast) {
        window.$toast.success('Đặt hàng thành công!', 'Kiểm tra email để xác nhận')
      }
    }

    console.log('✅ Order created:', response)

    // Xóa giỏ hàng sau khi đặt hàng thành công
    await clearCart()

    // Chuyển đến trang xác nhận đơn hàng
    // Guest checkout có thể không trả về response.id, chỉ có success: true
    if (response && (response.id || response.success)) {
      // Nếu có ID đơn hàng và user đã đăng nhập, chuyển đến trang chi tiết đơn hàng
      if (response.id && token && isAuthenticated.value) {
        router.push({ 
          name: 'order-detail', 
          params: { id: response.id },
          query: { new: 'true' } // Đánh dấu là đơn hàng mới
        })
      } else {
        // Guest hoặc không có ID, chuyển đến trang danh sách đơn hàng (hoặc trang xác nhận)
        router.push({ 
          name: 'order-success',
          query: { 
            orderId: response.id || 'pending',
            email: shippingFormData.value.email 
          }
        })
      }
    } else {
      // Fallback: về trang chủ với thông báo
      router.push('/')
    }

  } catch (error) {
    console.error('❌ Checkout error:', error)
    console.error('Error details:', {
      message: error.message,
      status: error.status,
      data: error.data,
      response: error.response
    })
    
    // Restore token nếu có (đã tạm xóa để chuyển sang session cart)
    if (window._tempAuthToken) {
      localStorage.setItem('auro_token', window._tempAuthToken)
      delete window._tempAuthToken
      console.log('🔑 Token restored after error')
    }
    
    let errorMessage = 'Có lỗi xảy ra khi đặt hàng'
    
    if (error.status === 403) {
      errorMessage = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!'
    } else if (error.status === 401) {
      errorMessage = 'Bạn cần đăng nhập để đặt hàng'
    } else if (error.message) {
      errorMessage = error.message
    } else if (error.data?.message) {
      errorMessage = error.data.message
    }
    
    if (window.$toast) {
      window.$toast.error(errorMessage, 'Đặt hàng thất bại')
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

