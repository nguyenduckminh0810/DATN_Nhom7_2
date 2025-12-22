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

// Inject shipping info from parent
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

// Sử dụng shipping fee từ GHN API hoặc default
const shippingFee = computed(() => {
  // Nếu có phí ship từ GHN API thì dùng
  if (shippingInfo.shippingFee.value > 0) {
    return shippingInfo.shippingFee.value
  }
  // Nếu không thì dùng logic cũ
  return subtotal.value >= 500000 ? 0 : 30000
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
  console.log(' Validating checkout data...')
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

  console.log(' Validation passed')
  return true
}

// Xử lý đặt hàng - ĐƠN GIẢN HÓA
const handleCheckout = async () => {
  console.log(' Starting checkout...')
  
  const token = localStorage.getItem('auro_token')
  console.log(' Token exists:', !!token)
  console.log(' isAuthenticated:', isAuthenticated.value)
  
  if (isProcessing.value) {
    console.log(' Already processing')
    return
  }
  // Mark processing immediately to prevent multiple handlers passing validation
  isProcessing.value = true

  // Validate dữ liệu
  if (!validateCheckoutData()) {
    console.log(' Validation failed')
    isProcessing.value = false
    return
  }

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
    console.log(' Syncing cart with backend before checkout...')
    
    // Nếu là guest (không có token), không cần sync cart với backend
    if (!token) {
      console.log(' Guest user - skipping backend cart sync')
    } else {
      // Xóa các sản phẩm không được chọn khỏi backend cart
      const unselectedItems = items.value.filter(item => item.selected === false)
      
      if (unselectedItems.length > 0) {
        console.log(' Removing unselected items from backend cart:', unselectedItems)
        
        // Xóa từng item không được chọn khỏi backend cart
        for (const item of unselectedItems) {
          if (item.id) {
            try {
              await cartService.removeFromCart(item.id)
              console.log(' Removed item from backend:', item.id)
            } catch (err) {
              console.warn(' Failed to remove item from backend (may not exist):', item.id, err.message)
              // Không throw error, tiếp tục xử lý
            }
          }
        }
      }
      
      // Đảm bảo các sản phẩm được chọn có trong backend cart
      console.log('Ensuring selected items are in backend cart...')
      for (const item of selectedItems) {
        // Nếu item chưa có ID từ backend (local item), thêm vào backend
        if (!item.id && (item.bienTheId || item.variantId)) {
          try {
            const addResponse = await cartService.addToCart({
              bienTheId: item.bienTheId || item.variantId,
              soLuong: item.quantity
            })
            console.log(' Added item to backend cart:', item.bienTheId, addResponse)
          } catch (err) {
            console.warn(' Failed to add item to backend (may already exist):', item.bienTheId, err.message)
            // Không throw error, có thể item đã có trong backend
          }
        } else {
          console.log(' Item already in backend:', item.id || item.bienTheId)
        }
      }
    }

    console.log(' Cart synced with backend')
