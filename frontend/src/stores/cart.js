import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import cartService from '../services/cartService'

export const useCartStore = defineStore('cart', () => {
  // State - ✅ KHỞI TẠO RỖNG, KHÔNG LOAD TỪ LOCALSTORAGE
  // Vì backend là nguồn chân lý
  const items = ref([])
  const isLoading = ref(false)

  // Getters
  const itemCount = computed(() => {
    return items.value.reduce((total, item) => {
      const quantity = parseInt(item.quantity) || 0
      return total + quantity
    }, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const price = parseFloat(item.price) || 0
      const quantity = parseInt(item.quantity) || 0
      return total + (price * quantity)
    }, 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  // Actions
  const addItem = (product, quantity = 1) => {
    // Validate and sanitize product data
    const sanitizedProduct = {
      id: product.id || null,
      name: product.name || 'Sản phẩm không tên',
      price: parseFloat(product.price) || 0,
      image: product.image || '',
      variantId: product.variantId || null,
      color: product.color || null,
      size: product.size || null,
      quantity: parseInt(product.quantity) || parseInt(quantity) || 1,
      stock: parseInt(product.stock) || 1
    }

    // Create unique key for variant-based products
    const itemKey = sanitizedProduct.variantId || sanitizedProduct.id
    const existingItem = items.value.find(item => 
      item.itemKey === itemKey || 
      (item.id === sanitizedProduct.id && !item.variantId && !sanitizedProduct.variantId)
    )
    
    if (existingItem) {
      existingItem.quantity += sanitizedProduct.quantity
      existingItem.price = sanitizedProduct.price // Update price in case it changed
    } else {
      items.value.push({
        id: sanitizedProduct.id,
        itemKey: itemKey,
        variantId: sanitizedProduct.variantId,
        color: sanitizedProduct.color,
        size: sanitizedProduct.size,
        name: sanitizedProduct.name,
        price: sanitizedProduct.price,
        image: sanitizedProduct.image,
        quantity: sanitizedProduct.quantity,
        addedAt: new Date().toISOString()
      })
    }
    
    saveToStorage()
  }

  const removeItem = async (itemKey) => {
    try {
      console.log('🗑️ [REMOVE ITEM] Removing item with itemKey:', itemKey)
      
      // Tìm item để lấy ID (GioHangChiTiet.id)
      const item = items.value.find(i => i.itemKey === itemKey)
      
      if (!item) {
        console.error('❌ [REMOVE ITEM] Item not found:', itemKey)
        return
      }
      
      const cartItemId = item.id // GioHangChiTiet.id từ backend
      console.log('🗑️ [REMOVE ITEM] Calling API to remove item ID:', cartItemId)
      
      // ✅ GỌI API BACKEND ĐỂ XÓA
      await cartService.removeFromCart(cartItemId)
      console.log('✅ [REMOVE ITEM] Removed from backend successfully')
      
      // Sau khi xóa trên backend, reload lại giỏ hàng
      await loadCart()
      console.log('✅ [REMOVE ITEM] Reloaded cart from backend')
      
    } catch (error) {
      console.error('❌ [REMOVE ITEM] Error:', error)
      
      // Nếu lỗi API, vẫn xóa trên frontend (fallback)
      const index = items.value.findIndex(item => item.itemKey === itemKey)
      if (index > -1) {
        items.value.splice(index, 1)
        saveToStorage()
      }
      
      throw error
    }
  }

  const updateQuantity = async (itemKey, quantity) => {
    try {
      const item = items.value.find(item => item.itemKey === itemKey)
      
      if (!item) {
        console.error('❌ [UPDATE QTY] Item not found:', itemKey)
        return
      }
      
      if (quantity <= 0) {
        // Nếu quantity = 0, xóa item
        await removeItem(itemKey)
        return
      }
      
      // KHÔNG GIỚI HẠN STOCK Ở ĐÂY - Frontend đã kiểm tra rồi
      const safeQuantity = Math.max(1, Math.min(quantity, 100)) // Chỉ giới hạn max 100
      
      console.log('📝 [UPDATE QTY] Updating item:', {
        itemKey,
        cartItemId: item.id,
        oldQuantity: item.quantity,
        newQuantity: safeQuantity
      })
      
      // ✅ GỌI API BACKEND ĐỂ CẬP NHẬT SỐ LƯỢNG
      await cartService.updateQuantity(item.id, safeQuantity)
      console.log('✅ [UPDATE QTY] Updated on backend successfully')
      
      // Sau khi update trên backend, reload lại giỏ hàng
      await loadCart()
      console.log('✅ [UPDATE QTY] Reloaded cart from backend')
      
    } catch (error) {
      console.error('❌ [UPDATE QTY] Error:', error)
      
      // Nếu lỗi API, vẫn update trên frontend (fallback)
      const item = items.value.find(item => item.itemKey === itemKey)
      if (item) {
        item.quantity = Math.max(1, Math.min(quantity, 100))
        saveToStorage()
      }
      
      throw error
    }
  }

  const clearCart = async () => {
    try {
      console.log('🗑️ [CLEAR CART] Clearing entire cart...')
      
      // ✅ GỌI API BACKEND ĐỂ XÓA TOÀN BỘ GIỎ HÀNG
      await cartService.clearCart()
      console.log('✅ [CLEAR CART] Cleared on backend successfully')
      
      // Xóa trên frontend
      items.value = []
      saveToStorage()
      
      console.log('✅ [CLEAR CART] Cart cleared completely')
      
    } catch (error) {
      console.error('❌ [CLEAR CART] Error:', error)
      
      // Nếu lỗi API, vẫn xóa trên frontend (fallback)
      items.value = []
      saveToStorage()
      
      throw error
    }
  }

  const saveToStorage = () => {
    localStorage.setItem('auro_cart_v1', JSON.stringify(items.value))
  }

  const loadFromStorage = () => {
    const stored = localStorage.getItem('auro_cart_v1')
    if (stored) {
      try {
        items.value = JSON.parse(stored)
        // Ensure all items have itemKey for backward compatibility
        items.value.forEach(item => {
          if (!item.itemKey) {
            item.itemKey = item.variantId || item.id
          }
        })
      } catch (error) {
        console.error('Error loading cart from storage:', error)
        items.value = []
      }
    }
  }

  // Utility function to format price
  const formatPrice = (price) => {
    const numPrice = parseFloat(price) || 0
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0
    }).format(numPrice)
  }

  // Load cart from backend API
  const loadCart = async () => {
    try {
      isLoading.value = true
      console.log('🔄 [CART STORE] Loading cart from backend...')
      
      const response = await cartService.getCart()
      console.log('📡 [CART STORE] Backend response:', response)
      
      if (response && Array.isArray(response.chiTietList) && response.chiTietList.length > 0) {
        console.log('✅ [CART STORE] Received', response.chiTietList.length, 'items from backend')
        
        // Map backend response to cart items format (only when backend has items)
        items.value = response.chiTietList.map(item => {
          console.log('🔍 [RAW ITEM FROM BACKEND]:', JSON.stringify(item, null, 2))
          
          const mapped = {
            id: item.id, // GioHangChiTiet ID
            itemKey: item.id, // ✅ Dùng GioHangChiTiet.id làm itemKey (unique)
            productId: item.productId || item.sanPhamId || null,
            bienTheId: item.bienTheId,
            variantId: item.bienTheId,
            sku: item.sku || '',
            name: item.tenSanPham || 'Sản phẩm',
            price: parseFloat(item.donGia) || 0,
            quantity: parseInt(item.soLuong) || 1,
            image: item.image || '',
            color: item.color || extractColorFromThuocTinh(item.thuocTinh),
            size: item.size || extractSizeFromThuocTinh(item.thuocTinh),
            thuocTinh: item.thuocTinh || '',
            stock: parseInt(item.tonKho) || 999,
            addedAt: new Date().toISOString()
          }
          
          console.log('📦 [MAPPED ITEM] stock =', mapped.stock, ', tonKho =', item.tonKho)
          return mapped
        })
        
        // ✅ Lưu vào localStorage SAU KHI map xong
        saveToStorage()
        
        console.log('✅ [CART STORE] Successfully loaded and saved', items.value.length, 'items')
      } else {
        console.log('⚠️ [CART STORE] Backend returned empty cart')
        
        // ✅ Backend giỏ rỗng → XÓA localStorage để đồng bộ
        // KHÔNG load từ localStorage vì backend là nguồn chân lý
        items.value = []
        saveToStorage()
      }
      
      return response
    } catch (error) {
      console.error('❌ [CART STORE] Error loading cart from backend:', error)
      
      // ✅ Nếu lỗi API → load từ localStorage (fallback)
      // Nhưng chỉ khi thực sự có lỗi network, không phải khi backend trả rỗng
      if (error.response?.status !== 200) {
        console.log('⚠️ [CART STORE] API error, loading from localStorage as fallback')
        loadFromStorage()
      }
      
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // Helper function to extract color from thuocTinh string
  const extractColorFromThuocTinh = (thuocTinh) => {
    if (!thuocTinh) return null
    const match = thuocTinh.match(/Màu:\s*([^,]+)/)
    return match ? match[1].trim() : null
  }

  // Helper function to extract size from thuocTinh string
  const extractSizeFromThuocTinh = (thuocTinh) => {
    if (!thuocTinh) return null
    const match = thuocTinh.match(/Size:\s*([^,]+)/)
    return match ? match[1].trim() : null
  }

  // ✅ KHÔNG khởi tạo từ localStorage nữa
  // Để đảm bảo luôn load từ backend (nguồn chân lý)

  return {
    // State
    items,
    isLoading,
    
    // Getters
    itemCount,
    totalPrice,
    isEmpty,
    
    // Actions
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    saveToStorage,
    loadFromStorage,
    loadCart,
    formatPrice
  }
})
