import { computed } from 'vue'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import cartService from '../services/cartService'
import authUtils from '../utils/auth'

export function useCart() {
  const cartStore = useCartStore()
  const router = useRouter()
  const userStore = useUserStore()

  // Computed properties
  const items = computed(() => cartStore.items)
  const itemCount = computed(() => cartStore.itemCount)
  const totalPrice = computed(() => cartStore.totalPrice)
  const isEmpty = computed(() => cartStore.isEmpty)

  // Cart operations
  const addItem = (product, quantity = 1) => {
    cartStore.addItem(product, quantity)
  }

  const removeItem = async (itemKey) => {
    try {
      await cartStore.removeItem(itemKey)
      
      if (window.$toast) {
        window.$toast.success('Đã xóa sản phẩm khỏi giỏ hàng', 'Thành công')
      }
    } catch (error) {
      console.error('Error removing item:', error)
      
      if (window.$toast) {
        window.$toast.error('Không thể xóa sản phẩm', 'Lỗi')
      }
    }
  }

  const updateQuantity = async (itemKey, quantity) => {
    const numQuantity = parseInt(quantity) || 0

    const item = cartStore.items.find(item => item.itemKey === itemKey)
    
    if (!item) {
      console.error('Item not found:', itemKey)
      return false
    }
    
    if (numQuantity <= 0) {
      if (window.$toast) {
        window.$toast.error('Số lượng phải lớn hơn 0')
      }
      return false
    }
    
    // Validate stock - nếu không có stock data thì set default là 1
    const maxStock = item.stock || 1
    if (numQuantity > maxStock) {
      if (window.$toast) {
        window.$toast.error(`Chỉ còn ${maxStock} sản phẩm trong kho`)
      }
      return false
    }
    
    try {
      await cartStore.updateQuantity(itemKey, numQuantity)
      return true
    } catch (error) {
      console.error('Error updating quantity:', error)
      
      if (window.$toast) {
        window.$toast.error('Không thể cập nhật số lượng', 'Lỗi')
      }
      
      return false
    }
  }

  const clearCart = async () => {
    try {
      await cartStore.clearCart()
      
      if (window.$toast) {
        window.$toast.success('Đã xóa toàn bộ giỏ hàng', 'Thành công')
      }
    } catch (error) {
      console.error('Error clearing cart:', error)
      
      if (window.$toast) {
        window.$toast.error('Không thể xóa giỏ hàng', 'Lỗi')
      }
    }
  }

  const formatPrice = (price) => {
    const numPrice = parseFloat(price) || 0
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0
    }).format(numPrice)
  }

  /**
   * Load giỏ hàng từ API (cho cả user đã đăng nhập và guest)
   */
  const loadCartFromAPI = async () => {
    try {
      console.log('🔄 [LOAD CART] Starting to load cart from API...')
      
      // ✅ CHỈ GỌI cartStore.loadCart() - KHÔNG FALLBACK
      // Vì cartStore.loadCart() đã handle tất cả logic cần thiết
      await cartStore.loadCart()
      
      console.log('✅ [LOAD CART] Loaded', cartStore.items.length, 'items')
      
      // Log chi tiết từng item
      if (cartStore.items.length > 0) {
        cartStore.items.forEach((item, index) => {
          console.log(`📦 [LOADED ITEM ${index + 1}]:`, {
            id: item.id,
            itemKey: item.itemKey,
            name: item.name,
            color: item.color,
            size: item.size,
            quantity: item.quantity,
            bienTheId: item.bienTheId
          })
        })
      }
      
    } catch (error) {
      console.error('❌ [LOAD CART] Error:', error)
      // Nếu lỗi, giữ nguyên local cart (không clear)
    }
  }

  /**
   * Parse chuỗi thuộc tính "Màu: Trắng, Size: M" thành object
   */
  const parseThuocTinh = (thuocTinh) => {
    const result = {}
    
    if (!thuocTinh) return result
    
    const parts = thuocTinh.split(',')
    parts.forEach(part => {
      const [key, value] = part.split(':').map(s => s.trim())
      
      if (key === 'Màu') {
        result.color = value
      } else if (key === 'Size') {
        result.size = value
      }
    })
    
    return result
  }

  // Business logic methods
  const addToCartWithValidation = (product, quantity = 1, variantData = null) => {
    // Validate stock
    if (product.stock <= 0) {
      if (window.$toast) {
        window.$toast.error('Sản phẩm đã hết hàng', 'Không thể thêm vào giỏ')
      }
      return false
    }

    // Check quantity against stock
    const finalQuantity = variantData?.quantity || quantity
    if (finalQuantity > product.stock) {
      if (window.$toast) {
        window.$toast.error(`Chỉ còn lại ${product.stock} sản phẩm`, 'Số lượng không hợp lệ')
      }
      return false
    }

    // Add to cart
    const cartItem = {
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.image,
      quantity: finalQuantity,
      stock: product.stock || 1,
      ...variantData
    }

    addItem(cartItem, finalQuantity)
    
    // Show success message
    const message = variantData?.color && variantData?.size 
      ? `Đã thêm ${product.name} (${variantData.color}, Size ${variantData.size}) vào giỏ`
      : `Đã thêm ${product.name} vào giỏ`
      
    if (window.$toast) {
      window.$toast.success(message, 'Thêm vào giỏ hàng thành công')
    }

    return true
  }

  const proceedToCheckout = () => {
    if (isEmpty.value) {
      if (window.$toast) {
        window.$toast.warning('Giỏ hàng trống', 'Vui lòng thêm sản phẩm')
      }
      return
    }

    // Check if user is logged in
    if (!authUtils.isLoggedIn()) {
      if (window.$toast) {
        window.$toast.warning('Vui lòng đăng nhập để thanh toán', 'Cần đăng nhập')
      }
      router.push('/login')
      return
    }

    router.push('/cart')
  }

  const proceedToCart = () => {
    router.push('/cart')
  }

  // Voucher operations
  const canApplyVoucher = () => {
    return authUtils.canUseVoucher()
  }

  const applyVoucher = (voucherCode, callback) => {
    return authUtils.requireLoginForVoucher(() => {
      // Implement voucher application logic
      if (callback) {
        return callback(voucherCode)
      }
      return true
    })
  }

  const removeVoucher = () => {
    // Implement voucher removal logic
    if (window.$toast) {
      window.$toast.success('Đã xóa mã giảm giá', 'Thành công')
    }
  }

  // Cart merging for guest users
  const mergeGuestCart = (guestCartItems) => {
    const userCartItems = items.value
    const mergedItems = authUtils.mergeGuestCartWithUserCart(guestCartItems, userCartItems)
    
    // Update cart with merged items
    clearCart()
    mergedItems.forEach(item => {
      addItem(item, item.quantity)
    })

    if (authUtils.hasGuestCartItems(mergedItems)) {
      if (window.$toast) {
        window.$toast.success('Đã chuyển giỏ hàng tạm thời vào tài khoản', 'Chào mừng trở lại')
      }
    }
  }

  // Stock validation
  const validateStock = (itemKey, newQuantity) => {
    const item = items.value.find(i => i.itemKey === itemKey)
    if (!item) return false

    // Mock stock check - replace with actual API call
    const maxStock = 10 // This should come from product data
    return newQuantity <= maxStock
  }

  const updateQuantityWithStockCheck = (itemKey, newQuantity) => {
    if (validateStock(itemKey, newQuantity)) {
      updateQuantity(itemKey, newQuantity)
      return true
    } else {
      if (window.$toast) {
        window.$toast.error('Số lượng vượt quá tồn kho', 'Không thể cập nhật')
      }
      return false
    }
  }

  // Analytics tracking
  const trackCartEvent = (event, data = {}) => {
    // Implement analytics tracking
    
    // Example: Google Analytics 4
    if (window.gtag) {
      window.gtag('event', event, {
        event_category: 'cart',
        ...data
      })
    }
  }

  const trackAddToCart = (product, variantData = null) => {
    trackCartEvent('add_to_cart', {
      currency: 'VND',
      value: product.price,
      items: [{
        item_id: product.id,
        item_name: product.name,
        item_category: 'fashion',
        quantity: variantData?.quantity || 1,
        price: product.price,
        ...(variantData?.color && { item_variant: variantData.color }),
        ...(variantData?.size && { item_size: variantData.size })
      }]
    })
  }

  const trackRemoveFromCart = (item) => {
    trackCartEvent('remove_from_cart', {
      currency: 'VND',
      value: item.price * item.quantity,
      items: [{
        item_id: item.id,
        item_name: item.name,
        quantity: item.quantity,
        price: item.price
      }]
    })
  }

  const trackViewCart = () => {
    trackCartEvent('view_cart', {
      currency: 'VND',
      value: totalPrice.value,
      items: items.value.map(item => ({
        item_id: item.id,
        item_name: item.name,
        quantity: item.quantity,
        price: item.price
      }))
    })
  }

  return {
    // State
    items,
    itemCount,
    totalPrice,
    isEmpty,

    // Basic operations
    addItem,
    removeItem,
    updateQuantity,
    clearCart,

    // API operations
    loadCartFromAPI,

    // Business logic
    addToCartWithValidation,
    proceedToCheckout,
    proceedToCart,

    formatPrice,

    // Voucher operations
    canApplyVoucher,
    applyVoucher,
    removeVoucher,

    // Cart merging
    mergeGuestCart,

    // Stock validation
    validateStock,
    updateQuantityWithStockCheck,

    // Analytics
    trackCartEvent,
    trackAddToCart,
    trackRemoveFromCart,
    trackViewCart
  }
}

export default useCart
